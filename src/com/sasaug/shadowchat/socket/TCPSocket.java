package com.sasaug.shadowchat.socket;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

public class TCPSocket {
	
	ArrayList<TCPListenInterface> adapters = new ArrayList<TCPListenInterface>();
	Hashtable<Integer, TCPClient> clients = new Hashtable<Integer, TCPClient>();
	
	private TCPListenSocket socket = null;
	
	int currentId = 0;
	String ip;
	int port;
	int threads;
	
	boolean useSSL = false;
	File fileCert = null;
	File filePriKey = null;
	
	public TCPSocket(String ip, int port, int threads){
		this.ip = ip;
		this.port = port;
		this.threads = threads;
	}
	
	public void start(){
		class InternalListener extends TCPListenAdapter{
			public void onReceive(TCPClient cl, byte[] data){
				
			}
			
			public void onSent(TCPClient cl, byte[] data){
				
			}
		}
		attach(new InternalListener());
		try{
			socket = new TCPListenSocket(this, ip, port, threads, adapters, clients);
			if(useSSL){
				if(fileCert != null && filePriKey != null)
					socket.enableSSL(fileCert, filePriKey);
				else
					socket.enableSSL();
			}
			socket.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	public void enableSSL(String cert, String priKey){
		try{
			fileCert = new File(cert);
			filePriKey = new File(priKey);
			useSSL = true;
		}catch(Exception e){}
	}
	
	public void enableSSL(){
		useSSL = true;
	}
	
	public void attach(TCPListenInterface adapter){
		this.adapters.add(adapter);
	}
	
	public void detach(TCPListenInterface adapter)
	{
		this.adapters.remove(adapter);
	}
	
	public boolean send(int uid, byte[] data)
	{
		TCPClient client = clients.get(uid);
		if(client != null){
			client.addOutgoingProcess();
			socket.write(client.c, data);
			client.reduceOutgoingProcess();
			return true;
		}else{
			return false;
		}
	}
	
	public void broadcast(byte[] data)
	{
		for(int i =0; i < clients.size();i++){
			TCPClient client = clients.get(i);
			client.addOutgoingProcess();
			socket.write(client.c, data);
			client.reduceOutgoingProcess();
		}
	}

	public int generateId()
	{
		if(currentId == Integer.MAX_VALUE)
			currentId = 0;
		currentId += 1;
		
		return currentId;
	}
	
	public TCPClient getClient(int uid){
		return clients.get(uid);
	}

}
