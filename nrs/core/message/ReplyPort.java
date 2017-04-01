package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyPort</tt> messages
 *
 * @author Darren Smith
 */
public class ReplyPort extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which only sets the name of the message: no message
     * fields are set.
     */
    public ReplyPort()
    {
	super(Constants.MessageTypes.ReplyPort);
    }
    //----------------------------------------------------------------------
    /**
     * Convenience method for setting all of the standard fields of the
     * <tt>ReplyPort</tt> message
     *
     * @param route route to component
     *
     * @param toVNID target variable numerical ID
     *
     * @param replyMsgID message ID of the original query
     *
     * @param portRoute a string which is the local route for a port
     *
     */
    public void setFields(String route,
			  int toVNID,
			  String replyMsgID,
                          String portRoute)
    {
	setNRSField(Constants.MessageFields.route, route);

	setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

	setField(Constants.MessageFields.replyMsgID, replyMsgID);

	setField(Constants.Fields.F_portRoute, portRoute);

    }
}
