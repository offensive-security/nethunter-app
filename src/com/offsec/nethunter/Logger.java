package com.offsec.nethunter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Logger {
    String lastErrorMessage = "";
	public Logger() {

    }
	public void appendLog(String text)
	{       
		lastErrorMessage = text;
		File logFile = new File("sdcard/mylog.txt");
	   if (!logFile.exists())
	   {
		   try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	
	public String getLastErrorMessage()
	{
		return lastErrorMessage;
	}
}