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
 * LogSession
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 15 July, 2002
 * Updated : $Date: 2003/01/08 17:01:36 $
 *	     $Revision: 1.4 $
 * Purpose : Used by LogExtractor for each log extraction session
 *
 */

package se.sics.tac.util;
import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.logging.*;

final class LogSession implements FileFilter, Comparator {

  private static final Logger log =
    Logger.getLogger("se.sics.tac.util.LogSession");

  private LogExtractor extractor;
  private TACGameLogListener listener;
  private int startGame;
  private int endGame;
  private boolean download;
  private boolean useDirectory;

  LogSession(LogExtractor extractor, TACGameLogListener listener,
	     int startGame, int endGame, boolean download,
	     boolean useDirectory) {
    this.extractor = extractor;
    this.listener = listener;
    this.startGame = startGame;
    this.endGame = endGame;
    this.download = download;
    this.useDirectory = useDirectory;
  }

  TACGameLogListener getHandler() {
    return listener;
  }

  public void run() {
    if (endGame < 0 || !download) {
      // Download not possible without a specified end game
      extractor.extract(this);
    } else {
      // Game interval has been specified together with download
      extractor.extract(this, startGame < 1 ? 1 : startGame, endGame);
    }
  }


  /*********************************************************************
   * FileFilter interface
   *********************************************************************/

  public boolean accept(File file) {
    String name = file.getName();
    int nameLength;
    if (useDirectory) {
      int gameID = getNumber(name, 0, name.length());
      return gameID > 0 && checkGame(gameID) && file.isDirectory();
    } else if ((nameLength = getLogEnd(name)) > 0) {
      // Only accept patterns of "applet<id>.log[.gz]"
      int gameID = getNumber(name, 6, nameLength);
      return gameID > 0 && checkGame(gameID);
    } else {
      return false;
    }
  }

  private boolean checkGame(int gameID) {
    if (startGame >= 0 && gameID < startGame) {
      return false;
    }
    if (endGame >= 0 && gameID > endGame) {
      return false;
    }
    return true;
  }


  /*********************************************************************
   * Comparator
   *********************************************************************/

  public int compare(Object o1, Object o2) {
    String n1 = ((File) o1).getName();
    String n2 = ((File) o2).getName();
    int n1len, n2len, v1, v2;

    // Special sorting of directories consisting of only digits
    if (useDirectory) {
      v1 = getNumber(n1, 0, n1.length());
      v2 = getNumber(n2, 0, n2.length());
      if (v1 > 0 && v2 > 0) {
	return v1 - v2;
      }
    } else if (((n1len = getLogEnd(n1)) > 0)
	       && ((n2len = getLogEnd(n2)) > 0)) {
      // Special sorting of file with the pattern "applet<id>.log[.gz]"
      v1 = getNumber(n1, 6, n1len);
      v2 = getNumber(n2, 6, n2len);
      if (v1 > 0 && v2 > 0) {
	return v1 - v2;
      }
    }

    return n1.compareTo(n2);
  }

  private int getLogEnd(String text) {
    if (!text.startsWith("applet")) {
      return -1;
    } else if (text.endsWith(".log")) {
      return text.length() - 4;
    } else if (text.endsWith(".log.gz")) {
      return text.length() - 7;
    } else {
      return -1;
    }
  }

  private int getNumber(String text, int start, int end) {
    char c;
    int value = 0;
    for (int i = start; i < end; i++) {
      c = text.charAt(i);
      if (c >= '0' && c <= '9') {
	value = value * 10 + c - '0';
      } else {
	return -1;
      }
    }
    return value;
  }

} // LogSession
