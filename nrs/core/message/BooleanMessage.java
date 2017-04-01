package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>BooleanMessage</tt> messages
 *
 * @deprecated Generating messages on the fly.
 *
 * @author Thomas French
 */
public class BooleanMessage extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which sets the name of the message and boolean field.
     */
    public BooleanMessage(boolean b)
    {
	super("boolean");
	setField("boolean", Boolean.toString(b));
    }

    public void setBoolean(boolean b){
	setField("boolean", Boolean.toString(b));
    }
}
