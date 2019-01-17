package process;

import java.io.PrintWriter;
import java.time.LocalDateTime;

public class LogFile 
{
	static PrintWriter fw;
	
	public static void createLog(String fName) throws Exception
	{
		String dt = (""+java.time.LocalDateTime.now()).replaceAll(":", ".");
		fw = new PrintWriter(fName+"-Tester-Log.txt");
	}
	
	public static void closeLog()
	{
		try{
		fw.flush();
		fw.close();
		}catch (Exception ee){}
	}
	
	public static void writeMsg(String msg)
	{
		LocalDateTime dt = java.time.LocalDateTime.now();	
		fw.println(dt+": "+msg);
		fw.flush();
	}
	
	public static void writeError(String msg, Exception e)
    {
		LocalDateTime dt = java.time.LocalDateTime.now();
    	try{
    		fw.println(dt+": "+msg);
    		fw.println(e.getMessage());
    		e.printStackTrace(fw);
    		fw.flush();
    		//Runtime.getRuntime().exec("tail -500 /var/log/tomcat7/catalina.out >> "+fname);
    	}catch (Exception ee){}
    }

}
