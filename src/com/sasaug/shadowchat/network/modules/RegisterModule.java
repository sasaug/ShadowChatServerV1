package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error Code:
 * 0 = Success
 * 100 = Register disabled
 * 101 = Server password invalid
 * 102 = max device limit hit
 * 110 = Invalid email/phone
 * 
 * 200 = Registration failed
 * 
 * */
public class RegisterModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".RegisterModule";}

	public Type getReceiveType() {return Type.REGISTER;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Register msg = Register.parseFrom(data);
				
		RegisterResponse.Builder builder = RegisterResponse.newBuilder();
		builder.setType(Message.Type.REGISTER_RESPONSE);
		
		boolean needEmail = ConfigCore.getInstance().getBool("register_email", false);
		boolean needPhone = ConfigCore.getInstance().getBool("register_phone", false);
		String serverPassword = ConfigCore.getInstance().get("register_password");
		
		if(!ConfigCore.getInstance().getBool("register", true)){
			builder.setErrorCode(100);
		}else if(needEmail && (!msg.hasEmail() || msg.getEmail().isEmpty())){
			builder.setErrorCode(110);
		}else if(needPhone && (!msg.hasPhone() || msg.getPhone().isEmpty())){
			builder.setErrorCode(110);
		}else if(serverPassword != null && !serverPassword.isEmpty() && (!msg.hasServerPassword() || !msg.getServerPassword().equals(serverPassword))){
			builder.setErrorCode(101);
		}else{
			//check if user existed
			Object obj = DB.get("User", "getUser", msg.getUsername());
			if(obj == null){	//user doesn't exist
				boolean success = DB.getBool("User", "registerUser", false, msg.getUsername(), msg.getPassword(), msg.getDevice(), msg.getName(), msg.getEmail(), msg.getPhone());
				builder.setErrorCode(success? 0: 1);
				if(success){
					if(ConfigCore.getInstance().getBool("verification", false)){
						int status = DB.getInt("User", "requestPin", 0, msg.getUsername(), msg.getDevice());
						builder.setStatus(Verification.valueOf(status));
					}else{
						builder.setStatus(Verification.NOT_REQUIRED);
					}
				}else{
					builder.setStatus(Verification.NOT_REQUIRED);
					builder.setErrorCode(200);
				}
			}else{	//user existed
				//check if this device existed
				boolean isValidDevice = DB.getBool("User", "isValidDevice", false, msg.getUsername(), msg.getDevice());
				if(isValidDevice){	
					//do verification
					boolean serverVerification = ConfigCore.getInstance().getBool("verification", false);
					if(serverVerification){
						boolean reverify = ConfigCore.getInstance().getBool("verification_reverify_known_device", false);
						if(reverify){
							DB.set("User", "resetDevice", msg.getUsername(), msg.getDevice());
							int status = DB.getInt("User", "requestPin", 0, msg.getUsername(), msg.getDevice());
							builder.setStatus(Verification.valueOf(status));
						}else{
							builder.setStatus(Verification.NOT_REQUIRED);
						}
					}else{
						builder.setStatus(Verification.NOT_REQUIRED);
					}
					builder.setErrorCode(0);
				}else{
					//try to register this new device
					int maxDevice = ConfigCore.getInstance().getInt("account_max_device");
					int deviceCount = DB.getInt("User", "getDeviceCount", 0, msg.getUsername());
					if(deviceCount < maxDevice){	//can register
						DB.set("User", "addDevice", msg.getUsername(), msg.getDevice());

						boolean serverVerification = ConfigCore.getInstance().getBool("verification", false);
						if(serverVerification){
							int status = DB.getInt("User", "requestPin", 0, msg.getUsername(), msg.getDevice());
							builder.setStatus(Verification.valueOf(status));
						}else{
							builder.setStatus(Verification.NOT_REQUIRED);
						}
						builder.setErrorCode(0);
					}else{	//hit max limit
						builder.setStatus(Verification.NOT_REQUIRED);
						builder.setErrorCode(102);
					}
				}
			}
		}
		RegisterResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
