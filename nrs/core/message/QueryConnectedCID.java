package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryConnectedCID</tt> messages
 *
 * @deprecated this message type is nolonger used in NRS
 *
 * @author Darren Smith
 */
public class QueryConnectedCID extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryConnectedCID()
  {
    super(Constants.MessageTypes.QueryConnectedCID);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryConnectedCID</tt> message
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
   * @param cid suggested CID that receipient component should take, if
   * it does not already have one.
   *
   * @param connection index of the connection to which to connect
   *
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        String cid,
                        int connection)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.cid, cid);

    setField(Constants.MessageFields.connection,
             Integer.toString(connection));
  }
}
