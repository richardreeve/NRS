package nrs.core.comms;

/**
 * Derived classes should implement this class so that they can be
 * registered when a {@link CommsRoute} has connected to or disconnected
 * from another NRS component.
 *
 * @author Darren Smith
 */
public interface PortConnectedListener
{
  /** Indicates the <code>port</code> has connected*/
  public void portConnected(CommsRoute port);

  /** Indicates the <code>port</code> has disconnected */
  public void portDisconnected(CommsRoute port);
};
  
