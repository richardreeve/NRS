package nrs.core.message;

import nrs.core.base.Message;
import nrs.core.base.Variable;

/**
 * Utility class for building <tt>ReplyVNName</tt> messages
 *
 * @author Darren Smith
 */
public class ReplyVNName extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyVNName()
  {
    super(Constants.MessageTypes.ReplyVNName);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>ReplyVNName</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param vnName name of variable or node queried, or empty if it
   * didn't exist
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        String vnName)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.MessageFields.vnName, vnName);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting the required messages fields to make
   * an appropriate reply to a QueryVNName message.
   *
   * @param query the {@link Message} representing a QueryVNName for
   * which a reply is being built. If the query does not contain a
   * returnToVNID field then the reply will be given an iTargetVNName
   * field with value {@link Constants.MessageTypes#ReplyVNName}.
   *
   * @param var the {@link Variable} which is responding to the query,
   * or <tt>null</tt> if no message variable corresponded to the query,
   * in which case an appropriate variable-not-found reply is
   * constructed.
   */
  public void setFields(Message query, Variable var)
  {
    setRoute(query);

    if (query.hasField(Constants.MessageFields.returnToVNID))
    {
      setNRSField(Constants.MessageFields.toVNID,
               query.getField(Constants.MessageFields.returnToVNID));
    }
    else
    {
      setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.ReplyVNName);
      setNRSField(Constants.MessageFields.intelligent, "true");
      setNRSField(Constants.MessageFields.toVNID, Integer.toString(0));
    }

    setReplyMsgID(query);

    if (var != null)
    {
      setField(Constants.MessageFields.vnName, var.getVNName());
    }
    else
    {
      setField(Constants.MessageFields.vnName, "");
    }
  }
}
