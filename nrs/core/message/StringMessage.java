package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>StringMessage</tt> messages
 *
 * @deprecated Generating messages on the fly.
 *
 * @author Thomas French
 */
public class StringMessage extends Message
{
    //----------------------------------------------------------------------
    /**
     * Constructor, which sets the name of the message and string field.
     */
    public StringMessage(String s)
    {
	super("string");
	setField("string", s);
    }

    public void setString(String s){
	setField("string", s);
    }
}
