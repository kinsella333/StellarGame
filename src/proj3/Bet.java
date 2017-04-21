package proj3;

public class Bet {
	public String amount, AccountID, memo, Number;

	public Bet(String amount, String AccountID, String memo, String Number){
		this.amount = amount;
		this.AccountID = AccountID;
		this.memo = memo;
		this.Number = Number;
	}
	
	public Bet(){
		this.amount = "-1";
		this.AccountID = "NONE";
		this.memo = "-1";
		this.Number = "-1";
	}
	
	public boolean isReady(){
		if(this.amount.equals("-1") || this.AccountID.equals("NONE") || this.memo.equals("-1")){
			return false;
		}
		return true;
	}
}
