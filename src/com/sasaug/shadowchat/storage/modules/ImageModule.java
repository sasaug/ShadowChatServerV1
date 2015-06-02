package com.sasaug.shadowchat.storage.modules;

import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.storage.AStorage;
import com.sasaug.shadowchat.storage.StorageCore;

public class ImageModule extends AStorage implements IModule{
	public String getModuleId() {
		return StorageCore.ID + "Image";
	}

	public String getType(){
		return "Image";
	}
	
	public String getPath(byte[] data){
		return "/var/www/image/";
	}
	
	public String getPath(String value){
		return "/var/www/image/";
	}
	
	public void onInit(StorageCore core){}
	
}
