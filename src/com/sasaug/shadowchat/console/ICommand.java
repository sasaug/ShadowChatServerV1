package com.sasaug.shadowchat.console;


public interface ICommand {
	String getCommand();
	
	void onInit(ConsoleCore core);	
	void onTrigger(String str);
}
