package com.sasaug.shadowchat.socket;

import java.net.InetAddress;


public interface UDPSocketInterface {
	public void onReceive(byte[] data, InetAddress ip, int port);
	public void onSend(byte[] data, InetAddress ip, int port);
	public void onSocketBinded();
	public void onError(String error);
}
