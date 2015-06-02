package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

public class UserInfoModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".UserInfoModule";}

	public Type getReceiveType() {return Type.USERINFO;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		String username = AuthManager.getInstance().getUsername(client);
		UserInfo msg = UserInfo.parseFrom(data);
		
		DB.set("User", "setLastSeen", username, System.currentTimeMillis()+"");
		DB.set("User", "setLastAlive",username, System.currentTimeMillis()+"");
		
		if(!DB.getBool("Friend", "canSendUser", true, username, msg.getTarget()))
			return true;
		
		Client target = AuthManager.getInstance().getClient(msg.getTarget());
		
		if(target != null && target.status == Client.ONLINE){
			String lastseen = DB.getString("User", "getLastSeen", "0", msg.getTarget());
			UserInfoResponse.Builder builder = UserInfoResponse.newBuilder();
			builder.setType(Message.Type.USERINFORESPONSE);
			builder.setLastseen(lastseen == null ? 0 : Long.parseLong(lastseen));
			builder.setTarget(msg.getTarget());
			builder.setStatus(true);
			UserInfoResponse response = builder.build();
			core.send(client.uid, response.toByteArray());
		}else{
			String lastseen = DB.getString("User", "getLastSeen", "0", msg.getTarget());
			UserInfoResponse.Builder builder = UserInfoResponse.newBuilder();
			builder.setType(Message.Type.USERINFORESPONSE);
			builder.setLastseen(lastseen == null ? 0 : Long.parseLong(lastseen));
			builder.setTarget(msg.getTarget());
			builder.setStatus(false);
			UserInfoResponse response = builder.build();
			core.send(client.uid, response.toByteArray());
		}

		return true;
	}
}
