package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message.*;

public class TypingModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".TypingModule";}

	public Type getReceiveType() {return Type.TYPING;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		String username = AuthManager.getInstance().getUsername(client);
		TypingMessage msg = TypingMessage.parseFrom(data);
		
		DB.set("User", "setLastSeen", username, System.currentTimeMillis()+"");
		DB.set("User", "setLastAlive", username, System.currentTimeMillis()+"");

		if(!DB.getBool("Friend", "canSendUser", true, username, msg.getTarget()))
			return true;

		Client target = AuthManager.getInstance().getClient(msg.getTarget());
		if(target != null){
			core.send(target.client.uid, msg.toByteArray());
		}

		return true;
	}
}
