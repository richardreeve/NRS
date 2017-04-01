package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>IntegerMessage</tt> messages
 *
 * @deprecated Generating messages on the fly.
 *
 * @author Thomas French
 */
public class IntegerMessage extends Message
{
    /**
     * Constructor, which sets the name of the message and integer field.
     */
    public IntegerMessage(int i)
    {
	super("integer");
	setField("integer", Integer.toString(i));
    }
}
