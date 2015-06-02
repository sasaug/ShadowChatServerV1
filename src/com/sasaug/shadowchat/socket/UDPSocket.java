package com.sasaug.shadowchat.socket;

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class UDPSocket extends Thread{
	
	final static int UDP_HEADER_SIZE = 8;

	private DatagramSocket serverSocket = null;
	private int port;
	private int packetSize;
	private int poolingThread;

	ArrayList<UDPSocketInterface> adapters = new ArrayList<UDPSocketInterface>();	
	
	ExecutorService pool;
	
	public UDPSocket(int port, int packetSize, int poolingThread)
	{
		this.port = port;
		this.poolingThread = poolingThread;
		if(poolingThread != 0 )
			pool = Executors.newFixedThreadPool(poolingThread);
		else
			pool = null;
		this.packetSize = packetSize;
	}
	
	public void attach(UDPSocketInterface adapter)
	{
		this.adapters.add(adapter);
	}
	
	public void detach(UDPSocketInterface adapter)
	{
		this.adapters.remove(adapter);
	}
	
	public void send(String ip, int port, byte[] data) throws Exception
	{
		InetAddress IPAddress = InetAddress.getByName(ip);
	    DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
	    serverSocket.send(sendPacket);
	    for(int i=0; i < adapters.size();i++)
			adapters.get(i).onSend(data, IPAddress, port);
	}
	
	public void run()
	{
		try{
			serverSocket = new DatagramSocket(port);
			for(int i=0; i < adapters.size();i++)
					adapters.get(i).onSocketBinded();

			//When we receive any packets, summon extended thread to do the dirty job for us, 
			//so we can continue listening to cool stuff :D
			class Handler implements Runnable {
			    private final DatagramPacket receivePacket;
			    private final byte[] receiveData;
			    Handler(DatagramPacket packet, byte[] data) { this.receivePacket = packet; this.receiveData = data; }
			    public void run() {
			      // read and service request
			    	byte data[] = new byte[receivePacket.getLength()];

					for(int i =0 ; i < receivePacket.getLength();i++)
					{
						data[i] = receiveData[i];
					}
					InetAddress IPAddress = receivePacket.getAddress();
	                int port = receivePacket.getPort();
					for(int i =0; i < adapters.size();i++)
					{
						adapters.get(i).onReceive(data, IPAddress, port);
					}
			    }
			 }
			
			while(true)
			{	
				byte[] receiveData = new byte[packetSize];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				if(poolingThread != 0)
					pool.execute(new Handler(receivePacket, receiveData));
				else
					new Handler(receivePacket, receiveData).run();
			}

		}
		catch(Exception exception)
		{
			for(int i=0; i < adapters.size();i++)
				adapters.get(i).onError(exception.getMessage());
		}
		finally
		{
			serverSocket.close();
		}		
	}
}
