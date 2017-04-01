package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>QueryLink</tt> messages
 *
 * @author Thomas French
 */
public class QueryLink extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public QueryLink()
  {
    super(Constants.MessageTypes.QueryLink);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>QueryLink</tt> message
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
   *
   * @param link number of link in the variable to be queried
   */
  public void setFields(String route,
                        int toVNID,
                        String returnRoute,
                        int returnToVNID,
                        String msgID,
                        int vnid,
                        boolean sourceNotTarget, 
                        int link)
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

    setField(Constants.MessageFields.link, Integer.toString(link));
  }
}
