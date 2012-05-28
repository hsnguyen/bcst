package se.sics.tac.solver;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import javax.swing.*;
import java.text.SimpleDateFormat;

public class TestSolver implements ActionListener {

  private JFrame window = new JFrame("Test Solver");
  private JTextArea textArea;
  private JTextField hostField;
  private JTextField portField;
  private JButton addButton;
  private JButton copyButton;
  private JButton stopButton;
  private JButton sendButton;

  private ArrayList solvers = new ArrayList();

  private SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM HH:mm:ss");
  private Date date = new Date(0L);

  public TestSolver() {
    Container c = window.getContentPane();
    c.add(new JScrollPane(textArea = new JTextArea()), BorderLayout.CENTER);

    JPanel p = new JPanel(new BorderLayout());
    JPanel p2 = new JPanel();
    p2.add(copyButton = new JButton("Copy from clipboard"));
    copyButton.addActionListener(this);
    p2.add(sendButton = new JButton("Send"));
    sendButton.addActionListener(this);
    p2.add(stopButton = new JButton("Stop"));
    stopButton.addActionListener(this);
    p.add(p2, BorderLayout.NORTH);
    p2 = new JPanel();
    p2.add(hostField = new JTextField("127.0.0.1", 20));
    p2.add(portField = new JTextField("6520", 5));
    p2.add(addButton = new JButton("Add Solver"));
    addButton.addActionListener(this);
    p.add(p2, BorderLayout.CENTER);

    c.add(p, BorderLayout.SOUTH);

    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setBounds(100, 100, 400, 250);
    window.setVisible(true);
  }

  public synchronized void addSolver(String host, int port) {
    try {
      solvers.add(new TestConnection(this, host, port));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private synchronized void sendCommand(String command) {
    StringBuffer sb = null;
    int len = command.length();
    for (int i = 0; i < len; i++) {
      char c = command.charAt(i);
      if (c == '\\' || c == '\r' || c == '\n') {
	// Filter out all backspaces and line feeds
	if (sb == null) {
	  sb = new StringBuffer();
	  if (i > 0) {
	    sb.append(command.substring(0, i));
	  }
	}
      } else if (sb != null) {
	sb.append(c);
      }
    }

    if (sb != null) {
      len = sb.length() - 1;
      if (len >= 0 && sb.charAt(len) != '.') {
	sb.append('.');
      }
      command = sb.append('\n').toString();
    } else if (len > 0) {
      if (command.charAt(len - 1) != '.') {
	command = command.substring(0, len) + ".\n";
      } else {
	command = command.substring(0, len) + '\n';
      }
    }
    System.out.print("Sending command:\n" + command);
    for (int i = 0, n = solvers.size(); i < n; i++) {
      ((TestConnection) solvers.get(i)).sendCommand(command);
    }
  }

  synchronized void hasDied(TestConnection connection) {
    solvers.remove(connection);
  }

  synchronized String formatDate(long time) {
    date.setTime(time);
    return dFormat.format(date);
  }


  /*********************************************************************
   * ActionListener
   *********************************************************************/

  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == sendButton) {
      String text = textArea.getText();
      if (text != null && text.length() > 0) {
	sendCommand(text);
      }
    } else if (source == copyButton) {
      try {
	Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable data = clip.getContents(this);
	if (data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
	  String text = (String)
	    data.getTransferData(DataFlavor.stringFlavor);
	  textArea.setText(text);
	}
      } catch (Exception e) {
	System.err.println("could not import text from clipboard");
	e.printStackTrace();
      }

    } else if (source == stopButton) {
      sendCommand("stop");

    } else if (source == addButton) {
      String host = hostField.getText();
      String portS = portField.getText();
      int port = -1;
      try {
	port = Integer.parseInt(portS);
      } catch (Exception e) {
	e.printStackTrace();
      }
      if (host.length() > 0 && port > 0) {
	addSolver(host, port);
      }
    }
  }


  /*********************************************************************
   *
   *********************************************************************/
  public static void main(String[] args) throws NumberFormatException {
    TestSolver tester = new TestSolver();
    for (int i = 0, n = args.length; i < n; i += 2) {
      tester.addSolver(args[i], Integer.parseInt(args[i + 1]));
    }
  }

} // TestSolver
