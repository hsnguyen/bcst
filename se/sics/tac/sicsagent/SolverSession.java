package se.sics.tac.sicsagent;
import se.sics.tac.aw.*;

public class SolverSession {
    private int utility;
    private int time = -1;
    private int[] allocation;
    private float[][][] prices;
    private int type;
    private int id;
    private int solverId;
    private String msg = null;
    private boolean isOptimal = false;
    private boolean result = false;
    private int responseCount = 0;
    
    public SolverSession(int type, int id, int solverId) {
	this.type = type;
	this.id = id;
	this.solverId = solverId;
	allocation = new int[28];
	prices = new float[28][2][9];
    }
    
    public void setPrice(int auction, int n, float price) {
	prices[auction][0][n] = price;
    }

    public int getPrice(int auction, int n) {
	return (int) prices[auction][0][n];
    }

    public void setBidPrice(int auction, int n, float price) {
	prices[auction][1][n] = price;
    }

    public float getBidPrice(int auction, int n) {
	return prices[auction][1][n];
    }
        
    public int  getNumOfResponses() {
	return responseCount;
    }

    public void setOptimal(boolean o) {
	isOptimal = o;
    }
    
    public boolean isOptimal() {
	return isOptimal;
    }

    public void setMsg(String msg) {
	responseCount++;
	this.msg = msg;
    }
    
    public String getMsg() {
	return msg;
    }
    
    public int getId() {
	return id;
    }
    
    public int getSolverId() {
	return solverId;
    }
    
    public int getTime() {
	return time;
    }
    
    public int getType() {
	return type;
    }
    
    public int getUtility() {
	return utility;
    }
    
    public int getAllocation(int auction) {
	return allocation[auction];
    }
    
    public String stringTo() {
	String str = "Utility: " + utility;
	str += "\nTime: " + time + "\n";
	for (int i = 0; i < 27; i++) {
	    str += allocation[i] + ",";
	}
	str += allocation[27] + "\n";
	
	return str;
    }
    
    public synchronized boolean generateResult() {
	int pos, start, end;
	String expr;
	String[] exprList;
	int[] inflight = new int[8];
	int[] outflight = new int[8];
	int auction;
	
	if ((msg == null) || (result))
	    return false;
	System.out.println("SOLVER_MSG=" + msg);
	result = true;

	pos = msg.indexOf("utility");
	start = msg.indexOf('(',pos);
	end = msg.indexOf(')',pos);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	utility = Integer.parseInt(exprList[0]);
	time = Integer.parseInt(exprList[1]);
	
	pos = msg.indexOf("flight_allocation");
	start = msg.indexOf('[',pos);
	end = msg.indexOf(']',pos);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    inflight[i] = Integer.parseInt(exprList[i]);
	    if (inflight[i] > 0) {
		auction = TACAgent.getAuctionFor(TACAgent.CAT_FLIGHT,
						 TACAgent.TYPE_INFLIGHT,
						 inflight[i]);
		allocation[auction]++;
	    }
	}
	start = msg.indexOf('[',end);
	end = msg.indexOf(']',start);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    outflight[i] = Integer.parseInt(exprList[i]);
	    if (outflight[i] > 1) {
		auction = TACAgent.getAuctionFor(TACAgent.CAT_FLIGHT,
						 TACAgent.TYPE_OUTFLIGHT,
						 outflight[i]);
		allocation[auction]++;
	    }
	}
	pos = msg.indexOf("hotel_allocation");
	start = msg.indexOf('[',pos);
	end = msg.indexOf(']',pos);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    if (exprList[i].equals("1")) {
		for (int day = inflight[i]; day < outflight[i]; day++) {
		    auction = TACAgent.getAuctionFor(TACAgent.CAT_HOTEL, 
						     TACAgent.TYPE_CHEAP_HOTEL,
						     day);
		    allocation[auction]++;
		}
	    }
	}
	start = msg.indexOf('[',end);
	end = msg.indexOf(']',start);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    if (exprList[i].equals("1")) {
		for (int day = inflight[i]; day < outflight[i]; day++) {
		    auction = TACAgent.getAuctionFor(TACAgent.CAT_HOTEL, 
						     TACAgent.TYPE_GOOD_HOTEL,
						     day);
		    allocation[auction]++;
		}
	    }
	}
	
	pos = msg.indexOf("event_allocation");
	start = msg.indexOf('[',pos);
	end = msg.indexOf(']',start);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    int day = Integer.parseInt(exprList[i]);
	    if (day > 0) {
		auction = TACAgent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, 
						 TACAgent.TYPE_ALLIGATOR_WRESTLING,
						 day);
		allocation[auction]++;
	    }
	}
	start = msg.indexOf('[',end);
	end = msg.indexOf(']',start);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    int day = Integer.parseInt(exprList[i]);
	    if (day > 0) {
		auction = TACAgent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, 
						 TACAgent.TYPE_AMUSEMENT,
						 day);
		allocation[auction]++;
	    }
	}
	start = msg.indexOf('[',end);
	end = msg.indexOf(']',start);
	expr = msg.substring(start+1,end);
	exprList = expr.split(",");
	for (int i = 0; i < 8; i++) {
	    int day = Integer.parseInt(exprList[i]);
	    if (day > 0) {
		auction = TACAgent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, 
						 TACAgent.TYPE_MUSEUM,
						 day);
		allocation[auction]++;
	    }
	}
	
	
	for (int i = 0; i < 28; i++) {
	    System.out.print(allocation[i] + ",");
	}
	
	return true;
    }
}






