package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryRoute</tt> messages
 *
 * @author Darren Smith
 */
public class QueryRoute extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryRoute()
  {
    super(Constants.MessageTypes.QueryRoute);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryRoute</tt> message. Consult the NRS Design Document for
   * more information about these fields.
   *
   * @param route route to component
   *
   * @param toVNID unqiue VNID of target node or variable
   *
   * @param returnRoute return route to take
   *
   * @param returnToVNID return variable ID
   *
   * @param msgID unique identifier for message to identify the reply
   *
   * @param forwardRoute forward route to use for sending back replies
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        String forwardRoute)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.forwardRoute, forwardRoute);
  }
}
