package com.sasaug.shadowchat.network.modules;

import com.google.protobuf.ByteString;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */

public class GetAvatarModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".GetAvatar";}

	public Type getReceiveType() {return Type.GETAVATAR;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{
		GetAvatar msg = GetAvatar.parseFrom(data);
		GetAvatarResponse.Builder builder = GetAvatarResponse.newBuilder();
		builder.setType(Type.GETAVATAR_RESPONSE);
		byte[] b = StorageCore.getInstance().load("Image", msg.getHash());
		if(b != null){
			builder.setErrorCode(0);
			builder.setAvatar(ByteString.copyFrom(b));
		}else{
			builder.setErrorCode(1);
		}
		GetAvatarResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
