package se.sics.tac.sicsagent;
import se.sics.tac.aw.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Solver implements Runnable {
    public static final int PRIO_HIGH = 0;
    public static final int PRIO_LOW = 1;
    public static final int TYPE_TEST_LOW = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_TEST_HIGH = 2;
    
    private SICSAgent agent;
    private String clientPrefs = null;
    private Socket server;
    private String host;
    private int port;
    private OutputStreamWriter out;
    private BufferedReader in;
    private SolverSession currentSession;
    private int myId;
    private boolean running = false;
    private Logger log;
    
    
    public Solver(SICSAgent agent, String host, int port, int id) {
	this.agent = agent;
	this.host = host;
	this.port = port;
	myId = id;
	log = Logger.getLogger("se.sics.tac.sicsagent.Solver");
	if (connectToSolver(host,port))
	    System.out.println("Has connected to solver!");
	else
	    throw new IllegalStateException("Could not connect to solver");
    }
    
    public int getId() {
	return myId;
    }
    
    public void run() {
	String msg;

	while (true) {
	    try {
		System.out.println("Waiting for solver response");
		msg = in.readLine();
		System.out.println("***SOLVER RESPONSE***");
		System.out.println(msg);
		if (msg != null)
		    handleSolverResult(msg);
		else {
		    disconnectFromSolver();
		    connectToSolver(host,port);	    
		}
	    } catch (Exception e) {
		log.log(Level.SEVERE, "***",e);
		    disconnectFromSolver();
		    connectToSolver(host,port);
	    } 
	}
    }
    
    public boolean connectToSolver(String host, int port) {
	try {
	    server = new Socket(host,port);
	    out = new OutputStreamWriter(server.getOutputStream());
	    in = new BufferedReader(new InputStreamReader(server.getInputStream()));
	    if (in.readLine().startsWith("ready")) {
		currentSession = null;
		running = false;
		return true;
	    } else
		return false;
	} catch (Exception e) {
	    log.log(Level.SEVERE,"Could not connect to solver", e);
	    return false;
	}
    }
    
    public void disconnectFromSolver() {
	try {
	    in.close();
	    out.close();
	    server.close();
	} catch (Exception e) {
	    log.log(Level.SEVERE,"Could not disconnect from solver", e);
	}
	try {
	    Thread.sleep(2000);
	} catch (Exception e) {
	}
    }
    
    public boolean isRunning() {
	return running;
    }
    
    public synchronized void solve(SolverSession session, int time) {
	String msg = createMsg(session);
	currentSession = session;
	printBidPrices(session);
	log.log(Level.FINE, "Sending to solver: " + msg);
	sendMsg(msg);
	running = true;
	Timer t = new Timer();
	try {
	    t.schedule(new Terminator(session.getId()), time);
	} catch (Exception e) {
	    log.log(Level.SEVERE,"Could not send to solver", e);
	    e.printStackTrace();	    
	}
    }

    
    public void printBidPrices(SolverSession session) {
	String msg;
    }

    public synchronized void stopSolver(int id) {
	if ((currentSession != null) && (id == currentSession.getId())) {
	    System.out.println("Stopping solver!");
	    sendMsg("stop.\n");
	    log.log(Level.FINE, "Stopped! Reporting to agent: " + currentSession.getMsg());
	    if (currentSession.generateResult())
		agent.solverReport(currentSession);
	    else {
		log.log(Level.FINE, "Solver did not report. Reconnecting to solver...");
		disconnectFromSolver();
		connectToSolver(host, port);
	    }		
	}
    }
    
    public String createMsg(SolverSession session) {
	String msg;
	msg = "solve_data([";
	msg += clientPrefs + ",";
	msg += generatePrices(session) + "]).\n";
	return msg;
    } 
    
    public void sendMsg(String msg) {
	try {
	    System.out.println("Sending to solver: " + msg);
	    out.write(msg);
	    out.flush();
	} catch (IOException e) {
	    log.log(Level.SEVERE, "sendMsg",e);
	    e.printStackTrace();
	}
    }
    
    private synchronized void handleSolverResult(String msg) {
	if (msg.startsWith("ready")) {
	    running = false;      
	} else if (msg.startsWith("yes")) {
	    if (currentSession != null) {
		log.log(Level.FINE, "Finnished! Reporting to agent: " + currentSession.getMsg());
		currentSession.setOptimal(true);
		if (currentSession.generateResult())
		    agent.solverReport(currentSession);
		currentSession = null;
	    } else
		log.log(Level.FINE, "UNEXPECTED yes!!!");
	} else if (msg.startsWith("alloc")) {
	    if (currentSession != null)
		currentSession.setMsg(msg);
	    else
		log.log(Level.FINE, "UNEXPECTED allocation!!!");
	}
    }
    
    public void generateClientPrefs() {
	int type = TACAgent.ARRIVAL;
	int day;
	
	clientPrefs = "client_inflight([";
	for (int i = 0; i < 7; i++) {
	    day = agent.getClientPreference(i, type);
	    clientPrefs += "[";
	    clientPrefs += Math.abs(day-1)*(-100) + ",";
	    clientPrefs += Math.abs(day-2)*(-100) + ",";
	    clientPrefs += Math.abs(day-3)*(-100) + ",";
	    clientPrefs += Math.abs(day-4)*(-100) + "],";
	}
	day = agent.getClientPreference(7, type);
	clientPrefs += "[";
	clientPrefs += Math.abs(day-1)*(-100) + ",";
	clientPrefs += Math.abs(day-2)*(-100) + ",";
	clientPrefs += Math.abs(day-3)*(-100) + ",";
	clientPrefs += Math.abs(day-4)*(-100) + "]]),";
	
	clientPrefs += "client_outflight([";
	type = TACAgent.DEPARTURE;
	for (int i = 0; i < 7; i++) {
	    day = agent.getClientPreference(i, type);
	    clientPrefs += "[";
	    clientPrefs += Math.abs(day-2)*(-100) + ",";
	    clientPrefs += Math.abs(day-3)*(-100) + ",";
	    clientPrefs += Math.abs(day-4)*(-100) + ",";
	    clientPrefs += Math.abs(day-5)*(-100) + "],";
	}
	day = agent.getClientPreference(7, type);
	clientPrefs += "[";
	clientPrefs += Math.abs(day-2)*(-100) + ",";
	clientPrefs += Math.abs(day-3)*(-100) + ",";
	clientPrefs += Math.abs(day-4)*(-100) + ",";
	clientPrefs += Math.abs(day-5)*(-100) + "]]),";
	
	clientPrefs += "client_hotelvalue([";
	type = TACAgent.HOTEL_VALUE;
	for (int i = 0; i < 7; i++) {
	    clientPrefs += agent.getClientPreference(i, type) + ",";
	}
	clientPrefs += agent.getClientPreference(7, type) + "]),";
	
	clientPrefs += "client_eventvalue([";
	for (int i = 0; i < 7; i++) {
	    clientPrefs += "[";
	    type = TACAgent.E1;
	    clientPrefs += agent.getClientPreference(i, type) + ",";
	    type = TACAgent.E2;
	    clientPrefs += agent.getClientPreference(i, type) + ",";
	    type = TACAgent.E3;
	    clientPrefs += agent.getClientPreference(i, type) + "],";
	}
	clientPrefs += "[";
	type = TACAgent.E1;
	clientPrefs += agent.getClientPreference(7, type) + ",";
	type = TACAgent.E2;
	clientPrefs += agent.getClientPreference(7, type) + ",";
	type = TACAgent.E3;
	clientPrefs += agent.getClientPreference(7, type) + "]])";
    }
    
    private String generatePrices(SolverSession session) {
	int auction;
	String prices = "";
	
	// inflight
	for (int d = 1; d < 5; d++) {
	    auction = d-1; 
	    prices += "price(inflight," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		if (session.getPrice(auction,n) != -1)
		    prices += session.getPrice(auction,n) + ",";
		else
		    prices += "sup,";
	    }
	    if (session.getPrice(auction,8) != -1)
		prices += session.getPrice(auction,8) + "]),";
	    else
		prices += "sup]),";
	} 

	// outflight
	for (int d = 2; d < 6; d++) {
	    auction = 4+d-2; 
	    prices += "price(outflight," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		if (session.getPrice(auction,n) != -1)
		    prices += session.getPrice(auction,n) + ",";
		else
		    prices += "sup,";
	    }
	    if (session.getPrice(auction,8) != -1)
		prices += session.getPrice(auction,8) + "]),";
	    else
		prices += "sup]),";
	} 

	
	// cheaphotel
	for (int d = 1; d < 5; d++) {
	    auction = 8+d-1;
	    prices += "price(cheaphotel," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		if (session.getPrice(auction,n) != -1)
		    prices += session.getPrice(auction,n) + ",";
		else
		    prices += "sup,";
	    }
	    if (session.getPrice(auction,8) != -1)
		prices += session.getPrice(auction,8) + "]),";
	    else
		prices += "sup]),";
	}
 

	// goodhotel
	for (int d = 1; d < 5; d++) {
	    auction = 12+d-1;
	    prices += "price(goodhotel," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		if (session.getPrice(auction,n) != -1)
		    prices += session.getPrice(auction,n) + ",";
		else
		    prices += "sup,";
	    }
	    if (session.getPrice(auction,8) != -1)
		prices += session.getPrice(auction,8) + "]),";
	    else
		prices += "sup]),";
	}
	
	// entertain1
	for (int d = 1; d < 5; d++) {
	    auction = 16+d-1;
	    prices += "price(wrestling," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		prices += session.getPrice(auction,n) + ",";
	    }
	    prices += session.getPrice(auction,8) + "]),";
	} 
	
	// entertain2
	for (int d = 1; d < 5; d++) {
	    auction = 20+d-1;
	    prices += "price(amusement," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		prices += session.getPrice(auction,n) + ",";
	    }
	    prices += session.getPrice(auction,8) + "]),";
	} 
	
	// entertain3
	for (int d = 1; d < 5; d++) {
	    auction = 24+d-1;
	    prices += "price(museum," + d + ",[";
	    for (int n = 0; n < 8; n++) {
		prices += session.getPrice(auction,n) + ",";
	    }
	    prices += session.getPrice(auction,8) + "]),";
	} 

	return prices.substring(0,prices.length()-1);
    }
  
    private class Terminator extends TimerTask {
	int id;
	
	public Terminator(int id) {
	    super();
	    this.id = id;
	}
	
	public void run() {
	    stopSolver(id);
	}
    }
}






