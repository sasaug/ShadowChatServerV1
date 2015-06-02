package com.sasaug.shadowchat.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;


public class IO {
	
	public static byte[] read(String path) throws IOException{   	 
		File file = new File(path);
		 
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int readNum; (readNum = fis.read(buf)) != -1;) {
            bos.write(buf, 0, readNum);
        }
        bos.flush();
        byte[] bytes = bos.toByteArray();
        bos.close();
        fis.close();
        return bytes;
	}
	
	public static ArrayList<String> readStringLine(String path) throws IOException{ 
        ArrayList<String> list = new ArrayList<String>();
        try {
			File fileDir = new File(path);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));
			String str;
			while ((str = in.readLine()) != null) {
			    list.add(str);
			}
			in.close();
	    } 
	    catch (Exception e)
	    {}
        return list;
	}
	
	public static String readLine(String path, int pos) throws IOException{ 
        String s= "";
        try {
			File fileDir = new File(path);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));
			String str;
			int count = 0;
			while ((str = in.readLine()) != null) {
				if(count == pos){
					s = str;
					break;
				}
				count++;
			}
			in.close();
	    } 
	    catch (Exception e)
	    {}
        return s;
	}
	
	public static void write(String path, byte[] data) throws IOException
	{
		File someFile = new File(path);
        FileOutputStream fos = new FileOutputStream(someFile);
        fos.write(data);
        fos.flush();
        fos.close();
	}
	
	public static void writeStringLine(String path, ArrayList<String> list)
	{        
		try 
		{
			File fileDir = new File(path);
	 
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
	 
			for(int i = 0; i < list.size(); i++)
			{
				out.append(list.get(i)).append("\n");
			}
			out.flush();
			out.close();
	 
	   } 
	   catch (Exception e)
	   {} 
	}
	
	public static void appendStringLine(String path, String data)
	{        
		try 
		{ 
    		File file =new File(path);
    		if(!file.exists()){
    	        file.createNewFile();
    		}
    		FileWriter fileWritter = new FileWriter(file.getAbsolutePath(),true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        bufferWritter.write(data);
	        bufferWritter.newLine();
	        bufferWritter.close();
	   } 
	   catch (Exception e)
	   {e.printStackTrace();} 
	}

	public static void appendStringLine(String path, int position, String data)
	{        
		try 
		{ 
			File file = new File(path);
			if(!file.exists()){
    	        file.createNewFile();
    		}
			File tmpfile =new File(path + ".tmp");
			if(!tmpfile.exists()){
    	        tmpfile.createNewFile();
    		}
			FileWriter fileWritter = new FileWriter(tmpfile.getAbsolutePath(),true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String str;
			int count = 0;
			while ((str = in.readLine()) != null) {
				
				if(count == position){
					str += data;
				}
				bufferWritter.write(str);
		        bufferWritter.newLine();
		        count++;
			}
			in.close();
	        bufferWritter.close();
	        
	        file.delete();
	        tmpfile.renameTo(file);
	   } 
	   catch (Exception e)
	   {e.printStackTrace();} 
	}
	
	public static void overwriteStringLine(String path, int position, String data)
	{        
		try 
		{ 
			File file = new File(path);
			if(!file.exists()){
    	        file.createNewFile();
    		}
			File tmpfile =new File(path + ".tmp");
			if(!tmpfile.exists()){
    	        tmpfile.createNewFile();
    		}
			FileWriter fileWritter = new FileWriter(tmpfile.getAbsolutePath(),true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			String str;
			int count = 0;
			while ((str = in.readLine()) != null) {
				
				if(count == position){
					str = data;
				}
				bufferWritter.write(str);
		        bufferWritter.newLine();
		        count++;
			}
			in.close();
	        bufferWritter.close();
	        
	        file.delete();
	        tmpfile.renameTo(file);
	   } 
	   catch (Exception e)
	   {e.printStackTrace();} 
	}
	
	public static ArrayList<String> list(String path, String regex)
	{       
		ArrayList<String> list = new ArrayList<String>();
		try 
		{ 
			File file = new File(path);
			File[] files = file.listFiles();
			for(File f: files){
				if(f.getName().matches(regex)){
					list.add(f.getName());
				}
			}
	    } 
	    catch (Exception e)
	    {e.printStackTrace();} 
		return list;
	}

}
