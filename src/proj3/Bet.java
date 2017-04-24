package proj3;

/**
 * Bet class serves as a container for a player bet sent to the banker.
 * 
 * @author ray
 *
 */
public class Bet {
	public String amount, AccountID, memo, Number;
	
	/**
	 * Creates a blank bet object containing error values
	 */
	public Bet(){
		this.amount = "-1";
		this.AccountID = "NONE";
		this.memo = "-1";
		this.Number = "-1";
	}
	
	/**
	 * Verifies all non-optional fields have been populated
	 * 
	 * @return false if no ready and true if it is
	 */
	public boolean isReady(){
		if(this.amount.equals("-1") || this.AccountID.equals("NONE") || this.memo.equals("-1")){
			return false;
		}
		return true;
	}
}
