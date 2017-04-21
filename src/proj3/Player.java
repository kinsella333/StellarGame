package proj3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Player extends Thread {
	volatile GUI g;
	volatile User u;
	public InetAddress address;
	volatile int kill = 0;

	public Player(GUI g, User u){
		this.g = g;
		this.u = u;
	}
	
	public void run(){
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		Socket s1 = null;
		String line, response = "";
		BufferedReader br = null;
		BufferedReader is = null;
		PrintWriter os = null;
		
		try{
		     s1=new Socket(address, 4445);
		     br= new BufferedReader(new InputStreamReader(System.in));
		     is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
		     os= new PrintWriter(s1.getOutputStream());
		     
		     line= this.u.AccountID;
	
		     os.println(line);
		     os.flush();
		     
		     this.g.setBankerID(is.readLine());
		     
		     
		     
		     while(!response.equals("Quit")){
		    	 response = is.readLine();
		    	 
		    	 if(response.equals("ACK")){
		    		 os.println(this.g.getNumber());
				     os.flush();
				     g.setOtherPlayerNumber(is.readLine());
				     g.setWinnerID(is.readLine());
		    	 }
		     }
		     
		     this.g.setEnded(true);
		     
		}catch (IOException e){
			 g.setclientStartError(true);
		     System.err.print("IO Exception");
		     return;
		}
	    try {
	    	is.close();
	    	os.close();
			br.close();
			s1.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println("Connection Closed");
		 
	}
}
