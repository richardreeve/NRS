package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>TimeMessage</tt> messages
 *
 * @deprecated Generating messages on the fly.
 *
 * @author Thomas French
 */
public class TimeMessage extends Message
{
  /**
   * Constructor, which sets the name of the message and float value for time.
   */
  public TimeMessage(float f)
  {
    super("Time");
    setField("Time", Float.toString(f));
  }
  /**
   * Constructor, which sets the name of the message and double value for time.
   */
  public TimeMessage(double d)
  {
    super("Time");
    setField("Time", Double.toString(d));
  }
  
}
