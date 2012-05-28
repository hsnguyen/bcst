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
 * PriceSolveServer
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 2 June, 2002
 * Updated : $Date: 2003/01/08 17:04:57 $
 *	     $Revision: 1.6 $
 * Purpose : Server for solving TAC agent allocations with regards to prices
 *
 */

package se.sics.tac.solver;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class PriceSolveServer implements Runnable {

  private ServerSocket serverSocket;
  private SolveRunner solveRunner;

  PriceSolveServer(int port) throws IOException {
    System.out.println("Opening Price Solver on " + port);
    serverSocket = new ServerSocket(port);
    solveRunner = new SolveRunner();
    new Thread(this).start();
  }

  public void run() {
    while(true) {
      try {
	System.out.println("Accepting new connections...");
	Socket socket = serverSocket.accept();
	InputStream input = socket.getInputStream();
	OutputStream out = socket.getOutputStream();

	solveRunner.solver.stopSolver();
	solveRunner.solver.setSolverWriter(new OutputStreamWriter(out));

	int c;
	StringBuffer msg = new StringBuffer();
	try {
	  while ((c = input.read()) != -1) {
	    if (c == '.') {
	      String message = msg.toString();
	      System.out.println("SOLVER IN:" + message);
	      solveRunner.solver.stopSolver();
	      solveRunner.addMessage(message);
	      msg = new StringBuffer();
	    } else {
	      msg.append((char) c);
	    }
	  }
	  System.out.println("EOF received... closing connection");
	} catch (Exception e2) {
	  e2.printStackTrace();
	}
	try {
	  input.close();
	  out.close();
	  socket.close();
	} catch (Exception e2) {
	  e2.printStackTrace();
	}
      } catch (IOException e) {
	e.printStackTrace();
      }
    }
  }

  private class SolveRunner implements Runnable {
    PriceOptimizer4 solver;
    ArrayList msgQueue = new ArrayList();

    SolveRunner() {
      solver = new PriceOptimizer4();
      new Thread(this).start();
    }

    public void run() {
      String msg;
      while((msg = getMessage()) != null) {
	if (solver.parseSolveRequest(msg)) {
	  solver.runSolver();
	} else {
	  System.out.println("Solver stopped after receiving: " + msg);
	}
      }
    }

    synchronized String getMessage() {
      while(msgQueue.isEmpty())
	try {
	  wait();
	} catch (Exception e) {
	  e.printStackTrace();
	}
      return (String) msgQueue.remove(0);
    }

    synchronized void addMessage(String message) {
      message = message.trim();
      msgQueue.add(message);
      notify();
    }
  }

  public static void main(String[] args) throws IOException {
    int port = 6520;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    }
    new PriceSolveServer(port);
  }
}
