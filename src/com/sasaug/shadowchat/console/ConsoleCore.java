package com.sasaug.shadowchat.console;

import java.util.ArrayList;

import com.sasaug.shadowchat.console.modules._Dummy;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.Log;

public class ConsoleCore {
	public static final String ID = "Console";
	private final String TAG = "ConsoleCore";
	
	ArrayList<ICommand> modules = new ArrayList<ICommand>();
	
	private static ConsoleCore instance;
	public static ConsoleCore getInstance(){
		if(instance == null)
			instance = new ConsoleCore();
		return instance;
	}
	
	public ConsoleCore(){
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
						ICommand module = (ICommand)list.get(i).newInstance();
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
	
	public void process(String command){
		String str = command.trim();
		String param = null;
		if(str.contains(" ")){
			param = str.substring(str.indexOf(" ") + 1);
			str = str.substring(0, str.indexOf(" ") - 1);
		}
		for(int i = 0; i < modules.size(); i++){
			ICommand cmd = modules.get(i);
			if(cmd.getCommand().equals(str)){
				if(param == null)
					cmd.onTrigger(str);
				else
					cmd.onTrigger(param);
			}
		}
		
	}
}
