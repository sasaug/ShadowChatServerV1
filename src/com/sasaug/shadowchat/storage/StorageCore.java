package com.sasaug.shadowchat.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.sasaug.shadowchat.security.SecurityCore;
import com.sasaug.shadowchat.storage.modules.*;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.IO;
import com.sasaug.shadowchat.utils.Log;
import com.sasaug.shadowchat.utils.SHA256;

public class StorageCore {
	public static final String ID = "Storage";
	private final String TAG = "StorageCore";
	
	ArrayList<IStorage> modules = new ArrayList<IStorage>();
	
	private static StorageCore instance;
	public static StorageCore getInstance(){
		if(instance == null)
			instance = new StorageCore();
		return instance;
	}
	
	public StorageCore(){
		loadModules();
		instance = this;
	}

	public void loadModules(){		
		modules.clear();
		try{
			ArrayList<Class<?>> list = ClassEnumerator.getClassesForPackage(_Dummy.class.getPackage());
			
			for(int i = 0; i < list.size(); i++){
				if(!list.get(i).getSimpleName().equals(_Dummy.class.getSimpleName())){
					try {
						IStorage module = (IStorage)list.get(i).newInstance();
						modules.add(module);
						module.onInit(this);
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
	
	public void save(String type, String filename, byte[] data){
		for(int i = 0; i < modules.size(); i++){
			IStorage module = modules.get(i);
			if(module.getType().equals(type)){
				try{
					filename = module.getPath() + filename;
			    	Path path = Paths.get(filename);
			    	data = module.preSave(data);
			    	if(data != null)
			    		Files.write(path, data);
				}catch(Exception ex){}
			}
		}
	}
	
	public byte[] load(String type, String filename){
		for(int i = 0; i < modules.size(); i++){
			IStorage module = modules.get(i);
			if(module.getType().equals(type)){
				byte[] data = module.preLoad();
				if(data == null){
					try{
						filename = module.getPath() + filename;
				    	Path path = Paths.get(filename);
				    	data  = Files.readAllBytes(path);
				    	data = module.postLoad(data);
					}catch(Exception ex){}
				}
				return data;
			}
		}
		return null;
	}
	
	public ArrayList<String> list(String type, String regex){
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i < modules.size(); i++){
			IStorage module = modules.get(i);
			if(module.getType().equals(type)){
				String path = module.getPath();
				list.addAll(IO.list(path, regex));
			}
		}
		return list;
	}
	
	
	public void delete(String type, String filename){
		class Filter implements FilenameFilter {
			private String prefix;
	 
			public Filter(String ext) {
				this.prefix = ext;
			}
	 
			public boolean accept(File dir, String name) {
				return (name.startsWith(prefix));
			}
		}
		for(int i = 0; i < modules.size(); i++){
			IStorage module = modules.get(i);
			if(type == null || module.getType().equals(type)){
				try{
					File folder = new File(module.getPath());
					File[] files = folder.listFiles(new Filter(filename));
					for(int j = 0; j < files.length; j++){
						File file = files[j];
				    	file.delete();
					}
				}catch(Exception ex){}
			}
		}
	}
}
