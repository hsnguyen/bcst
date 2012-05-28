package se.sics.tac.solver;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class TestConnection extends Thread implements ActionListener,
						      WindowListener {

  private TestSolver solver;
  private JFrame window;
  private JTextArea textArea;

  private Socket socket;
  private OutputStreamWriter output;
  private BufferedReader input;

  private JButton clearButton;
  private boolean isRunning = true;

  public TestConnection(TestSolver solver, String host, int port)
    throws IOException
  {
    super("solver@" + host + ':' + port);
    this.solver = solver;
    socket = new Socket(host, port);
    output = new OutputStreamWriter(socket.getOutputStream());
    input =
      new BufferedReader(new InputStreamReader(socket.getInputStream()));

    JPanel p = new JPanel();
    p.add(clearButton = new JButton("Clear"));
    clearButton.addActionListener(this);

    window = new JFrame(getName());
    window.addWindowListener(this);
    window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    window.getContentPane().add(new JScrollPane(textArea = new JTextArea()));
    window.getContentPane().add(p, BorderLayout.SOUTH);
    window.setSize(300, 200);
    window.setVisible(true);
    start();
  }

  public void sendCommand(String command) {
    if (isRunning && input != null) {
      try {
	output.write(command);
	output.flush();
      } catch (Exception e) {
	System.err.println("Solver " + getName() + " could not send command");
	e.printStackTrace();
      }
    }
  }

  public void run() {
    try {
      String line;
      while (isRunning && ((line = input.readLine()) != null)) {

	if (line.length() > 0 && line.charAt(line.length() - 1) != '\n') {
	  line = solver.formatDate(System.currentTimeMillis())
	    + ": " + line + '\n';
	} else {
	  line = solver.formatDate(System.currentTimeMillis())
	    + ": " + line;
	}
	textArea.append(line);
      }
      System.err.println("Solver " + getName() + " closed connection");
      textArea.append(solver.formatDate(System.currentTimeMillis())
		      + ": connection closed...");
    } catch (Exception e) {
      System.err.println("solver " + getName() + " died!");
      e.printStackTrace();
      textArea.append(solver.formatDate(System.currentTimeMillis())
		      + ": connection terminated...");
    } finally {
      if (isRunning) {
	isRunning = false;
	textArea.append(solver.formatDate(System.currentTimeMillis())
			+ ": closing connection...");
      }
      solver.hasDied(this);

      if (window.isVisible()) {
	window.setVisible(false);
	window.dispose();
      }
      try {
	if (input != null) {
	  input.close();
	  input = null;
	  output.close();
	  socket.close();
	}
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }

  void close() {
    if (isRunning) {
      sendCommand("stop.\n");
      isRunning = false;
      textArea.append(solver.formatDate(System.currentTimeMillis())
		      + ": closing connection...");
    }
  }


  /*********************************************************************
   * ActionListener
   *********************************************************************/

  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == clearButton) {
      textArea.setText("");
    }
  }


  /*********************************************************************
   * WindowListener
   *********************************************************************/

  public void windowOpened(WindowEvent e) {
  }

  public void windowClosing(WindowEvent e) {
    close();
  }

  public void windowClosed(WindowEvent e) {
  }

  public void windowIconified(WindowEvent e) {
  }

  public void windowDeiconified(WindowEvent e) {
  }

  public void windowActivated(WindowEvent e) {
  }

  public void windowDeactivated(WindowEvent e) {
  }

} // TestConnection
