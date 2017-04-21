package proj3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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

	 public BankerThread(Socket s, GUI g, int threadID, User u){
		 super("BankerThread");
	     this.s=s;
	     this.g = g;
	     this.threadID = threadID;
	     this.u = u;
	 }

	 public void run() {
		 try{
		     is= new BufferedReader(new InputStreamReader(s.getInputStream()));
		     os=new PrintWriter(s.getOutputStream());
	
		 }catch(IOException e){
		     System.out.println("IO error in server thread");
		 }
	
		 try {
		     
			 this.g.setPlayerID(is.readLine(), threadID);
			 
			 os.println(this.u.AccountID);
		     os.flush();
			 
		     while(this.kill != 1 && this.ack != 1);
		     
		     if(this.ack == 1){
	    		 os.println("ACK");
			     os.flush();
			     this.g.setPlayerNumber(is.readLine(), threadID);
			     
			     try{
					TimeUnit.SECONDS.sleep(1);
				 }catch (InterruptedException e1) {}
			     
			     os.println(this.g.getOtherPlayerNumber(threadID));
			     os.flush();
	    	 }
		     
		     while(this.kill != 1 && this.winnerID.equals("-1"));
		     
		     if(!this.winnerID.equals("-1")){
		    	 os.println(winnerID);
			     os.flush();
		     }
		     
		     while(this.kill != 1);
		     
		     os.println("Quit");
		     os.flush();
		     
		 }catch (IOException e) {
		     line=this.getName(); 
		     System.out.println("IO Error/ Client "+line+" terminated abruptly");
		 }catch(NullPointerException e){
		     line=this.getName(); 
		     System.out.println("Client "+line+" Closed");
		 }
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
	 
	 private void game() throws IOException{
		 os.println(line);
         os.flush();
         System.out.println("Response to Client  :  "+line);
         line=is.readLine();
	 }
	 
}
