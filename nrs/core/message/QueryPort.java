package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryPort</tt> messages
 *
 * @author Darren Smith
 */
public class QueryPort extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryPort()
  {
    super(Constants.MessageTypes.QueryPort);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryPort</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param returnRoute return route
   *
   * @param returnToVNID return target variable ID
   *
   * @param msgID unique message number, for identifying response
   *
   * @param port the index of the port being queried
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        int port)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.port, Integer.toString(port));
  }
}
