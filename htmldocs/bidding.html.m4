m4_page_begin(
TAC - bidding
)

m4_heading1(Bidding)
These methods are used to submit bids to the TAC server.
<p>
The methods reside in the TACAgent class. 

m4_heading2(void submitBid(Bid bid))
Submits a bid to the TAC server. If the bid is accepted by the
server then the AgentWare will automatically monitor the bid by
requesting bidInfos from the server. When information about
the bid is received there will be a callback to 'bidUpdated(Bid bid)'.
If the bid is rejected there will be a callback to 'bidRejected(Bid bid)',
and if there is an error the callback will be 'bidError(Bid bid, int status)'
 where status correpspond to the value of commandStatus.
<p>

m4_heading2(void replaceBid(Bid newBid))
Replaces the old bid with a new bid. The callbacks are the same as for 
submitBid.
<p>
m4_page_end()
