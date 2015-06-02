package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.obj.SCMessage;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.Log;
import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.message.Message.*;

/*
*	
*
*	Error code:
*	0 = Success
*	100 = Wrong user/pass
*	101 = Banned
*	102 = invalid device/ not verified
*	104 = exceed max device allow online
*
*/
public class AuthModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".AuthModule";}

	public Type getReceiveType() {return Type.AUTH;}
	
	NetworkCore core;
	AuthManager auth;
	public void onInit(NetworkCore core) {
		this.core = core;
		this.auth = AuthManager.getInstance();
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		DatabaseCore DB = DatabaseCore.getInstance();
		Auth msg = Auth.parseFrom(data);
		AuthResponse.Builder builder = AuthResponse.newBuilder();
		builder.setType(Message.Type.AUTH_RESPONSE);
		
		int maxOnline = ConfigCore.getInstance().getInt("account_max_device_online");
		
		boolean correctPassword = DatabaseCore.getInstance().getBool("User", "verifyPassword", false, msg.getUsername(), msg.getPassword());
		if(correctPassword){	
			//check if permitted device
			if(!DatabaseCore.getInstance().getBool("User", "isValidDevice", false, msg.getUsername(), msg.getDevice())){
				builder.setErrorCode(102);		
			}else if(DatabaseCore.getInstance().getBool("User", "isUserBanned", false, msg.getUsername())){
				builder.setErrorCode(101);				
			}else if(auth.getCurrentActiveByUsername(msg.getUsername()) >= maxOnline){
				builder.setErrorCode(104);				
			}else{
				auth.getClient(client).setId(msg.getUsername());
				auth.getClient(client).setDevice(msg.getDevice());				
				DB.set("User", "setLastAlive", msg.getUsername(), System.currentTimeMillis()+"");
				DB.set("OS", "setOS", msg.getUsername(), msg.getDevice(), msg.getOs().getNumber()+"");
				DB.set("User", "setCloudId", msg.getUsername(), msg.getDevice(), msg.getCloudId());
				
				Log.write(getModuleId(), msg.getUsername()  + " authenticated.");
				builder.setErrorCode(0);
			}
		}else{
			builder.setErrorCode(100);
		}
		
		AuthResponse response = builder.build();
		core.send(client.uid, response.toByteArray());
		
		if(builder.getErrorCode() == 0)
			checkMessage(client);
		return true;
	}
	
	public void onClientDisconnected(TCPClient client){
		Client cl = auth.getClient(client);
		if(cl != null && cl.hasAuth())
			Log.write(getModuleId(), cl.username  + " dropped.");
		auth.removeClient(client);
		
	}
	
	@SuppressWarnings("unchecked")
	public void checkMessage(TCPClient client) throws Exception{
		DatabaseCore DB = DatabaseCore.getInstance();
		StorageCore store = StorageCore.getInstance();
		Client cl = AuthManager.getInstance().getClient(client);
		if(cl != null){
			ArrayList<SCMessage> messages = (ArrayList<SCMessage>)DB.get("Message", "getMessage", cl.username, cl.device);
			for(SCMessage message: messages){
				if(message.data != null){	//read from db
					core.send(client.uid, message.data);
				}else{	//read from storage
					byte[] data = store.load("Message", message.id);
					if(data != null)
						core.send(client.uid, data);
				}
			}
		}
	}
	
	
}
