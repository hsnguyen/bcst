m4_page_begin(
Getting Started
)

m4_heading1(Requirements)
You will need Java SDK 1.4 (you can find it at
<a href="http://www.javasoft.com">http://www.javasoft.com</a>)
to be able to develop and run TAC agents using this AgentWare.

m4_heading1(Getting the DummyAgent to run)

There are brief documentation about a few of the important methods
and callbacks in the header of the DummyAgent file.

m4_heading2(Compiling)
Type "make" to compile the AgentWare (and the DummyAgent)
(or "javac se/sics/tac/*/*.java" followed by
 "jar cfm tacagent.jar AWManifest.txt se/sics/tac/*/*.class"
 if you do not have make installed)

m4_heading2(Running)
Type "java -jar tacagent.jar -agent &lt;agentname&gt; -password &lt;password&gt;"
to run the DummyAgent (logging in as "agentname" with password "password").

You should also add the argument "-host tac.sics.se" if you want it to
connect to the server at SICS (which will run the TAC'02 games). If you
do not specify a host it will connect to a server on your machine (localhost).
If everything is all right the DummyAgent will connect to the server
and a window showing the "internal" state of the agent will be
shown.
<p>
m4_heading2(Configuration)
The AgentWare is reading the configuration file 'agent.conf' at
startup. This file allows, among other things, the configuration
of log levels and agent implementation. See the file 'agent.conf'
for more information.
<p>
If you have any questions or comments regarding this AgentWare
please contact <a href="mailto:tac-dev@sics.se">tac-dev@sics.se</a>
or post a  message at our
<a href="http://www.sics.se/tac/forum/index.php?forum=agentware" target=other>forum</a>.

m4_page_end()
