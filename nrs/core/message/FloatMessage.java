package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>FloatMessage</tt> messages
 *
 * @deprecated Generating messages on the fly.
 *
 * @author Thomas French
 */
public class FloatMessage extends Message
{
    /**
     * Constructor, which sets the name of the message and float field.
     */
    public FloatMessage(float f)
    {
	super("float");
	setField("float", Float.toString(f));
    }
    
    /**
     * Constructor, which sets the name of the message and double field.
     */
    public FloatMessage(double d)
    {
	super("float");
	setField("float", Double.toString(d));
    }
}
