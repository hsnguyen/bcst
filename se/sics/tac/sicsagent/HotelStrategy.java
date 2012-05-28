package se.sics.tac.sicsagent;

import se.sics.tac.aw.*;
import java.util.logging.*;

public class HotelStrategy extends AgentStrategy {
    final int TYPE_CHEAP = 0;
    final int TYPE_GOOD = 1;
    final float[] INC_N_W = {1.15f,1.25f,1.25f,1.15f,1.15f,1.25f,1.25f,1.15f};
  
    Logger log;
    float[] incPriceW;
    float prices[];
    float[] startPrice;
    float[] oldAskPrice;
    float[] convPrice;
    float bidMargin = 1.2f;
    boolean gameRunning = false;

    public HotelStrategy() {
	log = Logger.getLogger("se.sics.tac.sicsagent.HotelStrategy");
	prices = new float[8];  	
	oldAskPrice = new float[8];  
	startPrice = new float[8];
	startPrice[0] = 30;
	startPrice[1] = 100;
	startPrice[2] = 100;
	startPrice[3] = 40;
	startPrice[4] = 95;
	startPrice[5] = 160;
	startPrice[6] = 155;
	startPrice[7] = 110;
	incPriceW = new float[8];
	incPriceW[0] = (float) 0.5;
	incPriceW[1] = (float) 0.8;
	incPriceW[2] = (float) 0.8;
	incPriceW[3] = (float) 0.5;
	incPriceW[4] = (float) 0.7;
	incPriceW[5] = (float) 0.8;
	incPriceW[6] = (float) 1.0;
	incPriceW[7] = (float) 0.7;	
    }
    
    /*
     * - there are TACAgent have received an answer on a bid query/submission
     * (new information about the bid is available)
     */
    public void bidUpdated(Bid bid) {
    }
    
    /*
     * - the bid has been rejected (reason is bid.getRejectReason())
     */
    public void bidRejected(Bid bid) {
    }
    
    /*
     * - the bid contained errors (error represent error status - commandStatus)
     */
    public void bidError(Bid bid, int error) {
    }
    
    
    /*
     * - new information about the quotes on the auction (quote.getAuction())
     * has arrived
     */
    public void quoteUpdated(Quote quote) {
    }
    
    public void quoteUpdated(int cat) {
	if (cat == TACAgent.CAT_HOTEL) {
	    Quote quote;
	    int hqw;
	    float askPrice;
	    float newPrice;
	    boolean solve = false;
	    boolean forbidSolve = false;
	    for (int a = 8; a < 16; a++) {
		quote = agent.getQuote(a);
		if (quote.getAuctionStatus() != Quote.AUCTION_CLOSED) {
		    hqw = quote.getHQW();
		    askPrice = getAskPrice(quote,a);
		    newPrice = newPrice(askPrice, a);
		    setPrice(a,newPrice);
		    if ((hqw < agent.getAllocation(a)) || lowBidMargin(askPrice, quote.getBid(),a)) {
			solve = true;
		    }
		    oldAskPrice[a-8] = askPrice;
		} else {
		    forbidSolve = true;
		}
	    }
	    if (solve && gameRunning && !forbidSolve) {
		agent.solveRequest(Solver.PRIO_HIGH, 20000, Solver.TYPE_NORMAL);
	    } 		
	}
    }

    private float getAskPrice(Quote quote, int a) {
	Quote otherQuote;
	if (a < 12) 
	    otherQuote = agent.getQuote(a+4);
	else
	    otherQuote = agent.getQuote(a-4);
	if (otherQuote.getAuctionStatus() == Quote.AUCTION_CLOSED) 
	    return Math.max(quote.getAskPrice(), otherQuote.getBidPrice()*(720000-agent.getGameTime())/480000);
	else
	    return quote.getAskPrice();
    }

    private float newPrice(float askPrice, int auction) {
	float newPrice = askPrice * getIncPriceW(auction);

	return newPrice;
    }
    
    private float getIncPriceW(int a) {
	float gameMinute = agent.getGameTime()/60000;    
	float inc = 1 + (float)Math.max(incPriceW[a-8] * sigmoid(gameMinute-8f),0.2);    
	log.log(Level.FINE, "IncWeight for auction " + a + " = " + inc);
	return inc;
    }
    
