m4_page_begin(
TAC - Quote
)

m4_heading1(Quote)
The Quote class holds all information about a quote that has
been reported from the TAC server.

m4_heading2(float getAskPrice())
returns the last ask price.

m4_heading2(float getBidPrice())
returns the last bid price.

m4_heading2(boolean hasHQW(Bid bid))
returns true if the HQW is valid for the bid 'bid'.

m4_heading2(int getHQW())
returns the current HQW (Hypothetical Quantity Won).

m4_heading2(Bid getBid())
return the bid that was active when the quote
was requested.

m4_heading2(int isAuctionClosed())
returns true if the auction is closed and false otherwise.

m4_heading2(int getAuctionStatus())
returns the auction status.

m4_heading2(long getNextQuoteTime())
returns the next time the quote is updated (less or equal to 0 if not
supported or not known).

m4_heading2(long getLastQuoteTime())
returns the time for the last quote update (less or equal to 0 if not
yet updated).

m4_page_end()
