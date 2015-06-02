package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.obj.SCUser;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

/*
 * Error code:
 * 0 - Success
 * 100 - Server disabled search
 * */
public class SearchModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".SearchModule";}

	public Type getReceiveType() {return Type.SEARCH;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	@SuppressWarnings("unchecked")
	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Search msg = Search.parseFrom(data);
		Client cl = AuthManager.getInstance().getClient(client);
		SearchResponse.Builder builder = SearchResponse.newBuilder();
		builder.setType(Message.Type.SEARCH_RESPONSE);
		
		if(ConfigCore.getInstance().get("search").equals("1")){
			String search = msg.getTerm();
			if(search.startsWith("@")){
				if(ConfigCore.getInstance().get("search_username").equals("1")){
					String username = search.substring(1);
					if(!username.equals(cl.username)){
						Object o = DB.get("User", "getUser", username);
						if(o != null){
							SCUser user = (SCUser)o;
							User.Builder b = User.newBuilder();;
							b.setUsername(user.username);
							b.setName(user.name != null? user.name: "");
							b.setAvatar(user.avatar != null? user.avatar: "");
							b.addAllFlag(user.flag);
							b.setIsGroup(true);
							for(SCUser u: user.users){
								User.Builder bd = User.newBuilder();;
								bd.setUsername(u.username);
								bd.setName(u.name);
								bd.setAvatar(u.avatar);
								bd.addAllFlag(u.flag);
								bd.setIsGroup(false);
								b.addUsers(bd);
							}
							builder.addList(b);
						}
					}
				}
			}else{
				ArrayList<SCUser> users = (ArrayList<SCUser>)DB.get("Search", "search", cl.username, search);
				for(int i = 0; i < users.size();i++){
					SCUser user = users.get(i);
					User.Builder b = User.newBuilder();;
					b.setUsername(user.username);
					b.setName(user.name != null? user.name: "");
					b.setAvatar(user.avatar != null? user.avatar: "");
					b.addAllFlag(user.flag);
					b.setIsGroup(true);
					for(SCUser u: user.users){
						User.Builder bd = User.newBuilder();;
						bd.setUsername(u.username);
						bd.setName(u.name);
						bd.setAvatar(u.avatar);
						bd.addAllFlag(u.flag);
						bd.setIsGroup(false);
						b.addUsers(bd);
					}
					builder.addList(b);
				}		
			}
			builder.setErrorCode(0);
		}else{
			builder.setErrorCode(100);
		}
		
		SearchResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){}
}
