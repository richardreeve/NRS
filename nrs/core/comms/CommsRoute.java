package nrs.core.comms;

import nrs.core.base.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import nrs.core.message.Constants;

/**
 * An abstract representation of a communications route/port available
 * to an NRS component for sending and receiving messages to other NRS
 * components.
 *
 * <p>A {@link CommsRoute} class must implement the {@link NRSMessageCallback} 
 * interface to receive message which the port has
 * just produced, and so require preprocessing of the intelligent
 * fields.
 *
 * @author Darren Smith
 */
abstract public class CommsRoute implements NRSMessageCallback
{
  /** Callback for when data is available */
  protected MessageListener m_msgListener;

  /** Textual label associated with this route */
  private String m_name;

  /** Locally unique ID used for wormhole routing */
  private PortNumber m_pn;

  /** Callback for connection and disconnection events. Each element is
   * of type {@link PortConnectedListener} */
  private ArrayList m_connectionListeners;

  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param ID the locally unique ID associated with this route / port
   * and used for wormhole routing.
   *
   * @param name a text label to associate with this route / port
   */
  public CommsRoute(int ID, String name)
  {
    m_pn = new PortNumber(ID);
    m_name = name;
    m_connectionListeners = new ArrayList();
  }
  //----------------------------------------------------------------------
  /**
   * Return the locally unique ID of this route / port, which is used
   * for wormhole routing of messages.
   */
  public PortNumber getID()
  {
    return m_pn;
  }
  //----------------------------------------------------------------------
  /**
   * Get the string label associated with this route / port.
   */
  public String getName()
  {
    return m_name;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the name and ID of this route / port
   */
  public String toString()
  {
    return new String(m_name + " (" + m_pn.getEncoding() + ")");
  }
  //----------------------------------------------------------------------
  /**
   * Write the message held within the {@link ByteArrayOutputStream} to
   * the port.
   *
   * @param message a {@link ByteArrayOutputStream} containing the data
   * message
   *
   * @throws IllegalStateException} if the port is not opened for writing
   *
   * @throws IOException if there is any other kind of IO problem
   */
  public abstract void write(ByteArrayOutputStream message)
    throws IllegalStateException, IOException;
  //----------------------------------------------------------------------
  /**
   * Set the object which will be called when a message is
   * available. Can be set to <code>null</code>, in which case no
   * message callbacks are made.
   */
  public void setMesssageListener(MessageListener listener)
  {
    m_msgListener = listener;
  }
  //----------------------------------------------------------------------
  /**
   * Return <tt>true</tt> if this communications port is UP. This means
   * the port can be used for both sending and receiving data.
   */
  abstract public boolean isUp();
  //----------------------------------------------------------------------
  /**
   * Attempt to open the port. Derived classes need to
   * implement this method so that it attempts to open the underlying
   * communication mechanism, and, when connection is achieved, results
   * in calls made to the {@link PortConnectedListener} listener
   * objects, via {@link #generateConnectionEvent(boolean)}.
   */
  abstract public void open();
  //----------------------------------------------------------------------
  /**
   * Add a listener for connection events
   */
  public void addConnectionListener(PortConnectedListener o)
  {
    if (o == null) return;
    m_connectionListeners.add(o);
  }
  //----------------------------------------------------------------------
  /**
   * Remove a listener for connection events
   */
  public void removeConnectionListener(PortConnectedListener o)
  {
    if (o == null) return;
    m_connectionListeners.remove(o);
  }
  //----------------------------------------------------------------------
  /**
   * Notify connection listeners of connected (<tt>true</tt>) or
   * disconnected (<tt>false</tt>) event.
   */
  protected void generateConnectionEvent(boolean kind)
  {
    if (kind)
    {
      Iterator i = m_connectionListeners.iterator();
      while (i.hasNext())
      {
        PortConnectedListener callback = (PortConnectedListener) i.next();
        callback.portConnected(this);
      }
    }
    else
    {
      Iterator i = m_connectionListeners.iterator();
      while (i.hasNext())
      {
        PortConnectedListener callback = (PortConnectedListener) i.next();
        callback.portDisconnected(this);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Handle receipt of the {@link Message} <tt>msg</tt>, and preprocess,
   * when it can, any intelligent fields.
   */
  public void handleMessage(Message msg)
  {
    //    System.out.println("Port is preprocessing message: " + msg.diagString());

    String iReturnRoute
      = msg.getNRSField(Constants.MessageFields.iReturnRoute);

    if (iReturnRoute != null)
    {
      msg.setNRSField(Constants.MessageFields.iReturnRoute,
                      new String(m_pn.getEncoding() + iReturnRoute));
    }


    //    System.out.println("Preprocessing compete: " + msg.diagString());
  }
}
