m4_page_begin(
TAC - accessing data
)

m4_heading1(Data Access Primitives)
The data access primitives lets you access different sorts
of data that is necessary to play a game of TAC well.
<p>
All these methods are located in the TACAgent class. At initialization
an agent will get access to a TACAgent instance which it will use to
communicate with the TAC server and to retrieve information about the
game.

m4_heading2(int getClientPreference(int client, int type))
Returns the client preference for the specified type.
Types are ARRIVAL, DEPARTURE, HOTEL_VALUE, E1, E2, and E3,
and they are defined in TACAgent.

m4_heading2(int getAuctionNo())
Returns the number of auctions in TAC.

m4_heading2(int getAuctionFor(int category, int type, int day))
Returns the auction-id for the requested resource.
Categories are CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
and types are TYPE_INFLIGHT, TYPE_OUTFLIGHT, etc.

m4_heading2(int getAuctionCategory(int auction))
Returns the category for this auction (see above).

m4_heading2(int getOwn(int auction))
Returns the number of items that the agent own for this auction.

m4_heading2(int getProbablyOwn(int auction))

Returns the number of items that the agent currently does not have but
might have in the near future i.e. the quantity in placed bids or HQW
if that is known. The actually quantity an agent might have if any
bids for this auction is fully transacted is getOwn(auction) +
getProbablyOwn(auction). Also note that if an agent both tries to sell
one item and buy one item at an entertainment auctions, the probably
own for that auction is zero.

m4_heading2(Bid getBid(int auction))
Returns the active bid for the specified auction.

m4_heading2(Quote getQuote(int auction))
Returns the current quote for the specified auction.

m4_heading2(int getAllocation(int auction))
Returns the agents allocation for this auction (number of items that
it wants to own).

m4_heading2(void setAllocation(int auction, int quantity))
Sets the number of items that the agent should own for this auction.

m4_page_end()
