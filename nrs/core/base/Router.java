package nrs.core.base;

import nrs.core.base.Message;
import nrs.core.base.MessageProcessor;
import nrs.core.base.MessageStop;
import nrs.core.message.Constants;

/**
 * Provides routing capability to an NRS application. Messages received
 * at this modules are examined for the presence of a non-zero
 * <code>route</code> field.
 *
 * <p>If such a field exists it is sent on to a specified handler (which
 * will typically be a {@link nrs.core.comms.PortManager PortManager} -
 * this is specified in the constructor). Otherwise a message is sent
 * onto the next destination (which will typically be some internal
 * module).
 *
 * <p>Messages which do contain a non-empty <code>route</code> are given
 * a default destination of a {@link MessageStop}.  This means that when
 * they arrive at and are processed by the module for handling outbound
 * messages (typically {@link nrs.core.comms.PortManager PortManager}),
 * they will next be deleted.
 *
 * @author Darren Smith
 */
public class Router extends MessageProcessor

{
  private MessageProcessor m_toExternal;
  private MessageStop m_stop = new MessageStop("RouterStop");

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  public Router()
  {
  }
  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param toExternal the {@link MessageProcessor} to which messages
   * are sent if they contain a non-empty <code>route</code> field
   *
   * @param toInternal the {@link MessageProcessor} to which messages
   * are sent if they do not contain a non-empty <code>route</code>
   * field
   */
  public Router(MessageProcessor toExternal,
                MessageProcessor toInternal)
  {
    m_toExternal = toExternal;
    setDest(toInternal);
  }
  //----------------------------------------------------------------------
  public void setExternalDest(MessageProcessor toExternal)
  {
    m_toExternal = toExternal;
  }
  //----------------------------------------------------------------------
  public MessageProcessor getExternalDest()
  {
    return m_toExternal;
  }
  //----------------------------------------------------------------------
  public void deliver(Message m, MessageProcessor sender)
  {
    String route = m.getNRSField(Constants.Fields.F_route);

    if ((route != null) && (route.length() != 0))
    {
      if (m_toExternal != null)
      {

        m.aux().setDest(m_stop);

        m_toExternal.deliver(m, this);
      }
      else
      {
        PackageLogger.log.warning("Can't route message with route '"
                                  + route + "' to external ports - no"
                                  + " MessageProcessor destination available");
      }
    }
    else
    {
      next(m);
    }
  }
}
