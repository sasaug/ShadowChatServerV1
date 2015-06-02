package com.sasaug.shadowchat.security;

import com.sasaug.shadowchat.client.Client;

public interface ISecurity {
	
	String getName();
	Key getKey(Client client);
	void setKey(Client client, byte[] key);
	
	void onInit(SecurityCore core);
	byte[] onSent(boolean isInitial, Client client, byte[] data);
	byte[] onReceive(boolean isInitial, Client client, byte[] data);
	
}
