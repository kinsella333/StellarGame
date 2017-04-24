package proj3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.Date;

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


/**
 * GUI for stellar game. Allows for stellar test account creation, and 
 * test account login. Once logged in users may send money to a target
 * account, play the super fun game, or host a super fun game as the banker.
 * Accounts created will only persist as long as the stellar test server has not
 * reset which occurs every few days.
 * 
 * @author ray
 *
 */
public class GUI extends Application{
	
	public Stage stage;
	public Server server;
	private String out = "";
	private int WIDTH = 900, HEIGHT = 600;
	private long number = -1, hash;
	private double balanceValue = 0;
	private Date transactionTime;
	private TextArea history = new TextArea(), gameHistory = new TextArea();
	private Text AccountBalance = new Text("Balance:"), alert_msg = new Text(),
			player1_ID_label = new Text("Player 1 ID:"), player2_ID_label = new Text("Player 2 ID:"),
			NumberLabel = new Text("Generated Number:"), HashLabel = new Text("Generated Hash: "),
			player1_Bet_label = new Text("Player 1 Bet and Hash:"), player2_Bet_label = new Text("Player 2 Bet and Hash:"),
			player1_number_label = new Text("Player 1 True Number:"), player2_number_label = new Text("Player 2 True Number:"),
			otherPlayer_label = new Text("Other Player Value:"), winner_label = new Text("Winner is ");
	private User u;
	private Banker b;
	private Player p;
	private boolean clientStartError = false, generated = false, ended = false, 
			running = false, bet_placed = false, isBankersScreen = false, betsRecv = false,
			first = true;
	private String BankerID, winnerID, gHist;
	private ArrayList<Bet> bets = new ArrayList<Bet>();
	
	/**
	 * Launches stellar GUI via Javafx launch command
	 */
	public static void main() {
        launch();
    }

	/**
	 * Javafx start method which runs on launch. It initializes the stage,
	 * and sets the scene to the login view.
	 */
	@Override
    public void start(Stage primaryStage){
		this.stage = primaryStage;
		
		//Set server to stellar test network
		Network.useTestNetwork();
		this.server = new Server("https://horizon-testnet.stellar.org");
		
		//Create login scene
        Scene start = login();
       
        //Title window, and set scene to login
        primaryStage.setTitle("Super Fun Game");
        primaryStage.setScene(start);
        primaryStage.show();
       
	}
	
	/**
	 * Creates login scene. Initializes all buttons and creates event handler for the 
	 * buttons on screen. Allows the user to create an account, or provide the account ID
	 * for a previously created account.
	 * 
	 * @return login scene object.
	 */
	private Scene login(){
		
		//Create grid pane to add elements to
		GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        //Create and add welcome title
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        
        //Add label for account id text field
        Label accountID = new Label("AccountID:");
        grid.add(accountID, 0, 1);
        
        //Optional account ID input field for login
        TextField idTextField = new TextField();
        grid.add(idTextField, 1, 1);
        
        //Error message in case of invalid login credentials
        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        
        EventHandler<ActionEvent> new_page = new EventHandler<ActionEvent>() {
         	
        	/**
        	 * Event handler for the buttons on screen. If button is login press
        	 * then try to create new user object from account id provided. If 
        	 * failed then provide error. If button press not login then it was 
        	 * create user, and continue to initialize user.
        	 * 
        	 * @param ActionEvent e button click event
        	 */
            @Override
            public void handle(ActionEvent e){
            	Button in = (Button)e.getSource();
                User user = new User();
                
                //If button is login
                if(in.getText().equals("Login")){
		            try{
		    			user = new User(idTextField.getText());
		    		}catch(ArrayIndexOutOfBoundsException ex){
		    			actiontarget.setFill(Color.FIREBRICK);
		                actiontarget.setText("Account Does Not Exist");
		                return;
		    		}
                }
                //Print account for debugging 
                System.out.println(user.AccountID);
                
                //Set global user to current
                u = user;
                
                //Create main screen view at set to current view.
                Scene main = mainMenu(user);
                stage.setScene(main);
                stage.show();
            }
        };
        
        //Create User Button
        Button CU_button = new Button("Create User");
        HBox CU_hb = new HBox(10);
        CU_hb.setAlignment(Pos.BASELINE_LEFT);
        CU_hb.getChildren().add(CU_button);
        CU_button.setOnAction(new_page);
        grid.add(CU_hb, 0, 3, 2, 2);
        
        //Create Login button
        Button L_button = new Button("Login");
        HBox L_hb = new HBox(10);
        L_hb.setAlignment(Pos.BASELINE_RIGHT);
        L_hb.getChildren().add(L_button);
        L_button.setOnAction(new_page);
        grid.add(L_hb, 1, 3, 2, 2);
        
        return new Scene(grid, WIDTH, HEIGHT);
	}
	
