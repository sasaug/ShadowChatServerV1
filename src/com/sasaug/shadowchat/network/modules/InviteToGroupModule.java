package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.notification.NotificationCore;
import com.sasaug.shadowchat.obj.SCOS;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */

public class InviteToGroupModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".InviteToGroupModule";}

	public Type getReceiveType() {return Type.INVITETOGROUP;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		InviteToGroup msg = InviteToGroup.parseFrom(data);
		InviteToGroupResponse.Builder builder = InviteToGroupResponse.newBuilder();
		builder.setType(Type.INVITETOGROUP_RESPONSE);
		builder.setErrorCode(DB.getBool("Group", "inviteToGroup", false, msg.getGroup(), cl.username, msg.getUser())? 0 : 1);
		InviteToGroupResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		
		String cloudid = DB.getString("User", "getCloudId", null, msg.getUser());
		if(cloudid != null){
			String os = DB.getString("User", "getOS", SCOS.ANDROID, msg.getUser());
			NotificationCore.getInstance().notify(os, cloudid, NotificationCore.GROUPINVITE);
		}
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
