package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyRoute</tt> messages
 *
 * @author Thomas French
 */
public class ReplyRoute extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyRoute()
  {
    super(Constants.MessageTypes.ReplyRoute);
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
   * @param forwardRoute
   *
   * @param returnRoute 
   *
   * @param translationCount number of translations that were done between
   *                         source and destination.
   *
   */
    public void setFields(String route,
			  int toVNID,
			  String replyMsgID,
			  String forwardRoute,
			  String returnRoute,
			  String translationCount)
    {
	setNRSField(Constants.MessageFields.route, route);

	setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

	setField(Constants.MessageFields.replyMsgID, replyMsgID);

	setField(Constants.MessageFields.forwardRoute, forwardRoute);

	setField(Constants.MessageFields.returnRoute, returnRoute);

	setField(Constants.MessageFields.translationCount, translationCount);
    }
}
