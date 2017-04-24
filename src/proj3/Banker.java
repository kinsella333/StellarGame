package proj3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Banker Server thread creater class.
 * 
 * @author ray
 *
 */
public class Banker extends Thread{
	
	volatile private Socket s=null;
	volatile private ServerSocket ss2=null;
	volatile private ArrayList<BankerThread> st = new ArrayList<BankerThread>(2);
	volatile private GUI g;
	volatile User u;
	volatile private int kill = 0;
	
	//Creates banker object and sets current gui and user global variables
	public Banker(GUI g, User u){
		 this.g = g;
		 this.u = u;
	}
	
	/**
	 * Thread run method, creates banker server threads
	 */
	public void run(){
		int i = 0;
		
		System.out.println("Server Listening......");
		 
		//Try to set up socket
		 try{ 
			 this.ss2 = new ServerSocket(4445);
		 }catch(IOException e){
			 System.out.println("Server error");
			 this.g.setGameRunning(true);
			 return;
		 }
		
		//Spawn at most two server threads to handle two clients
		while(i < 2){
		     try{
		         s= ss2.accept();
		         System.out.println("connection Established");
		         this.st.add(new BankerThread(s, g, i, u));
		         this.st.get(i).start();

		     }catch(Exception e){
			     System.out.println("Connection Error");
			     return;
		     }
		     i++;
		}
		//Clear wait message when threads have been created
		this.g.setWaitMsg("");
		
	}
	
	/**
	 * When user ends game, banker threads are killed and sockets closed
	 */
	public void kill(){
		try {
			this.st.get(0).kill = 1;
		}catch (Exception e){
			System.out.println("Error cannot kill thread 1");
		}	
		try {
			this.st.get(1).kill = 1;
		}catch (Exception e){
			System.out.println("Error cannot kill thread 2");
		}
		try {
			ss2.close();
		} catch (Exception e) {
			System.out.println("Error on socket close");
		}
	}
	
	/**
	 * When banker has received both bets and requests true values
	 */
	public void sendAcks(){
		this.st.get(0).ack = 1;
		this.st.get(1).ack = 1;
	}
	
	/**
	 * Sends winner account id to each client
	 * @param winnerID winner account id
	 */
	public void revealWinner(String winnerID){
		this.st.get(0).winnerID = winnerID;
		this.st.get(1).winnerID = winnerID;
	}
	
}
