package proj3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Player acts as client thread to the player view in GUI, and 
 * exchanges information with the banker thread.
 * 
 * @author ray
 *
 */
public class Player extends Thread {
	volatile GUI g;
	volatile User u;
	public InetAddress address;
	
	//Constructor to pass GUI and user objects down to thread
	public Player(GUI g, User u){
		this.g = g;
		this.u = u;
	}
	
	/**
	 * Thread contents, will terminate on completion
	 */
	public void run(){
		//Get local host and init socket and read/write
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
		
		//Try to communicate
		try{
		     s1=new Socket(address, 4445);
		     br= new BufferedReader(new InputStreamReader(System.in));
		     is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
		     os= new PrintWriter(s1.getOutputStream());
		     
		     //Send account id to server
		     line= this.u.AccountID;
		     os.println(line);
		     os.flush();
		     
		     //Receive banker id from server
		     this.g.setBankerID(is.readLine());
		     
		     //Loop on receiving until server sends quit command
		     while(!response.equals("Quit")){
		    	 response = is.readLine();
		    	 
		    	 //If receive ack then send real number and wait to receive other player number
		    	 //Winning id and game data for records
		    	 if(response.equals("ACK")){
		    		 os.println(this.g.getNumber());
				     os.flush();
				     g.setOtherPlayerNumber(is.readLine());
				     g.setWinnerID(is.readLine());
				     g.setWaitMsg("");
				     g.setGameHistory(is.readLine());
		    	 }
		     }
		     
		     //Set ended to true and print alert to user
		     this.g.setEnded(true, "Game ended, Come again!");
		     
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
