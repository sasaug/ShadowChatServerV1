package com.sasaug.shadowchat.notification;

import java.util.ArrayList;

import com.sasaug.shadowchat.notification.modules._Dummy;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.Log;

public class NotificationCore {
	public static final String ID = "Notification";
	public static final String TAG = "NotificationCore";
	
	public static final String NEWMSG = "newmsg";
	public static final String FRIENDINVITE = "friendinvite";
	public static final String GROUPINVITE = "groupinvite";
	
	ArrayList<INotification> modules = new ArrayList<INotification>();
	
	private static NotificationCore instance;
	public static NotificationCore getInstance(){
		if(instance == null)
			instance = new NotificationCore();
		return instance;
	}

	public NotificationCore(){
		loadModules();
		instance = this;
	}
	
	public void notify(String name, String target, String message){
		for(INotification module: modules){
			if(module.getName().equals(name)){
				module.onNotify(target, message);
			}
		}
	}
	
	public void loadModules(){
		modules.clear();
		
		try{
			ArrayList<Class<?>> list = ClassEnumerator.getClassesForPackage(_Dummy.class.getPackage());
			for(int i = 0; i < list.size(); i++){
				if(!list.get(i).getSimpleName().equals(_Dummy.class.getSimpleName())){
					try {
						INotification module = (INotification)list.get(i).newInstance();
						modules.add(module);
						Log.write(TAG, "Loaded module '" + list.get(i).getSimpleName() + "'");
					} catch (InstantiationException e) {
						Log.error(TAG, "Failed to initialise module '" + list.get(i).getSimpleName() +"'.");
					} catch (IllegalAccessException e) {
						Log.error(TAG, "Failed to access module '" + list.get(i).getSimpleName() +"'.");
					}
				}
			}
			Log.write(TAG, "Finish loading modules. " + modules.size() + " loaded.");
		}catch(Exception ex){
			ex.printStackTrace();
			Log.error(TAG, "Error loading modules. Modules not loaded.");
		}		
	}
}
