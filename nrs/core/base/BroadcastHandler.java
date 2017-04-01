package nrs.core.base;

import nrs.core.message.Constants;

/**
 * Filters broadcast messages. Messages which are not broadcast messages
 * are sent to the default destination. So too are broadcast messages
 * which are determined to have reached their target. Broadcast messages
 * which are not aimed at this application are sent to another,
 * configurable, destination.
 *
 * @author Darren Smith
 */
public class BroadcastHandler extends MessageProcessor
{
  private MessageProcessor m_bcastOut;
  private ComponentInfo m_self;
  //----------------------------------------------------------------------
  /*
   * Constructor
   *
   * @param self the {@link ComponentInfo} contianing information about
   * this NRS component.
   */
  public BroadcastHandler(ComponentInfo self)
  {
    m_self = self;
  }
  //----------------------------------------------------------------------
  /**
   * Determine whether the supplied broadcase message is aimed at this
   * NRS component. The determination is based on the iTargetCID field.
   *
   * @return true is the broadcast messasge is aimed at this component,
   * false otherwise
   */
  private boolean forSelf(Message bcastMsg)
  {
    String target = bcastMsg.getNRSField(Constants.MessageFields.iTargetCID);

    if (target == null) return false;

    return (target.equals(m_self.getCID()));
  }
  //----------------------------------------------------------------------
  /**
   * Handle a message identified as a broadcast. If the message is
   * broadcast and it not addressed to this component then it is passed
   * toward the {@link MessageProcessor} specified for handling
   * broadcasts; and, since we should not be interested in these kinds
   * of messages, the message is given a terminating {@link MessageStop}
   * default-destination (so, after the next delivery, it goes nowhere
   * else).
   */
  private void handleBroadcast(Message m)
  {
    if (forSelf(m))
    {
      next(m);
    }
    else
    {
      m.aux().setDest(MessageStop.getInstance());
      if (m_bcastOut != null) m_bcastOut.deliver(m, this);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Receive an handle a {@link Message}. If the message is not a
   * broadcast it is sent to the default destination component.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    if (MessageTools.isBroadcast(m))
    {
      handleBroadcast(m);
    }
    else
    {
      next(m);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Set the component to which broadcast messages should be sent
   * (typcially this will be a {@link nrs.core.comms.PortManager}
   * object.
   */
  public void setBroadcastDest(MessageProcessor bcastOut)
  {
    m_bcastOut = bcastOut;
  }
}