	/**
	 * Main Menu view. Offers service to send money to account,
	 * play the super fun game, or become banker for a game. It also shows a 
	 * transaction history.
	 * @param u Current user
	 * @return Main view to add to stage
	 */
	private Scene mainMenu(User u){
		
		//If first time through then initialize transaction and payment streams
		if(first){
			checkTransactions();
	        check_history();
	        first = false;
		}
		
		//Initialize outer grid object
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(50, 10, 35, 35));
		
        //Initialize inner grid object
		GridPane inner_grid = new GridPane();
		inner_grid.setAlignment(Pos.TOP_LEFT);
        inner_grid.setHgap(10);
        inner_grid.setVgap(20);
        inner_grid.setPadding(new Insets(10, 10, 35, 35));
        
        //Set history text area to size and non editable
        history.setEditable(false);
        history.wrapTextProperty();
        history.setPrefColumnCount(51);
        history.prefHeight(100);
        inner_grid.add(history, 0, 4, 2, 1);
        
        //Get user balance
        check_balance();
        
        //Add alert text object
        inner_grid.add(alert_msg, 0, 0);
        
        //Add text field for sending money to an account
        TextField sendAccountId = new TextField("Account ID to send to");
        inner_grid.add(sendAccountId, 0, 1);
        
        //Amount of money to send input field
        TextField amount = new TextField("Amount to send");
        inner_grid.add(amount, 0, 2);
        
        EventHandler<ActionEvent> gen_view = new EventHandler<ActionEvent>() {
        	
        	/**
        	 * Event handler for buttons in view.
        	 */
            @Override
            public void handle(ActionEvent e){
            	Button in = (Button)e.getSource();
            	
            	//Update the view for changes
            	Scene main = mainMenu(u);
                stage.setScene(main);
                stage.show();
            	
                //Return to login view
            	if(in.getText().equals("Logout")){
            		out = "";
            		clear_labels();
            		first = false;
            		Scene login = login();
                    stage.setScene(login);
                    stage.show();
            	}
            	
            	//Become a banker for a super fun game
            	if(in.getText().equals("Become Banker")){
        			alert_msg.setFill(Color.FIREBRICK);
            		alert_msg.setText("Waiting For Players to Connect");
					
					//If another game is not running then become banker else alert player of game
    				//Set view to banker view
    				Scene banker = bankerView();
    				if(banker != null){
                        stage.setScene(banker);
                        stage.show();
            		}else{
            			alert_msg.setFill(Color.FIREBRICK);
                		alert_msg.setText("Game already in Progress");
            		}
            		
            	}
            	
            	//If user wants to play game, check for available game
            	if(in.getText().equals("Play Game")){
            		
            		//If no game exists inform user, else update view to player view
        			alert_msg.setText("");
        			Scene player = playerView();
        			
        			if(player == null){
        				alert_msg.setFill(Color.FIREBRICK);
                		alert_msg.setText("No Games Available");
        			}else{
        				stage.setScene(player);
                        stage.show();
        			} 
            		
            		clientStartError = false;
            	}
            	
            	//Send money to specified account id at the amount provided
            	if(in.getText().equals("Send Money")){
            		//Verify value is numeric
            		double val;
        			try{
        				val = Double.parseDouble(amount.getText());
        			}catch(NumberFormatException nfe){
        				val = -1;
        				alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Amount must be number");
        			}
        			//get current balance
					check_balance();
					
					//Make sure amount is more than 0 and less than balance, alert the user when transaction completes
        			if(val > 0 && val < balanceValue){
        				sendTransaction(val, hash, sendAccountId.getText());
            			alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Amount Transfered");
        			}else{
        				alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Amount must be greater than 0 and less than available balance");
        			}
        			//reInitilalize amount and id fields and update balance.
        			amount.setText("Amount to send");
        			sendAccountId.setText("Account ID of Account to send to");
        			check_balance();
            	}
            }
        };
        
