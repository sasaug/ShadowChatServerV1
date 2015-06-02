package com.sasaug.shadowchat.notification;

public interface INotification {
	public String getName();
	public void onNotify(String target, String message);
}
