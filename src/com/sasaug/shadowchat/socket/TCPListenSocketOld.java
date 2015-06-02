package com.sasaug.shadowchat.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPListenSocketOld extends Thread{
	
	//Connection related
	Selector selector = null;
	ServerSocketChannel server = null;
	private String ip;
	private int port;
	
	ArrayList<TCPListenInterface> adapters;
	Hashtable<Integer, TCPClient> clients;
	Hashtable<String, Integer> index = new Hashtable<String, Integer>();
	
	TCPSocket parent;
	
	private final static int MSG_HEADER = 4;
	
    private final Map<SelectionKey, ByteBuffer> readBuffers = new HashMap<SelectionKey, ByteBuffer>(); 
    private final int defaultBufferSize = 8192;
    
    private ExecutorService pool;
	
	public TCPListenSocketOld(TCPSocket parent, String ip, int port, int threads, ArrayList<TCPListenInterface> adapters, Hashtable<Integer, TCPClient> clients)
	{
		this.ip = ip;
		this.port = port;
		this.adapters =  adapters;
		this.clients = clients;
		this.parent = parent;
		this.pool = Executors.newFixedThreadPool(threads);
	}
	
	public void run()
	{		
		try{
			selector = Selector.open(); 
			server= ServerSocketChannel.open(); 
			
			for(int i =0; i < adapters.size();i++)
				adapters.get(i).onSocketBinded();
			
			server.socket().bind(new InetSocketAddress(ip, port)); 
			server.configureBlocking(false); 
			server.register(selector, SelectionKey.OP_ACCEPT); 

			while(true)
			{
				selector.select(10);
				for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) { 
					SelectionKey key = i.next(); 
					try { 
    					i.remove(); 
    					if (key.isConnectable()) { 
    						((SocketChannel)key.channel()).finishConnect(); 
    					} 
    					if (key.isAcceptable()) { 
    						SocketChannel client = server.accept(); 
    						client.configureBlocking(false); 
    						client.socket().setTcpNoDelay(true); 
    						client.socket().setKeepAlive(true);
    						client.socket().setSoTimeout(60000);
    						SelectionKey k = client.register(selector, SelectionKey.OP_READ);
    						
    						int uid = parent.generateId();
    						TCPClient cl = new TCPClient(uid, client.socket().getInetAddress().getHostAddress(), client.socket().getPort(), k);
    						clients.put(uid, cl);
    						index.put(client.socket().getInetAddress().getHostAddress() + ":" + client.socket().getPort(), uid);
    						
    						for(int j = 0; j < adapters.size(); j++){
    							adapters.get(j).onClientConnected(cl);
    						}
    					} 
    					if (key.isReadable()) { 
    						for (ByteBuffer message: readIncomingMessage(key)) { 
    							messageReceived(message, key); 
    						} 
    					} 
    				} catch (IOException ioe) { 
    					resetKey(key);
    					disconnected(key); 
    				} 
				}
			}
		}
		catch(Exception ex)
		{
			for(int i =0; i < adapters.size();i++)
				adapters.get(i).onError(ex);
		}
		finally
		{
			try
			{
				selector.close();
				server.socket().close();
				server.close();
			}
			catch(Exception ex){
				for(int i = 0; i < adapters.size(); i++){
	    			adapters.get(i).onError(ex);
	    		}
			}
		}	
	}
		
	
	public synchronized boolean write(SelectionKey channelKey, byte[] buffer){
    	byte[] lengthBytes = intToBytes(buffer.length);
    	// copying into byte buffer is actually faster than writing to channel twice over many (>10000) runs
    	ByteBuffer writeBuffer = ByteBuffer.allocate(buffer.length+lengthBytes.length);
    	writeBuffer.put(lengthBytes);
    	writeBuffer.put(buffer);
    	writeBuffer.flip();
    	if (buffer!=null) {
    		int bytesWritten;
    		try {
    		     // only 1 thread can write to a channel at a time 
                SocketChannel channel = (SocketChannel)channelKey.channel(); 
                synchronized (channel) { 
                	while(writeBuffer.hasRemaining()) {
                		bytesWritten = channel.write(writeBuffer); 
                		if (bytesWritten==-1) {
        	    			resetKey(channelKey);
        					disconnected(channelKey); 
        					break;
        	        	}
                	}
                } 
	    		
    		} catch (Exception e) {
    			resetKey(channelKey);
				disconnected(channelKey); 
				return false;
    		}
    	}
    	return true;
    }
	
	protected void resetKey(SelectionKey key) { 
    	key.cancel(); 
    	readBuffers.remove(key); 
    }
	
	private List<ByteBuffer> readIncomingMessage(SelectionKey key) throws IOException { 
    	ByteBuffer readBuffer = readBuffers.get(key); 
    	if (readBuffer==null) {
    		readBuffer = ByteBuffer.allocate(defaultBufferSize); 
    		readBuffers.put(key, readBuffer); 
    	}
    	if (((ReadableByteChannel)key.channel()).read(readBuffer)==-1) {
    		throw new IOException("Read on closed key");
    	}
    	
    	readBuffer.flip(); 
    	List<ByteBuffer> result = new ArrayList<ByteBuffer>();
    	   	
    	ByteBuffer msg = readMessage(key, readBuffer);
    	while (msg!=null) {
    		result.add(msg);
    		msg = readMessage(key, readBuffer);
    	}
    	
     	return result;
    }
	 
	private ByteBuffer readMessage(SelectionKey key, ByteBuffer readBuffer) {
    	int bytesToRead; 
    	if (readBuffer.remaining()>  MSG_HEADER) { // must have at least enough bytes to read the size of the message    		
     		byte[] lengthBytes = new byte[MSG_HEADER];
    		readBuffer.get(lengthBytes);
			bytesToRead = bytesToInt(lengthBytes);
    		if ((readBuffer.limit()-readBuffer.position())<bytesToRead) { 
    			// Not enough data - prepare for writing again 
    			if (readBuffer.limit()==readBuffer.capacity()) {
    	    		// message may be longer than buffer => resize buffer to message size
    				int oldCapacity = readBuffer.capacity();
    				ByteBuffer tmp = ByteBuffer.allocate(bytesToRead+MSG_HEADER);
    				readBuffer.position(0);
    				tmp.put(readBuffer);
    				readBuffer = tmp;   				
    				readBuffer.position(oldCapacity); 
	    			readBuffer.limit(readBuffer.capacity()); 
    				readBuffers.put(key, readBuffer); 
    	    		return null;
    	    	} else {
    	    		// rest for writing
	    			readBuffer.position(readBuffer.limit()); 
	    			readBuffer.limit(readBuffer.capacity()); 
	    			return null; 
    	    	}
    		} 
    	} else { 
    		// Not enough data - prepare for writing again 
    		readBuffer.position(readBuffer.limit()); 
    		readBuffer.limit(readBuffer.capacity()); 
    		return null; 
    	} 
    	byte[] resultMessage = new byte[bytesToRead];
    	readBuffer.get(resultMessage, 0, bytesToRead); 
    	// remove read message from buffer
    	int remaining = readBuffer.remaining();
    	readBuffer.limit(readBuffer.capacity());
    	readBuffer.compact();
    	readBuffer.position(0);
    	readBuffer.limit(remaining);
    	return ByteBuffer.wrap(resultMessage);
    } 
	 
	static byte[] intToBytes(int i ) {
	    ByteBuffer bb = ByteBuffer.allocate(4); 
	    bb.putInt(i); 
	    return bb.array();
	}
	 
	static int bytesToInt(byte[] b){
		ByteBuffer bb = ByteBuffer.wrap(b);
		return bb.getInt();
		
	}

	protected void messageReceived(final ByteBuffer message, SelectionKey key){
		try{
	    	SocketChannel channel = (SocketChannel)key.channel();
	    	
	    	int uid = index.get(channel.socket().getInetAddress().getHostAddress() + ":" + channel.socket().getPort());
	    	final TCPClient cl = clients.get(uid);
	    	if(cl != null){
	    		class Task implements Runnable{
	    			public void run(){
	    				cl.addIncomingProcess();
	    				for(int i = 0; i < adapters.size(); i++){
	    					adapters.get(i).onReceive(cl, message.array());
	    		    	}
	    				cl.reduceIncomingProcess();
	    			}
	    		}
	    		pool.execute(new Task());   		
	    	}
		
    	}catch(Exception ex){
    		for(int i = 0; i < adapters.size(); i++){
    			adapters.get(i).onError(ex);
    		}
    	}
	}

    protected void disconnected(SelectionKey key){
    	try{
	    	SocketChannel channel = (SocketChannel)key.channel();
	    	
	    	int uid = index.get(channel.socket().getInetAddress().getHostAddress() + ":" + channel.socket().getPort());
	    	TCPClient cl = clients.get(uid);
			
	    	if(cl != null){
	    		index.remove(channel.socket().getInetAddress().getHostAddress() + ":" + channel.socket().getPort());
	    		clients.remove(uid);
	    		
		    	for(int j = 0; j < adapters.size(); j++)
					adapters.get(j).onClientDisconnected(cl);
	    	}
		
    	}catch(Exception ex){
    		for(int i = 0; i < adapters.size(); i++){
    			adapters.get(i).onError(ex);
    		}
    	}
    }
}
