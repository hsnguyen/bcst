m4_page_begin(
TAC - Transaction
)

m4_heading1(Transaction)
The transaction class holds all information about a
transaction that has been reported from the TAC server.

m4_heading2(int getQuantity())
returns the number of items that the agent sold or bought.
m4_heading2(float getPrice())
returns the price of one item (unit price)
m4_heading2(int getAuction())
returns the auction ID of this transaction.

m4_page_end()