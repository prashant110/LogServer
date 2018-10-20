/**
 * 
 */
package logserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;

/**
 * @author user
 *
 */
public class LogServer {

	
    public  static int SOCKET_PORT = 13267;
    static String  filePath;

	
    
	
    public static void main(String[] args) throws IOException 
    {
        ServerSocket servsock = null;
        Socket sock = null;
        
        SOCKET_PORT = Integer.parseInt(args[0]);
        // path of shared resource
         filePath = args[1];
         System.out.println(filePath);
        try 
        {
            servsock = new ServerSocket(SOCKET_PORT);
            System.out.println("File server is running on "+SOCKET_PORT);
            while (true) 
            {
               // System.out.println("Waiting...");
               
                    sock = servsock.accept();
                  //  System.out.println(sock);
                   (new HandleClient(sock,filePath)).start();
            }
        }
        finally 
        {
            if (servsock != null) try {
                servsock.close();
            } catch (IOException ex) {
                
            }
        }
    }
}
class HandleClient extends Thread
{
	Socket socket;
	long  cs = 0;
	long message = 0;
	String filePath;
	
	public HandleClient(Socket socket, String filePath) {
		this.socket = socket;
		this.filePath = filePath;
		
	}
	
	public  synchronized void updateFile(String data)
	{
		try
		{
			//System.out.println("Update file "+data);
			JSONObject obj = Converter.stringToJson(data);
	        String tag = obj.get("tag").toString();
	        String msg = obj.get("msg").toString();
	        switch(tag)
	        {
	            case "token_send":
	                message++;
	                break;
	            case "cs_request_forward":
	                  message++;
	                  break;
	            case "execution_completed":
	                  cs++;
	                 // stream.write("CS Execution "+cs + " :: "+data.msg + " :: "+message+"\n");
	                  writeInFile(cs,message,msg);
	                  break;
	        }
		}
		catch(Exception ex)
		{
			System.out.println("Exception in updatefile"+ex.getMessage());
			ex.printStackTrace();
		}
	}
	public void writeInFile(long cs, long message, String msg)
	{
		try
		{
			//System.out.println("Update file "+data);
			
	        FileWriter fw = new FileWriter(filePath, true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        PrintWriter out = new PrintWriter(bw);
	        out.println("CS Execution "+cs + " :: "+msg + " :: "+message);
            out.flush();
	      
		}
		catch(Exception ex)
		{
			System.out.println("Exception in writefile"+ex.getMessage());
			ex.printStackTrace();
		}
	}
	public void run()
	{
		try
		{
			// DataInputStream dis = new DataInputStream(socket.getInputStream());
			 BufferedReader dIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 while(true)
			 {
				 //byte[] buffer = new byte[4096];
	            // int read = dis.read(buffer, 0,4096);
	            // String data = new String(buffer,0,read);
				 String data = dIn.readLine();
	            // System.out.println("data " + data);
	             updateFile(data);
			 }
            
         }
		catch(Exception ex)
		{
			System.out.println("Exception in run "+ex.getMessage());
			ex.printStackTrace();
		}
	}
}