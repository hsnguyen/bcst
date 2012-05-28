/**
 * SICS TAC Server - InfoServer
 * http://www.sics.se/tac/	  tac-dev@sics.se
 *
 * Copyright (c) 2001, 2002 SICS AB. All rights reserved.
 * -----------------------------------------------------------------
 *
 * PriceOptimizer
 * Optimizes the score based on prices and ...
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 24 October, 2001
 * Updated : $Date: 2002/07/08 17:35:00 $
 *	     $Revision: 1.6 $
 * Purpose : Simple Branch and Bound optimizer for TAC games
 *
 *
 * Branch and Bound optimizer for TAC games
 * Takes scores and tries to optimize based on scores - with prices.
 */

package se.sics.tac.solver;
import java.io.*;
import java.util.StringTokenizer;

public class PriceOptimizer2 {

  // SUP means that it is not possible to buy anything at all...
  public static final int SUP = 1000000;

  private static final boolean DEBUG = true; // false;

  private static final int MAX_ALLOC_POS = 8;
  private static final int[] MAX_VAL = { 5, 4, 2, 5, 5, 5};
  private static final int NO_STAY = 4;
  private static final int NO_ENTERTAINMENT = 4;

  private static final int IN_FLIGHT = 0;
  private static final int OUT_FLIGHT = 1;
  private static final int HOTEL = 2;
  private static final int E1 = 3;
  private static final int E2 = 4;
  private static final int E3 = 5;

  private static final int HOTEL_ALLOC_POS = 6;
  private static final int HOTEL_MASK = 1 << HOTEL_ALLOC_POS; // = 0x40
  private static final int ENTER_ALLOC_POS = 0;

  private static final int ENTER1_BIT = 4;
  private static final int ENTER2_BIT = 5;
  private static final int ENTER3_BIT = 6;
  private static final int ENTER1_FLAG_MASK = 1 << ENTER1_BIT;
  private static final int ENTER2_FLAG_MASK = 1 << (1 + ENTER1_BIT);

  private static final int IN_FLIGHT_POS = 0;
  private static final int OUT_FLIGHT_POS = 16;
  private static final int GOOD_POS = 32;
  private static final int CHEAP_POS = 48;

  // Sorted just to test...
  // This game took 35 minutes on tac2 (800 Mhz) and around 17 minutes
  // On my 1.8 Ghz (bnb: 1428411567). Score shold be 9550.
  // With enter min optimization it takes around 50 seconds (bnb: 94907114).
  // Addition optimization with EnterMin gives
  /*int preferences[][] = new int[][] {
    {2,2,55,193,180,111},
    {2,2,110,68,193,148},
    {0,1,112,67,149,177},
    {1,1,69,87,142,189},
    {1,1,87,74,72,167},
    {1,2,72,77,78,37},
    {1,2,67,78,154,67},
    {0,2,140,141,3,23}
  };
  */

  int preferences[][] = new int[][] {{1,2,72,77,78,37},
				     {2,2,55,193,180,111},
				     {0,1,112,67,149,177},
				     {1,1,87,74,72,167},
				     {2,2,110,68,193,148},
				     {1,1,69,87,142,189},
				     {1,2,67,78,154,67},
				     {0,2,140,141,3,23}
  };

  int own[][] = new int[][] {{2,4,2,0},
			     {0,3,5,0},
			     {7,3,2,0},
			     {9,3,3,0},
			     {1,1,2,2},
			     {1,0,0,4},
			     {1,1,4,0}
  };

  private static final int IN_FLIGHT_PRICE = 0;
  private static final int OUT_FLIGHT_PRICE = 4;
  private static final int HOTEL_PRICE = 8; // GOOD FIRST
  private static final int E1_PRICE = 16;
  private static final int E2_PRICE = 20;
  private static final int E3_PRICE = 24;


