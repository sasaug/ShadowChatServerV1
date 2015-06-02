package com.sasaug.shadowchat.network;

import java.util.ArrayList;
import com.sasaug.shadowchat.client.Client;
import com.sasaug.shadowchat.socket.TCPClient;

public class AuthManager {
	
	ArrayList<Client> clients = new ArrayList<Client>();
	
	private static AuthManager instance;
	public static AuthManager getInstance(){
		if(instance == null)
			instance = new AuthManager();
		return instance;
	}
	
	public Client addClient(TCPClient client){
		Client cl = new Client(client);	
		clients.add(cl);
		return cl;
	}
	
	public void removeClient(TCPClient client){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).client.uid == client.uid){
				clients.remove(i);
				break;
			}
		}
	}
	
	public Client getClient(TCPClient client){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).client.uid == client.uid){
				return clients.get(i);
			}
		}
		return null;
	}
	
	public Client getClient(String username){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).hasAuth() && clients.get(i).username.equals(username)){
				return clients.get(i);
			}
		}
		return null;
	}
	
	public Client getClient(int uid){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).client.uid == uid){
				return clients.get(i);
			}
		}
		return null;
	}
	
	public String getUsername(TCPClient client){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).hasAuth() && clients.get(i).client.uid == client.uid){
				return clients.get(i).username;
			}
		}
		return null;
	}
	
	public int getCurrentActiveByUsername(String username){
		int count = 0;
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).hasAuth() && clients.get(i).getId().equals(username))
				count++;
		}
		return count;
	}

}
