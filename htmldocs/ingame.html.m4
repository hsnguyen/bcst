m4_page_begin(
TAC - In Game Callbacks
)


m4_heading1(In Game Callbacks)
The methods that are called in the agent implementation (AgentImpl)
during a TAC game.
m4_heading2(quoteUpdated(Quote quote))
Called by the AgentWare when new quote information has been
received from the server.
m4_heading2(quoteUpdated(int auctionCategory))
Called by the AgentWare when all quote information for the specified
category of auctions have been received from the server.


m4_heading2(bidUpdated(Bid bid))
Called when the AgentWare has received new information about the
specified bid 'Bid' from the server.
m4_heading2(bidRejected(Bid bid))
Called when the AgentWare has been information that the bid
has been rejected.

m4_heading2(bidError(Bid bid, int status))
This method is called when it was not possible to submit the specified
bid to the auction (status correspond to the commandStatus in the
TAC protocol).

m4_heading2(auctionClosed(int auctionID))
Called when an auction has closed.

<p>
When auctionClosed is called the following statements hold:
<ul>
<li>The auction with ID = AuctionID has closed
<li>Own contains the number of items bought in the auction (+ endowments)
<li>All transactions for this auction have been received
</ul>

m4_heading2(transaction(Transaction transaction))
Called when the AgentWare has received transaction information. When it
is called the own quantity returned from the predicate getOwn(int auction)
will be updated based on the information in this transaction.

m4_page_end()
