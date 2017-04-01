package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyCSL</tt> messages.
 *
 * Java framework cannot handle sending CSL in the ReplyCSL message. This includes 
 * routing ReplyCSL messages through Java components. The reason for this is because 
 * of the way Messages are stored internally.
 *
 * @author Thomas French
 */
public class ReplyCSL extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which only sets the name of the message: no message
     * fields are set.
     */
    public ReplyCSL()
    {
	super(Constants.MessageTypes.ReplyCSL);
    }
    //----------------------------------------------------------------------
    /**
     * Convenience method for setting all of the standard fields of the
     * <tt>ReplyCSL</tt> message
     *
     * @param route route to component
     *
     * @param toVNID target variable numerical ID
     *
     * @param replyMsgID message ID of the original query
     *
     */
    public void setFields(String route,
			  int toVNID,
			  String replyMsgID)
    {
	setNRSField(Constants.MessageFields.route, route);

	setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

	setField(Constants.MessageFields.replyMsgID, replyMsgID);
    }
}
