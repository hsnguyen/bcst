/**
 * SICS TAC Server - InfoServer
 * http://www.sics.se/tac/	  tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * Transaction
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 12 April, 2002
 * Updated : $Date: 2003/01/08 17:01:37 $
 *	     $Revision: 1.3 $
 * Purpose :
 *
 */

package se.sics.tac.util;

public class Transaction {

  private final int buyer;
  private final int seller;
  private final int auction;
  private final int quantity;
  private final float price;
  private final long time;

  public Transaction(int buyer, int seller, int auction,
		     int quantity, float price, long time) {
    this.buyer = buyer;
    this.seller = seller;
    this.auction = auction;
    this.quantity = quantity;
    this.price = price;
    this.time = time;
  }

  public boolean isParticipant(int agent) {
    return agent == buyer || agent == seller;
  }

  public int getBuyer() {
    return buyer;
  }

  public int getSeller() {
    return seller;
  }

  public int getAuction() {
    return auction;
  }

  public int getQuantity() {
    return quantity;
  }

  public float getPrice() {
    return price;
  }

  public long getTime() {
    return time;
  }

} // Transaction
