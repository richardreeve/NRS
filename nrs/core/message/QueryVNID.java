package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryVNID</tt> messages
 *
 * @author Darren Smith
 */
public class QueryVNID extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryVNID()
  {
    super(Constants.MessageTypes.QueryVNID);
  }
  //----------------------------------------------------------------------
  /**
   * Constructor, which fully describes a message.
   *
   * @see #setFields(String route, int toVNID, String returnRoute, int
   * returnToVNID, String msgID, String vnName)
   */
  public QueryVNID(String route,
                   int toVNID,
                   String returnRoute,
                   int returnToVNID,
                   String msgID,
                   String vnName)
  {
    this();

    setFields(route, toVNID, returnRoute, returnToVNID, msgID, vnName);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryVNID</tt> message
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
   * @param vnName name of the variable or node being queried
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        String vnName)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.vnName, vnName);
  }
}
