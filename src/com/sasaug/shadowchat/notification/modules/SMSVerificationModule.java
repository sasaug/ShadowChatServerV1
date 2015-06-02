package com.sasaug.shadowchat.notification.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.sasaug.shadowchat.config.ConfigCore;
import com.sasaug.shadowchat.modules.IModule;
import com.sasaug.shadowchat.notification.ANotification;
import com.sasaug.shadowchat.notification.NotificationCore;

public class SMSVerificationModule extends ANotification implements IModule{
	
	public String getModuleId() {return NotificationCore.ID + ".SMSVerificationModule";}
	
	public String getName() {return "SMSVerification";}
	
	public void onNotify(String target, String str) {
		sendSMS(target, str);
	}

	private static boolean sendSMS(String target, String code){
		try {
			String message = ConfigCore.getInstance().get("verification_sms_message").replaceAll("%code%", code);
			message = URLEncoder.encode(message, "UTF-8");
			String a = ConfigCore.getInstance().get("verification_sms_url").replaceAll("%target%", target).replaceAll("%msg%", message);            
            URL url = new URL(a);
            URLConnection conn = url.openConnection();

            BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                    System.out.println(inputLine);
            }
            br.close();

            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return false;
	}
}
