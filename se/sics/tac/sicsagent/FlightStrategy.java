package se.sics.tac.sicsagent;
import se.sics.tac.sicsagent.*;
import se.sics.tac.aw.*;
import java.util.logging.*;

public class FlightStrategy extends AgentStrategy {
    float[] prices;
    Logger log;
    boolean flightLock;
    boolean firstQuote = true;
    
    public FlightStrategy() {
	prices = new float[8];
	log = Logger.getLogger("se.sics.tac.sicsagent.FlightStrategy");
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
	if (cat == TACAgent.CAT_FLIGHT) {
	    Quote quote;
	    float askPrice;
	    long t;
	    for (int a = 0; a < 8; a++) {
		quote = agent.getQuote(a);
		askPrice =  quote.getAskPrice();
		t = agent.getGameTime();
		setPrice(a, askPrice); //(float) (askPrice + (t/(12*60000))*(50-10)));
	    }
	    if (firstQuote) {
		firstQuote = false;
		agent.solveRequest(Solver.PRIO_HIGH, 20000, Solver.TYPE_NORMAL);			
	    }
	}	
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
	firstQuote = true;
	flightLock = true;
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
	int hasIn = 0;
	int hasOut = 0;
	int has;
	
	for(int a = 0; a < 8; a++) {
	    has = agent.getOwn(a);
	    for (int n = 0; n < 9; n++) {
		if (n <= has) 
		    session.setPrice(a, n, 0);		 
		else 
		    session.setPrice(a, n, getPrice(a)*(n-has));
		session.setBidPrice(a, n, getPrice(a));		
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
	    case Solver.TYPE_TEST_HIGH:
		highSolverReport(session);
		break;
	    }
    }
    
    private void normalSolverReport(SolverSession session) {
	int alloc = 0;
	Bid bid;
	Bid oldBid;

	for (int a = 0; a < 8; a++) {
	    log.log(Level.FINE, "Changing allocation for auction " + a + 
		    " from: " + agent.getAllocation(a) + 
		    " to: " + session.getAllocation(a));
	}
	  
	
	for (int a = 0; a < 8; a++) {
	    agent.setAllocation(a, session.getAllocation(a));
	}
	
	for (int a = 0; a < 8; a++) {
	    alloc = agent.getAllocation(a) - agent.getOwn(a) - agent.getProbablyOwn(a);
	    if ((alloc > 0)) {
		oldBid = agent.getBid(a);
		if (oldBid == null) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc, 1000);
		    agent.submitBid(bid);
		}/* else if (oldBid.getProcessingState() != Bid.TRANSACTED) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc, 1000);
		    agent.replaceBid(oldBid,bid);	      
		    }*/
	    } 
	}
    }

    private void highSolverReport(SolverSession session) {
	int alloc;
	Bid bid;
	Bid oldBid;
	
	for (int a = 0; a < 8; a++) {
	    alloc = Math.min(session.getAllocation(a),agent.getAllocation(a)) - agent.getOwn(a);
	    if ((alloc > 0)) {
		oldBid = agent.getBid(a);
		if (oldBid == null) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc, 1000);
		    agent.submitBid(bid);
		} else if (!oldBid.isPreliminary()) {
		    bid = new Bid(a);
		    bid.addBidPoint(alloc, 1000);
		    agent.replaceBid(oldBid,bid);	      
		}
	    } 
	}
    }
    
    private float getPrice(int auction) {
	return prices[auction];
    }
    
    private void setPrice(int auction, float price) {
	prices[auction] = price;
    }
}



