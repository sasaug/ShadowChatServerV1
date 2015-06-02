package com.sasaug.shadowchat.socket;

public interface TCPListenInterface {
	public void onClientConnected(TCPClient client);
	public void onClientDisconnected(TCPClient client);
	public void onClientError(TCPClient client, Exception ex);
	public void onReceive(TCPClient client,byte[] data);
	public void onSent(TCPClient client,byte[] data);
	public void onSocketBinded();
	public void onError(Exception error);
}
