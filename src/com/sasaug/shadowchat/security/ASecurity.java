package com.sasaug.shadowchat.security;

import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.socket.TCPClient;

public class ASecurity implements ISecurity{

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onInit(SecurityCore core) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] onSent(boolean isInitial, Client client, byte[] data) {
		// TODO Auto-generated method stub
		return data;
	}

	@Override
	public byte[] onReceive(boolean isInitial, Client client, byte[] data) {
		// TODO Auto-generated method stub
		return data;
	}

	@Override
	public Key getKey(Client client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKey(Client client, byte[] key) {
		// TODO Auto-generated method stub
		
	}

}
