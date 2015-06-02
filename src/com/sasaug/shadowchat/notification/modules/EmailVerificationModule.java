package com.sasaug.shadowchat.notification.modules;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.notification.ANotification;
import com.sasaug.shadowchat.notification.NotificationCore;

public class EmailVerificationModule extends ANotification implements IModule{
	
	public static final String LOGIN_USERNAME = "admin@siggie.net";
	public static final String LOGIN_PASSWORD = "weizien";
	
	public static final String FROM = "no-reply@siggie.net";
	
	private ExecutorService pool = null;
	
	public String getModuleId() {return NotificationCore.ID + ".EmailVerificationModule";}
	
	public String getName() {return "EmailVerification";}
	
	public void onNotify(final String target, String str) {
		final String msg = ConfigCore.getInstance().get("verification_email_message").replaceAll("%code%", str);
		
		
		//summon 2 threads to send email,prevent it from slowing down registration especially
		if(pool == null)
			pool = Executors.newFixedThreadPool(2);
		class Sender implements Runnable{
			public void run(){
				sendEmail(FROM, target, ConfigCore.getInstance().get("verification_email_title"), msg);
			}
		}
		pool.execute(new Sender());
	}
	
	private boolean sendEmail(String from, String to, String title, String msg){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", ConfigCore.getInstance().get("verification_email_server"));
		props.put("mail.smtp.port", ConfigCore.getInstance().get("verification_email_server_port"));
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(LOGIN_USERNAME, LOGIN_PASSWORD);
			}
		  });
 
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
			message.setSubject(title);
			message.setText(msg);
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
