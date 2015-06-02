package com.sasaug.shadowchat.utils;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Log {
	
	public static final int ERROR = 0;
	public static final int NORMAL = 1;
	public static final int DEBUG = 2;
	
	static File file;		
	static int level = NORMAL;
	
	public static void init(int lvl){
		level = lvl;
		File file = new File("log.txt");	
		try {
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception ex){}
	}
	
	public static void write(String tag, String message){
		
		if(level <= NORMAL){
			System.out.println("[" + tag + "] " + message);
			
			FileWriter f;
			try {
				 f = new FileWriter(file.getAbsolutePath(), true);
				 DateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm:ss");
				 Date date = new Date();
				 
				 f.write("[" + tag + " (" +dateFormat.format(date)+" )]\n" + message + "\n");
				 f.flush();
				 f.close();
			}catch(Exception ex){}
		}
	}
	
	public static void debug(String tag, String message){
		
		if(level <= DEBUG){
			System.out.println("[" + tag + "] " + message);
			
			FileWriter f;
			try {
				 f = new FileWriter(file.getAbsolutePath(), true);
				 DateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm:ss");
				 Date date = new Date();
				 
				 f.write("[" + tag + " (" +dateFormat.format(date)+" )]\n" + message + "\n");
				 f.flush();
				 f.close();
			}catch(Exception ex){}
		}
	}
	
	public static void error(String tag, String message){
		if(level <= ERROR){
			System.out.println("[" + tag + "] " + message);
			
			FileWriter f;
			try {
				 f = new FileWriter(file.getAbsolutePath(), true);
				 DateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm:ss");
				 Date date = new Date();
				 System.out.println(message);
				 f.write("[" + tag + " (" +dateFormat.format(date)+" )]\n" + message + "\n");
				 f.flush();
				 f.close();
			}catch(Exception ex){}
		}
	}
	
	public static void error(String tag, Exception ex){
		if(level <= ERROR){
			System.out.println("[" + tag + "] " + convertStackTrace(ex.getStackTrace()));
			
			FileWriter f;
			try {
				 f = new FileWriter(file.getAbsolutePath(), true);
				 DateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm:ss");
				 Date date = new Date();
				 f.write("[" + tag + " (" +dateFormat.format(date)+" )]\n" + ex.getMessage() + "\n" + convertStackTrace(ex.getStackTrace()));
				 f.flush();
				 f.close();
			}catch(Exception e){}
		}
	}
	
    
    static String convertStackTrace(StackTraceElement[] stacks){
		String str = "";
		for (int i = 0; i < stacks.length; i++){
			str += stacks[i] + "\n";
		}
		return str;
	}
}
