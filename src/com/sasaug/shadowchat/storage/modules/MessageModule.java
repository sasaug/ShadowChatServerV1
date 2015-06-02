package com.sasaug.shadowchat.storage.modules;

import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.storage.AStorage;
import com.sasaug.shadowchat.storage.StorageCore;

public class MessageModule extends AStorage implements IModule{
	public String getModuleId() {
		return StorageCore.ID + "Message";
	}

	public String getType(){
		return "Message";
	}
	
	public String getPath(byte[] data){
		return "tmp";
	}
	
	public String getPath(String value){
		return "tmp";
	}
	
	public void onInit(StorageCore core){}	
}
