package nrs.core.message;

import nrs.core.base.Message;
import nrs.core.base.Variable;

/**
 * Utility class for building <tt>ReplyVNID</tt> messages
 *
 * @author Darren Smith
 */
public class ReplyVNID extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public ReplyVNID()
  {
    super(Constants.MessageTypes.ReplyVNID);
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>ReplyVNID</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   * @param replyMsgID message ID of the original query
   *
   * @param vnid VNID of the variable or node being queried, or empty if
   * no such name exists
   */
  public void setFields(String route,
                        int toVNID,
                        String replyMsgID,
                        int vnid)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

    setField(Constants.MessageFields.replyMsgID, replyMsgID);

    setField(Constants.MessageFields.vnid, Integer.toString(vnid));
  }
  //----------------------------------------------------------------------
  /**
   * Convenience method for setting the required messages fields to make
   * an appropriate reply to a QueryVNID message.
   *
   * @param query the {@link Message} representing a QueryVNID for which
   * a reply is being built. If the query does not contain a
   * returnToVNID field then the reply will be given an iTargetVNName
   * field with value {@link Constants.MessageTypes#ReplyVNID}.
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
               Constants.MessageTypes.ReplyVNID);
      setNRSField(Constants.MessageFields.intelligent, "true");
      setNRSField(Constants.MessageFields.toVNID, Integer.toString(0));
    }

    setReplyMsgID(query);

    int reply_vnid;
    if (var != null)
    {
      reply_vnid = var.getVNID();
    }
    else
    {
      reply_vnid = 0;
    }
    setField(Constants.MessageFields.vnid, Integer.toString(reply_vnid));
  }
}
