package nrs.core.message;

import nrs.core.base.Message;

/**
 * Utility class for building <tt>Reset</tt> messages
 *
 * @author Thomas French
 */
public class Reset extends Message
{
  //----------------------------------------------------------------------
  /**
   * Constructor, which only sets the name of the message: no message
   * fields are set.
   */
  public Reset()
  {
    super(Constants.MessageTypes.Reset);
  }

  //----------------------------------------------------------------------
  /**
   * Convenience method for setting all of the standard fields of the
   * <tt>Reset</tt> message
   *
   * @param route route to component
   *
   * @param toVNID target variable numerical ID
   *
   */
  public void setFields(String route,
                        int toVNID)
  {
    setNRSField(Constants.MessageFields.route, route);

    setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));
  }
}
