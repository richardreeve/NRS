package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryVNType</tt> messages
 *
 * @author Darren Smith
 */
public class QueryVNType extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryVNType()
  {
    super(Constants.MessageTypes.QueryVNType);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryVNType</tt> message
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
   * @param msgID unique identifier for message to identify the reply
   *
   * @param vnid VNID of the variable or node being queried
   */
  public void setNRSFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        int vnid)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.vnid, Integer.toString(vnid));
  }
}
