m4_page_begin(
Bid
)

m4_heading1(Bid)
Bid is the class that contains all the information about a bid.

m4_heading2(new Bid(int auction)) 
Creates a bid that will be submitted to the auction 'auction'.

m4_heading2(void addBidPoint(int quantity, float unitPrice))
adds a new bid point to this bid. Note that an exception will be 
thrown if you add a bid point with a negative price or 
a negative quantity for an auction that does not allow sell bids.

m4_heading2(int getNoBidPoints())
returns the number of bid points in this bid.

m4_heading2(int getQuantity(int index))
returns the quantity for bid point at index 'index'.

m4_heading2(float getPrice(int index))
returns the quantity for bid point at index 'index'.

m4_heading2(int getAuction())
returns the auction that this bid is (should be) submitted to.

m4_heading2(int getID())
returns the id of this bid. Can be -1 if no ID is set.

m4_heading2(boolean isRejected())
true if this bid has been rejected.

m4_heading2(int getRejectReason())
returns the reject reason for this bid.

m4_page_end()