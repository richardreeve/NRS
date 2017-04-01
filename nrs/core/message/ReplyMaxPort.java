package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyMaxPort</tt> messages
 *
 * @author Darren Smith
 */
public class ReplyMaxPort extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which only sets the name of the message: no message
     * fields are set.
     */
    public ReplyMaxPort()
    {
	super(Constants.MessageTypes.ReplyMaxPort);
    }
    //----------------------------------------------------------------------
    /**
     * Convenience method for setting all of the standard fields of the
     * <tt>ReplyMaxPort</tt> message
     *
     * @param route route to component
     *
     * @param toVNID target variable numerical ID
     *
     * @param replyMsgID message ID of the original query
     *
     * @param port a number at least as great as the highest numbered
     *             port in the component, starting from 1
     *
     */
    public void setFields(String route,
			  int toVNID,
			  String replyMsgID,
                          int port)
    {
	setNRSField(Constants.MessageFields.route, route);

	setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

	setField(Constants.MessageFields.replyMsgID, replyMsgID);

	setField(Constants.Fields.F_port, Integer.toString(port));

    }
}
