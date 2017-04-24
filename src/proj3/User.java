package proj3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.stellar.sdk.KeyPair;

/**
 * User object which holds user account information
 * 
 * @author ray
 *
 */
public class User {
	public String AccountID;
	public KeyPair pair;
	
	/**
	 * Generates a new user via random seed and 
	 * initializes it with the friendbot account on the stellar test server
	 */
	public User(){
		this.pair = KeyPair.random();
		this.AccountID = pair.getAccountId();
		
		String friendbotUrl = String.format(
				  "https://horizon-testnet.stellar.org/friendbot?addr=%s",
				  this.pair.getAccountId());
		try {
			InputStream response = new URL(friendbotUrl).openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates new user object from the provided account ID
	 * @param accountID account ID of user to login
	 */
	public User(String accountID){
		this.pair = KeyPair.fromAccountId(accountID);
		this.AccountID = accountID;
	}
}
