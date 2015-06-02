package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.notification.NotificationCore;
import com.sasaug.shadowchat.obj.SCOS;
import com.sasaug.shadowchat.obj.SCUser;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.Base64;
import com.sasaug.shadowchat.utils.SHA256;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */

public class AddFriendModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".AddFriendModule";}

	public Type getReceiveType() {return Type.ADDFRIEND;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		AddFriend msg = AddFriend.parseFrom(data);
		AddFriendResponse.Builder builder = AddFriendResponse.newBuilder();
		StringBuilder keys = new StringBuilder();
		for(int i = 0; i < msg.getKeysCount(); i++){
			String encoded = Base64.encodeToString(msg.getKeys(i).toByteArray(), Base64.DEFAULT);
			keys.append(encoded);
			if(msg.getKeysCount()-1 != i){
				keys.append(";");
			}
		}
		boolean success = DB.getBool("Friend", "addFriend", false, cl.username, msg.getFriend(), keys.toString());
		builder.setType(Type.ADDFRIEND_RESPONSE);
		builder.setErrorCode(success? 0 : 1);
		if(success){
			try{
				if(DB.getBool("Friend", "canSendUser", true, cl.username, msg.getFriend())){
					SCUser user = (SCUser)DB.get("User", "getUser", msg.getFriend());
					User.Builder b = User.newBuilder();
					b.setUsername(user.username);
					b.setName(user.name);
					b.setAvatar(user.avatar);
					b.addAllFlag(user.flag);
					b.setIsGroup(false);	
					builder.setUser(b.build());
					
					FriendRequest.Builder bl = FriendRequest.newBuilder();
					bl.setType(Type.FRIEND_REQUEST);
					bl.setStatus(SCUser.FRSTATUS_REQUESTING);
					bl.addAllKeys(msg.getKeysList());
					SCUser u= (SCUser)DB.get("User", "getUser", cl.username);
					User.Builder bld = User.newBuilder();
					bld.setUsername(u.username);
					bld.setName(u.name);
					bld.setAvatar(u.avatar);
					bld.addAllFlag(u.flag);
					bld.setIsGroup(false);	
					bl.setUser(bld.build());
					
					byte[] dat = bl.build().toByteArray();
					save(msg.getFriend(), dat);
						
					Client target = AuthManager.getInstance().getClient(msg.getFriend());
					if(target != null){
						core.send(target.client.uid, dat);
					}else{
						ArrayList<String> devices = DB.getStringArray("User", "getDevices", msg.getFriend());
						for(String device: devices){
							String cloudid = DB.getString("User", "getCloudId", null, msg.getFriend(), device);
							if(cloudid != null){
								String os = DB.getString("User", "getOS", SCOS.ANDROID, msg.getFriend(), device);
								NotificationCore.getInstance().notify(os, cloudid, NotificationCore.NEWMSG);
							}
						}
					}	
				}else{
					builder.setErrorCode(1);
				}
				
			}catch(Exception e){e.printStackTrace();}
		}
		AddFriendResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		
		
		return true;
	}
	
	public void save(String username, byte[] data) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		String value = ConfigCore.getInstance().get("message_max_memory_size");
		int length = Integer.parseInt(value);
		String hash = SHA256.hashString(data);
		if(data.length < length){
			DB.set("Message", "addMessage", username, hash, Base64.encodeToString(data, Base64.DEFAULT));
		}else{
			DB.set("Message", "addMessage", username, hash, "");
			StorageCore.getInstance().save("Message", username+ "." + hash,  data);
		}
	}
	
	public void onClientDisconnected(TCPClient client){}
}
