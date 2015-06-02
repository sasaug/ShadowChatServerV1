package com.sasaug.shadowchat.socket;

import io.netty.channel.Channel;

import java.nio.channels.SelectionKey;
import java.util.Hashtable;

public class TCPClient {
	public final static int IDLE = 0;
	public final static int BUSY = 1;
	
	public int uid;
	public String ip;
	public int port;
	public SelectionKey key;
	Channel c;
	
	public int incomingStatus = IDLE;
	public int outgoingStatus = IDLE;
	
	private int incomingProcess = 0;
	private int outgoingProcess = 0;

	public TCPClient(int uid, String ip, int port, SelectionKey key){
		this.uid = uid;
		this.ip = ip;
		this.port = port;
		this.key = key;
	}
	
	public TCPClient(int uid, String ip, int port, Channel c){
		this.uid = uid;
		this.ip = ip;
		this.port = port;
		this.c = c;
	}
	
	private Hashtable<String, Object> tags = new Hashtable<String, Object>();
	
	public Object getTag(String tag){
		return tags.get(tag);
	}
	
	public void setTag(String tag, Object obj){
		tags.put(tag, obj);
	}

	public synchronized int addIncomingProcess() {
		incomingProcess++;
		checkStatus();
		return incomingProcess;
	}
	
	public synchronized  int addOutgoingProcess() {
		incomingProcess++;
		checkStatus();
		return incomingProcess;
	}
	
	public synchronized int reduceIncomingProcess() {
		incomingProcess--;
		checkStatus();
		return incomingProcess;
	}
	
	public synchronized int reduceOutgoingProcess() {
		incomingProcess++;
		checkStatus();
		return incomingProcess;
	}
	
	private void checkStatus(){
		if(incomingProcess != 0)
			incomingStatus = BUSY;
		else
			incomingStatus = IDLE;
		
		if(outgoingProcess != 0)
			outgoingStatus = BUSY;
		else
			outgoingStatus = IDLE;
	}	
}