    // Prices for all types of items
  int prices[][] = new int[][] {
      {0,0,0,100,200,300,400,500,600},     // In flight 1
      {0,0,0,0,0,100,200,300,400},         // In flight 2
      {0,0,0,100,200,300,400,500,600},     // In flight 3
      {0,100,200,300,400,500,600,700,800}, // In flight 4

      {0,100,200,300,400,500,600,700,800},   // Out flight 2
      {0,0,0,0,100,200,300,400,500},         // Out flight 3
      {0,0,0,0,0,0,100,200,300},             // Out flight 4
      {0,100,200,300,400,500,600,700,800},   // Out flight 5

      {0,100,200,300,400,500,600,700,800},   // Hotel Good
      {0,100,200,300,400,500,600,700,800},   // Hotel
      {0,400,800,1200,1600,2000,2400,2800,3200},   // Hotel
      {0,100,200,300,400,500,600,700,800},   // Hotel

      {0,100,200,300,400,500,600,700,800},   // Hotel Bad
      {0,0,0,0,0,100,200,300,400},   // Hotel
      {0,0,0,0,0,100,200,300,400},   // Hotel
      {0,100,200,300,400,500,600,700,800},   // Hotel

      {0,100,200,300,400,500,600,700,800},   // E1
      {0,100,200,300,400,500,600,700,800},   //
      {0,0,0,0,0,100,200,300,400},   //
      {0,100,200,300,400,500,600,700,800},   //

      {0,100,200,300,400,500,600,700,800},   // E2
      {0,100,200,300,400,500,600,700,800},   //
      {0,100,200,300,400,500,600,700,800},   //
      {0,100,200,300,400,500,600,700,800},   //

      {0,100,200,300,400,500,600,700,800},   // E3
      {0,100,200,300,400,500,600,700,800},   //
      {0,100,200,300,400,500,600,700,800},   //
      {0,100,200,300,400,500,600,700,800}   //
  };

  int deltaPrices[][] = new int[28][8];

  // Client preferences (fligth, hotel g/c & entertainment 1, 2, 3)
  /*
  int preferences[][] = new int[][] { { 1, 1, 94, 75, 174, 161},
				      { 3, 3, 68, 174, 13, 43},
				      { 2, 3, 122, 175, 189, 187},
				      { 2, 3, 126, 77, 181, 79},
				      { 2, 2, 138, 135, 198, 12},
				      { 0, 3, 109, 13, 48, 17},
				      { 1, 3, 92, 84, 147, 103},
				      { 2, 3, 148, 94, 10, 1} };
  */
  // Tries to shorten the trip if possible
  int bestInFlight[][] = new int[][] { { 0, 1, 2, 3},
				       { 1, 2, 3, 0}, 
				       { 2, 3, 1, 0},
				       { 3, 2, 1, 0} };

  // Tries to shorten the trip if possible
  int bestOutFlight[][] = new int[][] { { 0, 1, 2, 3},
					{ 1, 0, 2, 3},
					{ 2, 1, 0, 3},
					{ 3, 2, 1, 0} };

  /*
  int bestInFlight[][] = new int[][] { { 0, 1, 2, 3},
				       { 1, 0, 2, 3},
				       { 2, 1, 3, 0},
				       { 3, 2, 1, 0} };

  int bestOutFlight[][] = new int[][] { { 0, 1, 2, 3},
					{ 1, 2, 0, 3},
					{ 2, 3, 1, 0},
					{ 3, 2, 1, 0} };

  */
  // What we own
  /*
  int own[][] = new int[][]  { { 1, 2, 4, 1},
			       { 1, 2, 1, 4},
			       { 1, 1, 4, 5},
			       { 0, 1, 0, 3},
			       { 0, 0, 0, 1},
			       { 0, 1, 1, 1},
			       { 3, 0, 1, 2}};
  */
  int scoreClient[] = new int[8];

  // From this the allocation can be extracted
  // Should be a better interface later...
  public long finalAlloc = 0;
  public long finalStay = 0;
  int bound = 0;

  // 8 bits/client: 4 bits stay, 3 bits entertainment
  long newStay = 0;
  // 8 bits/client: 1 bit good hotel, 2 bits/entertainment day per type,
  long newAlloc = 0;
  long newStuff1 = 0;
  long newStuff2 = 0;

  long allocs = 0L;
  long bnbs = 0L;
  long startTime = 0L;
  long calculationTime = -1L;




  // SOLVER stuff (for remoting the solver)
  OutputStreamWriter out = null;
  boolean runSolver = false;

