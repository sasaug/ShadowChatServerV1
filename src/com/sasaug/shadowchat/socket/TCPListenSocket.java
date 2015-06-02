package com.sasaug.shadowchat.socket;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.channel.Channel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPListenSocket extends Thread { 	
	private String ip;
	private int port;
	TCPSocket parent;

	ArrayList<TCPListenInterface> adapters;
	Hashtable<Integer, TCPClient> clients;
	Hashtable<String, Integer> index = new Hashtable<String, Integer>();
	
	private ExecutorService pool;
	
	
	SslContext sslCtx = null;

	public TCPListenSocket(TCPSocket parent, String ip, int port, int threads,
			ArrayList<TCPListenInterface> adapters,
			Hashtable<Integer, TCPClient> clients) throws Exception {
		this.ip = ip;
		this.port = port;
		this.adapters = adapters;
		this.clients = clients;
		this.parent = parent;
		this.pool = Executors.newFixedThreadPool(threads);
	}
	
	public void enableSSL(){
		try{
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void enableSSL(File certificate, File privateKey){
		try{
			sslCtx = SslContext.newServerContext(certificate, privateKey);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new SecureChatServerInitializer(sslCtx));
			
			for(int i =0; i < adapters.size();i++)
				adapters.get(i).onSocketBinded();
			b.bind(InetAddress.getByName(ip), port).sync().channel()
					.closeFuture().sync();
		}catch(Exception ex){
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public boolean write(Channel c, byte[] buffer){
    	if(c != null && c.isOpen() && c.isActive()){
    		c.writeAndFlush(buffer);
    		return true;
    	}
    	return false;
    }
	
	public class SecureChatServerHandler extends SimpleChannelInboundHandler<byte[]> {

		final ChannelGroup channels = new DefaultChannelGroup(
				GlobalEventExecutor.INSTANCE);

		public void channelActive(final ChannelHandlerContext ctx) {
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			InetAddress inetAddress = socketAddress.getAddress();
			int uid = parent.generateId();
			TCPClient cl = new TCPClient(uid, inetAddress.getHostAddress(), socketAddress.getPort(), ctx.channel());
			clients.put(uid, cl);
			index.put(inetAddress.getHostAddress() + ":" + socketAddress.getPort(), uid);
			for(int j = 0; j < adapters.size(); j++){
				adapters.get(j).onClientConnected(cl);
			}
		}
		
		public void channelInactive(ChannelHandlerContext ctx) throws Exception{
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			InetAddress inetAddress = socketAddress.getAddress();
			String ip = inetAddress.getHostAddress();
			int port = socketAddress.getPort();
			ctx.close();
			try{
				int uid = index.get(ip + ":" + port);
		    	TCPClient cl = clients.get(uid);
		    	if(cl != null){
		    		index.remove(ip + ":" + port);
		    		clients.remove(uid);
			    	for(int j = 0; j < adapters.size(); j++)
						adapters.get(j).onClientDisconnected(cl);
		    	}
			}catch(Exception e){}
		}

		@Override
		public void channelRead0(ChannelHandlerContext ctx, final byte[] bytes) throws Exception {
			try{
				InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
				InetAddress inetAddress = socketAddress.getAddress();
				String ip = inetAddress.getHostAddress();
				int port = socketAddress.getPort();
		    	int uid = index.get(ip + ":" + port);
		    	final TCPClient cl = clients.get(uid);
		    	if(cl != null){
		    		class Task implements Runnable{
		    			public void run(){    						    				
		    				cl.addIncomingProcess();
		    				for(int i = 0; i < adapters.size(); i++){
		    					adapters.get(i).onReceive(cl, bytes);
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

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			InetAddress inetAddress = socketAddress.getAddress();
			String ip = inetAddress.getHostAddress();
			int port = socketAddress.getPort();
			ctx.close();
			
			int uid = index.get(ip + ":" + port);
	    	TCPClient cl = clients.get(uid);
			
	    	if(cl != null){
	    		index.remove(ip + ":" + port);
	    		clients.remove(uid);
		    	for(int j = 0; j < adapters.size(); j++)
					adapters.get(j).onClientDisconnected(cl);
	    	}
		}

	}
    
	public class SecureChatServerInitializer extends ChannelInitializer<SocketChannel> {
		private final SslContext sslCtx;

		public SecureChatServerInitializer(SslContext sslCtx) {
			this.sslCtx = sslCtx;
		}

		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
			if(sslCtx != null)
				pipeline.addLast(sslCtx.newHandler(ch.alloc()));
			pipeline.addLast("frameDecoder",
		            new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
		    pipeline.addLast("bytesDecoder",
		            new ByteArrayDecoder());

		    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
		    pipeline.addLast("bytesEncoder", new ByteArrayEncoder());
			pipeline.addLast(new SecureChatServerHandler());
		}
	}

}
