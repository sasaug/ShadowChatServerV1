package com.sasaug.shadowchat.notification.modules;

import java.io.IOException;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.sasaug.shadowchat.message.Message.OS;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.notification.ANotification;
import com.sasaug.shadowchat.notification.NotificationCore;

public class AndroidModule extends ANotification implements IModule{

	static final String API_KEY = "AIzaSyBdbrVSmmJwZzeuhj0uWs3IUezc3yzzd4I";
	static final String MESSAGE = "msg";
	static final int NUMOFRETRIES = 5;

	
	public String getModuleId() {return NotificationCore.ID + ".AndroidModule";}
	
	public String getName() {return OS.ANDROID_VALUE+"";}
	
	public void onNotify(String id, String msg) {
		send(id, msg);
	}
	
	public void send(String id, String msg){
		try {
			Sender sender = new Sender(API_KEY);
			Message message = new Message.Builder()
			    .addData(MESSAGE, msg)
			    .build();
			sender.send(message, id, NUMOFRETRIES);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
