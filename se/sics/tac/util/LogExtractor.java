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
 * LogExtractor
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 13 May, 2002
 * Updated : $Date: 2003/01/08 17:01:36 $
 *	     $Revision: 1.28 $
 * Purpose : A simple tool for downloading and parsing game data files.
 *
 */

package se.sics.tac.util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

public class LogExtractor implements TACGameLogListener {

  public final static String USE_DIRECTORY = "useDirectory";
  public final static String PATH = "path";
  public final static String BASE = "base";
  public final static String MAX_DOWNLOAD_ERRORS = "maxDownloadErrors";

  private final static String VERSION = "1.0-beta1";

  private final static String USER_AGENT;

  static {
    String os;
    try {
      os = System.getProperty("os.name");
    } catch (Exception e) {
      os = null;
    }
    StringBuffer sb = new StringBuffer();
    sb.append("TACGameDataToolkit/" + VERSION);
    if (os != null) {
      sb.append(" (");
      sb.append(os);
      sb.append(')');
    }
    USER_AGENT = sb.toString();
  }

  private static final Logger log =
    Logger.getLogger("se.sics.tac.util.LogExtractor");

  private boolean useDirectory = false;
  private String path = "games";
  private String base = "http://www.sics.se/tac/games/";
  private int pathFlag = 0;
  private int maxDownloadErrors = 10;

  public LogExtractor() {
  }

  public String getProperty(String name) {
    if (PATH.equals(name)) {
      return path;
    } else if (USE_DIRECTORY.equals(name)) {
      return Boolean.toString(useDirectory);
    } else if (BASE.equals(name)) {
      return base;
    } else if (MAX_DOWNLOAD_ERRORS.equals(name)) {
      return Integer.toString(maxDownloadErrors);
    } else {
      return null;
    }
  }

  public void setProperty(String name, String value) {
    if (value == null) {
      throw new NullPointerException("no value for " + name);
    }
    if (PATH.equals(name)) {
      path = (value.endsWith("/") || value.endsWith(File.separator))
	? value.substring(0, value.length() - 1)
	: value;
    } else if (USE_DIRECTORY.equals(name)) {
      useDirectory = "true".equals(value);
    } else if (BASE.equals(name)) {
      base = value.endsWith("/") ? value : (value + '/');
    } else if (MAX_DOWNLOAD_ERRORS.equals(name)) {
      try {
	maxDownloadErrors = Integer.parseInt(value);
      } catch (Exception e) {
	throw new IllegalArgumentException("maxDownloadErrors must be "
					   + "an integer");
      }
    }
  }

  public void extract(TACGameLogListener listener, String path) {
    if (!extractFile(listener, path)) {
      log.severe("could not find file '" + path + '\'');
    }
    listener.finishedGames();
  }

  public void extract(TACGameLogListener listener) {
    extract(listener, -1, -1, false);
  }

  public void extract(TACGameLogListener listener,
		      int startGame, int endGame) {
    extract(listener, startGame, endGame, false);
  }

  public void extract(TACGameLogListener listener, int startGame, int endGame,
		      boolean download) {
    LogSession session =
      new LogSession(this, listener, startGame, endGame, download,
		     useDirectory);
    session.run();
    listener.finishedGames();
  }


  private String getFileName(String name) {
    return useDirectory
      ? (path + File.separatorChar
	 + name + File.separatorChar + "applet.log")
      : (path + File.separatorChar + name);
  }

  private String getFileName(int gameID) {
    return useDirectory
      ? (path + File.separatorChar + gameID
	 + File.separatorChar + "applet.log")
      : (path + File.separatorChar + "applet" + gameID + ".log");
  }


  /*********************************************************************
   * API towards LogSession
   *********************************************************************/

  // Extract all games accepted by the session with no downloading
  void extract(LogSession session) {
    File fp = new File(path);
    if (fp.exists() && fp.isDirectory()) {
      File[] childs = fp.listFiles(session);
      if (childs != null) {
	Arrays.sort(childs, session);
// 	if (childs.length > 0) {
// 	  System.out.println("CHILDS:");
// 	  for (int i = 0, n = childs.length; i < n; i++) {
// 	    System.out.println("  " + i + ": " + childs[i].getName());
// 	  }
// 	}
	for (int i = 0, n = childs.length; i < n; i++) {
	  String name = getFileName(childs[i].getName());
	  if (!extractFile(session.getHandler(), name)) {
	    log.warning("could not parse '" + name + '\'');
	  }
	}
      }
    }
  }

