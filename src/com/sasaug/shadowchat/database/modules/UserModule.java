package com.sasaug.shadowchat.database.modules;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.database.IDatabase;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.notification.NotificationCore;
import com.sasaug.shadowchat.obj.SCUser;
import com.sasaug.shadowchat.security.SecurityCore;
import com.sasaug.shadowchat.utils.SHA256;

public class UserModule implements IDatabase, IModule{
	
	public String getModuleId() {
		return DatabaseCore.ID + ".UserModule";
	}
	
	public String getName(){
		return "User";
	}
	
	DatabaseCore core;
	public void onInit(DatabaseCore core){
		this.core = core;
		core.registerFunction(getName(), "verifyPassword", DatabaseCore.FuncType.GET, Boolean.class, 2);
		core.registerFunction(getName(), "getCloudId", DatabaseCore.FuncType.GET, String.class, 2);
		core.registerFunction(getName(), "getOS", DatabaseCore.FuncType.GET, String.class, 2);
		core.registerFunction(getName(), "getLastSeen", DatabaseCore.FuncType.GET, Long.class, 1);
		core.registerFunction(getName(), "isUserBanned", DatabaseCore.FuncType.GET, Boolean.class, 1);
		core.registerFunction(getName(), "canSendUser", DatabaseCore.FuncType.GET, Boolean.class, 2);
		core.registerFunction(getName(), "registerUser", DatabaseCore.FuncType.GET, Boolean.class, 6);
		core.registerFunction(getName(), "requestPin", DatabaseCore.FuncType.GET, Integer.class, 2);
		core.registerFunction(getName(), "verify", DatabaseCore.FuncType.GET, Integer.class, 3);
		core.registerFunction(getName(), "getUser", DatabaseCore.FuncType.GET, SCUser.class, 1);
		core.registerFunction(getName(), "getDevices", DatabaseCore.FuncType.GET, (new ArrayList<String>()).getClass(), 1);
		core.registerFunction(getName(), "getDeviceCount", DatabaseCore.FuncType.GET, Integer.class, 1);
		core.registerFunction(getName(), "isValidDevice", DatabaseCore.FuncType.GET, Boolean.class, 2);
		
		core.registerFunction(getName(), "setCloudId", DatabaseCore.FuncType.SET, null, 3);
		core.registerFunction(getName(), "setOS", DatabaseCore.FuncType.SET, null, 3);
		core.registerFunction(getName(), "setLastSeen", DatabaseCore.FuncType.SET, null, 2);
		core.registerFunction(getName(), "setLastAlive", DatabaseCore.FuncType.SET, null, 2);
		core.registerFunction(getName(), "addDevice", DatabaseCore.FuncType.SET, null, 2);
		core.registerFunction(getName(), "resetDevice", DatabaseCore.FuncType.SET, null, 2);
	}

