package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

public class OnlineModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".OnlineModule";}

	public Type getReceiveType() {return Type.ONLINE;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		String username = cl.username;

		DB.set("User", "setLastSeen", username, System.currentTimeMillis()+"");
		DB.set("User", "setLastAlive", username, System.currentTimeMillis()+"");
		cl.status = Client.ONLINE;
		
		ArrayList<String> ids = DB.getStringArray("Friend", "getFriends", new String[]{username});
		for(int i = 0; i < ids.size(); i++){
			Client target = AuthManager.getInstance().getClient(ids.get(i));
			if(target != null && DB.getBool("Friend", "canSendUser", true, username, target.username)){
				UserInfoResponse.Builder resbuilder = UserInfoResponse.newBuilder();
				resbuilder.setType(Message.Type.USERINFORESPONSE);
				resbuilder.setLastseen(System.currentTimeMillis());
				resbuilder.setStatus(true);
				resbuilder.setTarget(username);
				UserInfoResponse res = resbuilder.build();
				core.send(target.client.uid, res.toByteArray());
			}
		}
		
		return true;
	}
}
