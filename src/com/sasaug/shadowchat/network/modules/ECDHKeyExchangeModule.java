package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.notification.NotificationCore;
import com.sasaug.shadowchat.obj.SCOS;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.utils.Base64;
import com.sasaug.shadowchat.utils.SHA256;
import com.sasaug.shadowchat.message.Message.*;

public class ECDHKeyExchangeModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".ECDHKeyExchangeModule";}

	public Type getReceiveType() {return Type.ECDH_KEYX;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		String username = cl.username;
		ECDHKeyExchangeMessage msg = ECDHKeyExchangeMessage.parseFrom(data);

		DB.set("User", "setLastSeen", new String[] {username, System.currentTimeMillis()+""});
		DB.set("User", "setLastAlive", new String[] {username, System.currentTimeMillis()+""});
		
		Ack.Builder ackBuilder = Ack.newBuilder();
		ackBuilder.setType(Type.ACK);
		ackBuilder.setAckType(ACKType.SENT);
		BaseMessage.Builder baseBuilder = BaseMessage.newBuilder();
		baseBuilder.setId(msg.getId());
		baseBuilder.setTarget(msg.getTarget());
		baseBuilder.setOrigin(msg.getOrigin());
		baseBuilder.setTimestamp(System.currentTimeMillis());
		if(msg.hasGroup())
			baseBuilder.setGroup(msg.getGroup());
		ackBuilder.setMessage(baseBuilder.build());
		core.send(cl.client.uid, ackBuilder.build().toByteArray());
		
		ArrayList<String> receivers = new ArrayList<String>();
		if(msg.hasGroup() && msg.getTarget().equals(msg.getGroup())){
			String groupId = msg.getGroup();
			groupId = groupId.substring(5);
			ArrayList<String> temp = DB.getStringArray("Group", "getMembers", new String[]{groupId});
			for(int i = 0; i < temp.size(); i++){
				if(!temp.get(i).equals(username))
					receivers.add(temp.get(i));
			}
		}else{
			receivers.add(msg.getTarget());
		}
		
		ECDHKeyExchangeMessage.Builder builder = ECDHKeyExchangeMessage.newBuilder(msg);
		builder.setTimestamp(System.currentTimeMillis());
		ECDHKeyExchangeMessage response = builder.build();
		
		for(int i = 0; i < receivers.size(); i++){
			Client target = AuthManager.getInstance().getClient(receivers.get(i));
			boolean canSend =false;
			if(msg.hasGroup()){
				if(DB.getBool("Group", "canSendGroup", true, receivers.get(i), msg.getGroup())){
					canSend = true;
					String obj = Base64.encodeToString(response.toByteArray(), Base64.DEFAULT);
					DB.set("Message", "addMessage", receivers.get(i), getMessageHash(msg), obj);
				}
			}else{
				if(DB.getBool("Friend", "canSendUser", true, username, receivers.get(i))){
					canSend = true;
					String obj = Base64.encodeToString(response.toByteArray(), Base64.DEFAULT);
					DB.set("Message", "addMessage", receivers.get(i), getMessageHash(msg), obj);
				}
			}
			
			if(target != null){
				if(canSend)
					core.send(target.client.uid, response.toByteArray());
			}else{
				if(canSend){
					String cloudid = DB.getString("User", "getCloudId", null, receivers.get(i));
					if(cloudid != null){
						String os = DB.getString("User", "getOS", SCOS.ANDROID, receivers.get(i));
						NotificationCore.getInstance().notify(os, cloudid, NotificationCore.NEWMSG);
					}
				}
			}
		}						

		return true;
	}
	
	private String getMessageHash(ECDHKeyExchangeMessage msg){
		StringBuffer buffer = new StringBuffer();
		buffer.append(msg.getId());
		buffer.append(".");
		buffer.append(msg.getTarget());
		if(msg.hasGroup()){
			buffer.append(".");
			buffer.append(msg.getGroup());
		}
		try{
			String hash = SHA256.hash(buffer.toString());
			return hash;
		}catch(Exception ex){}
		return buffer.toString();
	}
}
