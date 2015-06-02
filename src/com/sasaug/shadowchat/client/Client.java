package com.sasaug.shadowchat.client;

import com.sasaug.shadowchat.socket.TCPClient;

public class Client {
	public final static int DISCONNECTED = -1;
	public final static int CONNECTED = 0;
	public final static int AUTHENTICATED = 1;
	public final static int ONLINE = 2;
	
	public String username;
	public String device;
	public int status = DISCONNECTED;
	
	public TCPClient client;

	public Client(TCPClient client){
		this.client = client;
	}
	
	public void setId(String username){
		this.username = username;
	}
	
	public String getId(){
		return username;
	}
	
	public void setDevice(String device){
		this.device = device;
	}
	
	public String getDevice(){
		return device;
	}
	
	public boolean hasAuth(){
		return username != null;
	}
}
