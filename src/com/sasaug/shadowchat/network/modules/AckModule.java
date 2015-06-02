package com.sasaug.shadowchat.network.modules;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.Base64;
import com.sasaug.shadowchat.utils.SHA256;
import com.sasaug.shadowchat.message.Message.*;

public class AckModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".AckModule";}

	public Type getReceiveType() {return Type.ACK;}
	
	public boolean requiresAuth() {
		return false;
	}
	
	NetworkCore core;
	AuthManager auth;
	
	public void onInit(NetworkCore core) {
		this.core = core;
		this.auth = AuthManager.getInstance();
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		String username = cl.username;
		Ack msg = Ack.parseFrom(data);

		
		DB.set("User", "setLastAlive", cl.username, System.currentTimeMillis()+"");
		
		//deliver status back to original target
		if(msg.getAckType() == ACKType.RECEIVED){
			DB.set("Message", "removeMessage", username, cl.getDevice(), getMessageHash(msg, false));
			if(msg.hasMessage()){
				String target = msg.getMessage().getOrigin();
				String hash = getMessageHash(msg, true);
				
				save(target, hash, data);
				Client targetCl = AuthManager.getInstance().getClient(target);
				if(target != null){
					core.send(targetCl.client.uid, msg.toByteArray());
				}
			}
		}else if(msg.getAckType() == ACKType.COMPLETE){
			DB.set("Message", "removeMessage", username, cl.getDevice(), getMessageHash(msg, true));
		}

		return true;
	}
	
	private String getMessageHash(Ack msg, boolean complete){
		if(msg.hasHash()){
			return msg.getHash();
		}else{
			StringBuffer buffer = new StringBuffer();
			buffer.append(msg.getMessage().getId());
			buffer.append(".");
			buffer.append(msg.getMessage().getOrigin());
			buffer.append(".");
			buffer.append(msg.getMessage().getTarget());
			if(msg.getMessage().hasGroup()){
				buffer.append(".");
				buffer.append(msg.getMessage().getGroup());
			}
			
			try{
				String hash = SHA256.hash(buffer.toString());
				return hash;
			}catch(Exception ex){}
			return buffer.toString();
		}
	}

	private void save(String username, String hash, byte[] data) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		String value = ConfigCore.getInstance().get("message_max_memory_size");
		int length = Integer.parseInt(value);	
		if(data.length < length){
			DB.set("Message", "addMessage", username, hash, Base64.encodeToString(data, Base64.DEFAULT));
		}else{
			DB.set("Message", "addMessage", username, hash, "");
			StorageCore.getInstance().save("Message", username+ "." + hash,  data);
		}
	}
}
