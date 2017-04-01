package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyMaxVNID</tt> messages
 *
 * @author Darren Smith
 * @author Thomas French
 */
public class ReplyMaxVNID extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which only sets the name of the message: no message
     * fields are set.
     */
    public ReplyMaxVNID()
    {
	super(Constants.MessageTypes.ReplyMaxVNID);
    }
    //----------------------------------------------------------------------
    /**
     * Convenience method for setting all of the standard fields of the
     * <tt>ReplyMaxVNID</tt> message
     *
     * @param route route to component
     *
     * @param toVNID target variable numerical ID
     *
     * @param replyMsgID message ID of the original query
     *
     * @param vnid a number at least as great as the highest numbered
     *             VNID of any variable or node in the component
     *
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
}
