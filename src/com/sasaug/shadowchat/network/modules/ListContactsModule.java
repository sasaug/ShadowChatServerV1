package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.*;
import com.sasaug.shadowchat.obj.SCUser;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 1 - Failed
 * */
public class ListContactsModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".ListContactsModule";}

	public Type getReceiveType() {return Type.LISTCONTACTS;}
	
	NetworkCore core;
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	@SuppressWarnings("unchecked")
	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		
		ListContactsResponse.Builder builder = ListContactsResponse.newBuilder();
		builder.setType(Type.LISTCONTACTS_RESPONSE);
		builder.setErrorCode(0);
		ArrayList<SCUser> friends = (ArrayList<SCUser>)DB.get("Friend", "getFriends", cl.username);
		for(SCUser user:friends){
			User.Builder b = User.newBuilder();;
			b.setUsername(user.username);
			b.setName(user.name);
			b.setAvatar(user.avatar);
			b.addAllFlag(user.flag);
			b.setIsGroup(false);
			builder.addFriends(b.build());
		}
		friends = (ArrayList<SCUser>)DB.get("Group", "getGroups", cl.username);
		for(SCUser group:friends){
			User.Builder b = User.newBuilder();;
			b.setUsername(group.username);
			b.setName(group.name);
			b.setAvatar(group.avatar);
			b.addAllFlag(group.flag);
			b.setIsGroup(true);
			for(SCUser user: group.users){
				User.Builder bd = User.newBuilder();;
				bd.setUsername(user.username);
				bd.setName(user.name);
				bd.setAvatar(user.avatar);
				bd.addAllFlag(user.flag);
				bd.setIsGroup(false);
				b.addUsers(bd.build());
			}
			builder.addFriends(b.build());
		}
		ListContactsResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