  void extract(LogSession session, int startGame, int endGame) {
    if (startGame < 1 || endGame < 1) {
      throw new IllegalArgumentException("game interval must be specified");
    }

    TACGameLogListener listener = session.getHandler();
    int numberOfErrors = 0;
    for (int i = startGame; i <= endGame; i++) {
      String name = getFileName(i);
      if (!extractFile(listener, name)) {
	// Make sure not to download games to often to avoid overloading
	// the web server.
	try {
	  Thread.sleep(800);
	} catch (Exception e) {
	}

	if (download(i)) {
	  numberOfErrors = 0;
	  extractFile(listener, name);
	} else {
	  numberOfErrors++;
	  if (numberOfErrors > maxDownloadErrors) {
	    log.severe("terminating log extraction after " + numberOfErrors
		       + " sequential download errors");
	    break;
	  }
	}
      }
    }
  }

  private boolean extractFile(TACGameLogListener listener, String path) {
    try {
      Reader input;
      // Handle gzipped applet files
      if (path.endsWith(".gz")) {
	input = openGzipFile(path);
      } else {
	try {
	  input = new FileReader(path);
	} catch (FileNotFoundException e) {
	  input = openGzipFile(path + ".gz");
	}
      }

      BufferedReader reader = new BufferedReader(input);
      String line;
      TACGameInfo game = new TACGameInfo();
      boolean reportOpened = true;
      while ((line = reader.readLine()) != null) {
	// Ignore empty lines
	if ((line = line.trim()).length() > 0) {
	  game.gameData(new ISTokenizer(line));
	  if (reportOpened && game.getGameID() > 0) {
	    reportOpened = false;
	    listener.gameOpened(path, game);
	  }
	}
      }
      listener.gameClosed(path, game);
      reader.close();
      return true;
    } catch (FileNotFoundException e) {
      return false;
    } catch (Exception e) {
      log.log(Level.SEVERE, "could not parse file '" + path + '\'', e);
      return false;
    }
  }

  private Reader openGzipFile(String name) throws IOException {
    FileInputStream input = new FileInputStream(name);
    return new InputStreamReader(new GZIPInputStream(input, 2048));
  }

  private boolean download(int gameID) {
    if (pathFlag < 2) {
      if (pathFlag == 1) {
	return false;
      }
      // Make sure the game path exists
      File fp = new File(path);
      if (!fp.exists() && !fp.mkdirs()) {
	pathFlag = 1;
	return false;
      } else {
	pathFlag = 2;
      }
    }

    String baseURL = base + gameID + "/applet.log";
    try {
      URL url = new URL(baseURL);
      URLConnection conn = url.openConnection();
      conn.setRequestProperty("User-Agent", USER_AGENT);
      InputStream input = conn.getInputStream();
      log.finer("downloading " + baseURL + "...");
      try {
	if (useDirectory) {
	  String name = path + File.separatorChar + gameID;
	  File fp = new File(name);
	  if (!fp.exists() && !fp.mkdirs()) {
	    log.warning("could not create directory " + name);
	    return false;
	  }
	}

	FileOutputStream out = new FileOutputStream(getFileName(gameID));
	byte[] buffer = new byte[8192];
	int n;
	while ((n = input.read(buffer)) > 0) {
	  out.write(buffer, 0, n);
	}
	out.close();
	return true;
      } finally {
	input.close();
      }
    } catch (FileNotFoundException e) {
      log.log(Level.WARNING, "could not find " + baseURL);
      return false;
    } catch (Exception e) {
      log.log(Level.WARNING, "could not download game " + baseURL, e);
      return false;
    }
  }


  /*********************************************************************
   * TACGameLogListener interface
   *********************************************************************/

  public void init(ArgEnumerator a) {
  }

  public String getUsage() {
    return null;
  }

  public void gameOpened(String path, TACGameInfo game) {
    System.out.println("OPENING GAME " + game.getGameID());
    // Minor optimization: the quotes are not needed here
    game.setProperty(TACGameInfo.IGNORE_QUOTES, "true");
    game.setProperty(TACGameInfo.IGNORE_BIDS, "true");
  }

  public void gameClosed(String path, TACGameInfo game) {
    // DEBUG OUTPUT
//     System.out.println("Quotes for game " + game.getGameID());
//     for (int i = 8, n = 15; i <= n; i++) {
//       TACQuote[] quotes = game.getAuctionQuotes(i);
//       int aid = game.getAuctionID(i);
//       String au = game.getItemName(game.getAuctionType(i)) + ' '
// 	+ game.getAuctionDay(i);
//       if (quotes != null && quotes.length > 0) {
// 	for (int j = 0, m = quotes.length; j < m; j++) {
// 	  TACQuote q = quotes[j];
// 	  System.out.println(aid + " (" + au + "): "
// 			     + (q.getLastUpdated() / 1000)
// 			     + ',' + q.getAuction()
// 			     + ',' + q.getAsk() + ',' + q.getBid());
// 	}
//       } else {
// 	System.out.println("Auction " + au + " had no quotes");
//       }
//     }
  }

