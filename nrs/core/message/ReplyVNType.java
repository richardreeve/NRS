package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyVNType</tt> messages
 *
 * @author Darren Smith
 */
public class ReplyVNType extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyVNType()
  {
    super(Constants.MessageTypes.ReplyVNType);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>ReplyVNType</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param vnType VNType of the variable or node being queried, or
   * empty if no such vnid exists
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        String vnType)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.MessageFields.vnType, vnType);
  }
}
