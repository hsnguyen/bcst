/**
 * SICS TAC Server - InfoServer
 * http://www.sics.se/tac/	  tac-dev@sics.se
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
 * TACGameStatistics
 *
 * Author  : Peter Hultman
 * Created : 13 May, 2002
 * Updated : $Date: 2003/01/08 17:01:37 $
 *	     $Revision: 1.4 $
 * Purpose :
 *
 */

package se.sics.tac.util;
import java.util.Arrays;

public class TACGameStatistics implements TACGameLogListener {

  private float[][][][] prices;
  private int numOfGames;

  public TACGameStatistics() {
  }

  public void init(ArgEnumerator a) {
    int startGame = a.getArgument("-startGame", -1);
    int endGame = a.getArgument("-endGame", -1);
    int maxGames;
    if (endGame > 0) {
      maxGames = (startGame > 0) ? endGame - startGame + 1 : endGame;
    } else {
      maxGames = 2000;
    }
    prices = new float[20][12][2][maxGames];
    numOfGames = 0;
  }

  public String getUsage() {
    return null;
  }

  public void gameOpened(String path, TACGameInfo game) {
    System.err.print(((char)13) + "Processing game " + game.getGameID());
  }

  public void gameClosed(String path, TACGameInfo game) {
    if (!game.isFinished()) {
      System.err.println("    game "
			 + (game.isScratched()
			    ? "scratched" : "not finished"));
    } else if (numOfGames < prices[0][0][0].length) {
      for (int a = 8; a < 28; a++) {
	extractPrices(game, a, numOfGames);
      }
      numOfGames++;
    } else {
      System.err.println("    error: too many games...");
    }
  }

  // Called when all games have been read
  public void finishedGames() {
    System.err.println();
    System.err.println("Finished processing games!");
    if (numOfGames > 0) {
      for (int a = 8; a < 28; a++) {
	askPriceStat(a);
	System.out.println();
      }
      askPriceStat();
    }
    System.err.println("Number of games used: " + numOfGames);
  }

  private void extractPrices(TACGameInfo game, int auction, int n) {
    TACQuote quote;
    for (int t = 0; t < 12; t++) {
      quote = game.getAuctionQuote(40+t*60, auction);
      if (quote != null) {
        prices[auction-8][t][0][n] = quote.getAsk();
        prices[auction-8][t][1][n] = quote.getBid();
      }
    }
  }

  private void askPriceStat(int a) {
    System.out.println("Ask/Bid prices for auction: " + TACGameInfo.getItemName(TACGameInfo.getAuctionType(a)) +
		       " Day: " + TACGameInfo.getAuctionDay(a));
    System.out.println("time\tmean\t10%\t20%\t30%\t40%\t50%\t60%\t70%\t80%\t90%\t100%");
    System.out.println("-------------------------------------------------------------------------------------------");
    for (int t = 0; t < 12; t++) {
      System.out.print((t+1) + '\t');
      Arrays.sort(prices[a-8][t][0],0,numOfGames);
      Arrays.sort(prices[a-8][t][1],0,numOfGames);
      System.out.print(mean(prices[a-8][t][0], numOfGames));
      System.out.print("/" + mean(prices[a-8][t][1], numOfGames));
      for(int p = 1; p <= 10; p++) {
	System.out.print("\t" + (int)prices[a-8][t][0][Math.max((int)Math.ceil(p*numOfGames/10)-1, 0)]);
	System.out.print("/" + (int)prices[a-8][t][1][Math.max((int)Math.ceil(p*numOfGames/10)-1, 0)]);
      }
      System.out.println();
    }
  }

  private void askPriceStat() {
    System.out.println("Average Ask/Bid prices");
    System.out.println("1\t2\t3\t4\t5\t6\t7\t8\t9\t10\t11\t12");
    System.out.println("-------------------------------------------------------------------------------------------");
    for (int a = 8; a < 28; a++) {
      System.out.print(mean(prices[a-8][0][0], numOfGames));
      System.out.print("/" + mean(prices[a-8][0][1], numOfGames));
      for (int t = 1; t < 12; t++) {
	System.out.print("\t" + mean(prices[a-8][t][0], numOfGames));
	System.out.print("/" + mean(prices[a-8][t][1], numOfGames));
      }
      System.out.println();
    }
  }

  private int mean(float[] vec, int n) {
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += vec[i];
    }
    return (int) (sum / n);
  }

  public static void main(String[] args) {
    LogExtractor.main(args, new TACGameStatistics());
  }

} // TACGameLogListener
