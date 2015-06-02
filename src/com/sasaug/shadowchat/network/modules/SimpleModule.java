package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.notification.NotificationCore;
import com.sasaug.shadowchat.obj.SCOS;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.Base64;
import com.sasaug.shadowchat.utils.SHA256;
import com.sasaug.shadowchat.message.Message.*;

public class SimpleModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".SimpleModule";}

	public Type getReceiveType() {return Type.SIMPLE;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		String username = cl.username;
		SimpleMessage msg = SimpleMessage.parseFrom(data);

		DB.set("User", "setLastSeen", new String[] {username, System.currentTimeMillis()+""});
		DB.set("User", "setLastAlive", new String[] {username, System.currentTimeMillis()+""});
		
		Ack.Builder ackBuilder = Ack.newBuilder();
		ackBuilder.setType(Type.ACK);
		ackBuilder.setAckType(ACKType.SENT);
		BaseMessage.Builder baseBuilder = BaseMessage.newBuilder();
		baseBuilder.setType(Type.SIMPLE);
		baseBuilder.setId(msg.getId());
		baseBuilder.setTarget(msg.getTarget());
		baseBuilder.setOrigin(msg.getOrigin());
		baseBuilder.setTimestamp(System.currentTimeMillis());
		if(msg.hasGroup())
			baseBuilder.setGroup(msg.getGroup());
		ackBuilder.setMessage(baseBuilder.build());
		ackBuilder.setHash(SHA256.hashString(data));
		core.send(cl.client.uid, ackBuilder.build().toByteArray());
		
		ArrayList<String> receivers = new ArrayList<String>();
		if(msg.hasGroup() && msg.getTarget().equals(msg.getGroup())){
			String groupId = msg.getTarget();
			groupId = groupId.substring(5);
			ArrayList<String> temp = DB.getStringArray("Group", "getMembers", new String[]{groupId});
			for(int i = 0; i < temp.size(); i++){
				if(!temp.get(i).equals(username))
					receivers.add(temp.get(i));
			}
		}else{
			receivers.add(msg.getTarget());
		}
		
		SimpleMessage.Builder builder = SimpleMessage.newBuilder(msg);
		builder.setTimestamp(System.currentTimeMillis());
		SimpleMessage response = builder.build();
		
		for(int i = 0; i < receivers.size(); i++){
			Client target = AuthManager.getInstance().getClient(receivers.get(i));
			boolean canSend =false;
			if(msg.hasGroup()){
				if(DB.getBool("Group", "canSendGroup", true, receivers.get(i), msg.getGroup())){
					canSend = true;
					save(receivers.get(i), response.toByteArray());
				}
			}else{
				if(DB.getBool("Friend", "canSendUser", true, username, receivers.get(i))){
					canSend = true;
					save(receivers.get(i), response.toByteArray());
				}
			}
			
			if(target != null){
				if(canSend)
					core.send(target.client.uid, response.toByteArray());
			}else{
				if(canSend){
					ArrayList<String> devices = DB.getStringArray("User", "getDevices", receivers.get(i));
					for(String device: devices){
						String cloudid = DB.getString("User", "getCloudId", null, receivers.get(i), device);
						if(cloudid != null){
							String os = DB.getString("User", "getOS", SCOS.ANDROID, receivers.get(i), device);
							NotificationCore.getInstance().notify(os, cloudid, NotificationCore.NEWMSG);
						}
					}
				}
			}
		}						

		return true;
	}
	
	private void save(String username, byte[] data) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		String value = ConfigCore.getInstance().get("message_max_memory_size");
		int length = Integer.parseInt(value);

		BaseMessage msg = BaseMessage.parseFrom(data);
		StringBuffer buffer = new StringBuffer();
		buffer.append(msg.getId());
		buffer.append(".");
		buffer.append(msg.getOrigin());
		buffer.append(".");
		buffer.append(msg.getTarget());
		if(msg.hasGroup()){
			buffer.append(".");
			buffer.append(msg.getGroup());
		}
		String hash = SHA256.hash(buffer.toString());		
		
		if(data.length < length){
			DB.set("Message", "addMessage", username, hash, Base64.encodeToString(data, Base64.DEFAULT));
		}else{
			DB.set("Message", "addMessage", username, hash, "");
			StorageCore.getInstance().save("Message", username+ "." + hash,  data);
		}
	}
}
