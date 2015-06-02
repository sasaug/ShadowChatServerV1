package com.sasaug.shadowchat.obj;

import java.util.ArrayList;

public class SCUser {
	public static final int FRSTATUS_REJECTED = 0;
	public static final int FRSTATUS_REQUESTING = 1;
    public static final int FRSTATUS_REQUESTED = 2;
    public static final int FRSTATUS_ACCEPTED = 3;    
    public static final int FRSTATUS_PENDING = 4;    
    
	public final static String OWNER = "z";
	public final static String ADMIN = "y";
	public final static String MODERATOR = "x";
	public final static String MEMBER = "w";
	public final static String GUEST = "v";
	
	
	public final static String BLOCKED = "a";
	
	
	public String username;
	public String name;
	public String avatar;
	public ArrayList<String> flag = new ArrayList<String>();
	
	//group related
	public boolean isGroup = false;
	public ArrayList<SCUser> users = new ArrayList<SCUser>();
	
	public SCUser(String username, String name, String avatar, String[] flag){
		this.username = username;
		this.name = name;
		this.avatar = avatar;
		for(String f: flag){
			this.flag.add(f);
		}
	}
}
