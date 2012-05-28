m4_page_begin(
TAC Java AgentWare - Changes
)

m4_heading1(Changes)
The current version of the AgentWare is m4_tac_version().
<p>

Changes in Beta 6 include:
<ul>
<li> Added recovering of bids at startup (in case the agent is
  restarted during a game). Also immediately requests hotel
  quotes to get latest HQW for recovered bids.
<li> Added option to display a compact representation of allocation
  and own information at regular intervals in the log file.
<li> Minor bug fixes
</ul>

Changes in Beta 5 include:
<ul>
<li> Added Bid.isAwaitingTransactions() that returns true if the bid
  or part of it has been transacted, and the transaction info
  requested but not yet received (and getOwn() not yet updated).
  The bid will no longer be updated immediately when a transaction
  is determined with bidInfo but the change will be pending until
  the transaction information is retrieved and getOwn() can be updated.
<li>Fixed so that a transacted bid (when bidString gets non-empty)
  is not "preliminary" (bug fix)
<li> bidUpdated() will not be called until the transactions
  been received for the bid
<li> Fixed logging to display XML_IN and XML_out messages together
<li> Various bug fixes
</ul>

Changes in Beta 4 include:
<ul>
<li> Fixed a bug that made the AW request quotes with auctionID = 0
<li> And other minor bug fixes.
</ul>

Changes in Beta 3 include (<font color=red>API changes</font>):
<ul>
<li> AgentImpl is now an abstract class
<li> New method: quoteUpdated(int auctionCategory)<br>
Called when all quotes for a specific category of auctions have been updated
<li> getStatusAsString in Quote has been renamed to getAuctionStatusAsString
<li> getTypeAsString in TACAgent has been renamed to getAuctionTypeAsString
<li> Hotel quotes are now requested as soon as they are updated instead
of each 30 seconds (hotel quotes are known to be updated once per minute,
on the minute)
<li> TACAgent can handle startup with support for arguments and config file
<li> Generates separate log files for each game
<li> Lots of bug fixes
</ul>
<p>

Changes in Beta 2 include:
<ul>
<li>measurements of response time and number of messages sent
<li>bug fixes in communication with server (more robust)
<li>-nogui argument for agent to run without GUI
</ul>

m4_page_end()