  int calcBestScore() {
    int score = 0;
    for (int i = 0; i < 8; i++) {
      score += (scoreClient[i] = 1000 + preferences[i][HOTEL] +
		preferences[i][E1] + preferences[i][E2] +
		preferences[i][E3]);
    }
    for (int i = 0; i < 28; i++)
      score -= prices[i][0];


    // Also calculate the deltaPrices
    for (int a = 0; a < 28; a++)
      for (int i = 0; i < 8; i++) {
	if (prices[a][i] != SUP && prices[a][i + 1] != SUP)
	  deltaPrices[a][i] = prices[a][i + 1] - prices[a][i];
	else {
	  deltaPrices[a][i] = SUP;
	}
      }

    return score;
  }

  boolean bnb(int hscore, long stay, long alloc, int allocPos,
	      long stuff1, long stuff2, int flights) {
    //  System.out.println("Alloc: " + allocPos + " Max: " + MAX_ALLOC_POS);
    bnbs++;

    // Here we might be able to stop the solver !!!
    if (!runSolver) return false;

    if (allocPos >= MAX_ALLOC_POS) {
      int inf, outf, a, a2;
      int score = 0;
      int tmp1, tmp2;
      int[] prefs;

      allocs++;
      for (int i = 0; i < 8; i++) {
	a = (int) ((stay >> (i * 8)) & 0xff);
	if (a != 0) {
	  inf = (int) ((flights >> (i * 4) + 2) & 3) + 1;
	  outf = (int) ((flights >> (i * 4)) & 3) + 2;

	  prefs = preferences[i];
	  score += 1000
	    - ((((tmp1 = inf - 1 - prefs[0]) >= 0) ? tmp1 : -tmp1) +
	       (((tmp2 = outf - 2 - prefs[1]) >= 0) ? tmp2 : -tmp2))
	    * 100;
	  a2 = (int) ((alloc >> i * 8) & 0xff);
	  if ((a2 & HOTEL_MASK) > 0) {
	    score += prefs[HOTEL]; //hotel[i];
	  }
	  for (int e = 0; e < 3; e++) {
	    if ((a & (0x10 << e)) > 0) {
	      score += prefs[E1 + e];
	    }
	  }
	}
      }
      int price = 0;
      for (int i = 0; i < 4; i++) {
	int inF = (int) (stuff1 >> (IN_FLIGHT_POS + i * 4)) & 0xf;
	int outF = (int) (stuff1 >> (OUT_FLIGHT_POS + i * 4)) & 0xf;

	int noGHotel = (int) (stuff1 >> (GOOD_POS + i * 4)) & 0xf;
	int noCHotel = (int) (stuff1 >> (CHEAP_POS + i * 4)) & 0xf;

	int e1 = (int) ((stuff2 >> (i * 4)) & 0xf);
	int e2 = (int) ((stuff2 >> (16 + i * 4)) & 0xf);
	int e3 = (int) ((stuff2 >> (32 + i * 4)) & 0xf);

	price += prices[IN_FLIGHT_PRICE + i][inF];
	price += prices[OUT_FLIGHT_PRICE + i][outF];

	price += prices[HOTEL_PRICE + i][noGHotel];
	price += prices[HOTEL_PRICE + 4 + i][noCHotel];

	price += prices[E1_PRICE + i][e1];
	price += prices[E2_PRICE + i][e2];
	price += prices[E3_PRICE + i][e3];
      }
      score -= price;
      if (score > bound) {
	bound = score;
	finalAlloc = alloc;
	finalStay = stay;
	long time = System.currentTimeMillis() - startTime;
	System.out.println("New Bound: " + bound + ", h = " + hscore +
			   " price = " + price + " time = " +
			   time);
	if (DEBUG) setLatestAlloc(stay, alloc, price, time, true);
      }
    } else {
      // Allocate inflight for this client
      int user = allocPos;
      int user8 = user * 8;
      int[] prefs = preferences[user];
      int prefO = prefs[1];
      int prefI = prefs[0];
      int newScore;
      int price;
      int dPrice;
      for (int i = 0; i < 4; i++) {
	int inday = bestInFlight[prefI][i];
	// Check if flight exists this day
	int inF = (int) (stuff1 >> (IN_FLIGHT_POS + inday * 4)) & 0xf;

	// This check might not be needed (SUP on flight is not happening?)
	if ((price = deltaPrices[IN_FLIGHT_PRICE + inday][inF]) != SUP) {
	  int inScore;
	  if (inday > prefI) {
	    inScore = hscore - (inday - prefI) * 100 - price;
	  } else {
	    inScore = hscore - (prefI - inday) * 100 - price;
	  }

	  if (inScore > bound) {
	    for (int j = 0; j < 4; j++) {
	      int outday = bestOutFlight[prefO][j];
	      int outF = (int) (stuff1 >> (OUT_FLIGHT_POS + outday * 4)) & 0xf;
	      if (outday >= inday &&
		  (price = deltaPrices[OUT_FLIGHT_PRICE + outday][outF])!= SUP){
		// We have an ok stay !!! Update variables!!!
		int outScore;
		if (outday > prefO) {
		  outScore = inScore - (outday - prefO) * 100 - price;
		} else {
		  outScore = inScore - (prefO - outday) * 100 - price;
		}
		if (outScore > bound) {
		  long bits = 0L;
		  for (int b = 1 << outday, min = 1 << inday;
		       b >= min; b = (b >> 1)) {
		    bits |= b;
		  }
		  long newStay = stay | (bits << user8);

		  // Increase the number of flights used!
		  long outStuff1 = stuff1
		    + (1L << (OUT_FLIGHT_POS + outday * 4))
		    + (1L << (IN_FLIGHT_POS + inday * 4));
		  int newFlights = flights
		    | (inday << (user * 4 + 2))
		    | (outday << (user * 4));
		  

		  // Check the hotel prices!!
		  long newStuff1;
		  int totGPrice = 0;
		  int totCPrice = prefs[HOTEL];
		  long tmpStuff = 0;
		  for (int d = inday; d <= outday; d++) {
		    int noGHotel = (int) (stuff1 >> (GOOD_POS + d * 4)) & 0xf;
		    int noCHotel = (int) (stuff1 >> (CHEAP_POS + d * 4)) & 0xf;
		    totGPrice += deltaPrices[HOTEL_PRICE + d][noGHotel];
		    totCPrice += deltaPrices[HOTEL_PRICE + 4 + d][noCHotel];
		    tmpStuff += (1L << (d * 4));
		  }
		  
		  // A quick fix for the enter...
		  long newerAlloc;
		  int newerScore;
		  long newerStay = newStay;
		  newAlloc = alloc;
		  newScore = outScore;
		  if (totGPrice <= totCPrice) {
		    newerAlloc = newAlloc | 
		      (1L << (HOTEL_ALLOC_POS + user8));
		    newerScore = newScore - totGPrice;
		    newStuff1 = outStuff1 + 
		      (tmpStuff << GOOD_POS);
		    
		    if (newerScore > bound) {
		      bnb(newerScore, newerStay,
			  newerAlloc, allocPos + 1,
			  newStuff1, newStuff2, newFlights);
		    }
		    // Check other hotel
		    newerScore = newScore - totCPrice;
		    newerAlloc = newerAlloc;
		    newStuff1 = outStuff1 +
		      (tmpStuff << CHEAP_POS);
		  } else {
		    newerScore = newScore - totCPrice;
		    newerAlloc = newAlloc;
		    newStuff1 = outStuff1 + 
		      (tmpStuff << CHEAP_POS);
				      
		    if (newerScore > bound) {
		      bnb(newerScore, newerStay,
			  newerAlloc, allocPos + 1,
			  newStuff1, newStuff2, newFlights);
		    }

		    // Check other hotel
		    newerAlloc = newAlloc | 
		      (1L << (HOTEL_ALLOC_POS + user8));
		    newerScore = newScore - totGPrice;
		    newStuff1 = outStuff1 + 
		      (tmpStuff << GOOD_POS);
		  }
		  if (newerScore > bound) {
		    bnb(newerScore, newerStay,
			newerAlloc, allocPos + 1,
			newStuff1, newStuff2, newFlights);
		  }
		}
	      }
	    }
	    // Else this cant be an assignment...
	  }
	}
      }
      // this client does not go... is this correct?? CHECK THIS
      newScore = hscore - scoreClient[user];
      if (newScore > bound) {
	bnb(newScore, stay, alloc, allocPos + 1,
	    stuff1, stuff2, flights);
      }
    }
    return runSolver;
  }