    private double sigmoid(double x) {
	return 1-1/(1+Math.exp(-x));
    }
    

    private boolean lowBidMargin(float askPrice, Bid bid, int auction) {
	if (bid == null)
	    return false;
	
	float oldBidPrice = bid.getPrice(0);
	double margin = oldBidPrice / askPrice;
	
	if (margin < (1 + (incPriceW[auction-8] - 1)/2)) 
	    return true;
	else
	    return false;
    }
    
    /*
     * - the auction with id "auction" has closed
     */
    public void auctionClosed(int auction) {
	if (agent.getAuctionCategory(auction) == TACAgent.CAT_HOTEL) {
	    setPrice(auction, (-1));
	    //if (agent.getAllocation(auction) > agent.getOwn(auction)) {
		agent.solveRequest(Solver.PRIO_HIGH, 20000, Solver.TYPE_NORMAL);
		//} 
	}
    }
    
    /*
     * - there has been a transaction
     */
    public void transaction(Transaction transaction) {
    }
    
    /*
     * - a TAC game has started, and all information about the
     * game is available (preferences etc).
     */
    public void gameStarted() {    
	Bid bid;
	
	for (int a = 8; a < 16; a++) {
	    setPrice(a, startPrice[a-8]);
	    oldAskPrice[a-8] = 0;
	}
	gameRunning = true;
    }

    /*
     * - the game is over
     */
    public void gameStopped() {
	gameRunning = false;
    }
    
    /*
     * - strategy is asked to update the priceinformation to the solver
     */
    public void setSolverPrice(SolverSession session) {
	Quote quote;
	Bid bid;
	float oldBidPrice;
	float newUnitPrice;
	float price;
	int hqw;

	for (int a = 8; a < 16; a++) {
	    quote = agent.getQuote(a);
	    if (quote.getAuctionStatus() == Quote.AUCTION_CLOSED) {
		for (int n = 0; n < 9; n++) {
		    if (n <= agent.getOwn(a)) {
			session.setPrice(a,n,0);
			session.setBidPrice(a,n,0);
		    } else {
			session.setPrice(a,n,-1);
			session.setBidPrice(a,n,-1);		    
		    }
		}
	    } else {
		hqw = Math.max(0,quote.getHQW());
		log.fine("Solver prices for: " + a + " hqw = " + hqw);
		for (int n = 0; n < 9; n++) {
		    session.setBidPrice(a, n, adjustBidPrice(getPrice(a),n,a));
		    if (n <= hqw) {
		      price = n * priceOnProbablyOwned(a,hqw,n,getPrice(a));
		    } else {
		      price = hqw * priceOnProbablyOwned(a,hqw,hqw,getPrice(a)) + 
			  (n-hqw) * priceOnNotOwned(n,getPrice(a),a);
		    }
		    session.setPrice(a,n, price);
		}
	    }
	}
    }

    private float priceOnProbablyOwned(int a, int hqw, int n, float unitPrice) {
	//return unitPrice - oldAskPrice[a-8];
	return adjustPrice(unitPrice,n,a) - adjustPrice(0,n,a);
	// * Math.min(1, (askInc-1)/(getIncPriceW(a)-1));
    }
    
    private float priceOnNotOwned(int n, float unitPrice, int a) {
	return adjustPrice(unitPrice,n,a);
    }
    
    private float adjustPrice(float price, int n, int a) {
	if (n <= 0)
	    return 0;
	else 
	    return Math.max(price, startPrice[a-8] * (float)Math.pow(INC_N_W[a-8],n-1));
    }

    private float adjustBidPrice(float price, int n, int a) {
	if (n <= 0)
	    return 0;
	else
	    return bidMargin * Math.max(price, startPrice[a-8] * (float)Math.pow(INC_N_W[a-8],n));
    }
    
