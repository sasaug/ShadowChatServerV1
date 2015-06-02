package com.sasaug.shadowchat.network.modules;

import com.google.gson.Gson;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.obj.SCServerInfo;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */

public class InitialModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".InitialModule";}

	public Type getReceiveType() {return Type.INITIAL;}
	
	public boolean requiresAuth() {
		return false;
	}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		AuthManager.getInstance().addClient(client);
		ConfigCore cfg = ConfigCore.getInstance();
		
		SCServerInfo info = new SCServerInfo();
		info.setRegister(cfg.getBool("register", false));
		info.setRegisterEmail(cfg.getBool("register_email", false));
		info.setRegisterPhone(cfg.getBool("register_phone", false));
		info.setRegisterPassword(cfg.get("register_password").isEmpty()? false: true);
		info.setVerificationMethod(cfg.getInt("verification_method"));
		info.setSecurity(cfg.getInt("security"));
	
		
		InitialResponse.Builder builder = InitialResponse.newBuilder();
		builder.setType(Message.Type.INITIAL_RESPONSE);
		builder.setInfo(new Gson().toJson(info));
		builder.setErrorCode(0);
		InitialResponse response = builder.build();
		core.send(client.uid, response.toByteArray());

		return true;
	}
}
