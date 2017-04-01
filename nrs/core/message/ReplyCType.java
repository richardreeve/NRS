package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>ReplyCType</tt> messages
 *
 * @author Thomas French
 */
public class ReplyCType extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which only sets the name of the message: no message
     * fields are set.
     */
    public ReplyCType()
    {
	super(Constants.MessageTypes.ReplyCType);
    }
    //----------------------------------------------------------------------
    /**
     * Convenience method for setting all of the standard fields of the
     * <tt>ReplyCType</tt> message
     *
     * @param route route to component
     *
     * @param toVNID target variable numerical ID
     *
     * @param replyMsgID message ID of the original query
     *
     * @param cType CType of the variable or node being queried
     *
     * @param cVersion cVersion of the variable or node being queried
     */
    public void setFields(String route,
			  int toVNID,
			  String replyMsgID,
			  String cType,
			  String cVersion)
    {
	setNRSField(Constants.MessageFields.route, route);

	setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));

	setField(Constants.MessageFields.replyMsgID, replyMsgID);

	setField(Constants.MessageFields.cType, cType);

	setField(Constants.MessageFields.cVersion, cVersion);
    }
}
