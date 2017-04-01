package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>CreateLink</tt> messages
 *
 * @author Darren Smith
 */
public class CreateLink extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public CreateLink()
  {
    super(Constants.MessageTypes.CreateLink);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>CreateLink</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param sourceNotTarget whether the message is for the source
      (<code>true</code>) or the target (<code>false</code>) of the connection
   *
   * @param cid unqiue component ID of source component
   *
   * @param vnid vnid of variable which is source of connection
   *
   * @param targetCID unique component ID of target component
   *
   * @param targetVNID vnid of variable which is target of connection
   *
   * @param temporary indicate whether the connection is a temporary
   * connection with just one message
   */
  public void setFields(String route,
                        int toVNID,
                        boolean sourceNotTarget,
                        String cid,
                        int vnid,
                        String targetCID,
                        int targetVNID,
                        //String logPort,
                        boolean temporary)
  {
    setNRSField(Constants.MessageFields.route, route);
    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));
    setField(Constants.MessageFields.sourceNotTarget,
             Boolean.toString(sourceNotTarget));
    setField(Constants.MessageFields.cid, cid);
    setField(Constants.MessageFields.vnid, Integer.toString(vnid));
    setField(Constants.MessageFields.targetCID, targetCID);
    setField(Constants.MessageFields.targetVNID, Integer.toString(targetVNID));
    //setField(Constants.MessageFields.passOnRequest,
    //       Boolean.toString(false));
    //setField(Constants.MessageFields.logPort, logPort);
    setField(Constants.MessageFields.temporary,
             Boolean.toString(temporary));
  }
}
