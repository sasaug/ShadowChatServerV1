package com.sasaug.shadowchat.database;

public interface IDatabase {

	String getName();
	Object get(String function, String[] params);
	void set(String function, String[] params);
	
	void onInit(DatabaseCore core);
	
}
