package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryCType</tt> messages
 *
 * @author Darren Smith
 */
public class QueryCType extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryCType()
  {
    super(Constants.MessageTypes.QueryCType);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryCType</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param returnRoute return route to take. Typically provide an empty
   * string.
   *
   * @param returnToVNID return variable ID
   *
   * @param messageID unique identifier for message to identify the reply
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String messageID)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, messageID);
  }
}
