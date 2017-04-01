package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>DeleteNode</tt> messages
 *
 * @author Darren Smith
 */
public class DeleteNode extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public DeleteNode()
  {
    super(Constants.MessageTypes.DeleteNode);
  }
  //----------------------------------------------------------------------
  /**
   * Constructor which fully describes a message.
   *
   * @see #setFields(String route, int toVNID, int vnid)
   */
  public DeleteNode(String route,
                    int toVNID,
                    int vnid)
  {
    this();

    setFields(route, toVNID, vnid);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>DeleteNode</tt> message
   *
   * @param route route to component
   *
   * @param toVNID VNID of target variable
   *
   * @param vnid VNID of node to be deleted -  can be empty if next is full
   *
   */
  public void setFields(String route,
                        int toVNID,
                        int vnid)
  {
     setNRSField(Constants.MessageFields.route, route);

     setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

     setField(Constants.MessageFields.vnid, Integer.toString(vnid));
  }
}