    /*
     * - the solver has come up with a new result
     */
    public void solverReport(SolverSession session) {
	if (session.isOptimal() || (session.getTime() > 100) || (session.getNumOfResponses() > 1)) {
	    switch (session.getType()) {
	    case Solver.TYPE_NORMAL:
		normalSolverReport(session);
		break;
	    }
	} else {
	    agent.solveRequest(Solver.PRIO_LOW, 25000, Solver.TYPE_NORMAL);
	}
    }
    
    private void normalSolverReport(SolverSession session) {
	int alloc;
	float bidPrice;
	Quote quote;
	Bid bid;
	Bid oldBid;
	
	for (int a = 8; a < 16; a++) {
	    agent.setAllocation(a, session.getAllocation(a));
	    alloc = agent.getAllocation(a) - agent.getOwn(a);
	    if ((alloc >= 0) && (agent.getGameTime() > 180000)) {
		bidPrice = session.getBidPrice(a,alloc);
		if (bidPrice >= 0) {
		    bid = new Bid(a);
		    if (alloc > 0) {
			if (alloc > 1)
			    bid.addBidPoint(alloc-1, bidPrice);
			bid.addBidPoint(1, 999);
		    }
		    oldBid = agent.getBid(a);
		    if (bidDiffers(bid,oldBid, alloc)) {
		      log.finest("Time: " + (agent.getGameTime()/1000)
				 + " Attempting bid: Hotel, Auction: " + a
				 + " Price: " + bidPrice);
		      if (oldBid == null) {
			log.finest("Time: " + agent.getGameTime()/1000
				   + " New bid: Hotel, Auction: " + a + 
				   " Price: " + bidPrice);
			agent.submitBid(bid);
		      } else if (!oldBid.isPreliminary()) {
			log.finest("Time: " + agent.getGameTime()/1000
				   + " Replace bid: Hotel, Auction: " + a + 
				   " Price: " + bidPrice);
			agent.replaceBid(oldBid, adjustBid(bid));
		      } else {
			log.finest("time: " + agent.getGameTime()/1000
				   + " Bid still preliminary, Auction: " + a +
				   " Id: " + oldBid.getID() + " State "
				   + oldBid.getProcessingStateAsString());
		      }
		    }
		} else {
		  log.severe("Negative price detected for auction "
			     + TACAgent.getAuctionTypeAsString(a)
			     + ": " + bidPrice);
		}
	    }
	}    
    }
    
  private boolean bidDiffers(Bid newBid, Bid oldBid, int alloc) {
    if (oldBid == null) {
      return alloc > 0;
    }

    if (newBid.getNoBidPoints() > 0) {
      if (oldBid.getNoBidPoints() == 0) {
	return true;
      }
      return (newBid.getPrice(0) > oldBid.getPrice(0)) ||
	  (newBid.getQuantity(0) != oldBid.getQuantity(0));
    } else {
      return oldBid.getNoBidPoints() > 0;
    }
  }

    private Bid adjustBid(Bid bid) {
	int auction = bid.getAuction();
	int diff = agent.getProbablyOwn(auction) - bid.getQuantity();
	if (diff > 0) {
	    bid.addBidPoint(diff, agent.getQuote(auction).getAskPrice() + 1);
	}
	return bid;
    }

    public float getPrice(int a) {
	return prices[a-8];
    }

    public void setPrice(int a, float price) {
	prices[a-8] = price;
    }

    public static void main(String[] args) {
	int hqw = 0;
	float price = 0;
	int a = 9;
	HotelStrategy strat = new HotelStrategy(); 
	strat.setPrice(a, 0);
	strat.oldAskPrice[a-8] = 142;
	strat.gameStarted();
	for (int n = 0; n < 9; n++) {
	    /*if (n <= hqw) {
		price = n * strat.priceOnProbablyOwned(a,hqw,n,strat.getPrice(a));
		} else {*/
	    // price = //hqw * strat.priceOnProbablyOwned(a,hqw,hqw,strat.getPrice(a)) + 
		price = n * strat.priceOnNotOwned(n,strat.getPrice(a),a);
	
	//   price = n*strat.adjustPrice(0,n,a);
	    System.out.println("getPrice " + n + " = " + strat.getPrice(a));	   
	    System.out.println("Price " + n + " = " + price);
	}
    }

}






