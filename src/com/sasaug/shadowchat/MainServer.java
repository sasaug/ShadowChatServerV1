package com.sasaug.shadowchat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sasaug.shadowchat.console.ConsoleCore;
import com.sasaug.shadowchat.modules.CoreManager;
import com.sasaug.shadowchat.utils.Log;

public class MainServer {

	public final static int STATE_RUN = 1;
	public final static int STATE_END = -1;

	public static int state = STATE_RUN;
	
	public static void main(String[] args) {
		Log.init(Log.NORMAL);
		
		CoreManager.init();
		
		while(state != STATE_END){
			try{
				BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
				String cmd = bufferRead.readLine();
				ConsoleCore.getInstance().process(cmd);
			}catch(Exception ex){}
		}
	}
}
