package com.offsec.nethunter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class ShellExecuter {
   public ShellExecuter() 
   {
      
   }
   public String Executer(String command) 
   {
      StringBuffer output = new StringBuffer();
      Process p;
      try {
        p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = reader.readLine())!= null) {
          output.append(line + "\n");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      String response = output.toString();
      return response;
    }
   
   public String Executer(String command[]) 
   {
      StringBuffer output = new StringBuffer();
      Process p;
      try {
        p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = reader.readLine())!= null) {
          output.append(line + "\n");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      String response = output.toString();
      return response;
    }
   
   public void RunAsRoot(String[] command) 
   {
	   try {
		   Process process = Runtime.getRuntime().exec("su");
		   DataOutputStream os = new DataOutputStream(process.getOutputStream());
		   for (String tmpmd : command)
		   {
			   os.writeBytes(tmpmd +"\n" );
		   }
		   os.writeBytes("exit\n");
		   os.flush();
	   }
	   catch (IOException e) {
		   e.printStackTrace();
	   }
   }
   
   public String RunAsRootOutput (String command)
   {
	   try {
		   Process process;
		   process = Runtime.getRuntime().exec("su");
		   process = Runtime.getRuntime().exec(command);
		   
		   BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		   //BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		   String str = "";
		   String s = null;
		   while ((s = stdInput.readLine()) != null) {
			   str += s;
		   }
		   return str;
       } catch (Exception ex) {
           throw new RuntimeException(ex);
       }
   }
}