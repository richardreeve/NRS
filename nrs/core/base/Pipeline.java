package nrs.core.base;

/**
 *
 * @author Thomas French
*/
public class Pipeline extends MessageProcessor 
{
   /** Constructor. */
  public Pipeline(){}

  //----------------------------------------------------------------------
  /**
   * Deliver a {@link Message} object for processing and/or routing.
   *
   * @param m the {@link Message} being transferred
   * @param sender the {@link MessageProcessor} which is sending the message
   */
  public void deliver(Message m, MessageProcessor sender){
    if (m.aux().getReceivedPort() != null)
      {
      PackageLogger.log.fine("Received " + m.diagString()
                             + " on port:" + m.aux().getReceivedPort());
    }
    else
      {
      PackageLogger.log.fine("Received " + m.diagString()
                             + " on port: NOT AVAILABLE");
    }
    
    // pass message on to next processor
    next(m);
  }

}
