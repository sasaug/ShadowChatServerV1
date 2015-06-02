package com.sasaug.shadowchat.modules;

import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.console.ConsoleCore;
import com.sasaug.shadowchat.database.DatabaseCore;
import com.sasaug.shadowchat.network.NetworkCore;
import com.sasaug.shadowchat.notification.NotificationCore;
import com.sasaug.shadowchat.security.SecurityCore;
import com.sasaug.shadowchat.storage.StorageCore;
import com.sasaug.shadowchat.utils.Log;

public class CoreManager {
	
	public static final String TAG = "CoreManager";
	
	public static void init(){
		Log.write(TAG, "Initializing all cores...");
		ConfigCore cfg = new ConfigCore();
		Log.write(TAG, "Initialized " + ConsoleCore.ID);
		ConsoleCore console = new ConsoleCore();
		Log.write(TAG, "Initialized " + ConfigCore.ID);
		DatabaseCore db = new DatabaseCore();
		Log.write(TAG, "Initialized " + DatabaseCore.ID);
		StorageCore storage = new StorageCore();
		Log.write(TAG, "Initialized " + StorageCore.ID);
		SecurityCore security = new SecurityCore();
		Log.write(TAG, "Initialized " + SecurityCore.ID);
		NotificationCore notification = new NotificationCore();
		Log.write(TAG, "Initialized " + NotificationCore.ID);
		NetworkCore network = new NetworkCore("0.0.0.0", 8080, 20);
		Log.write(TAG, "Initialized " + NetworkCore.ID);
		
		
		Log.write(TAG, "Cores initialized.");
	}
}
