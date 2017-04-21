package proj3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Banker extends Thread{
	
	volatile private Socket s=null;
	volatile private ServerSocket ss2=null;
	volatile private ArrayList<BankerThread> st = new ArrayList<BankerThread>(2);
	volatile private GUI g;
	volatile User u;
	volatile private int kill = 0;
	
	public Banker(GUI g, User u){
		 this.g = g;
		 this.u = u;
	}
	
	public void run(){
		int i = 0;
		
		System.out.println("Server Listening......");
		 
		 try{ 
			 this.ss2 = new ServerSocket(4445);
		 }catch(IOException e){
			 //e.printStackTrace();
			 System.out.println("Server error");
			 this.g.setGameRunning(true);
			 return;
		 }
			
		while(i < 2){
		     try{
		         s= ss2.accept();
		         System.out.println("connection Established");
		         this.st.add(new BankerThread(s, g, i, u));
		         this.st.get(i).start();

		     }catch(Exception e){
			     //e.printStackTrace();
			     System.out.println("Connection Error");
			     return;
		     }
		     i++;
		}
		this.g.setWaitMsg();
		
	}
	
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
			//s.close();
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Error on socket close");
		}
	}
	
	public void sendAcks(){
		this.st.get(0).ack = 1;
		this.st.get(1).ack = 1;
	}
	
	public void revealWinner(String winnerID){
		this.st.get(0).winnerID = winnerID;
		this.st.get(1).winnerID = winnerID;
	}
	
}
