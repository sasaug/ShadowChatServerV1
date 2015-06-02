package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.obj.SCUser;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.SHA256;
import com.sasaug.shadowchat.message.Message.*;

public class CreateGroupModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".CreateGroupModule";}

	public Type getReceiveType() {return Type.CREATEGROUP;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		CreateGroup msg = CreateGroup.parseFrom(data);
		CreateGroupResponse.Builder builder = CreateGroupResponse.newBuilder();
		builder.setType(Type.CREATEGROUP_RESPONSE);
		byte[] d = msg.getAvatar().toByteArray();
		String hash = SHA256.hashString(d);
		String id  = DB.getString("Group", "createGroup", "", msg.getName(), hash, msg.getOwner());
		if(!id.equals("")){
			builder.setErrorCode(1);
			StorageCore.getInstance().save("Image", hash, d);
		}else{
			builder.setErrorCode(1);
		}
		User.Builder userBuilder = User.newBuilder();
		userBuilder.setUsername(id);
		userBuilder.setAvatar(hash);
		userBuilder.setName(msg.getName());
		userBuilder.setFlag(0, SCUser.OWNER);
		builder.setGroup(userBuilder);
		
		CreateGroupResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