        //Logout button
        Button l_button = new Button("Logout");
        HBox l_hb = new HBox(10);
        l_hb.setAlignment(Pos.BASELINE_LEFT);
        l_hb.getChildren().add(l_button);
        l_button.setOnAction(gen_view);
        
        //Become Banker Button
        Button B_button = new Button("Become Banker");
        HBox B_hb = new HBox(10);
        B_hb.setAlignment(Pos.BASELINE_LEFT);
        B_hb.getChildren().add(B_button);
        B_button.setOnAction(gen_view);
        inner_grid.add(B_hb, 0, 3);
        
        //Play Game button
        Button P_button = new Button("Play Game");
        HBox P_hb = new HBox(10);
        P_hb.setAlignment(Pos.BASELINE_LEFT);
        P_hb.getChildren().add(P_button);
        P_button.setOnAction(gen_view);
        inner_grid.add(P_hb, 1, 3);
        
        //Send Money Button
        Button t_button = new Button("Send Money");
        HBox t_hb = new HBox(10);
        t_hb.setAlignment(Pos.BASELINE_LEFT);
        t_hb.getChildren().add(t_button);
        t_button.setOnAction(gen_view);
        inner_grid.add(t_hb, 1, 2);
        
        //Current user's account ID
        Text AccountID = new Text("Account ID: " + u.AccountID);
        AccountID.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        
        //Add all elements and return new scene object
        grid.add(AccountID, 0,0);
        grid.add(l_hb, 1, 0);
        grid.add(AccountBalance, 0, 1);
        grid.add(inner_grid, 0,2);
        
