package proj3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.stellar.sdk.KeyPair;

public class User {
	public String AccountID;
	public KeyPair pair;
	
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
	
	public User(String accountID){
		this.pair = KeyPair.fromAccountId(accountID);
		this.AccountID = accountID;
	}
	
	public boolean compareTo(User other) {
	    return (other.AccountID).equals(this.AccountID);
	}
}
