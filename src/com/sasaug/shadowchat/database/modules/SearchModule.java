package com.sasaug.shadowchat.database.modules;

import java.util.ArrayList;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.database.IDatabase;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.obj.SCUser;

public class SearchModule implements IDatabase, IModule{

	public String getModuleId() {
		return DatabaseCore.ID + ".SearchModule";
	}
	
	public String getName(){
		return "Search";
	};
	
	DatabaseCore core;
	public void onInit(DatabaseCore core){
		this.core = core;
		core.registerFunction(getName(), "search", DatabaseCore.FuncType.GET,  (new ArrayList<SCUser>()).getClass(), 2);
	}

	public Object get(String function, String[] params) {
		if(function.equals("search")){
			Jedis jedis = core.getJedis();
			ArrayList<SCUser> users = new ArrayList<SCUser>();
			Set<String> set = jedis.keys("user:*");
			for(String str: set){
				String[] arr = str.split(":");
				if(arr.length == 2){
					str = arr[1];
					String n = jedis.hget("user:" + str, "name");
					if(params[1].contains(n) && !str.equals(params[0])){
						String username = str;
						String name = jedis.hexists("user:" + username, "name")? jedis.hget("user:" + username, "name") : "";
						String avatar = jedis.hexists("user:" + username, "name")? jedis.hget("user:" + username, "avatar"): "";
						Set<String> f = jedis.smembers("user:" + username + ":flags");
						String[] flags = new String[f.size()];
						flags = f.toArray(flags);
						SCUser user = new SCUser(username, name, avatar, flags);
						users.add(user);
					}
				}
				
			}
			jedis.close();
			return users;
		}

		return null;
	}

	public void set(String function, String[] params) {
		
	}
}
