package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryMaxLink</tt> messages
 *
 * @author Thomas French
 */
public class QueryMaxLink extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryMaxLink()
  {
    super(Constants.MessageTypes.QueryMaxLink);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryMaxLink</tt> message
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
   * @param vnid variable ID being queried
   *
   * @param sourceNotTarget whether the query refers to how many times the 
   * vnid is source or target of a connection
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        int vnid,
                        boolean sourceNotTarget)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.returnRoute, returnRoute);

    setField(Constants.MessageFields.returnToVNID,
             Integer.toString(returnToVNID));

    setField(Constants.MessageFields.msgID, msgID);

    setField(Constants.MessageFields.vnid, Integer.toString(vnid));

    setField(Constants.MessageFields.sourceNotTarget, 
             Boolean.toString(sourceNotTarget));
  }
}
