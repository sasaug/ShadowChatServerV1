package com.sasaug.shadowchat.database;

import java.util.ArrayList;

import redis.clients.jedis.Jedis;

import com.sasaug.shadowchat.database.modules._Dummy;
import com.sasaug.shadowchat.utils.ClassEnumerator;
import com.sasaug.shadowchat.utils.Log;

@SuppressWarnings("rawtypes")
public class DatabaseCore {
	public final static String ID = "Database";
	public static String IP = "127.0.0.1";
	public static int PORT = 6379;
		
	public enum FuncType{
		GET,
		SET
	}
	
	class Function{
		public String header;
		public String name;
		public FuncType type;
		public Class objType;
		public int paramsCount;
		
		public Function(String header, String name, FuncType type, Class objType, int paramsCount){
			this.header = header;
			this.name = name;
			this.type = type;
			this.objType = objType;
			this.paramsCount = paramsCount;
		}
	}
	
	private final String TAG = "DatabaseCore";
	
	ArrayList<IDatabase> modules = new ArrayList<IDatabase>();
	ArrayList<Function> functions = new ArrayList<Function>();
	
	private static DatabaseCore instance;
	public static DatabaseCore getInstance(){
		if(instance == null)
			instance = new DatabaseCore();
		return instance;
	}
	
	public DatabaseCore(){
		loadModules();
		instance = this;
	}
	
	public Jedis getJedis(){
		return new Jedis(IP, PORT);
	}
	
	public void loadModules(){		
		modules.clear();
		try{
			ArrayList<Class<?>> list = ClassEnumerator.getClassesForPackage(_Dummy.class.getPackage());
			
			for(int i = 0; i < list.size(); i++){
				if(!list.get(i).getSimpleName().equals(_Dummy.class.getSimpleName())){
					try {
						IDatabase module = (IDatabase)list.get(i).newInstance();
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

	public void registerFunction(String header, String function, FuncType type, Class objType, int paramsCount){
		functions.add(new Function(header, function, type, objType, paramsCount));
	}

	public Object get(String name, String function, String... params) throws Exception{		
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					Function func = functions.get(j);
					if(func.type == FuncType.GET && 
							func.header.equals(name) &&
							func.name.equals(function)){
						if(func.paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}	
						try{
							Object obj = modules.get(i).get(function, params);
							if(obj != null)
								return func.objType.cast(obj);
						}catch(Exception ex){
							throw new Exception("Object casting failed.");
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public boolean getBool(String name, String function, boolean defaultValue, String... params)throws Exception{
		
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					Function func = functions.get(j);
					if(func.type == FuncType.GET && 
							func.header.equals(name) &&
							func.name.equals(function)){
						if(func.paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}	
						try{
							Object obj = modules.get(i).get(function, params);
							if(obj != null)
								return (Boolean)func.objType.cast(obj);
						}catch(Exception ex){
							ex.printStackTrace();
							return defaultValue;
						}
					}
				}
			}
		}
		
		return defaultValue;
	}
	
	public String getString(String name, String function, String defaultValue, String... params)throws Exception{
		
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					Function func = functions.get(j);
					if(func.type == FuncType.GET && 
							func.header.equals(name) &&
							func.name.equals(function)){
						if(func.paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}	
						try{
							Object obj = modules.get(i).get(function, params);
							if(obj != null)
								return (String)func.objType.cast(obj);
						}catch(Exception ex){
							ex.printStackTrace();
							return defaultValue;
						}
					}
				}
			}
		}
		
		return defaultValue;
	}
	
	public int getInt(String name, String function, int defaultValue, String... params)throws Exception{
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					Function func = functions.get(j);
					if(func.type == FuncType.GET && 
							func.header.equals(name) &&
							func.name.equals(function)){
						if(func.paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}	
						try{
							Object obj = modules.get(i).get(function, params);
							if(obj != null)
								return (Integer)func.objType.cast(obj);
						}catch(Exception ex){
							ex.printStackTrace();
							return defaultValue;
						}
					}
				}
			}
		}
		
		return defaultValue;
	}
	
	public long getLong(String name, String function, long defaultValue, String... params)throws Exception{
		
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					Function func = functions.get(j);
					if(func.type == FuncType.GET && 
							func.header.equals(name) &&
							func.name.equals(function)){
						if(func.paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}	
						try{
							Object obj = modules.get(i).get(function, params);
							if(obj != null)
								return (Long)func.objType.cast(obj);
						}catch(Exception ex){
							return defaultValue;
						}
					}
				}
			}
		}
		
		return defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> getStringArray(String name, String function, String... params)throws Exception{
		
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					Function func = functions.get(j);
					if(func.type == FuncType.GET && 
							func.header.equals(name) &&
							func.name.equals(function)){
						if(func.paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}	
						try{
							Object obj = modules.get(i).get(function, params);
							if(obj != null)
								return (ArrayList<String>)func.objType.cast(obj);
						}catch(Exception ex){
							return new ArrayList<String>();
						}
					}
				}
			}
		}
		
		return new ArrayList<String>();
	}
	
	
	public void set(String name, String function, String... params) throws Exception{
		for(int i = 0; i < modules.size(); i++){
			if(modules.get(i).getName().equals(name)){
				for(int j = 0; j < functions.size(); j++){
					if(functions.get(j).name.equals(function) && functions.get(j).header.equals(name) ){
						if(functions.get(j).paramsCount != params.length){
							throw new Exception("Params length does not match function definition.");
						}
						modules.get(i).set(function, params);
					}
				}
			}
		}
	}
}
