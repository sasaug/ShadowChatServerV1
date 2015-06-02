package com.sasaug.shadowchat.network.modules;

import java.util.ArrayList;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.network.ANetwork;
import com.sasaug.shadowchat.network.AuthManager;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.obj.SCMessage;
import com.sasaug.shadowchat.socket.TCPClient;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.message.Message.*;

public class KeepAliveModule extends ANetwork implements IModule{

	public String getModuleId() {return NetworkCore.ID + ".KeepAliveModule";}

	public Type getReceiveType() {return Type.KEEPALIVE;}
	
	NetworkCore core;
	
	public void onInit(NetworkCore core) {
		this.core = core;
	}

	public boolean onReceive(TCPClient client, byte[] data) throws Exception{	
		if(client.outgoingStatus == TCPClient.IDLE)				
			checkMessage(client);
		return true;
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
					core.send(client.uid, data);
				}
			}
		}
	}
}
