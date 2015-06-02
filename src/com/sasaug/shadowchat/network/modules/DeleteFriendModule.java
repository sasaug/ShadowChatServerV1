package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */

public class DeleteFriendModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".DeleteFriendModule";}

	public Type getReceiveType() {return Type.DELETEFRIEND;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		DeleteFriend msg = DeleteFriend.parseFrom(data);
		DeleteFriendResponse.Builder builder = DeleteFriendResponse.newBuilder();
		builder.setType(Type.DELETEFRIEND_RESPONSE);
		builder.setErrorCode(DB.getBool("Friend", "deleteFriend", false, cl.username, msg.getFriend())? 0: 1);
		DeleteFriendResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
	
		
		//TODO: send notification
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
