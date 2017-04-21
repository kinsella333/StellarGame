package test;

import java.net.*;
import java.io.*;
import java.util.*;

import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

public class Test_Account_Creation {

	public static void main(String[] args) throws IOException {
		/*KeyPair pair = KeyPair.random();

		System.out.println(new String(pair.getSecretSeed()));
		System.out.println(pair.getAccountId());
		
		String friendbotUrl = String.format(
		  "https://horizon-testnet.stellar.org/friendbot?addr=%s",
		  pair.getAccountId());
		InputStream response = new URL(friendbotUrl).openStream();
		
		String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
		System.out.println("SUCCESS! You have a new account :)\n" + body);

		//Server server = new Server("https://horizon-testnet.stellar.org");
		AccountResponse account = server.accounts().account(pair);
		System.out.println("Balances for account " + pair.getAccountId());
		for (AccountResponse.Balance balance : account.getBalances()) {
		  System.out.println(String.format(
		    "Type: %s, Code: %s, Balance: %s",
		    balance.getAssetType(),
		    balance.getAssetCode(),
		    balance.getBalance()));
		}*/
		
		KeyPair pair1 = KeyPair.fromSecretSeed("SCZANGBA5YHTNYVVV4C3U252E2B6P6F5T3U6MM63WBSBZATAQI3EBTQ4");
		KeyPair pair2 = KeyPair.fromAccountId("GDFJ57WKI3X2CU33FMBWF62PVDVDSM4EUHL2VS2PQFA5H7PAX2OB3PU2");
		
		/*String friendbotUrl1 = String.format(
				  "https://horizon-testnet.stellar.org/friendbot?addr=%s",
				  pair1.getAccountId());
		InputStream response1 = new URL(friendbotUrl1).openStream();
		*/
		/*String friendbotUrl2 = String.format(
				  "https://horizon-testnet.stellar.org/friendbot?addr=%s",
				  pair2.getAccountId());
		InputStream response2 = new URL(friendbotUrl2).openStream();*/
		
		
		Network.useTestNetwork();
		Server server = new Server("https://horizon-testnet.stellar.org");
		//KeyPair source = KeyPair.fromSecretSeed("SCZANGBA5YHTNYVVV4C3U252E2B6P6F5T3U6MM63WBSBZATAQI3EBTQ4");
		//KeyPair destination = KeyPair.fromAccountId("GA2C5RFPE6GCKMY3US5PAB6UZLKIGSPIUKSLRB6Q723BM2OARMDUYEJ5");

		// First, check to make sure that the destination account exists.
		// You could skip this, but if the account does not exist, you will be charged
		// the transaction fee when the transaction fails.
		// It will throw HttpResponseException if account does not exist or there was another error.
		server.accounts().account(pair2);

		// If there was no error, load up-to-date information on your account.
		AccountResponse sourceAccount = server.accounts().account(pair1);

		// Start building the transaction.
		Transaction transaction = new Transaction.Builder(sourceAccount)
		        .addOperation(new PaymentOperation.Builder(pair2, new AssetTypeNative(), "10").build())
		        // A memo allows you to add your own metadata to a transaction. It's
		        // optional and does not affect how Stellar treats the transaction.
		        .addMemo(Memo.text("Test Transaction"))
		        .build();
		// Sign the transaction to prove you are actually the person sending it.
		transaction.sign(pair1);

		// And finally, send it off to Stellar!
		try {
		  SubmitTransactionResponse response = server.submitTransaction(transaction);
		  System.out.println("Success!");
		  //System.out.println(response);
		} catch (Exception e) {
		  System.out.println("Something went wrong!");
		  System.out.println(e.getMessage());
		}
		
		AccountResponse account1 = server.accounts().account(pair1);
		System.out.println("Balances for account " + pair1.getAccountId());
		for (AccountResponse.Balance balance : account1.getBalances()) {
		  System.out.println(String.format(
		    "Type: %s, Code: %s, Balance: %s",
		    balance.getAssetType(),
		    balance.getAssetCode(),
		    balance.getBalance()));
		}
		System.out.println();
		
		AccountResponse account2 = server.accounts().account(pair2);
		System.out.println("Balances for account " + pair2.getAccountId());
		for (AccountResponse.Balance balance : account2.getBalances()) {
		  System.out.println(String.format(
		    "Type: %s, Code: %s, Balance: %s",
		    balance.getAssetType(),
		    balance.getAssetCode(),
		    balance.getBalance()));
		}

	}

}
