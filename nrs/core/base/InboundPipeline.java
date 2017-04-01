package nrs.core.base;

import nrs.core.comms.NRSMessageCallback;
import nrs.core.comms.PortManager;

/**
 * Co-ordinate the flow of messages (represented by {@link Message}
 * objects) through major modules of an NRS application.
 *
 * <p>Typically a newly build {@link Message} object will be submitted
 * to an {@link InboundPipeline} so that it can be processed by other
 * parts of an NRS application (an example of such use is {@link
 * PortManager} which needs an InboundPipeline for dispatching the
 * message objects that it creates).
 *
 * <p>Before an {@link InboundPipeline} can be used it must be
 * appropriately configured; this entails providing it with the various
 * objects required for processing messages.
 *
 * @author Darren Smith
 */
public class InboundPipeline implements NRSMessageCallback
{
  private MessageProcessor m_router;
  private MessageProcessor m_storage;
  private MessageProcessor m_varMan;
  private MessageProcessor m_broadcastHandler;
  private MessageProcessor m_first;
  //----------------------------------------------------------------------
  /**
   * Specify the various {@link MessageProcessor} objects that will be
   * used to process {@link Message} objects. For probably all of the
   * components specified their default destinations, and sometimes
   * other processpecific destinations, will be set by this
   * constructor. Other aspects of the configuration of these message
   * processors provided are the callers responsibility.
   *
   * <p>Use this method to <b>exclude</b> message routing capability.
   *
   * @param storage the {@link MessageStorage} which has conditionally
   * stored messages which have previously been sent. Arriving messages
   * will use this module to retrieve the previous message issued.
   *
   * @param thisComponent the {@link BaseComponent} which contains the
   * NRS-style variables which are the ultimate destination for
   * messages.
   *
   * @param variableManager the {@link VariableManager} which will attempt
   * to route messages to variable.
   *
   * @param broadcastHandler the {@link BroadcastHandler} which will be
   * used to filter out broadcast messages.
   *
   * @param portMan the {@link PortManager} used for sending
   * messages. The broadcast handler will be configured to filter
   * unwanted broadcast messages to this message-processor.
   */
  public void setModules(MessageStorage storage,
                         BaseComponent thisComponent,
                         VariableManager variableManager,
                         BroadcastHandler broadcastHandler,
                         PortManager portMan)
  {
    m_storage = storage;
    m_varMan = variableManager;
    m_broadcastHandler = broadcastHandler;

    // wire up
    m_first = broadcastHandler;
    broadcastHandler.setDest(storage);
    broadcastHandler.setBroadcastDest(portMan);

    storage.setInboundDest(variableManager);
    variableManager.setDest(thisComponent);
  }
  //----------------------------------------------------------------------
  /**
   * Specify the various {@link MessageProcessor} objects that will be
   * used to process {@link Message} objects. For probably all of the
   * components specified their default destinations, and sometimes
   * other processpecific destinations, will be set by this
   * constructor. Other aspects of the configuration of these message
   * processors provided are the callers responsibility.
   *
   * <p>Use this method to <b>include</b> message routing capability.
   *
   * @param router the {@link Router} which decides if messages should
   * be sent to an external port, or, sent them for internal processing
   * if the route field is empty (ie if they are destined for this
   * component).
   *
   * @param storage the {@link MessageStorage} which has conditionally
   * stored messages which have previously been sent. Arriving messages
   * will use this module to retrieve the previous message issued.
   *
   * @param thisComponent the {@link BaseComponent} which contains the
   * NRS-style variables which are the ultimate destination for
   * messages.
   *
   * @param variableManager the {@link VariableManager} which will attempt
   * to route messages to variable.
   *
   * @param broadcastHandler the {@link BroadcastHandler} which will be
   * used to filter out broadcast messages.
   *
   * @param portMan the {@link PortManager} used for sending
   * messages. The broadcast handler will be configured to filter
   * unwanted broadcast messages to this message-processor.
   */
  public void setModules(MessageProcessor router,
                         MessageStorage storage,
                         BaseComponent thisComponent,
                         VariableManager variableManager,
                         BroadcastHandler broadcastHandler,
                         PortManager portMan)
  {
    m_router = router;
    m_storage = storage;
    m_varMan = variableManager;
    m_broadcastHandler = broadcastHandler;

    // wire up
    m_first = broadcastHandler;
    broadcastHandler.setDest(router);
    router.setDest(storage);
    broadcastHandler.setBroadcastDest(portMan);

    storage.setInboundDest(variableManager);
    variableManager.setDest(thisComponent);
  }
  //----------------------------------------------------------------------
  /**
   * Accept a {@link Message} from a recent {@link
   * nrs.core.comms.CommsRoute} port and begin the sequence of internal
   * message processing. The message arrival is also logged.
   */
  public void handleMessage(Message m)
  {
    if (m.aux().getReceivedPort() != null)
    {
      PackageLogger.log.fine("Received " + m.diagString()
                             + " on port:" + m.aux().getReceivedPort());
    }
    else
    {
      PackageLogger.log.fine("Received " + m.diagString()
                             + " on port: NOT AVAILABLE");
    }

    m_first.deliver(m, new MessageProcessor()
      {
        public void deliver(Message _m, MessageProcessor _sender) {}
        public void setDest(MessageProcessor _dest) {}
      });
  }
}
