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

public class JoinGroupModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".JoinGroupModule";}

	public Type getReceiveType() {return Type.JOINGROUP;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		JoinGroup msg = JoinGroup.parseFrom(data);
		JoinGroupResponse.Builder builder = JoinGroupResponse.newBuilder();
		builder.setType(Type.JOINGROUP_RESPONSE);
		builder.setErrorCode(DB.getBool("Group", "joinGroup", false, msg.getGroup(), cl.username)? 0 : 1);
		JoinGroupResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
