package nrs.core.base;

import nrs.core.comms.PortManager;

/**
 * Coordinates the process of receiving and processing messages destined
 * for final transmission through a specified {@link
 * nrs.core.comms.CommsRoute} port.
 *
 * @author Darren Smith
 */
public class OutboundPipeline extends MessageProcessor
{
  /** The module which does the actual transmitting of messages */
  private MessageProcessor m_portManager;

  /** The module which provides a unique ID */
  private MessageProcessor m_IDprovider;

  /** The module which stores messages that have been sent */
  private MessageProcessor m_storage;

  //----------------------------------------------------------------------
  /**
   * Specify the various {@link MessageProcessor} objects that will be
   * used to process and transmit {@link Message} objects.
   *
   * @param portManager the {@link PortManager} responsible for actually
   * transmitting messages through a {@link nrs.core.comms.CommsRoute}
   *
   * @param IDprovider the {@link MessageIDStamp} which can give each
   * {@link Message} a unique ID
   *
   * @param storage the {@link MessageStorage} which will conditionally
   * stored messages which have be successfully transmitted by a port
   */
  public void setModules(PortManager portManager,
                         MessageIDStamp IDprovider,
                         MessageStorage storage)
  {
    m_portManager = portManager;
    m_IDprovider = IDprovider;
    m_storage = storage;

    // wire these modules up
    m_IDprovider.setDest(m_portManager);
    m_portManager.setDest(storage);
  }
  //----------------------------------------------------------------------
  /**
   * Submit a {@link Message} for transmission out of this application to
   * another NRS component.
   *
   * @deprecated use <tt>deliver(Message m, MessageProcessor
   * sender)</tt> instead
   */
  public void submit(Message m)
  {
    m_IDprovider.deliver(m, new MessageProcessor()
      {
        public void deliver(Message m, MessageProcessor sender) {}
        public void setDest(MessageProcessor dest) {}
      });
  }
  //----------------------------------------------------------------------
  /**
   * Submit a {@link Message} for transmission out of this application to
   * another NRS component.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    m_IDprovider.deliver(m, this);
  }
}