  public boolean solve() {
    int startScore = calcBestScore();
    System.out.println("Best Util: " + startScore);
    bound = 0;
    finalAlloc = allocs = 0L;
    finalStay = 0L;
    bnbs = 0L;
    calculationTime = -1L;

    long stuff1 = 0;
    long stuff2 = 0;

    startTime = System.currentTimeMillis();
    boolean res = bnb(startScore, 0, 0L, 0, stuff1, stuff2, 0);

    System.out.println("-------------------------------------------");
    System.out.println("Final score");
    System.out.println("-------------------------------------------");
    // THIS METHOD MUST BE CALLED BEFORE RETURNING BECAUSE IT WILL SET
    // LATESTALLOC!!!!
    calculationTime = (System.currentTimeMillis() - startTime);
    setLatestAlloc(finalStay, finalAlloc, 0, calculationTime, false);
    System.out.println("-------------------------------------------");
    System.out.println("Time: " + calculationTime
		       + "    Allocs: " + allocs);
    System.out.println("Bnbs: " + bnbs + "    Bound: " + bound);
    System.out.println("-------------------------------------------");
    startTime = 0L;
    return res;
  }

  private long max15(long own) {
    // Make sure the value is NOT negative (in case there are some errors
    // in the server or an agent has sold more than it owns)
    return own < 15 ? (own < 0 ? 0 : own) : 15;
  }


