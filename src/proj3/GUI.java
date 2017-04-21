package proj3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoText;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.TransactionsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.responses.AccountResponse.Balance;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GUI extends Application{
	
	public Stage stage;
	public Server server;
	private String out = "";
	private int WIDTH = 1000, HEIGHT = 600;
	private long number = -1, hash;
	private double balanceValue = 0;
	private TextArea history = new TextArea();
	private Text AccountBalance = new Text(), wait_msg = new Text(),
			player1_ID_label = new Text("Player 1 ID:"), player2_ID_label = new Text("Player 2 ID:"),
			NumberLabel = new Text("Generated Number:"), HashLabel = new Text("Generated Hash: "),
			player1_Bet_label = new Text("Player 1 Bet and Hash:"), player2_Bet_label = new Text("Player 2 Bet and Hash:"),
			player1_number_label = new Text("Player 1 True Number:"), player2_number_label = new Text("Player 2 True Number:"),
			otherPlayer_label = new Text("Other Player Value:"), winner_label = new Text("Winner is ");
	private User u;
	private Banker b;
	private Player p;
	private boolean clientStartError = false, generated = false, ended = false, 
			running = false, bet_placed = false, isBankersScreen = false, betsRecv = false;
	private String BankerID, winnerID;
	private ArrayList<Bet> bets = new ArrayList<Bet>();
	private ArrayList<String> paymentHistory = new ArrayList<String>();
	
	public static void main(String[] args) {
        launch();
    }
	
	@Override
    public void start(Stage primaryStage){
		this.stage = primaryStage;
		
		Network.useTestNetwork();
		this.server = new Server("https://horizon-testnet.stellar.org");
		
        Scene start = login();
       
        primaryStage.setTitle("Super Fun Game");
        primaryStage.setScene(start);
        primaryStage.show();
       
	 }
	
	private Scene login(){
		GridPane grid = new GridPane();
        
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label accountID = new Label("AccountID:");
        grid.add(accountID, 0, 1);

        TextField idTextField = new TextField();
        grid.add(idTextField, 1, 1);
        
        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        EventHandler<ActionEvent> new_page = new EventHandler<ActionEvent>() {
         	 
            @Override
            public void handle(ActionEvent e){
            	Button in = (Button)e.getSource();
                User user = new User();
   
                if(in.getText().equals("Login")){
		            try{
		    			user = new User(idTextField.getText());
		    		}catch(ArrayIndexOutOfBoundsException ex){
		    			actiontarget.setFill(Color.FIREBRICK);
		                actiontarget.setText("Account Does Not Exist");
		                return;
		    		}
                }
                System.out.println(user.AccountID);
                u = user;
                Scene main = mainMenu(user);
                stage.setScene(main);
                stage.show();
            }
        };
        
        
        Button CU_button = new Button("Create User");
        HBox CU_hb = new HBox(10);
        CU_hb.setAlignment(Pos.BASELINE_LEFT);
        CU_hb.getChildren().add(CU_button);
        CU_button.setOnAction(new_page);
        grid.add(CU_hb, 0, 3, 2, 2);
        
        Button L_button = new Button("Login");
        HBox L_hb = new HBox(10);
        L_hb.setAlignment(Pos.BASELINE_RIGHT);
        L_hb.getChildren().add(L_button);
        L_button.setOnAction(new_page);
        grid.add(L_hb, 1, 3, 2, 2);
        
        return new Scene(grid, WIDTH, HEIGHT);
	}
	
	private Scene mainMenu(User u){
		b = new Banker(this, u);
		p = new Player(this, u);
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(50, 10, 35, 35));
        
        grid.add(AccountBalance, 0, 3, 1, 1);
        
        history.setEditable(false);
        history.wrapTextProperty();
        history.setPrefColumnCount(51);
        history.prefHeight(100);
       
        grid.add(history, 0, 4, 2, 1);
        
        check_history();
        
        grid.add(wait_msg, 0, 2);
        
        EventHandler<ActionEvent> gen_view = new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent e){
            	Button in = (Button)e.getSource();
            	
            	Scene main = mainMenu(u);
                stage.setScene(main);
                stage.show();
            	
            	if(in.getText().equals("Logout")){
            		out = "";
            		Scene login = login();
                    stage.setScene(login);
                    stage.show();
            	}
            	
            	if(in.getText().equals("Become Banker")){
            		
        			wait_msg.setFill(Color.FIREBRICK);
            		wait_msg.setText("Waiting For Players to Connect");
					b.start();
					
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {}
					
    				if(!running){
    					Scene banker = bankerView(u);
                        stage.setScene(banker);
                        stage.show();
            		}else{
            			wait_msg.setFill(Color.FIREBRICK);
                		wait_msg.setText("Game already in Progress");
            		}
            		
            	}	
            	
            	if(in.getText().equals("Play Game")){
            		p.start();
            		try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e1) {}
            		
            		if(clientStartError){
            			wait_msg.setFill(Color.FIREBRICK);
                		wait_msg.setText("No Games Available");
            		}else{
            			wait_msg.setText("");
            			Scene player = playerView(u);
                        stage.setScene(player);
                        stage.show();
            		}
            		clientStartError = false;
            	}
            }
        };
        
        Button l_button = new Button("Logout");
        HBox l_hb = new HBox(10);
        l_hb.setAlignment(Pos.BASELINE_LEFT);
        l_hb.getChildren().add(l_button);
        l_button.setOnAction(gen_view);
        grid.add(l_hb, 0, 1, 1, 1);
        
        Button B_button = new Button("Become Banker");
        HBox B_hb = new HBox(10);
        B_hb.setAlignment(Pos.BASELINE_LEFT);
        B_hb.getChildren().add(B_button);
        B_button.setOnAction(gen_view);
        grid.add(B_hb, 1, 1, 1, 1);
        
        Button P_button = new Button("Play Game");
        HBox P_hb = new HBox(10);
        P_hb.setAlignment(Pos.BASELINE_LEFT);
        P_hb.getChildren().add(P_button);
        P_button.setOnAction(gen_view);
        grid.add(P_hb, 2, 1, 1, 1);
        
        Text AccountID = new Text("Account ID: " + u.AccountID);
        AccountID.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        grid.add(AccountID, 0, 0, 1, 1);
        
		return new Scene(grid, WIDTH, HEIGHT);
	}
	
	private Scene bankerView(User u){
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(50, 10, 35, 35));
        
        this.isBankersScreen = true;
        
        this.bets.add(new Bet());
        this.bets.add(new Bet());
        
        grid.add(wait_msg, 0, 0);
        
        wait_msg.setFill(Color.FIREBRICK);
		wait_msg.setText("Waiting for Bets");
        
        checkTransactions();
        check_history();
        
        EventHandler<ActionEvent> gen_view = new EventHandler<ActionEvent>() {
       	 
            @Override
            public void handle(ActionEvent e){
            	CRC32 c = new CRC32();
            	Button in = (Button)e.getSource();
            	
            	if(in.getText().equals("End Game")){
            		if(!betsRecv){
            			clear_labels();
	            		isBankersScreen = false;
	            		bets = new ArrayList<Bet>();
	            		b.kill();
	            		out = "";
	            		Scene main = mainMenu(u);
	                    stage.setScene(main);
	                    stage.show();
            		}else{
            			wait_msg.setFill(Color.FIREBRICK);
            			wait_msg.setText("Game must be completed once bets have been received");
            		}            		
            	}
            	
            	if(in.getText().equals("Decide Winner")){
            		if(betsRecv){
            		
            			c.update(longToBytes(Long.parseLong(bets.get(0).Number)));
            			long p1hash = c.getValue();
            			
            			c = new CRC32();
            			
            			c.update(longToBytes(Long.parseLong(bets.get(1).Number)));
            			long p2hash = c.getValue();
            			
            			boolean cheating = false;
            			
            			wait_msg.setText("Checking for cheating...\n");
            			
            			if(!bets.get(0).memo.equals(Long.toString(p1hash))){
            				wait_msg.setFill(Color.FIREBRICK);
                			wait_msg.setText(wait_msg.getText() + "Player 1 cheated!");
                			cheating = true;
            			}
            			
            			if(!bets.get(1).memo.equals(Long.toString(p2hash))){
            				wait_msg.setFill(Color.FIREBRICK);
                			wait_msg.setText(wait_msg.getText() + "Player 2 cheated!");
                			cheating = true;
            			}
            			
            			if(!cheating){
            				wait_msg.setText("No one Cheated, Computing winner...");
            				long winner = (Long.parseLong(bets.get(0).Number) + Long.parseLong(bets.get(1).Number))%2;
            				
            				if(winner == 0){
            					winner_label.setText(winner_label.getText() + " Player 1");
            					winnerID = bets.get(0).AccountID;
            				}else{
            					winner_label.setText(winner_label.getText() + " Player 2");
            					winnerID = bets.get(1).AccountID;
            				}
            				b.revealWinner(winnerID);
            				betsRecv = false;
            			}
            		}else{
            			wait_msg.setFill(Color.FIREBRICK);
            			wait_msg.setText("Must have both Player's Values to Compare");
            		}
            	}
            }
        };
        
        Button E_button = new Button("End Game");
        HBox E_hb = new HBox(10);
        E_hb.setAlignment(Pos.BASELINE_LEFT);
        E_hb.getChildren().add(E_button);
        E_button.setOnAction(gen_view);
        grid.add(E_hb, 0, 1, 1, 1);
        
        Button C_button = new Button("Decide Winner");
        HBox C_hb = new HBox(10);
        C_hb.setAlignment(Pos.BASELINE_LEFT);
        C_hb.getChildren().add(C_button);
        C_button.setOnAction(gen_view);
        grid.add(C_hb, 1, 1, 1, 1);
        
        grid.add(this.player1_ID_label, 0, 2, 1, 1);
        grid.add(this.player2_ID_label, 0, 3, 1, 1);
        
        grid.add(this.player1_Bet_label, 0, 4, 1, 1);
        grid.add(this.player2_Bet_label, 0, 5, 1, 1);
        
        grid.add(this.player1_number_label, 0, 6, 1, 1);
        grid.add(this.player2_number_label, 0, 7, 1, 1);
        
        grid.add(this.winner_label, 0, 8, 1, 1);
        
		return new Scene(grid, WIDTH, HEIGHT);
	}
	
	private Scene playerView(User u){
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(50, 10, 35, 35));
        
        TextField betField = new TextField();
        grid.add(betField, 0, 3);
                
        EventHandler<ActionEvent> gen_view = new EventHandler<ActionEvent>() {
       	 
            @Override
            public void handle(ActionEvent e){
            	Button in = (Button)e.getSource();
            	Random rand = new Random();
            	CRC32 c = new CRC32();
            	
            	if((in.getText().equals("Leave Game") && ended)){
            		clear_labels();
            		generated = false;
            		ended = false;
            		bet_placed = false;
            		number = -1;
            		out = "";
            		Scene main = mainMenu(u);
                    stage.setScene(main);
                    stage.show();
            	}
            	
            	if(in.getText().equals("Generate Number")){
            		if(!generated){
            			number = Math.abs(rand.nextLong())%100000;
            			NumberLabel.setText("Generated Number: " + number);
            			c.update(longToBytes(number));
            			hash = c.getValue();
            			HashLabel.setText("Generated Hash: " + hash);
            			
            			generated = true;
            		}else{
            			wait_msg.setFill(Color.FIREBRICK);
            			wait_msg.setText("Number is only generated once per round");
            		}
            	}
            	
            	if(in.getText().equals("Place Bet") && !bet_placed){
            		if(generated){
            			double bet;
            			try{
            				bet = Double.parseDouble(betField.getText());
            			}catch(NumberFormatException nfe){
            				bet = -1;
            				wait_msg.setFill(Color.FIREBRICK);
                			wait_msg.setText("Bet must be number");
            			}
            			try {
							check_balance();
						} catch (IOException e1) {
							System.out.println("Unable to retrieve balance");
						}
            			if(bet > 0 && bet < balanceValue){
            				sendTransaction(bet, hash);
                			bet_placed = true;
                			wait_msg.setFill(Color.FIREBRICK);
                			wait_msg.setText("Bet has been placed, waiting for Banker");
            			}else{
            				wait_msg.setFill(Color.FIREBRICK);
                			wait_msg.setText("Bet must be greater than 0 and less than available balance");
            			}
            		}else{
            			wait_msg.setFill(Color.FIREBRICK);
            			wait_msg.setText("Must Generate Number before placing bet");
            		}
            	}
            }
        };
        
        Button E_button = new Button("Leave Game");
        HBox E_hb = new HBox(10);
        E_hb.setAlignment(Pos.BASELINE_LEFT);
        E_hb.getChildren().add(E_button);
        E_button.setOnAction(gen_view);
        grid.add(E_hb, 0, 1, 1, 1);
        
        Button Gen_button = new Button("Generate Number");
        HBox Gen_hb = new HBox(10);
        Gen_hb.setAlignment(Pos.BASELINE_LEFT);
        Gen_hb.getChildren().add(Gen_button);
        Gen_button.setOnAction(gen_view);
        grid.add(Gen_hb, 0, 2, 1, 1);
        
        Button Bet_button = new Button("Place Bet");
        HBox Bet_hb = new HBox(10);
        Bet_hb.setAlignment(Pos.BASELINE_LEFT);
        Bet_hb.getChildren().add(Bet_button);
        Bet_button.setOnAction(gen_view);
        grid.add(Bet_hb, 1, 3, 1, 1);
        
        grid.add(NumberLabel, 0, 4, 1, 1);
        grid.add(HashLabel, 0, 5, 1, 1);
        grid.add(wait_msg, 0, 8);
        grid.add(otherPlayer_label, 0, 6);
        grid.add(winner_label, 0, 7, 1, 1);
        
        
		return new Scene(grid, WIDTH, HEIGHT);
	}
	
	private String check_balance() throws IOException{
		AccountResponse account = server.accounts().account(u.pair);
		Balance[] balance = account.getBalances(); 
		this.balanceValue = Double.parseDouble(balance[0].getBalance());
		
		return "Balance for account: " + balance[0].getBalance() + " XLM";
	}
	
	private void check_history(){
		PaymentsRequestBuilder paymentsRequest = this.server.payments().forAccount(u.pair);
		paymentHistory = new ArrayList<String>();
		paymentsRequest.stream(stream);
	}
	
	public EventListener stream = new EventListener<OperationResponse>() {

		@Override
		public void onEvent(OperationResponse payment) {
			
			if(payment instanceof PaymentOperationResponse) {

			      String amount = ((PaymentOperationResponse) payment).getAmount();

			      Asset asset = ((PaymentOperationResponse) payment).getAsset();
			      String assetName;
			      if (asset.equals(new AssetTypeNative())) {
			        assetName = "lumens";
			      }else{
			        StringBuilder assetNameBuilder = new StringBuilder();
			        assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getCode());
			        assetNameBuilder.append(":");
			        assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getIssuer().getAccountId());
			        assetName = assetNameBuilder.toString();
			      }

			      StringBuilder output = new StringBuilder();
			      output.append(amount);
			      output.append(" ");
			      output.append(assetName);
			      output.append(" from ");
			      String from = ((PaymentOperationResponse) payment).getFrom().getAccountId();
			      output.append(from);
			     // System.out.println(output);
			      
			      if(isBankersScreen){
			    	  if(bets.get(0).AccountID.equals(from)){
			    		  bets.get(0).amount = amount;
			    	  }else if(bets.get(1).AccountID.equals(from)){
			    		  bets.get(1).amount = amount;
			    	  }
			    	  
			    	  if(bets.get(0).isReady() && bets.get(1).isReady()){
			    		  wait_msg.setFill(Color.FIREBRICK);
			    		  wait_msg.setText("Both Bets Received");
			    		  player1_Bet_label.setText("Player 1 Bet and Hash: " + bets.get(0).amount + " " + bets.get(0).memo);
			    		  player2_Bet_label.setText("Player 2 Bet and Hash: " + bets.get(1).amount + " " + bets.get(1).memo);
			    		  betsRecv = true;
			    		  
			    		  b.sendAcks();
			    	  }
			      }
			      
			      out = out + output.toString() + "\n";
			      try {
			        	AccountBalance.setText(check_balance());
				  }catch(IOException e1) {
						AccountBalance.setText("Balance Unavailable");
				  }
			      history.setText(out);
			}
		}
	};
	
	private void checkTransactions(){
		TransactionsRequestBuilder response = server.transactions().forAccount(this.u.pair);
		response.stream(TStream);
	}
	
	public EventListener TStream = new EventListener<TransactionResponse>() {

		@Override
		public void onEvent(TransactionResponse transaction) {
			
			if(transaction instanceof TransactionResponse) {

			    String memo = ((MemoText) ((TransactionResponse) transaction).getMemo()).getText();
			    KeyPair src = ((TransactionResponse) transaction).getSourceAccount();
			    
			    if(src.getAccountId().equals(bets.get(0).AccountID)){
			    	//System.out.println("Account 1 Memo: " + memo);
			    	bets.get(0).memo = memo;
			    }else if(src.getAccountId().equals(bets.get(1).AccountID)){
			    	//System.out.println("Account 2 Memo: " + memo);
			    	bets.get(1).memo = memo;
			    }
			}
		}
	};
	
	private void clear_labels(){
		AccountBalance = new Text();
		wait_msg = new Text();
		player1_ID_label = new Text("Player 1 ID:");
		player2_ID_label = new Text("Player 2 ID:");
		NumberLabel = new Text("Generated Number:");
		HashLabel = new Text("Generated Hash: ");
		player1_Bet_label = new Text("Player 1 Bet and Hash:");
		player2_Bet_label = new Text("Player 2 Bet and Hash:");
		player1_number_label = new Text("Player 1 True Number:");
		player2_number_label = new Text("Player 2 True Number:");
		otherPlayer_label = new Text("Other Player Value:");
		winner_label = new Text("Winner is ");
	}

	public void setWaitMsg() {
		this.wait_msg.setText("");
	}
	
	public void setEnded(boolean ended) {
		this.ended = ended;
		wait_msg.setFill(Color.FIREBRICK);
		this.wait_msg.setText("Game Has been ended by Banker");
	}
	
	public void setGameRunning(boolean running) {
		this.running = running;
		if(running){
			wait_msg.setFill(Color.FIREBRICK);
			this.wait_msg.setText("Game Already In Progress");
			this.b.kill();
		}
	}
	
	public void setPlayerID(String ID, int playerNum) {
		if(playerNum == 0){
			this.bets.get(0).AccountID = ID;
			this.player1_ID_label.setText(this.player1_ID_label.getText() + " " + ID);
		}else{
			this.bets.get(1).AccountID = ID;
			this.player2_ID_label.setText(this.player2_ID_label.getText() + " " + ID);
		}
	}
	
	public void setPlayerNumber(String number, int playerNum) {
		if(playerNum == 0){
			this.bets.get(0).Number = number;
			this.player1_number_label.setText(this.player1_number_label.getText() + " " + number);
		}else{
			this.bets.get(1).Number = number;
			this.player2_number_label.setText(this.player2_number_label.getText() + " " + number);
		}
	}
	
	public void setBankerID(String ID) {
		this.BankerID = ID;
	}
	
	public void setclientStartError(boolean clientStartError){
		this.clientStartError = clientStartError;
	}
	
	public String getNumber(){
		return Long.toString(this.number);
	}
	
	public void setWinnerID(String winnerID){
		if(winnerID.equals(u.AccountID)){
			winner_label.setText("You Win!");
		}else{
			winner_label.setText("You Lose :(");
		}
	}
	
	public String getOtherPlayerNumber(int threadID){
		if(threadID == 0){
			return bets.get(1).Number;
		}else{
			return bets.get(0).Number;
		}
		
	}
	
	public void setOtherPlayerNumber(String number){
		this.otherPlayer_label.setText(this.otherPlayer_label.getText() + " " + number);
	}
	
	private byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	private void sendTransaction(double bet, long hash){
		KeyPair banker = KeyPair.fromAccountId(this.BankerID);
		
		try{
			server.accounts().account(banker);
		}catch(Exception e){
			System.out.println("Banker Account Does not Exist");
		}
		
		AccountResponse sourceAccount = null;
		try {
			sourceAccount = server.accounts().account(this.u.pair);
		} catch (IOException e) {
			System.out.println("User Account Does not Exist");
		}
		
		Transaction transaction = new Transaction.Builder(sourceAccount)
		        .addOperation(new PaymentOperation.Builder(banker, new AssetTypeNative(), Double.toString(bet)).build())
		        .addMemo(Memo.text(Long.toString(hash)))
		        .build();
		
		transaction.sign(this.u.pair);
		
		System.out.println(Long.toString(hash));
		
		try{
		  SubmitTransactionResponse response = server.submitTransaction(transaction);
		  System.out.println("Success!");
		}catch (Exception e) {
		  System.out.println("Something went wrong!");
		  System.out.println(e.getMessage());
		}
	}
}
