package nrs.core.base;

import nrs.core.message.Constants;

/**
 * Conditionally store and retreive messages in a {@link
 * OutboundMessageCache}. Note that this module requires two {@link
 * MessageProcessor} destinations: one of inbound messages; one for
 * outbound.
 *
 * @author Darren Smith
 */
public class MessageStorage extends MessageProcessor
{
  private OutboundMessageCache m_cache;
  private MessageProcessor m_inboundDest;

  //----------------------------------------------------------------------
  public MessageStorage(OutboundMessageCache cache)
  {
    m_cache = cache;
  }
  //----------------------------------------------------------------------
  /**
   * Receive and conditionally store a {@link Message}. Each received
   * {@link Message} is only stored if it possesses a msgID field, its
   * value is positive, and if the message was successfully sent (as
   * determined by its auxillary information). If the message is inbound
   * and possesses a replyMsgID field then a lookup will made to find a
   * previous message stored with the same msgID. If one is found it
   * will be attached to message's auxillary data.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    if ((m.hasField(Constants.MessageFields.msgID))
        && (m.aux().getSendStatus())
        && (m.aux().getDirection() == AuxillaryInfo.OUTBOUND)
        && (m_cache != null))
    {
      try
      {
        if (Integer.parseInt(m.getField(Constants.MessageFields.msgID)) > 0)
        {
          m_cache.add(m);
        }
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning("Message " + m + " has non-numeric ID ("
                                  + m.getField(Constants.MessageFields.msgID)
                                  + "): not storing : " + e);
      }
    }
    else if ((m.hasField(Constants.MessageFields.replyMsgID))
             && (m.aux().getDirection() == AuxillaryInfo.INBOUND)
             && (m_cache != null))
    {
      try
      {
        int messageID =
          Integer.parseInt(m.getField(Constants.MessageFields.replyMsgID));

        m.aux().setOrigMessageRequest(m_cache.get(messageID));
      }
      catch (Exception e)
      {
        PackageLogger
          .log
          .warning("Message "
                   + m + " has non-numeric value ("
                   + m.getField(Constants.MessageFields.replyMsgID)
                   + ") for field"
                   + Constants.MessageFields.replyMsgID
                   + ":" + e);
      }
    }

    // route
    if (m.aux().getDirection() == AuxillaryInfo.INBOUND)
    {
      if (m_inboundDest != null) m_inboundDest.deliver(m, this);
    }
    else
    {
      next(m);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Set the destination for inbound messages.
   *
   * @see #setDest(MessageProcessor)
   */
  public void setInboundDest(MessageProcessor dest)
  {
    m_inboundDest = dest;
  }
}