  public void finishedGames() {
  }


  /*********************************************************************
   * Startup handling
   *********************************************************************/

  public static void main(String[] args) throws IOException {
    main(args, null);
  }

  public static void main(String[] args, TACGameLogListener listener) {
    String usage =
      "Usage: LogExtractor [-options]\n"
      + "where options include:\n"
      + "    -path <path>             set game data path\n"
      + "    -base <base URL>         set the base to download game data from\n"
      + "    -download                download game data if needed\n"
      + "    -useDirectory            use separate directory for each game data\n"
      + (listener == null
      ? "    -handler <className>     set handler for game data\n"
	 : "")
      + "    -consoleLogLevel <level> set the console log level\n"
      + "    -fileLogLevel <level>    set the file log level\n"
      + "    -logFile <logfile>       write log to this file\n"
      + "    -startGame <game id>     only games with this id and higher\n"
      + "    -endGame <game id>       only games with this id and lower\n"
      + "    -file <game data file>   parse this game log file\n"
      + "    -version                 show version information\n"
      + "    -h                       show this help message\n";
    ArgEnumerator a =
      new ArgEnumerator(args, usage
			+ "and various handler options (specify handler and use '-h' for more information)\n",
			false);
    String className = listener == null ? a.getArgument("-handler") : null;
    String appletFile = a.getArgument("-file");
    String logPath = a.getArgument("-path");
    String base = a.getArgument("-base");
    int startGame = a.getArgument("-startGame", -1);
    int endGame = a.getArgument("-endGame", -1);
    int consoleLevel = a.getArgument("-consoleLogLevel", 3);
    Level consoleLogLevel = LogFormatter.getLogLevel(consoleLevel);
    int fileLevel = a.getArgument("-fileLogLevel", 0);
    Level fileLogLevel = LogFormatter.getLogLevel(fileLevel);
    String logFile = a.getArgument("-logFile", null);
    boolean doDownload = a.hasArgument("-download");
    boolean useDirectory = a.hasArgument("-useDirectory");
    if (a.hasArgument("-version")) {
      System.out.println("LogExtractor version " + VERSION);
      System.exit(0);
    }
    // Create game data handler if such was specified
    if (className != null) {
      try {
	Class listenerClass = Class.forName(className);
	listener = (TACGameLogListener) listenerClass.newInstance();
      } catch (Throwable e) {
	System.err.println("could not create handler '" + className + '\'');
	e.printStackTrace();
	a.usage(1);
      }
    }
    // Check if the user simply requested the help text
    if (a.hasArgument("-h") || a.hasArgument("-help")) {
      if (listener != null) {
	a.setUsage(usage + "\nOptions for handler " + className
		   + '\n' + listener.getUsage() + '\n');
      }
      a.usage(0);
    }

    if (listener != null) {
      listener.init(a);
    }
    // Warn for any unknown arguments
    a.checkArguments();

    // allow garbage collection
    a = null;

    Logger root = Logger.getLogger("");
    LogFormatter.setConsoleLevel(consoleLogLevel);
    if (fileLogLevel != Level.OFF && logFile != null) {
      try {
	FileHandler fileHandler = new FileHandler(logFile, true);
	fileHandler.setLevel(fileLogLevel);
	root.addHandler(fileHandler);
      } catch (IOException e) {
	System.err.println("could not open log file '" + logFile + '\'');
	e.printStackTrace();
	System.exit(1);
      }
    }
    root.setLevel(Level.FINEST);
    LogFormatter formatter = new LogFormatter();
    // Set to use shorter log names
    formatter.setAliasLevel(1);
    LogFormatter.setFormatterForAllHandlers(formatter);

    LogExtractor extractor = new LogExtractor();
    if (logPath != null) {
      extractor.setProperty(PATH, logPath);
    }
    if (base != null) {
      extractor.setProperty(BASE, base);
    }
    if (useDirectory) {
      extractor.setProperty(USE_DIRECTORY, "true");
    }
    if (listener == null) {
      listener = extractor;
    }
    if (appletFile != null) {
      extractor.extract(listener, appletFile);
    } else {
      extractor.extract(listener, startGame, endGame, doDownload);
    }
  }
}
