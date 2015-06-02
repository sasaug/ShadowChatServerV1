package com.sasaug.shadowchat.network;

import com.sasaug.shadowchat.message.Message.Type;
import com.sasaug.shadowchat.socket.TCPClient;

public class ANetwork implements INetwork {

	/*
	 * 	Get receiver type so onReceive will only be called when the type fits
	 * */
	@Override
	public Type getReceiveType() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 	When this get initialised then handle of the network core will be passed in
	 * */
	@Override
	public void onInit(NetworkCore core) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * 	Called when incoming packet's receive type fit as defined on getReceiveType
	 * 
	 * 	Return true to block futher processing, false to allow other receiver to receive this
	 * 	This allow multiple listener while allow higher priority listener to block others from the
	 *  rest from receiving it.
	 *  
	 *  Advise to return true most of the time.
	 * */
	@Override
	public boolean onReceive(TCPClient client, byte[] data) throws Exception{
		// TODO Auto-generated method stub
		return false;	
	}

	@Override
	public void onClientConnected(TCPClient client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientDisconnected(TCPClient client) {
		// TODO Auto-generated method stub
		
	}
}
