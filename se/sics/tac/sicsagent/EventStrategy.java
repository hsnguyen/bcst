package se.sics.tac.sicsagent;
import se.sics.tac.sicsagent.*;
import se.sics.tac.aw.*;
import java.util.logging.*;

public class EventStrategy extends AgentStrategy {
    private final int SELL = 0;
    private final int BUY = 1;    
    private final double INC_N_W = 1.05;
    private final double DEC_N_W = 0.95;
    
    private Logger log;
    private float[][] prices;
    
    public EventStrategy() {
	prices = new float[12][2]; //event, day, sell/buy
	log = Logger.getLogger("se.sics.tac.sicsagent.EventStrategy");
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
	log.log(Level.FINE, "Bid rejected " + bid.getAuction() + " " + bid.getBidString());
    }
    
    /*
     * - the bid contained errors (error represent error status - commandStatus)
     */
    public void bidError(Bid bid, int error) {
	log.log(Level.FINE, "Bid error " + bid.getAuction() + " " + bid.getBidString());
    }
    
    
    /*
     * - new information about the quotes on the auction (quote.getAuction())
     * has arrived
     */
    public void quoteUpdated(Quote quote) {
	int a = quote.getAuction();
	double time;
	Bid bid;
	Bid oldBid;
	int alloc;
	
	if ((agent.getAuctionCategory(a) == TACAgent.CAT_ENTERTAINMENT) && (agent.getAllocation(a) >= 0)) {
	    /*int buyPrice = (int) quote.getBidPrice();
	    int sellPrice = (int) quote.getAskPrice();
	    if (buyPrice >= 0)
		setPrice(a, SELL, buyPrice-5);
	    if (sellPrice >= 0)
		setPrice(a, BUY, sellPrice+5);
	    */
	    setPrice(a, SELL, 80);
	    setPrice(a, BUY, 100);
	    alloc = agent.getAllocation(a) - agent.getOwn(a);
	    log.log(Level.FINE, "Quote for entertainment " + a + " alloc " + alloc);
	    if (alloc > 0) {
		oldBid = agent.getBid(a);
		if (oldBid == null) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc,getBuyPrice());

		    agent.submitBid(bid);
		} else if (!oldBid.isPreliminary()) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc,getBuyPrice());
		    agent.replaceBid(oldBid,bid);	      
		}
	    } else if (alloc < 0) {
		oldBid = agent.getBid(a);
		if (oldBid == null) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc,getSellPrice());
		    agent.submitBid(bid);
		} else if (!oldBid.isPreliminary()) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc,getSellPrice());
		    agent.replaceBid(oldBid,bid);	      
		}
	    } 
	}
    }

    public float getBuyPrice() {
	long time = agent.getGameTime();
	return (float) (50 + (70 - 50 * Math.random()) * time / 720000);
    }

    public float getSellPrice() {
	long time = agent.getGameTime();
	return (float) (50 + (150 - 100 * Math.random()) * (720000-time) / 720000);
    }

    public void quoteUpdated(int cat) {
    }
    
    /*
     * - the auction with id "auction" has closed
     */
    public void auctionClosed(int auction) {
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
	for (int a = 16; a < 28; a++) {
	    setPrice(a, SELL, 80);
	    setPrice(a, BUY, 100);
	    agent.setAllocation(a, -1);
	}
    }
    
    /*
     * - the game is over
     */
    public void gameStopped() {
    }
    
    /*
     * - strategy is asked to update the priceinformation to the solver
     */
    public void setSolverPrice(SolverSession session) {
	for(int a = 16; a < 28; a++) {
	    int owns = agent.getOwn(a);
	    for (int n = 0; n < 9; n++) {
		if (n < owns)
		    session.setPrice(a, n, (int) (getPrice(a,SELL)*(n-owns)*Math.pow(DEC_N_W, owns-n)));
		else if (n == owns)
		    session.setPrice(a, n, 0);
		else
		    session.setPrice(a, n, (int) (getPrice(a,BUY)*(n-owns)*Math.pow(INC_N_W, n-owns)));
	    }
	}
    }
    
    /*
     * - the solver has come up with a new result
     */
    public void solverReport(SolverSession session) {
	if (session.isOptimal() || (session.getTime() > 100) || (session.getNumOfResponses() > 1))
	    switch (session.getType()) {
	    case Solver.TYPE_NORMAL:
		normalSolverReport(session);
		break;
	    }
    }
    
    private void normalSolverReport(SolverSession session) {
	int alloc;
	Quote quote;
	Bid bid;
	Bid oldBid;
	double time;
	
	for (int a = 16; a < 28; a++) {
	    agent.setAllocation(a, session.getAllocation(a));
	}    
    }
    
    private float getPrice(int auction, int trans) {
	return prices[auction-16][trans];
    }
    
    private void setPrice(int auction, int trans, float price) {
	prices[auction-16][trans] = price;
    }
}



