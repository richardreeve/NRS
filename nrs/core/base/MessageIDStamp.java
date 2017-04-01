package nrs.core.base;

import nrs.core.message.Constants;

/**
 * Stamps a received {@link Message} objects with a unique message ID if
 the message ID field is present and possesses a * negative (invalid)
 value.
 *
 * @author Darren Smith
 */
public class MessageIDStamp extends MessageProcessor
{
  private OutboundMessageCache m_cache;

  //----------------------------------------------------------------------
  public MessageIDStamp(OutboundMessageCache cache)
  {
    m_cache = cache;
  }
 //----------------------------------------------------------------------
  /**
   * Stamp the received {@link Message} with a unique message ID. This
   * is only done if the message ID field is present and possesses a
   * negative (invalid) value. If a message does need to use its message
   * ID, then it should have already been given a message ID of zero.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    if (m.hasField(Constants.MessageFields.msgID))
      try
      {
        if (Integer.parseInt(m.getField(Constants.MessageFields.msgID)) < 0)
        m.setField(Constants.MessageFields.msgID,
                   Integer.toString(m_cache.getFreeID()));
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning("Message " + m + " has non-numeric ID ("
                                  + m.getField(Constants.MessageFields.msgID)
                                  + "). Setting to a numeric value.");

        m.setField(Constants.MessageFields.msgID,
                   Integer.toString(m_cache.getFreeID()));
      }

    next(m);
  }
}
