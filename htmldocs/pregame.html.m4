m4_page_begin(
TAC - Pre/Post Game Callbacks
)

m4_heading1(Pre/Post Game Callbacks)
The methods that are called in the agent implementation (AgentImpl)
before/after a TAC game.
m4_heading2(gameStarted())
This method is called when a game that this agent should
play has started.When this is called, client preferences
and auction information have been received from the server.


m4_heading2(gameStopped())
This method is called when the current game has finished.
m4_page_end()