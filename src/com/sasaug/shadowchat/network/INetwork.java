package com.sasaug.shadowchat.network;

import com.sasaug.shadowchat.message.Message;
import com.sasaug.shadowchat.socket.TCPClient;

public interface INetwork {
	Message.Type getReceiveType();	
	
	void onInit(NetworkCore core);
	boolean onReceive(TCPClient client, byte[] data) throws Exception;
	
	void onClientConnected(TCPClient client);
	void onClientDisconnected(TCPClient client);
}
