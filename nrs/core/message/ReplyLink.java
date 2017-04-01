package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyLink</tt> messages
 *
 * @author Thomas French
 */
public class ReplyLink extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyLink()
  {
    super(Constants.MessageTypes.ReplyLink);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>ReplyLink</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param cid CID of component at other end of link
   *
   * @param vnid vnid of variable at other end of connection 
   * (can be 0 if logging connection)
   *
   * @param logPort if connection is log connection this is output for log, 
   * otherwise empty
   *
   * @param resolved whether this link is fully resolved or not
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        String cid,
                        int vnid, 
                        String logPort,
                        boolean resolved)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.MessageFields.cid, cid);

    setField(Constants.MessageFields.vnid, Integer.toString(vnid));

    setField(Constants.MessageFields.logPort, logPort);

    setField(Constants.MessageFields.resolved, Boolean.toString(resolved));
  }
}
