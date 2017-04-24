package proj3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Banker thread class, exists as server to a client player
 * @author ray
 *
 */
public class BankerThread extends Thread{
	 String line=null;
	 BufferedReader  is = null;
	 PrintWriter os=null;
	 Socket s=null;
	 volatile public int kill = 0, ack = 0;
	 volatile private GUI g;
	 private int threadID;
	 volatile User u;
	 volatile String winnerID = "-1";

	 //Initialize thread sockets and arguments.
	 public BankerThread(Socket s, GUI g, int threadID, User u){
		 super("BankerThread");
	     this.s=s;
	     this.g = g;
	     this.threadID = threadID;
	     this.u = u;
	 }

	 /**
	  * Execute thread, kills thread on completion
	  */
	 public void run() {
		 //Try to start socket read/write
		 try{
		     is= new BufferedReader(new InputStreamReader(s.getInputStream()));
		     os=new PrintWriter(s.getOutputStream());
	
		 }catch(IOException e){
		     System.out.println("IO error in server thread");
		 }
		 
		 //Try to execute, catch any errors
		 try {
			 
			 //Get client player ID and set it in BAnker GUI class
			 this.g.setPlayerID(is.readLine(), threadID);
			 
			 //Send banker account ID to client
			 os.println(this.u.AccountID);
		     os.flush();
			 
		     //Wait for fill or for bets received ack
		     while(this.kill != 1 && this.ack != 1);
		     
		     //If bets received ack then send ack to clients and wait for players true number
		     if(this.ack == 1){
	    		 os.println("ACK");
			     os.flush();
			     this.g.setPlayerNumber(is.readLine(), threadID);
			     
			     //Wait a second while other thread receives and sets other player value in GUI
			     try{
					TimeUnit.SECONDS.sleep(1);
				 }catch (InterruptedException e1) {}
			     
			     //send the client the other players value
			     os.println(this.g.getOtherPlayerNumber(threadID));
			     os.flush();
	    	 }
		     
		     //Wait for kill or winner
		     while(this.kill != 1 && this.winnerID.equals("-1"));
		     
		     //If winner, notify each player of winner id, and send game history to client
		     if(!this.winnerID.equals("-1")){
		    	 os.println(winnerID);
			     os.flush();
			     
			     os.println(this.g.getGameHistory());
			     os.flush();
		     } 
			 
		     //wait to be killed
			 while(this.kill != 1);
			 
			 //When killed, kill clients as well
		     os.println("Quit");
		     os.flush();
		     
		 }catch (IOException e) {
		     line=this.getName(); 
		     System.out.println("IO Error/ Client "+line+" terminated abruptly");
		 }catch(NullPointerException e){
		     line=this.getName(); 
		     System.out.println("Client "+line+" Closed");
		 }
		 //Debug client close
		 finally{    
			 try{
			     System.out.println("Connection Closing..");
			     if (is!=null){
			         is.close(); 
			         System.out.println("Socket Input Stream Closed");
			     }
			     if(os!=null){
			         os.close();
			         System.out.println("Socket Out Closed");
			     }
			     if (s!=null){
			     s.close();
			     System.out.println("Socket Closed");
			     }
		
			 }catch(IOException ie){
			     System.out.println("Socket Close Error");
			 }
		 }
	 }
}