		return new Scene(grid, WIDTH, HEIGHT);
	}
	
	/**
	 * Banker view creation 
	 * 
	 * @return new instance of banker view
	 */
	private Scene bankerView(){
		//Create local grid object
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(50, 10, 35, 35));
        
        //Create new instance of banker b, and start the server.
        b = new Banker(this, u);
        b.start();
        
        checkTransactions();
        check_history();
        
        //Wait in case another game is running, helps prevent socket conflicts
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {}
		//If game already running return null and notify user
		if(running){
			return null;
		}
        
        //Set global boolean for current screen
        this.isBankersScreen = true;
        
        //Add two empty bet objects to bets arraylist
        this.bets.add(new Bet());
        this.bets.add(new Bet());
        
        //Alert user that the system is waiting for bets
        grid.add(alert_msg, 0, 0);
        alert_msg.setFill(Color.FIREBRICK);
		alert_msg.setText("Waiting for Bets");
        
        EventHandler<ActionEvent> gen_view = new EventHandler<ActionEvent>() {
       	 	
        	/**
        	 * Button event handler for on screen buttons
        	 */
            @Override
            public void handle(ActionEvent e){
            	CRC32 c = new CRC32();
            	Button in = (Button)e.getSource();
            	
            	//End game button which can only be selected when banker is not
            	//in possession of bets.
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
            			alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Game must be completed once bets have been received");
            		}            		
            	}
            	
            	//Decide winner button, decides the winner given the values once bets have been received.
            	if(in.getText().equals("Decide Winner")){
            		if(betsRecv){
            			
            			//Get CRC 32 val for player 1s true value
            			c.update(longToBytes(Long.parseLong(bets.get(0).Number)));
            			long p1hash = c.getValue();
            			
            			c = new CRC32();
            			
            			//Get CRC 32 val for player 2s true value
            			c.update(longToBytes(Long.parseLong(bets.get(1).Number)));
            			long p2hash = c.getValue();
            			
            			boolean cheating = false;
            			
            			alert_msg.setText("Checking for cheating...\n");
            			
            			//Verify no one cheated by comparing original CRC vals and 
            			//just calculated CRC vals
            			if(!bets.get(0).memo.equals(Long.toString(p1hash))){
            				alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText(alert_msg.getText() + "Player 1 cheated!");
                			cheating = true;
                			winnerID = bets.get(1).AccountID;
            			}
            			if(!bets.get(1).memo.equals(Long.toString(p2hash))){
            				alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText(alert_msg.getText() + "Player 2 cheated!");
                			cheating = true;
                			winnerID = bets.get(0).AccountID;
            			}
            			
            			//If not cheating, decide winner and notify players
            			if(!cheating){
            				alert_msg.setText("No one Cheated, Computing winner...");
            				long winner = (Long.parseLong(bets.get(0).Number) + Long.parseLong(bets.get(1).Number))%2;
            				
            				if(winner == 0){
            					winner_label.setText(winner_label.getText() + " Player 1");
            					winnerID = bets.get(0).AccountID;
            				}else{
            					winner_label.setText(winner_label.getText() + " Player 2");
            					winnerID = bets.get(1).AccountID;
            				}
            			}
            			//Retain game data for later user query
            			save_game_data();
            			
            			//Reveal winners to players
            			b.revealWinner(winnerID);
            			
            			//Send winnings to victor
            			double winnings = Double.parseDouble(bets.get(0).amount) + Double.parseDouble(bets.get(1).amount)*.95;
            			sendTransaction(winnings, 0, winnerID);
            			
            			//Allow for the game to be reset or ended
            			ended = true;
            			betsRecv = false;
            		}else{
            			alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Must have both Player's Values to Compare");
            		}
            	}
            	
            	//Banker can reset if game is over. This clears all labels and ends game for clients
            	if(in.getText().equals("Reset")){
            		if(ended){
            			clear_labels();
	            		bets = new ArrayList<Bet>();
	            		b.kill();
	            		isBankersScreen = true;
	            		
	            		try{
							TimeUnit.SECONDS.sleep(1);
	            		}catch (InterruptedException e1) {}
	            	
	            		betsRecv = false;
	            		
	            		Scene bv = bankerView();
	                    stage.setScene(bv);
	                    stage.show();
            		}else{
            			alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Game may only be reset after completion");
            		}
            	}
            }
        };
        
        //End game button
        Button E_button = new Button("End Game");
        HBox E_hb = new HBox(10);
        E_hb.setAlignment(Pos.BASELINE_LEFT);
        E_hb.getChildren().add(E_button);
        E_button.setOnAction(gen_view);
        grid.add(E_hb, 0, 1, 1, 1);
        
        //Decide winner button
        Button C_button = new Button("Decide Winner");
        HBox C_hb = new HBox(10);
        C_hb.setAlignment(Pos.BASELINE_LEFT);
        C_hb.getChildren().add(C_button);
        C_button.setOnAction(gen_view);
        grid.add(C_hb, 1, 1, 1, 1);
        
        //Reset button
        Button R_button = new Button("Reset");
        HBox R_hb = new HBox(10);
        R_hb.setAlignment(Pos.BASELINE_LEFT);
        R_hb.getChildren().add(R_button);
        R_button.setOnAction(gen_view);
        grid.add(R_hb, 0, 9);
        
        //Add all text labels
        grid.add(this.player1_ID_label, 0, 2, 1, 1);
        grid.add(this.player2_ID_label, 0, 3, 1, 1);
        grid.add(this.player1_Bet_label, 0, 4, 1, 1);
        grid.add(this.player2_Bet_label, 0, 5, 1, 1);
        grid.add(this.player1_number_label, 0, 6, 1, 1);
        grid.add(this.player2_number_label, 0, 7, 1, 1);
        grid.add(this.winner_label, 0, 8, 1, 1);
        
		return new Scene(grid, WIDTH, HEIGHT);
	}
	
	/**
	 * Creates new instance of Player view
	 * 
	 * @return player view scene
	 */
	private Scene playerView(){
		//Creates grid container object for screen elements 
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(50, 10, 35, 35));
        
        //Create new player instance
        p = new Player(this, u);
        p.start();
        
        checkTransactions();
        check_history();
        
        try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {}
        
        //verify that banker has a game started, if not return null
        if(clientStartError){
        	return null;
        }
        
        //Bet input field
        TextField betField = new TextField();
        grid.add(betField, 0, 3);
                
        EventHandler<ActionEvent> gen_view = new EventHandler<ActionEvent>() {
       	 	
        	/**
        	 * Button event handler for player view buttons
        	 */
            @Override
            public void handle(ActionEvent e){
            	Button in = (Button)e.getSource();
            	Random rand = new Random();
            	CRC32 c = new CRC32();
            	
            	//Player may leave game only if banker has ended the game or reset
            	if((in.getText().equals("Leave Game") && ended)){
            		clear_labels();
            		generated = false;
            		ended = false;
            		bet_placed = false;
            		number = -1;
            		
            		Scene main = mainMenu(u);
                    stage.setScene(main);
                    stage.show();
            	}
            	
            	//Player may generate number to use for game only once
            	if(in.getText().equals("Generate Number")){
            		if(!generated){
            			//Generates a value modulated by 100000
            			number = Math.abs(rand.nextLong())%100000;
            			NumberLabel.setText("Generated Number: " + number);
            			
            			//Use CRC to modify original value
            			c.update(longToBytes(number));
            			hash = c.getValue();
            			HashLabel.setText("Generated Hash: " + hash);
            			generated = true;
            		}else{
            			alert_msg.setFill(Color.FIREBRICK);
            			alert_msg.setText("Number is only generated once per round");
            		}
            	}
            	
            	//Place Bet sends bet value and hash from generated value to banker
            	if(in.getText().equals("Place Bet") && !bet_placed){
            		if(generated && !ended){
            			//verify that bet is numeric
            			double bet;
            			try{
            				bet = Double.parseDouble(betField.getText());
            			}catch(NumberFormatException nfe){
            				bet = -1;
            				alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText("Bet must be number");
            			}
            			
            			//Get updated balance
						check_balance();
					
						//verify bet is nonzero and less than user balance
            			if(bet > 0 && bet < balanceValue){
            				sendTransaction(bet, hash, BankerID);
                			bet_placed = true;
                			alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText("Waiting for banker to declare winner");
                			
            			}else{
            				alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText("Bet must be greater than 0 and less than available balance");
            			}
            		}else{
            			if(ended){
            				alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText("Game has been ended no bets may be placed");
            			}else{
            				alert_msg.setFill(Color.FIREBRICK);
                			alert_msg.setText("Must Generate Number before placing bet");
            			}
            		}
            	}
            	
            	//Get previous game history from banker
            	if(in.getText().equals("Game History")){
            		gameHistory.setText(gHist);
            	}
            }
        };
        
        //Leave Button
        Button E_button = new Button("Leave Game");
        HBox E_hb = new HBox(10);
        E_hb.setAlignment(Pos.BASELINE_LEFT);
        E_hb.getChildren().add(E_button);
        E_button.setOnAction(gen_view);
        grid.add(E_hb, 0, 1, 1, 1);
        
        //Generate Number Button
        Button Gen_button = new Button("Generate Number");
        HBox Gen_hb = new HBox(10);
        Gen_hb.setAlignment(Pos.BASELINE_LEFT);
        Gen_hb.getChildren().add(Gen_button);
        Gen_button.setOnAction(gen_view);
        grid.add(Gen_hb, 0, 2, 1, 1);
        
        //Place bet button
        Button Bet_button = new Button("Place Bet");
        HBox Bet_hb = new HBox(10);
        Bet_hb.setAlignment(Pos.BASELINE_LEFT);
        Bet_hb.getChildren().add(Bet_button);
        Bet_button.setOnAction(gen_view);
        grid.add(Bet_hb, 1, 3, 1, 1);
        
        //Game History button
        Button H_button = new Button("Game History");
        HBox h_hb = new HBox(10);
        h_hb.setAlignment(Pos.BASELINE_LEFT);
        h_hb.getChildren().add(H_button);
        H_button.setOnAction(gen_view);
        grid.add(h_hb, 0, 4, 1, 1);
        
        //Add all Text elements to grid
        grid.add(NumberLabel, 0, 5, 1, 1);
        grid.add(HashLabel, 0, 6, 1, 1);
        grid.add(alert_msg, 0, 9);
        grid.add(otherPlayer_label, 0, 7);
        grid.add(winner_label, 0, 8, 1, 1);
        grid.add(gameHistory, 0, 10);
        
        
		return new Scene(grid, WIDTH, HEIGHT);
	}
	
	/**
	 * Gets balance for current user and updates the balance global text and double
	 */
	private void check_balance(){
		AccountResponse account = null;
		
		//Try to get account balance, if failed then notify user
		try {
			account = server.accounts().account(u.pair);
		} catch (IOException e) {
			this.AccountBalance.setText("Balance Unavailiable");
		}
		Balance[] balance = account.getBalances(); 
		this.balanceValue = Double.parseDouble(balance[0].getBalance());
		
		this.AccountBalance.setText("Balance: " + balance[0].getBalance() + " XLM");
	}
	
	/**
	 * Begin payment stream from test server
	 */
	private void check_history(){
		PaymentsRequestBuilder paymentsRequest = this.server.payments().forAccount(u.pair);
		paymentsRequest.stream(stream);
	}
	
	/**
	 * Event handler for payment screen, pulls all transactions every time 
	 * a transaction occurs.
	 */
	public EventListener stream = new EventListener<OperationResponse>() {

		@Override
		public void onEvent(OperationResponse payment) {
			
			//For each transaction instance, parse and add to history text area
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
			      String from = ((PaymentOperationResponse) payment).getFrom().getAccountId();
			      
			      if(u.AccountID.equals(from)){
			    	  output.append(" Sent");
			      }else{
			    	  output.append(" from ");
			    	  output.append(from);
			      }
			      //If bankers screen, populate amount for players bets
			      if(isBankersScreen){
			    	  if(bets.get(0).AccountID.equals(from)){
			    		  bets.get(0).amount = amount;
			    	  }else if(bets.get(1).AccountID.equals(from)){
			    		  bets.get(1).amount = amount;
			    	  }
			    	  //If both bets are fully populated then update bets received, mark transaction time, and send acks to players
			    	  if(bets.get(0).isReady() && bets.get(1).isReady()){
			    		  alert_msg.setFill(Color.FIREBRICK);
			    		  alert_msg.setText("Both Bets Received");
			    		  player1_Bet_label.setText("Player 1 Bet and Hash: " + bets.get(0).amount + " " + bets.get(0).memo);
			    		  player2_Bet_label.setText("Player 2 Bet and Hash: " + bets.get(1).amount + " " + bets.get(1).memo);
			    		  betsRecv = true;
			    		  
			    		  transactionTime = new Date();
			    		  b.sendAcks();
			    	  }
			      }
			      
			      //Remove duplicates
			      if(!out.contains(output.toString())){
			    	  out = out + output.toString() + "\n";
				      history.setText(out); 
			      }
			      //update balance
			      check_balance();
			}
		}
	};
	
	/**
	 * Open Transaction stream
	 */
	private void checkTransactions(){
		TransactionsRequestBuilder response = server.transactions().forAccount(this.u.pair);
		response.stream(TStream);
	}
	
	/**
	 * Transaction stream for collecting memos from received transactions
	 */
	public EventListener TStream = new EventListener<TransactionResponse>() {

		@Override
		public void onEvent(TransactionResponse transaction) {
			
			if(transaction instanceof TransactionResponse) {

			    String memo = ((MemoText) ((TransactionResponse) transaction).getMemo()).getText();
			    KeyPair src = ((TransactionResponse) transaction).getSourceAccount();
			    
			    //Populate bet objects with memos from transaction stream
			    if(src.getAccountId().equals(bets.get(0).AccountID)){
			    	bets.get(0).memo = memo;
			    }else if(src.getAccountId().equals(bets.get(1).AccountID)){
			    	bets.get(1).memo = memo;
			    }
			}
		}
	};
	
	//Sets all label texts to default
	public void clear_labels(){
		AccountBalance = new Text("Balance:");
		alert_msg = new Text();
		NumberLabel = new Text("Generated Number:");
		HashLabel = new Text("Generated Hash: ");
		player1_Bet_label = new Text("Player 1 Bet and Hash:");
		player2_Bet_label = new Text("Player 2 Bet and Hash:");
		player1_number_label = new Text("Player 1 True Number:");
		player2_number_label = new Text("Player 2 True Number:");
		otherPlayer_label = new Text("Other Player Value:");
		winner_label = new Text("Winner is ");
		player1_ID_label = new Text("Player 1 ID:");
		player2_ID_label = new Text("Player 2 ID:");
	}
	
	/**
	 * Records previous game data for banker
	 */
	private void save_game_data(){
		this.gameHistory.setText("Time: " + transactionTime.toString() + "--"
				+ player1_ID_label.getText() + "--"
				+ player1_number_label.getText() + "--" 
				+ player1_Bet_label.getText() + "--"
				+ player2_ID_label.getText() + "--"
				+ player2_number_label.getText() + "--" 
				+ player2_Bet_label.getText() + "--"
				+ winner_label.getText());
	}
	
	/**
	 * Getter for the server thread to send game data to the player client thread
	 * @return game history text
	 */
	public String getGameHistory(){
		return gameHistory.getText();
	}
	
	/**
	 * Sets the global gHist string to the received game history
	 * @param in banker's game history
	 */
	public void setGameHistory(String in){
		this.gHist = in.replace("--", "\n");
	}
	
	/**
	 * Setter for threads to set alert msg
	 * @param msg Text to set alert msg to
	 */
	public void setWaitMsg(String msg) {
		this.alert_msg.setText(msg);
	}
	
	/**
	 * Set the game status to ended
	 * @param ended value that is almost always false
	 * @param msg Message to alert the user
	 */
	public void setEnded(boolean ended, String msg) {
		this.ended = ended;
		alert_msg.setFill(Color.FIREBRICK);
		this.alert_msg.setText(msg);
	}
	
	/**
	 * Set game running variable. This is used by server thread to show that 
	 * there is already a server running for banker
	 * @param running is server running
	 */
	public void setGameRunning(boolean running) {
		this.running = running;
		if(running){
			alert_msg.setFill(Color.FIREBRICK);
			this.alert_msg.setText("Game Already In Progress");
			this.b.kill();
		}
	}
	
	/**
	 * For Server thread, set the player ids for client players
	 * @param ID Account id of player 
	 * @param playerNum thread id of client thread
	 */
	public void setPlayerID(String ID, int playerNum) {
		if(playerNum == 0){
			this.bets.get(0).AccountID = ID;
			this.player1_ID_label.setText(this.player1_ID_label.getText() + " " + ID);
		}else{
			this.bets.get(1).AccountID = ID;
			this.player2_ID_label.setText(this.player2_ID_label.getText() + " " + ID);
		}
	}
	
	/**
	 * For server thread, set player true value for given client player
	 * @param number true va ue received from client
	 * @param playerNum Client thread id
	 */
	public void setPlayerNumber(String number, int playerNum) {
		if(playerNum == 0){
			this.bets.get(0).Number = number;
			this.player1_number_label.setText(this.player1_number_label.getText() + " " + number);
		}else{
			this.bets.get(1).Number = number;
			this.player2_number_label.setText(this.player2_number_label.getText() + " " + number);
		}
	}
	
	/**
	 * Set the banker account id for the server banker account
	 * @param ID account id of banker
	 */
	public void setBankerID(String ID) {
		this.BankerID = ID;
	}
	
	/**
	 * Error for client creation when no server exists
	 * @param clientStartError thrown when client is created without server
	 */
	public void setclientStartError(boolean clientStartError){
		this.clientStartError = clientStartError;
	}
	
	/**
	 * Getter for player client threads to get player number to send to server
	 * @return number to send to server
	 */
	public String getNumber(){
		return Long.toString(this.number);
	}
	
	/**
	 * Set winner ID label, depending on winner account ID
	 * @param winnerID Account ID of winning account
	 */
	public void setWinnerID(String winnerID){
		if(winnerID.equals(u.AccountID)){
			winner_label.setText("You Win!");
		}else{
			winner_label.setText("You Lose :(");
		}
	}
	
	/**
	 * Get other player number, used by banker server thread to send clients
	 * each others number
	 * @param threadID thread to send to
	 * @return number of other client
	 */
	public String getOtherPlayerNumber(int threadID){
		if(threadID == 0){
			return bets.get(1).Number;
		}else{
			return bets.get(0).Number;
		}
		
	}
	
	/**
	 * Setter for client thread to set the recieved other player's number
	 * @param number
	 */
	public void setOtherPlayerNumber(String number){
		this.otherPlayer_label.setText(this.otherPlayer_label.getText() + " " + number);
	}
	
	/**
	 * Convert long to byte array for CRC code generation
	 * @param x long to convert
	 * @return byter array representing long
	 */
	private byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	/**
	 * Converts the byte array back to long value
	 * @param bytes byte array to convert
	 * @return the converted long
	 */
	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	/**
	 * Send transaction to specified account
	 * @param bet amount of money to send in lumens
	 * @param hash optional hash memo for game 
	 * @param id account id to send to
	 */
	private void sendTransaction(double bet, long hash, String id){
		//Banker or destination account 
		KeyPair banker = KeyPair.fromAccountId(id);
		
		//Verify account exists
		try{
			server.accounts().account(banker);
		}catch(Exception e){
			System.out.println("Account Does not Exist");
		}
		
		//verify source account exists
		AccountResponse sourceAccount = null;
		try {
			sourceAccount = server.accounts().account(this.u.pair);
		} catch (IOException e) {
			System.out.println("User Account Does not Exist");
		}
		
		//Create transaction with input parameters
		Transaction transaction = new Transaction.Builder(sourceAccount)
		        .addOperation(new PaymentOperation.Builder(banker, new AssetTypeNative(), Double.toString(bet)).build())
		        .addMemo(Memo.text(Long.toString(hash)))
		        .build();
		//sign the transaction with user keypair
		transaction.sign(this.u.pair);
		
		//Verify that the transaction was sent properly
		try{
		  SubmitTransactionResponse response = server.submitTransaction(transaction);
		  System.out.println("Success!");
		}catch (Exception e) {
		  System.out.println("Something went wrong!");
		  System.out.println(e.getMessage());
		}
	}
}
