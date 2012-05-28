This is the beta 6 version of the SICS TAC AgentWare for Java.

You will need Java SDK 1.4 (you can find it at http://www.javasoft.com)
to be able to develop and run TAC agents using this AgentWare.


Features of the AgentWare:
--------------------------

- automatic connection, login and retrieval of game data
- automatic refreshing of bids and quote information (each 30 seconds)
  (will send bidInfo and getQuote to the server and call the agent when
   the answers been received)
- asynchronous communication with the TAC server
- bookkeeping of transactions so that the agent knows what it own
- window showing the internal state of the agent, bids, ownership, etc.
- logging to disk


Getting the DummyAgent to run.
------------------------------

There are brief documentation about a few of the important methods
and callbacks in the header of the DummyAgent file.

Compiling
---------
Type "make" to compile the AgentWare (and the DummyAgent)

(or "javac se/sics/tac/*/*.java" followed by
 "jar cfm tacagent.jar AWManifest.txt se/sics/tac/*/*.class"
 if you do not have make installed)

Running
-------
Type "java -jar tacagent.jar -agent <agentname> -password <password>",
with "<agentname>" and "<password>" replaced with your own agent name
and password, to run the DummyAgent.

You should also add the argument "-host tac.sics.se" if you want it to
connect to the server at SICS (which will run the TAC'02 games). If
you do not specify a host it will connect to a server on your machine
(localhost).

Please note that you must have registered the agent at the server
before starting it.  For the SICS TAC servers new agents can be
registered at http://www.sics.se/tac/server/

If everything is all right the DummyAgent will connect to the server
and a window showing the "internal" state of the agent will be shown.


Configuring the AgentWare
-------------------------

The AgentWare is reading the configuration file 'agent.conf' at
startup. This file allows, among other things, the configuration
of log levels and agent implementation. See the file 'agent.conf'
for more information.


If you have any questions or comments regarding this AgentWare
please contact tac-dev@sics.se

-- The SICS TAC Team
