package se.sics.tac.sicsagent;
import se.sics.tac.sicsagent.*;
import se.sics.tac.aw.*;

public abstract class AgentStrategy {
  protected SICSAgent agent;

  public void init(SICSAgent agent) {
    this.agent = agent;
  }
  
  /*
   * - there are TACAgent have received an answer on a bid query/submission
   * (new information about the bid is available)
   */
  public abstract void bidUpdated(Bid bid);

  /*
   * - the bid has been rejected (reason is bid.getRejectReason())
   */
  public abstract void bidRejected(Bid bid);
  
  /*
   * - the bid contained errors (error represent error status - commandStatus)
   */
  public abstract void bidError(Bid bid, int error);

  
  /*
   * - new information about the quotes on the auction (quote.getAuction())
   * has arrived
   */
  public abstract void quoteUpdated(Quote quote);
  public abstract void quoteUpdated(int cat);
  
  /*
   * - the auction with id "auction" has closed
   */
  public abstract void auctionClosed(int auction);
  
  /*
   * - there has been a transaction
   */
  public abstract void transaction(Transaction transaction);
  
  /*
   * - a TAC game has started, and all information about the
   * game is available (preferences etc).
   */
  public abstract void gameStarted();

  /*
   * - the game is over
   */
  public abstract void gameStopped();

  /*
   * - strategy is asked to update the priceinformation to the solver
   */
  public abstract void setSolverPrice(SolverSession s);

  /*
   * - the solver has come up with a new result
   */
  public abstract void solverReport(SolverSession session);
}