  // This will store the latest allocation
  int[][] latestAlloc = new int[8][6];
  public int[][] getLatestAllocation() {
    return latestAlloc;
  }

  public long getCalculationTime() {
    return calculationTime;
  }

  void setLatestAlloc(long stay, long alloc, int price, long time,
		      boolean writeAlloc) {
    int day;
    int total = 0;
    int[] clientAlloc;

    if (!writeAlloc)
      System.out.println("------------------------------------------------");
    for (int i = 0; i < 8; i++) {
      int inf = -1;
      int outf = -1;
      int a = (int) ((stay >> (i * 8)) & 0xff);
      clientAlloc = latestAlloc[i];

      // This could actually be an 16 bytes array !!!!
      for (int f = 0; f < 4; f++) {
	if (((a >> f) & 1) == 1  && inf == -1) {
	  inf = f + 1;
	}
	if (((a >> f) & 1) == 0 && inf != -1 && outf == -1) {
	  outf = f + 1;
	}
      }
      if (outf == -1)
	outf = 5;

      int a2 = (int) (alloc  >> i * 8) & 0xff;
      if (inf != -1) {
	clientAlloc[0] = inf;
	clientAlloc[1] = outf;

	int score = 1000 - Math.abs(inf - 1 - preferences[i][0]) * 100 +
	  - Math.abs(outf - 2 - preferences[i][1]) * 100;
	if (!writeAlloc) {
	  System.out.print("Client " + (i + 1) + "  Stays: " +
			   inf + " - " + outf);
	}
	if ((a2 & HOTEL_MASK) > 0) {
	  if (!writeAlloc)
	    System.out.print(" has good hotel ");
	  score += preferences[i][HOTEL];
	  clientAlloc[2] = 1;
	} else {
	  if (!writeAlloc)
	  System.out.print(" has cheap hotel ");
	  clientAlloc[2] = 0;
	}

	if ((a & 0x10) > 0) {
	  if (!writeAlloc)
	    System.out.print(" E1 day " + (1 + (a2 & 3)));
	  score += preferences[i][E1];
	  clientAlloc[3] = (1 + (a2 & 3));
	} else {
	  clientAlloc[3] = 0;
	}
	if ((a & 0x20) > 0) {
	  if (!writeAlloc)
	    System.out.print(" E2 day " + (1 + ((a2 >> 2) & 3)));
	  score += preferences[i][E2];
	  clientAlloc[4] = (1 + ((a2 >> 2) & 3));
	} else {
	  clientAlloc[4] = 0;
	}

	if ((a & 0x40) > 0) {
	  if (!writeAlloc)
	    System.out.print(" E3 day " + (1 + ((a2 >> 4) & 3)));
	  score += preferences[i][E3];
	  clientAlloc[5] = (1 + ((a2 >> 4) & 3));
	} else {
	  clientAlloc[5] = 0;
	}

	if (!writeAlloc)
	  System.out.print("\t score = " + score);
	total += score;
	if (!writeAlloc)
	  System.out.println();
      } else {
	if (!writeAlloc)
	  System.out.println("Client " + (i + 1) + " does not go, score = 0");
	// MUST CLEAR latestAlloc if the client does not go!!!!
	for (int j = 0, m = clientAlloc.length; j < m; j++) {
	  clientAlloc[j] = 0;
	}
      }
    }
    System.out.println("Total score = " + (total - price) + " utility = " +
		       total + " costs = " + price);
    StringBuffer sb = new StringBuffer();

    sb.append("alloc(utility(" + (total - price) + ',' + time +
		       "), flight_allocation([" + latestAlloc[0][0]);
    for (int i = 1; i < 8; i++) {
      sb.append("," + latestAlloc[i][0]);
    }
    sb.append("],[" + latestAlloc[0][1]);
    for (int i = 1; i < 8; i++) {
      sb.append("," + latestAlloc[i][1]);
    }
    sb.append("]), hotel_allocation([" + (1^latestAlloc[0][2]));
    for (int i = 1; i < 8; i++) {
      sb.append("," + (1^latestAlloc[i][2]));
    }
    sb.append("],[" + latestAlloc[0][2]);
    for (int i = 1; i < 8; i++) {
      sb.append("," + latestAlloc[i][2]);
    }

    sb.append("]), event_allocation([");
    for (int etype = 3; etype < 6; etype++) {
      if (etype > 3) {
	sb.append("],[");
      }
      sb.append(latestAlloc[0][etype]);
      for (int i = 1; i < 8; i++) {
	sb.append("," + latestAlloc[i][etype]);
      }
    }
    sb.append("])).\r\n");
    // System.out.println("Solve Result:" + sb);
    if (writeAlloc) writeToSolver(sb.toString());
  }


