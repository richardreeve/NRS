package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>FailedRoute</tt> messages
 *
 * @author Darren Smith
 * @author Thomas French
 */
public class FailedRoute extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public FailedRoute()
  {
    super(Constants.MessageTypes.FailedRoute);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>FailedRoute</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param cid CID of component which rejected the message
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

    setField(Constants.Fields.F_cid, cid);
  }
}
