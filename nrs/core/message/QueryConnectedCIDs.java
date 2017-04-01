package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryConnectedCIDs</tt> messages
 *
 * @deprecated this message type is nolonger used in NRS
 *
 * @author Darren Smith
 */
public class QueryConnectedCIDs extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryConnectedCIDs()
  {
    super(Constants.MessageTypes.QueryConnectedCIDs);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryConnectedCIDs</tt> message
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
