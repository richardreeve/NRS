package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyMaxLink</tt> messages
 *
 * @author Thomas French
 */
public class ReplyMaxLink extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyMaxLink()
  {
    super(Constants.MessageTypes.ReplyMaxLink);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>ReplyMaxLink</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param link number at least as great as the highest numbered link in the 
   * variable
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        int link)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.MessageFields.link, Integer.toString(link));
  }
}
