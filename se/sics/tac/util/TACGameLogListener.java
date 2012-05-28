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
 * TACGameLogListener
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 13 May, 2002
 * Updated : $Date: 2003/01/08 17:01:37 $
 *	     $Revision: 1.8 $
 * Purpose : A class implements the TACGameLogListener interface when its wants
 *	     to listen on parsed TAC game data.
 *
 */

package se.sics.tac.util;

public interface TACGameLogListener {

  // Called at startup for initialization and handling of any
  // extra arguments
  public void init(ArgEnumerator a);

  // Returns any extra argument this listener handles or NULL if
  // it does not handle any argument
  public String getUsage();

  // Called when a new game is opened for parsing
  public void gameOpened(String path, TACGameInfo game);

  // Called when a game has been completely parsed
  public void gameClosed(String path, TACGameInfo game);

  // Called when all games have been read
  public void finishedGames();

} // TACGameLogListener
