package com.sasaug.shadowchat.config;

import java.util.ArrayList;
import java.util.Hashtable;

import com.sasaug.shadowchat.config.modules._Dummy;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.IO;
import com.sasaug.shadowchat.utils.Log;

public class ConfigCore {
	public final static String ID = "Config";
	private final String TAG = "ConfigCore";
	
	ArrayList<IConfig> modules = new ArrayList<IConfig>();
	Hashtable<String, String> map = new Hashtable<String, String>();
	
	private static ConfigCore instance;
	public static ConfigCore getInstance(){
		if(instance == null)
			instance = new ConfigCore();
		return instance;
	}
	
	public ConfigCore(){
		loadModules();
		instance = this;
	}
	
	public String get(String key){
		return map.get(key);
	}
	
	public boolean getBool(String key, boolean defaultValue){
		String str = map.get(key);
		if(str != null ){
			if(str.equals("1"))
				return true;
			else
				return false;
		}
		return defaultValue;
	}
	
	public int getInt(String key){
		String str = map.get(key);
		if(str != null){
			try{
				return Integer.parseInt(str);
			}catch(Exception e){}
		}
		return 0;
	}
	
	public int getInt(String key, int defaultValue){
		String str = map.get(key);
		if(str != null){
			try{
				return Integer.parseInt(str);
			}catch(Exception e){}
		}
		return defaultValue;
	}
	
	public void loadModules(){		
		modules.clear();
		map.clear();
		try{
			ArrayList<Class<?>> list = ClassEnumerator.getClassesForPackage(_Dummy.class.getPackage());
			
			for(int i = 0; i < list.size(); i++){
				if(!list.get(i).getSimpleName().equals(_Dummy.class.getSimpleName())){
					try {
						IConfig module = (IConfig)list.get(i).newInstance();
						modules.add(module);	
						module.onInit(this, map);
						
						ArrayList<String> lines = IO.readStringLine(module.getPath());
						String regex = module.getExclude();						
						for(int j = 0; j < lines.size(); j++){
							String str = lines.get(j);
							if(!str.startsWith("#") && !str.startsWith("//") && !str.trim().equals("")){
								if(regex == null || !str.matches(regex)){
									try{
										String var = str.substring(0, str.indexOf("=")).trim();
										String value = str.substring(str.indexOf("=")+1).trim();
										if(!var.trim().equals("") && !value.trim().equals(""))
											map.put(var.trim(), value.trim());
									}catch(Exception e){}
								}
							}
						}
						
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
