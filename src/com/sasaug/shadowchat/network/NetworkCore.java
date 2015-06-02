package com.sasaug.shadowchat.network;

import java.util.ArrayList;

import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.message.Message.*;
import com.sasaug.shadowchat.network.modules.*;
import com.sasaug.shadowchat.socket.*;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.Log;

public class NetworkCore {	
	public final static String ID = "Network";
	final static String TAG = "NetworkCore";
	
	private TCPSocket socket;

	ArrayList<INetwork> modules = new ArrayList<INetwork>();

	private static NetworkCore instance;
	public static NetworkCore getInstance(){
		if(instance == null)
			instance = new NetworkCore("0.0.0.0", 8080, 10);
		return instance;
	}

	public NetworkCore(String ip, int port, int threads){	
		socket = new TCPSocket(ip, port, threads);
		socket.attach(new Listener());
		if(ConfigCore.getInstance().getBool("ssl", false)){
			if(!ConfigCore.getInstance().get("sslCert").equals("") && !ConfigCore.getInstance().get("sslKey").equals("")){
				socket.enableSSL(ConfigCore.getInstance().get("sslCert"), ConfigCore.getInstance().get("sslKey"));
			}else{
				socket.enableSSL();
			}
		}
		socket.start();
		loadModules();
		instance = this;
	}

	public void loadModules(){
		modules.clear();
		
		try{
			ArrayList<Class<?>> list = ClassEnumerator.getClassesForPackage(_Dummy.class.getPackage());
			
			for(int i = 0; i < list.size(); i++){
				if(!list.get(i).getSimpleName().equals(_Dummy.class.getSimpleName())){
					try {
						INetwork module = (INetwork)list.get(i).newInstance();
						modules.add(module);
						module.onInit(this);
						Log.write(TAG, "Loaded module '" + list.get(i).getSimpleName() + "'");
					} catch (InstantiationException e) {
						Log.error(TAG, "Failed to initialise module '" + list.get(i).getSimpleName() +"'.");
					} catch (IllegalAccessException e) {
						Log.error(TAG, "Failed to access module '" + list.get(i).getSimpleName() +"'.");
					}
				}
			}
			Log.write(TAG, "Finish loading modules.");
		}catch(Exception ex){
			ex.printStackTrace();
			Log.error(TAG, "Error loading modules. Modules not loaded.");
		}		
	}

	public boolean send(int uid, byte[] data){
		try{
			Log.debug(TAG, "Sending " + data.length + " bytes.");
			return socket.send(uid, data);
		}catch(Exception ex){ex.printStackTrace();}
		return false;
	}

	class Listener extends TCPListenAdapter{
		public void onSocketBinded(){
			Log.write(TAG, "Listen socket initialised!");
		}
		
		public void onClientConnected(TCPClient client){
			Log.write(TAG, client.ip + ":" + client.port + "  connected.");
			for(int i = 0; i < modules.size(); i++)
				modules.get(i).onClientConnected(client);
		}
		
		public void onClientDisconnected(TCPClient client)
		{
			Log.write(TAG, client.ip + ":" + client.port + "  disconnected.");		
			for(int i = 0; i < modules.size(); i++)
				modules.get(i).onClientDisconnected(client);
		}
		
		public void onClientError(TCPClient client, Exception ex){
			ex.printStackTrace();
		}
		
		public void onError(Exception ex){
			ex.printStackTrace();
		}
	
		public void onReceive(TCPClient client, byte[] data){
			Log.debug(TAG, "Receiving " + data.length + " bytes.");
			try{
				Base base = Base.parseFrom(data);
				for(int i = 0; i < modules.size(); i++){
					if(modules.get(i).getReceiveType() == base.getType()){
						if(modules.get(i).onReceive(client, data))
							break;
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}		
	}
}
