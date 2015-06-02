package com.sasaug.shadowchat.database.modules;


import java.util.ArrayList;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.database.IDatabase;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.obj.SCUser;

public class GroupModule implements IDatabase, IModule{

	public String getModuleId() {
		return DatabaseCore.ID + ".GroupModule";
	}
	
	public String getName(){
		return "Group";
	};
	
	DatabaseCore core;
	public void onInit(DatabaseCore core){
		this.core = core;
		core.registerFunction(getName(), "getMembers", DatabaseCore.FuncType.GET, (new ArrayList<String>()).getClass(), 1);
		core.registerFunction(getName(), "canSendGroup", DatabaseCore.FuncType.GET, Boolean.class, 2);
		core.registerFunction(getName(), "createGroup", DatabaseCore.FuncType.GET, String.class, 2);
		core.registerFunction(getName(), "inviteToGroup", DatabaseCore.FuncType.GET, Boolean.class, 3);
		core.registerFunction(getName(), "joinGroup", DatabaseCore.FuncType.GET, Boolean.class, 2);
		core.registerFunction(getName(), "getRank", DatabaseCore.FuncType.GET, String.class, 2);
		core.registerFunction(getName(), "getGroups", DatabaseCore.FuncType.GET, (new ArrayList<SCUser>()).getClass(), 1);
		
		core.registerFunction(getName(), "updateGroup", DatabaseCore.FuncType.SET, null, 2);
	}

	public Object get(String function, String[] params) {
		if(function.equals("getGroups")){
			Jedis jedis = core.getJedis();
			ArrayList<SCUser> groups = new ArrayList<SCUser>();
			Set<String> set = jedis.smembers("user:" + params[0] + ":groups");
			for(String str : set){
				if(jedis.exists("group:" + str)){
					String username = str;
					String name = jedis.hexists("group:" + str, "name")? jedis.hget("group:" + str, "name"):"";
					String avatar = jedis.hexists("group:" + str, "avatar")? jedis.hget("group:" + str, "avatar"):"";
					Set<String> s = jedis.smembers("group:" + params[0] + ":friends:" + str +":flags");
					String[] flag = new String[s.size()]; 
					flag = s.toArray(flag);
					SCUser group = new SCUser(username, name, avatar, flag);
					group.isGroup = true;
					
					Set<String> uSet =  jedis.smembers("group:" + params[0] + ":members");
					for(String x: uSet){
						if(jedis.exists("user:" + x)){
							String username1 = x;
							String name1 = jedis.hexists("user:" + x, "name")? jedis.hget("user:" + x, "name"):"";
							String avatar1 = jedis.hexists("user:" + x, "avatar")? jedis.hget("user:" + x, "avatar"):"";
							Set<String> s1 = jedis.smembers("user:" + params[0] + ":friends:" + x +":flags");
							String[] flag1 = new String[s1.size()]; 
							flag1 = s1.toArray(flag);
							SCUser user = new SCUser(username1, name1, avatar1, flag1);
							group.users.add(user);
						}
					}
					groups.add(group);
				}
			}
			jedis.close();
			return groups;
		}else if(function.equals("getMembers")){
			Jedis jedis = core.getJedis();
			ArrayList<String> ids = new ArrayList<String>();
			Set<String> set = jedis.smembers("group:" + params[0] + ":members");
			for(String str : set)
			    ids.add(str);
			jedis.close();
			return ids;
		}else if(function.equals("canSendGroup")){
			Jedis jedis = core.getJedis();
			boolean allow = true;
			//check if temp block
			if(jedis.exists("user:" + params[0] +":group_blocked:"+ params[1])){
				allow = false;
			}
			//check if perm block
			if(jedis.exists("user:" + params[0] +":group_blocked")){
				if(jedis.sismember("user:" + params[0] +":group_blocked", params[1]))
					allow = false;
			}
			jedis.close();
			return allow;
		}else if(function.equals("createGroup")){
			Jedis jedis = core.getJedis();
			String ii = jedis.hget("group", "id");
			if(ii == null)
				jedis.hset("group" , "id", "0");
			final long id = jedis.hincrBy("group", "id", 1);
			jedis.sadd("group_ids", id+"");
			jedis.hset("group:" + id, "name", params[0]);
			jedis.hset("group:" + id, "avatar", params[1]);	
			jedis.sadd("group:" + id + ":members", params[2]);
			jedis.sadd("group:" + id + ":members:" + params[2], SCUser.OWNER);
			jedis.sadd("user:" + params[2] + ":groups", id+"");
			jedis.close();
			return id + "";
		}else if(function.equals("inviteToGroup")){
			Jedis jedis = core.getJedis();
			boolean success = false;
			String key = params[0] + ";" + params[1];
			if(!jedis.sismember("user:" + params[2] + ":groups_invite", key)){
				jedis.sadd("user:" + params[2] + ":groups_invite", key);
				success = true;
			}
			jedis.close();
			return success;
		}else if(function.equals("joinGroup")){
			Jedis jedis = core.getJedis();
			boolean success = false;
			
			Set<String> set = jedis.smembers("user:" + params[1] + ":groups_invite");
			for(String str : set){
				if(str.startsWith(params[0])){
					jedis.srem("user:" + params[1] + ":groups_invite", str);
					jedis.sadd("user:" + params[1] + ":groups", params[0]);
					jedis.sadd("group:" + params[0] + ":members", params[1]);	
					jedis.sadd("group:" + params[0] + ":members:" + params[1], SCUser.MEMBER);
					success = true;
					break;
				}
			}
			jedis.close();
			return success;
		}else if(function.equals("getRank")){
			Jedis jedis = core.getJedis();
			String result = SCUser.GUEST;
			if(jedis.sismember("group:" + params[0] + ":members:" + params[1], SCUser.MEMBER))
				result = SCUser.MEMBER;
			else if(jedis.sismember("group:" + params[0] + ":members:" + params[1], SCUser.MODERATOR))
				result = SCUser.MODERATOR;
			else if(jedis.sismember("group:" + params[0] + ":members:" + params[1], SCUser.ADMIN))
				result = SCUser.ADMIN;
			else if(jedis.sismember("group:" + params[0] + ":members:" + params[1], SCUser.OWNER))
				result = SCUser.OWNER;	
			jedis.close();
			return result;
		}

		return null;
	}

	public void set(String function, String[]  params) {
		if(function.equals("updateGroup")){
			Jedis jedis = core.getJedis();
			jedis.hset("user:" + params[0], "updateGroup", params[1]);
			jedis.close();
		}
	}
}
