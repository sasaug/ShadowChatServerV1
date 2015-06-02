package com.sasaug.shadowchat.storage;

public interface IStorage {
	String getType();
	String getPath();
	
	void onInit(StorageCore core);	

	byte[] preSave(byte[] data);
	byte[] preLoad();
	byte[] postLoad(byte[] data);
}
