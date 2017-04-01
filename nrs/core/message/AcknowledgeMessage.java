package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>AcknowledgeMessage</tt> messages
 *
 * @author Darren Smith
 * @author Thomas French
 */
public class AcknowledgeMessage extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public AcknowledgeMessage()
  {
    super(Constants.MessageTypes.AcknowledgeMessage);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>AcknowledgeMessage</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param success whether the message succeeded in its task
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        boolean success,
                        boolean failure)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.Fields.F_success, Boolean.toString(success));
  }
}
