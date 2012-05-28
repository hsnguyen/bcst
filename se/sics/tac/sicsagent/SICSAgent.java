/**
 * TAC AgentWare
 * http://www.sics.se/tac        tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 23 April, 2002
 * Updated : $Date: 2003/01/08 17:04:56 $
 *	     $Revision: 1.23 $
 * ---------------------------------------------------------
 * DummyAgent is a simplest possible agent for TAC. It uses
 * the TACAgent agent ware to interact with the TAC server.
 *
 * Important methods in TACAgent:
 *
 * Retrieving information about the current Game
 * ---------------------------------------------
 * int getAuctionNo()
 *  - returns the number of auctions in TAC
 *
 * int getClientPreference(int client, int type)
 *  - returns the clients preference for the specified type
 *   (types are TACAgent.{ARRIVAL, DEPARTURE, HOTEL_VALUE, E1, E2, E3}
 *
 * int getAuctionFor(int category, int type, int day)
 *  - returns the auction-id for the requested resource
 *   (categories are TACAgent.{CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
 *    and types are TACAgent.TYPE_INFLIGHT, TACAgent.TYPE_OUTFLIGHT, etc)
 *
 * int getAuctionCategory(int auction)
 *  - returns the category for this auction (se above)
 *
 * int getOwn(int auction)
 *  - returns the number of items that the agent own for this
 *    auction
 *
 * Submitting Bids
 * ---------------------------------------------
 * void submitBid(Bid)
 *  - submits a bid to the tac server
 *
 * void replaceBid(OldBid, Bid)
 *  - replaces the old bid (the current active bid) in the tac server
 *
 *   Bids have the following important methods:
 *    - create a bid with new Bid(AuctionID)
 *
 *   void addBidPoint(int quantity, float price)
 *    - adds a bid point in the bid
 *
 * Help methods for remembering what to buy for each auction:
 * ----------------------------------------------------------
 * int getAllocation(int auctionID)
 *   - returns the allocation set for this auction
 * void setAllocation(int auctionID, int quantity)
 *   - set the allocation for this auction
 *
 *
 * Callbacks from the TACAgent (caused via interaction with server)
 *
 * bidUpdated(Bid bid)
 *  - there are TACAgent have received an answer on a bid query/submission
 *   (new information about the bid is available)
 * bidRejected(Bid bid)
 *  - the bid has been rejected (reason is bid.getRejectReason())
 * bidError(Bid bid, int error)
 *  - the bid contained errors (error represent error status - commandStatus)
 *
 * quoteUpdated(Quote quote)
 *  - new information about the quotes on the auction (quote.getAuction())
 *  has arrived
 *
 * auctionClosed(int auction)
 *  - the auction with id "auction" has closed
 *
 * transaction(Transaction transaction)
 *  - there has been a transaction
 *
 * gameStarted()
 *  - a TAC game has started, and all information about the
 *  game is available (preferences etc).
 *
 * gameStopped()
 *  - the current game has ended
 *
 */

package se.sics.tac.sicsagent;
import se.sics.tac.aw.*;
import se.sics.tac.util.ArgEnumerator;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Enumeration;

public class SICSAgent extends AgentImpl {

    private final static Logger log =
        Logger.getLogger("se.sics.tac.sicsagent.SICSAgent");

    private Vector strategies = new Vector();
    private int numSolveRequests = 0;
    private Solver[] solvers;
    private int solveId;

    /*
     *  - returns the number of auctions in TAC
     */
    public int getAuctionNo() {
	return agent.getAuctionNo();
    }

    /*
     *  - returns the auction-id for the requested resource
     *   (categories are TACAgent.{CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
     *    and types are TACAgent.TYPE_INFLIGHT, TACAgent.TYPE_OUTFLIGHT, etc)
     */
    public int getAuctionFor(int category, int type, int day) {
	return agent.getAuctionFor(category, type, day);
    }

    /*
     *  - returns the category for this auction (se above)
     */
    public int getAuctionCategory(int auction) {
	return agent.getAuctionCategory(auction);
    }

    /*
     *  - returns the number of items that the agent own for this
     *    auction
     */
    public int getOwn(int auction) {
	return agent.getOwn(auction);
    }

    public int getProbablyOwn(int auctionID) {
	return agent.getProbablyOwn(auctionID);
    }

    public Bid getBid(int auctionID) {
	return agent.getBid(auctionID);
    }

    public Quote getQuote(int auctionID) {
	return agent.getQuote(auctionID);
    }

