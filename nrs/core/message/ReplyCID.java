package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyCID</tt> messages
 *
 * @author Darren Smith
 */
public class ReplyCID extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyCID()
  {
    super(Constants.MessageTypes.ReplyCID);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>ReplyCID</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param cid CID of component
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        String cid)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.MessageFields.cid, cid);
  }
}
