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
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */
public class RequestPinModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".RequestPinModule";}

	public Type getReceiveType() {return Type.REQUESTPIN;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		RequestPin msg = RequestPin.parseFrom(data);
			
		RequestPinResponse.Builder builder = RequestPinResponse.newBuilder();
		builder.setType(Message.Type.REQUESTPIN_RESPONSE);
		builder.setErrorCode(0);
		if(!ConfigCore.getInstance().getBool("verification", false)){
			builder.setStatus(Verification.FAIL);
		}else{
			boolean verified = DB.getBool("User", "verifyPassword", false, msg.getUsername(), msg.getPassword());
			if(verified){
				builder.setStatus(Verification.valueOf(DB.getInt("User", "requestPin", 1, msg.getUsername(), msg.getDevice())));
			}else{
				builder.setStatus(Verification.FAIL);
			}
		}

		RequestPinResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
