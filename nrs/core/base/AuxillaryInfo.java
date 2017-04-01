package nrs.core.base;

import nrs.core.comms.CommsRoute;
import nrs.csl.CSL_Message_Registry;
import nrs.csl.CSL_Node_Registry;
import nrs.csl.CSL_Unit_Registry;

/**
 * {@link Message} objects being processed within a Java application can
 * have additional internal-specific information attached to them
 * through the use of this class. Each item of information has a
 * corresponding <tt>setXXXX()</tt> and <tt>getXXXX()</tt> pair of methods.
 *
 * @author Darren Smith
 */
public class AuxillaryInfo
{
  /** Indicates a message to be transmitted through a port */
  public static final boolean OUTBOUND = false;

  /** Indicates a message that has been received from a port */
  public static final boolean INBOUND = true;

  /** Port this message was received on, if applicable */
  private CommsRoute m_recvdPort;

  /** Port this message should be sent through */
  private CommsRoute m_txPort;

  /** Was the message successfully sent through the port */
  private boolean m_sent = false;

  /** Indicate message direction  */
  private boolean m_direction = OUTBOUND;

  /** Original message sent */
  private Message m_origMsgSent;

  /** Target component */
  private String m_targetCID;

  /** Original route */
  private String m_origRoute;

  // Registries, present for ReplyCSL messages
  private CSL_Unit_Registry m_unitReg;
  private CSL_Message_Registry m_messageReg;
  private CSL_Node_Registry m_nodeReg;

  // Custom next destination for the message
  private MessageProcessor m_defaultDest;

  //----------------------------------------------------------------------
  /**
   * Get the orignal value of the message's route field, as it was set
   * before being passed to a local port for transmission. Generally the
   * value returned by this function will be different to that returned
   * by looking up the orginal message sent (see {@link
   * #getOrigMessageRequest()}) and querying the value of the "route"
   * field. The reason is that the latter will have been altered by the
   * local port as the message was transmitted. So if you need to find
   * the route a message was originally given, use this method.
   *
   * @see #setRoute(String)
   * @see nrs.core.comms.PortManager#transmitMessage(Message m)
   *
   * @return the original route value, or <tt>null</tt> if it has not
   * been set.
   */
  public String getRoute()
  {
    return m_origRoute;
  }
  //----------------------------------------------------------------------
  /**
   * Set the original value of the "route" field
   *
   * @see #getRoute()
   */
  public void setRoute(String newM_origRoute)
  {
    this.m_origRoute = newM_origRoute;
  }
 //----------------------------------------------------------------------
  /**
   * Set the port this message was received from; can be set to
   * <tt>null</tt>.
   */
  public void setReceivedPort(CommsRoute port)
  {
    m_recvdPort = port;
  }
  //----------------------------------------------------------------------
  /**
   * Get the port this message was received from, or <tt>null</tt> if not set.
   */
  public CommsRoute getReceivedPort()
  {
    return m_recvdPort;
  }
  //----------------------------------------------------------------------
  /**
   * Set the port this message should be sent from; can be set to
   * <tt>null</tt>.
   */
  public void setSenderPort(CommsRoute port)
  {
    m_txPort = port;
  }
  //----------------------------------------------------------------------
  /**
   * Get the port this message should be sent from, or <tt>null</tt> if
   * not set.
   */
  public CommsRoute getSenderPort()
  {
    return m_txPort;
  }
  //----------------------------------------------------------------------
  /**
   * Set the send status; <tt>true</tt> if has been sent, <tt>false</tt>
   * otherwise.
   */
  public void setSendStatus(boolean b)
  {
    m_sent = b;
  }
  //----------------------------------------------------------------------
  /**
   * Get the send status; <tt>true</tt> if has been sent, <tt>false</tt>
   * otherwise; defaults to <tt>false</tt>.
   */
  public boolean getSendStatus()
  {
    return m_sent;
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link Message} direction: either {@link
   * AuxillaryInfo#INBOUND} or {@link AuxillaryInfo#OUTBOUND}.
   */
  public void setDirection(boolean b)
  {
    m_direction = b;
  }
  //----------------------------------------------------------------------
  /**
   * Get the {@link Message} direction: either {@link
   * AuxillaryInfo#INBOUND} or {@link AuxillaryInfo#OUTBOUND}.
   */
  public boolean getDirection()
  {
    return m_direction;
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link Message} that was the original request associated
   * with a particular message response; can be set to <tt>null</tt>.
   */
  public void setOrigMessageRequest(Message msg)
  {
    m_origMsgSent = msg;
  }
  //----------------------------------------------------------------------
  /**
   * Get the {@link Message} that was the original request associated
   * with a particular message response.
   */
  public Message getOrigMessageRequest()
  {
    return m_origMsgSent;
  }
  //----------------------------------------------------------------------
  /**
   * Set the CID of the component which the message is sent to.
   */
  public void setTargetCID(String cidString)
  {
    m_targetCID = cidString;
  }
  //----------------------------------------------------------------------
  /**
   * Get the CID of the component which the message is sent to. Default
   * value is <tt>null</tt>.
   */
  public String getTargetCID()
  {
    return m_targetCID;
  }
  //----------------------------------------------------------------------
  /**
   * Set the message registry
   */
  public void setMessageRegistry(CSL_Message_Registry r)
  {
    m_messageReg = r;
  }
  //----------------------------------------------------------------------
  /**
   * Get the message registry
   */
  public CSL_Message_Registry getMessageRegistry()
  {
    return m_messageReg;
  }
  //----------------------------------------------------------------------
  /**
   * Set the node registry
   */
  public void setNodeRegistry(CSL_Node_Registry r)
  {
    m_nodeReg = r;
  }
  //----------------------------------------------------------------------
  /**
   * Get the node registry
   */
  public CSL_Node_Registry getNodeRegistry()
  {
    return m_nodeReg;
  }
  //----------------------------------------------------------------------
  /**
   * Set the unit registry
   */
  public void setUnitRegistry(CSL_Unit_Registry r)
  {
    m_unitReg = r;
  }
  //----------------------------------------------------------------------
  /**
   * Get the unit registry
   */
  public CSL_Unit_Registry getUnitRegistry()
  {
    return m_unitReg;
  }
  //----------------------------------------------------------------------
  /**
   * Set the default destination for this message.  This overrides the
   * default destination of whatever {@link MessageProcessor} object
   * that may be processing this message.
   */
  public void setDest(MessageProcessor dest)
  {
    m_defaultDest = dest;
  }
  //----------------------------------------------------------------------
  /**
   * Get the default destination for this message. Can return
   * <tt>null</tt>, in which case there is no custom default-destination
   * for this message.
   *
   * @see #setDest(MessageProcessor)
   */
  public MessageProcessor getDest()
  {
    return m_defaultDest;
  }
}
