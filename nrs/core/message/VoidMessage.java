package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>VoidMessage</tt> messages
 *
 * @deprecated Generating messages on the fly.
 *
 * @author Thomas French
 */
public class VoidMessage extends Message
{
    /**
     * Constructor, which sets the name of the message.
     */
    public VoidMessage()
    {
        super("void");
    }
}
