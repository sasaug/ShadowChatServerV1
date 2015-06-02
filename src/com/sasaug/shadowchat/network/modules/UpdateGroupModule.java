package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.obj.SCUser;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.SHA256;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */

public class UpdateGroupModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".UpdateGroupModule";}

	public Type getReceiveType() {return Type.UPDATEGROUP;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		UpdateGroup msg = UpdateGroup.parseFrom(data);
		UpdateGroupResponse.Builder builder = UpdateGroupResponse.newBuilder();
		builder.setType(Type.UPDATEGROUP_RESPONSE);
		String flag = DB.getString("Group", "getRank", SCUser.GUEST, msg.getGroup(), cl.username);
		if(flag.equals(SCUser.ADMIN) || flag.equals(SCUser.OWNER)){
			String name = msg.hasName()? msg.getName() : null;
			String hash = null;
			byte[] d = msg.hasImage()? msg.getImage().toByteArray(): null;
			if(d != null){
				hash = SHA256.hashString(d);
				StorageCore.getInstance().save("Image", hash, d);
			}
			DB.set("Group", "updateGroup", msg.getGroup(), name, hash);
			builder.setErrorCode(0);
		}else{
			builder.setErrorCode(1);
		}
		UpdateGroupResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
