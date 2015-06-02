package com.sasaug.shadowchat.obj;

public class SCMessage {
	public String id;
	public long lastAttempt;
	public int attempts;
	
	public byte[] data = null;
	
	public SCMessage(String id, int attempts, long lastAttempt){
		this.id = id;
		this.lastAttempt = lastAttempt;
		this.attempts = attempts;
	}
}