  private void writeToSolver(String result) {
    if (out != null) {
      try {
	System.out.println("SOLVER RESPONSE: {" + result);
	out.write(result);
	out.flush();
	System.out.println("}");
      } catch (Exception e) {
	e.printStackTrace();
	out = null;
	runSolver = false;
      }
    } else {
      System.out.println("COULD NOT SEND: " + result);
    }
  }

  // This should be called by the runner of the solver...
  public void runSolver() {
    if (runSolver == false) {
      runSolver = true;
      if(solve()) {
	writeToSolver("yes.\r\n");
	writeToSolver("ready.\r\n");
      }
      runSolver = false;
      System.out.println("Notify that solver is stopped...");
      synchronized(this) {
	notify();
      }
    } else {
      throw new IllegalStateException("Can not run two solvers");
    }
  }

  public synchronized void stopSolver() {
    if (runSolver) {
      runSolver = false;
      try {
	System.out.println("Wait for solver to stop");
	wait();
	System.out.println("Solver is stopped");
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }

  public void setSolverWriter(OutputStreamWriter writer) {
    out = writer;
    writeToSolver("ready.\r\n");
  }

  public boolean parseSolveRequest(String request) {
    if (request.startsWith("solve_data([")) {
      int index = request.indexOf("client_inflight");
      int inFlight[] = parseFlight(request, index);
      index = request.indexOf("client_outflight");
      int outFlight[] = parseFlight(request, index);

      index = request.indexOf("client_hotelvalue");
      int hotelVal[] = parseHotel(request, index);

      index = request.indexOf("client_eventvalue");
      int eventVal[][] = parseEvent(request, index);

      // Copy into preferences...
      for (int i = 0; i < 8; i++) {
	preferences[i][0]= inFlight[i];
	preferences[i][1]= outFlight[i];
	preferences[i][2]= hotelVal[i];
	preferences[i][3]= eventVal[i][0];
	preferences[i][4]= eventVal[i][1];
	preferences[i][5]= eventVal[i][2];
	showPreferences(i);
      }
      setPrices(request);
      return true;
    }
    // Nothing to solve -> ready again!!!
    writeToSolver("ready.\r\n");
    return false;
  }

  private void showPreferences(int i) {
    System.out.println("Client " + (i + 1) +
		       "  " + (preferences[i][0] + 1) +
		       " - " + (preferences[i][1] + 2) +
		       "   " + (preferences[i][2]) +
		       ", " + (preferences[i][3]) +
		       ", " + (preferences[i][4]) +
		       ", " + (preferences[i][5]));
  }

  private int[] parseFlight(String request, int index) {
    int[] f = new int[8];
    index = request.indexOf('[', index) + 1;
    if (request.charAt(index) == '[')
      index++;
    int i2 = request.indexOf(',', index);
    String sub;
    for (int i = 0; i < 8; i++) {
      sub = request.substring(index, i2);
      try {
	f[i] = Integer.parseInt(sub) / -100;
      } catch (Exception e) {
	e.printStackTrace();
      }
      index = request.indexOf('[', index) + 1;
      i2 = request.indexOf(',', index);
    }
    return f;
  }

  private int[] parseHotel(String request, int index) {
    int[] f = new int[8];
    index = request.indexOf('[', index) + 1;
    if (request.charAt(index) == '[')
      index++;

    int i2 = request.indexOf(',', index);
    String sub;
    for (int i = 0; i < 8; i++) {
      sub = request.substring(index, i2);
      try {
	f[i] = Integer.parseInt(sub);
      } catch (Exception e) {
	e.printStackTrace();
      }
      index = i2 + 1;
      if (i == 6)
	i2 = request.indexOf(']', index);
      else
	i2 = request.indexOf(',', index);
    }
    return f;
  }

  private int[][] parseEvent(String request, int index) {
    int[][] f = new int[8][3];
    index = request.indexOf('[', index) + 1;
    int i2 = request.indexOf(',', index);
    String sub;
    for (int i = 0; i < 8; i++) {
      index = request.indexOf('[', index) + 1;
      i2 = request.indexOf(',', index);
      for (int et = 0; et < 3; et++) {
	sub = request.substring(index, i2);
	try {
	  f[i][et] = Integer.parseInt(sub);
	} catch (Exception e) {
	  e.printStackTrace();
	}
	index = i2 + 1;
	if (et == 1)
	  i2 = request.indexOf(']', index);
	else
	  i2 = request.indexOf(',', index);
      }
    }
    return f;
  }

  private void setPrices(String request) {
    // Should be 28 price vectors...
    int index = 0;
    int i2 = 0;
    String type;
    int day = 0;
    int auction;
    String sub;
    for (int i = 0; i < 28; i++) {
      index = request.indexOf("price", index) + 6;
      i2 = request.indexOf(',', index);
      type = request.substring(index, i2);
      index = i2 + 1;
      try {
	day = Integer.parseInt(request.substring(index, index + 1));
      } catch (Exception e) {
	e.printStackTrace();
      }
      auction = getAuction(type, day);
      // System.out.println("Prices for auction: " + auction);
      index = request.indexOf('[', index) + 1;
      for (int p = 0; p < 9; p++) {
	if (p < 8) {
	  i2 = request.indexOf(',', index);
	} else {
	  i2 = request.indexOf(']', index);
	}
	try {
	  sub = request.substring(index, i2);
	  if ("sup".equals(sub)) {
	    prices[auction][p] = SUP;
	    // System.out.print(" sup");
	  } else {
	    prices[auction][p] = Integer.parseInt(sub);
	    // System.out.print(" " + prices[auction][p]);
	  }
	} catch (Exception e) {
	  e.printStackTrace();
	}
	index = i2 + 1;
      }
      // System.out.println();
    }
  }

  private int getAuction(String cat, int day) {
    switch (cat.charAt(0)) {
    case 'i': // Inflight
      return IN_FLIGHT_PRICE + day - 1;
    case 'o': // Outflight
      return  OUT_FLIGHT_PRICE + day - 2;
    case 'g': // GoodHotel
      return HOTEL_PRICE + day - 1;
    case 'c': // CheapHotel
      return 4 + HOTEL_PRICE + day - 1;
    case 'w': // Wrestling
      return E1_PRICE + day - 1;
    case 'a': // Amusement
      return E2_PRICE + day - 1;
    case 'm': // Museum
      return E3_PRICE + day - 1;
    }
    return -1;
  }

  // Set a clients preferences based on an array of 8 x 6
  // In, Out, Hotel, E1, E2, E3
  // Owns is 5 x 7
  public void setClientData(int prefs[][], int owns[][]) {
    for (int c = 0; c < 8; c++) {
      // Modify Flight data
      preferences[c][0] = prefs[c][0] - 1;
      preferences[c][1] = prefs[c][1] - 2;
      for (int i = 2; i < 6; i++)
	preferences[c][i] = prefs[c][i];
    }
    for (int day = 0; day < 4; day++) {
      own[0][day] = owns[day][0];
      own[1][day] = owns[day + 1][1];
      for (int j = 2; j < 7; j++)
	own[j][day] = owns[day][j];
    }
  }


  /*********************************************************************
   * DEBUG OUTPUT
   *********************************************************************/

  void showDebug() {
    synchronized (System.out) {
      System.out.println("> ====================================================================");
      if (startTime > 0) {
	System.out.println("> Bound = " + bound + ", bnbs = " + bnbs
			   + ", allocs = " + allocs + ", time = "
			   + (System.currentTimeMillis() - startTime));
      } else {
	System.out.println("> Optimizer is not running");
      }
      System.out.println("> ====================================================================");
    }
  }

  public static void main (String[] args) {
    PriceOptimizer2 po = new PriceOptimizer2();
    po.parseSolveRequest("solve_data([client_inflight([[0,-100,-200,-300],[-100,0,-100,-200],[-100,0,-100,-200],[0,-100,-200,-300],[0,-100,-200,-300],[-200,-100,0,-100],[-200,-100,0,-100],[0,-100,-200,-300]]),client_outflight([[-300,-200,-100,0],[-300,-200,-100,0],[-200,-100,0,-100],[-300,-200,-100,0],[-200,-100,0,-100],[-300,-200,-100,0],[-200,-100,0,-100],[-100,0,-100,-200]]),client_hotelvalue([91,105,88,131,94,68,111,92]),client_eventvalue([[187,76,4],[90,171,40],[15,162,199],[79,74,26],[158,176,80],[94,163,39],[7,6,187],[90,23,66]]),price(inflight,1,[0,0,0,0,0,264,528,792,1056]),price(inflight,2,[0,0,0,351,702,1053,1404,1755,2106]),price(inflight,3,[0,0,0,291,582,873,1164,1455,1746]),price(inflight,4,[0,408,816,1224,1632,2040,2448,2856,3264]),price(outflight,2,[0,362,724,1086,1448,1810,2172,2534,2896]),price(outflight,3,[0,0,348,696,1044,1392,1740,2088,2436]),price(outflight,4,[0,0,0,0,288,576,864,1152,1440]),price(outflight,5,[0,0,0,0,0,325,650,975,1300]),price(cheaphotel,1,[0,36,72,130,191,263,347,446,561]),price(cheaphotel,2,[0,125,250,540,864,1296,1866,2612,3583]),price(cheaphotel,3,[0,126,252,544,870,1306,1881,2633,3611]),price(cheaphotel,4,[0,46,92,166,244,336,444,570,717]),price(goodhotel,1,[0,110,220,399,585,805,1062,1364,1714]),price(goodhotel,2,[0,180,360,777,1244,1866,2687,3762,5159]),price(goodhotel,3,[0,185,370,799,1278,1918,2762,3866,5303]),price(goodhotel,4,[0,130,260,471,692,951,1256,1612,2026]),price(wrestling,1,[0,105,220,347,486,638,804,984,1181]),price(wrestling,2,[0,105,220,347,486,638,804,984,1181]),price(wrestling,3,[0,105,220,347,486,638,804,984,1181]),price(wrestling,4,[0,105,220,347,486,638,804,984,1181]),price(amusement,1,[0,105,220,347,486,638,804,984,1181]),price(amusement,2,[0,105,220,347,486,638,804,984,1181]),price(amusement,3,[-260,-205,-144,-76,0,105,220,347,486]),price(amusement,4,[-144,-76,0,105,220,347,486,638,804]),price(museum,1,[0,105,220,347,486,638,804,984,1181]),price(museum,2,[0,105,220,347,486,638,804,984,1181]),price(museum,3,[-144,-76,0,105,220,347,486,638,804]),price(museum,4,[-260,-205,-144,-76,0,105,220,347,486])])."); 
    po.runSolver();
  }
} // PriceOptimizer
