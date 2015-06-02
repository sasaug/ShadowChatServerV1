package com.sasaug.shadowchat.config.modules;

import java.util.Hashtable;

import com.sasaug.shadowchat.config.AConfig;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.modules.IModule;

public class GeneralModule extends AConfig implements IModule{
	
	public String getModuleId() {
		return ConfigCore.ID + ".GeneralModule";
	}
	
	public String getPath() {
		return "config/shadowchat.cfg";
	}

	public void onInit(ConfigCore core, Hashtable<String, String> map){		
		map.put("ssl", "1");
		map.put("sslCert", "");
		map.put("sslKey", "");
		
		map.put("security", "1");
		
		map.put("account_cloud", "1");
		map.put("account_max_device", "1");
		map.put("account_max_device_online", "1");
		
		map.put("register", "1");
		map.put("register_password", "");
		map.put("register_email", "1");
		map.put("register_phone", "0");
		
		map.put("verification", "1");
		map.put("verification_method", "0");
		map.put("verification_reverify_known_device", "1");
		map.put("verification_renew_time", "300");
		map.put("verification_email_server", "smtp.gmail.com");
		map.put("verification_email_server_port", "587");
		map.put("verification_email_login_username", "");
		map.put("verification_email_login_password", "");
		map.put("verification_email_title", "Email Verification");
		map.put("verification_email_message", "Your verification code is : %code%");
		
		map.put("verification_sms_url", "");
		map.put("verification_sms_url", "Your verification code is : %code%");
		
		map.put("search", "1");
		map.put("search_username", "1");
		
		map.put("direct_invite", "1");
		
		map.put("message_max_memory_size", "5120");	
		
	}	
}