    public void submitBid(Bid bid) {
	agent.submitBid(bid);
    }

    public void replaceBid(Bid oldBid, Bid bid) {
	agent.replaceBid(oldBid, bid);
    }

    /*
     *   - returns the allocation set for this auction
     */
    public int getAllocation(int auctionID) {
	return agent.getAllocation(auctionID);
    }

    /*
     * - set the allocation for this auction
     */
    public void setAllocation(int auctionID, int quantity) {
	agent.setAllocation(auctionID, quantity);
    }

    public int getClientPreference(int client, int type) {
	return agent.getClientPreference(client, type);
    }

    public long getGameTime() {
	return agent.getGameTime();
    }

    public long getGameTimeLeft() {
	return agent.getGameTimeLeft();
    }

    /*
     * - request is only accepted if PRIO is HI or if solver is not running
     */
    public boolean solveRequest(int prio, int time, int type) {
	for (int i = 0; i < solvers.length; i++) {
	    if ((!solvers[i].isRunning()) || (prio == Solver.PRIO_HIGH)) {
		SolverSession session = new SolverSession(type, solveId++, solvers[i].getId());
		Enumeration e = strategies.elements();
		while (e.hasMoreElements()) {
		    ((AgentStrategy) e.nextElement()).setSolverPrice(session);
		}
		solvers[i].solve(session,time);
		return true;
	    }
	}
	return false;
    }

    public void solverReport(SolverSession result) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).solverReport(result);
	}
    }

    /********************************
     * Methods called from TACAgent *
     ********************************/

    public String getUsage() {
	return "    -solver <host>            set the solver host\n" +
	    "    -solverport <port>        set the port used to connect to the solver\n"+
	    "    -strategies <\"strategy1 strategy2 .. strategyN\">     set the auction strategies to use\n" ;
    }

    public void init(ArgEnumerator args) {
	String solverHost = args.getArgument("-solver",agent.getConfig("solver","127.0.0.1"));
	int solverPort = args.getArgument("-solverport",agent.getConfig("solverport",6520));
	String strategies = args.getArgument("-strategies",agent.getConfig("strategies", "\"se.sics.tac.sicsagent.FlightStrategy se.sics.tac.sicsagent.HotelStrategy se.sics.tac.sicsagent.EventStrategy\""));

	// Load strategies
	String[] strategyList = strategies.replaceAll("\"","").split(" ");
	for (int i = 0; i < strategyList.length; i++) {
	    if (strategyList[i] != null)
		addStrategy(strategyList[i]);
	}

	// Initialize solvers
	solvers = new Solver[1];
	solvers[0] = new Solver(this, solverHost, solverPort, 0);
	for (int i = 0; i < solvers.length; i++) {
	    Thread th = new Thread(solvers[i]);
	    th.start();
	}

    }


    public void init(TACAgent agent) {
	this.agent = agent;
    }

    public void quoteUpdated(Quote quote) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).quoteUpdated(quote);
	}
    }

    public void quoteUpdated(int cat) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).quoteUpdated(cat);
	}
    }

    public void bidUpdated(Bid bid) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).bidUpdated(bid);
	}
    }

    public void bidRejected(Bid bid) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).bidRejected(bid);
	}
    }

    public void bidError(Bid bid, int status) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).bidError(bid, status);
	}
    }

    public void gameStarted() {
	solveId = 0;
	for (int i = 0; i < solvers.length; i++) {
	    solvers[i].generateClientPrefs();
	}
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).gameStarted();
	}
    }

    public void gameStopped() {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).gameStopped();
	}
    }

    public void auctionClosed(int auction) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).auctionClosed(auction);
	}
    }


    public void transaction(Transaction transaction) {
	Enumeration e = strategies.elements();
	while (e.hasMoreElements()) {
	    ((AgentStrategy) e.nextElement()).transaction(transaction);
	}
    }

    /*********************************************************************
     *
     *********************************************************************/
    private void addStrategy(String strategyClass) {
	try {
	    AgentStrategy as = (AgentStrategy) Class.forName(strategyClass).newInstance();
	    as.init(this);
	    strategies.add(as);
	    log.log(Level.FINE, "Sucessfully added strategy: " + strategyClass);
	} catch (Exception e) {
	    if (strategyClass != null)
		log.log(Level.SEVERE, "could not create strategy of type "
			+ strategyClass, e);
	    else
		log.log(Level.SEVERE, "could not create strategy of type "
			+ "null", e);
	}
    }
} // SICSAgent
