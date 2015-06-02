package com.sasaug.shadowchat.config;

import java.util.Hashtable;

public interface IConfig {
	String getPath();
	String getExclude();
	
	void onInit(ConfigCore core, Hashtable<String, String> map);
}
