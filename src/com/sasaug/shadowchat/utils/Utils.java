package com.sasaug.shadowchat.utils;

public class Utils {

	public static String bytesToHexString(byte[] data)
	{
		StringBuffer retString = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            retString.append(Integer.toHexString(0x0100 + (data[i] & 0x00FF)).substring(1));
        }
        return retString.toString();
	}
	
	public static byte[] hexStringToBytes(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

}
