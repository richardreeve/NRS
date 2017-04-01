package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>CreateNode</tt> messages
 *
 * @author Darren Smith
 */
public class CreateNode extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public CreateNode()
  {
    super(Constants.MessageTypes.CreateNode);
  }
  //----------------------------------------------------------------------
  /**
   * Constructor, which fully describes a message.
   *
   * @see #setFields(String route, int toVNID, String vnType, int vnid,
   * String vnName)
   */
  public CreateNode(String route,
                    int toVNID,
                    String vnType,
                    int vnid,
                    String vnName)
  {
    this();

    setFields(route, toVNID, vnType, vnid, vnName);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>CreateNode</tt> message
   *
   * @param route route to component
   *
   * @param toVNID VNID of target variable
   *
   * @param vnType type of node to be created
   *
   * @param vnid VNID of node to be created, usually 0 to indicate
   * unspecified, but can be non-zero if VNID is specified in attributes
   *
   * @param vnName name of node to be created
   */
  public void setFields(String route,
                        int toVNID,
                        String vnType,
                        int vnid,
                        String vnName)
  {
     setNRSField(Constants.MessageFields.route, route);

     setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

     setField(Constants.MessageFields.vnType, vnType);

     setField(Constants.MessageFields.vnid,
              Integer.toString(vnid));

     setField(Constants.MessageFields.vnName, vnName);
  }
}