	public Object get(String function, String[] params) {
		if(function.equals("verifyPassword")){
			String username = params[0];
			String password = params[1];
			
			Jedis jedis = core.getJedis();
			String pass = jedis.hget("user:" + username, "password");
			String salt = jedis.hget("user:" + username, "password_salt");
			try{
				password = SHA256.hash(password+salt);
			}catch(Exception e){}
			boolean result = false;
			if(password.equals(pass))
				result = true;
			jedis.close();
			return result;		
		}else if(function.equals("getLastSeen")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			String result = jedis.hget("user:" + username, "lastSeen");
			jedis.close();
			return result;
		}else if(function.equals("getCloudId")){
			String username = params[0];
			String device = params[1];
			Jedis jedis = core.getJedis();
			String result = jedis.hget("user:" + username + ":devices:" + device, "cloudId");
			jedis.close();
			return result;
		}else if(function.equals("getOS")){
			String username = params[0];
			String device = params[1];
			Jedis jedis = core.getJedis();
			String result = jedis.hget("user:" + username + ":devices:" + device, "os");
			jedis.close();
			return result;
		}else if(function.equals("isUserBanned")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			boolean banned = false;
			if(jedis.hexists("user:" + username, "status")){
				//0 = ban, -1 = delete, 1 = ok
				if(jedis.hget("user:" + username, "status").equals("0")){
					banned = true;
				}
			}
			return banned;	
		}else if(function.equals("registerUser")){
			String username = params[0];
			String password = params[1];
			String device = params[2];
			String name = params[3];
			String email = params[4];
			String phone = params[5];
			boolean requireVerification = params[6].equals("1");
			
			Jedis jedis = core.getJedis();
			boolean success = false;
			if(!jedis.exists("user:" + username)){
				String salt = SecurityCore.getInstance().randomPassword();
				try{
					password = SHA256.hash(password+salt);
				}catch(Exception e){}
				jedis.hset("user:" + username, "password", password);
				jedis.hset("user:" + username, "password_salt", salt);
				jedis.hset("user:" + username, "name", name);	
				if(email != null)
					jedis.hset("user:" + params[0], "email", email);
				if(phone != null)
					jedis.hset("user:" + params[0], "phone", phone);
				jedis.hset("user:" + params[0], "avatar", "");
				jedis.sadd("user:" + username + ":devices", device);
				jedis.hset("user:" + username + ":devices:" + device, "verified", requireVerification? "0": "1");
				jedis.hset("user:" + username + ":devices:" + device, "verificationCode", "");
				jedis.hset("user:" + username + ":devices:" + device, "verificationTime", requireVerification? "": System.currentTimeMillis()+"");
				success = true;
			}else{
				success = false;
			}
			jedis.close();
			return success;
		}else if(function.equals("requestPin")){
			String username = params[0];
			String device = params[1];
			Jedis jedis = core.getJedis();
			int res = 0;
			String result = jedis.hget("user:" + username + ":devices:" + device, "verified");
			if(!result.equals("1")){
				long time = Long.parseLong(jedis.hget("user:" + username + ":devices:" + device, "verificationTime"));
				int renewal = ConfigCore.getInstance().getInt("verification_renew_time");
				time += (renewal * 1000);
				if(time <= System.currentTimeMillis()){
					int max = 999999;
					int min = 100000;
					int randomNumber = new Random().nextInt((max - min) + 1) + min;
					jedis.hset("user:" + username + ":devices:" + device, "verificationTime", System.currentTimeMillis()+"");
					jedis.hset("user:" + username + ":devices:" + device, "verificationCode", randomNumber+"");
					
					String method = ConfigCore.getInstance().get("verification_method");
					if(method != null){
						if(method.equals("0")){
							String email = jedis.hget("user:" + username, "email");
							NotificationCore.getInstance().notify("EmailVerification", email, randomNumber+"");
						}else if(method.equals("1")){
							String phone = jedis.hget("user:" + username, "phone");
							NotificationCore.getInstance().notify("SMSVerification", phone, randomNumber+"");
						}
					}
					res = 3;
				}else{
					res = 2;
				}
			}else{
				res = 0;
			}
			jedis.close();
			return res;
		}else if(function.equals("verify")){
			String username = params[0];
			String device = params[1];
			String pin = params[2];
			Jedis jedis = core.getJedis();
			int res = 1;
			if(!jedis.hget("user:" + username + ":devices:" + device, "verified").equals("1")){
				long time = Long.parseLong(jedis.hget("user:" + username + ":devices:" + device, "verificationTime"));
				int renewal = ConfigCore.getInstance().getInt("verification_renew_time");
				time += (renewal * 1000);
				if(time <= System.currentTimeMillis()){
					res = 2;
				}else{
					String code = jedis.hget("user:" + username + ":devices:" + device, "verificationCode");
					if(pin.equals(code)){
						jedis.hset("user:" + username + ":devices:" + device, "verified", "1");
						res = 0;
					}else{
						res = 1;
					}
				}
			}else{
				res = 0;
			}
			jedis.close();
			return res;
		}else if(function.equals("getUser")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			if(jedis.exists("user:" + username)){
				String name = jedis.hexists("user:" + username, "name")? jedis.hget("user:" + username, "name") : "";
				String avatar = jedis.hexists("user:" + username, "avatar")? jedis.hget("user:" + username, "avatar"): "";
				Set<String> f = jedis.smembers("user:" + username + ":flags");
				String[] flags = new String[f.size()];
				flags = f.toArray(flags);
				SCUser user = new SCUser(username, name, avatar, flags);
				return user;
			}
		}else if(function.equals("getDevices")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			ArrayList<String> result = new ArrayList<String>();
			Set<String> set = jedis.smembers("user:" + username + ":devices");
			for(String str: set){
				result.add(str);
			}
			jedis.close();
			return result;
		}else if(function.equals("getDeviceCount")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			long count = jedis.scard("user:" + username + ":devices");
			int result = (int)count;
			jedis.close();
			return result;
		}else if(function.equals("isValidDevice")){
			String username = params[0];
			Jedis jedis = core.getJedis();
			boolean result = false;
			Set<String> set = jedis.smembers("user:" + username + ":devices");
			for(String str: set){
				if(str.equals(params[1])){
					result = jedis.hget("user:" + username + ":devices:" + str, "verified").equals("1")? true: false;
					break;
				}
			}
			jedis.close();
			return result;
		}

		return null;
	}

	public void set(String function,String[] params) {
		if(function.equals("setLastSeen")){
			String username = params[0];
			String lastSeen = params[1];
			Jedis jedis = core.getJedis();
			if(username != null)
				jedis.hset("user:" + username, "lastSeen", lastSeen);
			jedis.close();
		}else if(function.equals("setLastAlive")){
			String username = params[0];
			String lastAlive = params[1];
			Jedis jedis = core.getJedis();
			if(username != null)
				jedis.hset("user:" + username, "lastAlive", lastAlive);
			jedis.close();
		}else if(function.equals("setCloudId")){
			String username = params[0];
			String device = params[1];
			String cloudId = params[2];
			Jedis jedis = core.getJedis();
			jedis.hset("user:" + username + ":devices:" + device, "cloudId", cloudId);
			jedis.close();
		}else if(function.equals("setOS")){
			String username = params[0];
			String device = params[1];
			String os = params[2];
			Jedis jedis = core.getJedis();
			jedis.hset("user:" + username + ":devices:" + device, "os", os);
			jedis.close();
		}else if(function.equals("addDevice")){
			String username = params[0];
			String device = params[1];
			Jedis jedis = core.getJedis();
			jedis.sadd("user:" + username + ":devices", device);
			jedis.hset("user:" + username + ":devices:" + device, "verified", "0");
			jedis.hset("user:" + username + ":devices:" + device, "verificationCode", "");
			jedis.hset("user:" + username + ":devices:" + device, "verificationTime", "0");
			jedis.close();
		}else if(function.equals("resetDevice")){
			String username = params[0];
			String device = params[1];
			Jedis jedis = core.getJedis();
			jedis.hset("user:" + username + ":devices:" + device, "verified", "0");
			jedis.close();
		}
	}
}
