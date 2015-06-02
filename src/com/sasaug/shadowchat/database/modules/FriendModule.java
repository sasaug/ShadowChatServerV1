package com.sasaug.shadowchat.database.modules;


import java.util.ArrayList;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.database.IDatabase;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.obj.SCUser;

public class FriendModule implements IDatabase, IModule{

	public String getModuleId() {
		return DatabaseCore.ID + ".FriendModule";
	}
	
	public String getName(){
		return "Friend";
	};
	
	DatabaseCore core;
	public void onInit(DatabaseCore core){
		this.core = core;
		core.registerFunction(getName(), "getFriends", DatabaseCore.FuncType.GET, (new ArrayList<SCUser>()).getClass(), 1);
		core.registerFunction(getName(), "addFriend", DatabaseCore.FuncType.GET, Boolean.class, 3);
		core.registerFunction(getName(), "acceptFriend", DatabaseCore.FuncType.GET, Boolean.class, 4);
		core.registerFunction(getName(), "deleteFriend", DatabaseCore.FuncType.GET, Boolean.class, 2);
	}

	public Object get(String function, String[] params) {
		if(function.equals("canSendUser")){
			String from = params[0];
			String to = params[1];
			Jedis jedis = core.getJedis();
			boolean allow = true;
			if(jedis.exists("user:" + to +":friends_blocked:"+ from)){
				allow = false;
			}
			if(jedis.hexists("user:" + to +":friends:" + from, "blocked")){
				allow = false;
			}
			jedis.close();
			return allow;
		}else if(function.equals("getFriends")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			ArrayList<SCUser> users = new ArrayList<SCUser>();
			Set<String> set = jedis.smembers("user:" + username + ":friends");
			for(String str : set){
				if(jedis.exists("user:" + str)){
					String name = jedis.hexists("user:" + str, "name")? jedis.hget("user:" + str, "name"):"";
					String avatar = jedis.hexists("user:" + str, "avatar")? jedis.hget("user:" + str, "avatar"):"";
					Set<String> s = jedis.smembers("user:" + params[0] + ":friends:" + str +":flags");
					String[] flag = new String[s.size()]; 
					flag = s.toArray(flag);
					SCUser u = new SCUser(username, name, avatar, flag);
					users.add(u);
				}
			}
			jedis.close();
			return users;
		}else if(function.equals("addFriend")){
			String username = params[0];
			String friend  = params[1];
			String keys = params[2];
			
			Jedis jedis = core.getJedis();
			boolean success = false;
			if(!jedis.exists("user:" + username + ":friends:" + friend)){
				jedis.sadd("user:" + username + ":friends", friend);
				jedis.hset("user:" + username + ":friends:" + friend , "status", SCUser.FRSTATUS_REQUESTING+"");
				
				jedis.sadd("user:" + friend + ":friends", username);
				jedis.hset("user:" + friend + ":friends:" + username , "keys", keys);
				jedis.hset("user:" + friend + ":friends:" + username , "status", SCUser.FRSTATUS_REQUESTED+"");
				success = true;
			}
			jedis.close();
			return success;
		}else if(function.equals("acceptFriend")){
			String username = params[0];
			String friend = params[1];
			String accept = params[2];
			String keys = params[3];
			Jedis jedis = core.getJedis();
			boolean success = false;
				
			if(jedis.exists("user:" + params[0] + ":friends:" + params[1])){
				if(accept.equals("1")){
					jedis.hset("user:" + friend + ":friends:" + username , "keys", keys);
					jedis.hset("user:" + username + ":friends:" + friend , "status", SCUser.FRSTATUS_ACCEPTED +"");
					jedis.hset("user:" + friend + ":friends:" + username , "status", SCUser.FRSTATUS_ACCEPTED +"");
				}else{
					jedis.hset("user:" + username + ":friends:" + friend , "status", SCUser.FRSTATUS_REJECTED +"");
					jedis.hset("user:" + friend + ":friends:" + username , "status", SCUser.FRSTATUS_REJECTED +"");
				}
				success = true;
			}
			jedis.close();
			return success;
		}else if(function.equals("deleteFriend")){
			String username = params[0];
			String friend = params[1];
			Jedis jedis = core.getJedis();
			boolean success = false;
			if(jedis.sismember("user:" + username + ":friends", params[1])){
				jedis.srem("user:" + username + ":friends", friend);
				jedis.srem("user:" + friend + ":friends", username);
				jedis.del("user:" + username + ":friends:" + friend);
				jedis.del("user:" + username + ":friends:" + username);
			}
			jedis.close();
			return success;}

		return null;
	}

	public void set(String function, String[] params) {
		
	}
}
