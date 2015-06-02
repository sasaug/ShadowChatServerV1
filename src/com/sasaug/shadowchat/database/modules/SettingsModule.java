package com.sasaug.shadowchat.database.modules;

import redis.clients.jedis.Jedis;

import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.database.IDatabase;
import com.sasaug.shadowchat.modules.IModule;

public class SettingsModule implements IDatabase, IModule{

	public String getModuleId() {
		return DatabaseCore.ID + ".SettingsModule";
	}
	
	public String getName(){
		return "Settings";
	};
	
	DatabaseCore core;
	public void onInit(DatabaseCore core){
		this.core = core;
		core.registerFunction(getName(), "verificationEnabled", DatabaseCore.FuncType.GET, Boolean.class, 0);
		core.registerFunction(getName(), "getPassword", DatabaseCore.FuncType.GET, String.class, 0);
		core.registerFunction(getName(), "getVerificationRenewal", DatabaseCore.FuncType.GET, Integer.class, 0);
	}

	public Object get(String function, String[] params) {
		if(function.equals("verificationEnabled")){
			Jedis jedis = core.getJedis();
			String result = jedis.hget("settings", "verification");
			jedis.close();
			if(result != null & result.equals("1"))
				return true;
			else return false;
		}else if(function.equals("getPassword")){
			Jedis jedis = core.getJedis();
			String result = jedis.hget("settings", "password");
			jedis.close();
			return result;
		}else if(function.equals("getVerificationRenewal")){
			Jedis jedis = core.getJedis();
			int result = 300;
			if(jedis.hexists("settings", "verificationRenewalTime"))
				result = Integer.parseInt(jedis.hget("settings", "verificationRenewalTime"));
			jedis.close();
			return result;
		}

		return null;
	}

	public void set(String function, String[] params) {
		
	}
}
