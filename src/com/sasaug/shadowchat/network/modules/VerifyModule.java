package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */
public class VerifyModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".VerifyModule";}

	public Type getReceiveType() {return Type.VERIFY;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Verify msg = Verify.parseFrom(data);
				
		VerifyResponse.Builder builder = VerifyResponse.newBuilder();
		builder.setType(Message.Type.VERIFY_RESPONSE);
		builder.setErrorCode(0);
		boolean verified = DB.getBool("User", "verifyPassword", false, msg.getUsername(), msg.getPassword());
		if(verified){
			int status = DB.getInt("User", "verify", 1, msg.getUsername(), msg.getDevice(), msg.getCode());
			if(status == 2){
				//if old tac, we resend
				builder.setStatus(Verification.valueOf(DB.getInt("User", "requestPin", 1, msg.getUsername(), msg.getDevice())));
			}
			builder.setStatus(Verification.valueOf(status));
		}else{
			builder.setStatus(Verification.FAIL);
		}
		VerifyResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
}
