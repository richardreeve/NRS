package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryCID</tt> messages
 *
 * @author Darren Smith
 */
public class QueryCID extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryCID()
  {
    super(Constants.MessageTypes.QueryCID);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryCID</tt> message
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
   * @param cid suggested CID that receipient component should take, if
   * it does not already have one.
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        String cid)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.cid, cid);
  }
}
