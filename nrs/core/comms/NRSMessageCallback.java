package nrs.core.comms;

import nrs.core.base.Message;

/**
 * Interface for receiving NRS messages (as {@link Message} objects)
 * that have been decoded from an underlying transmission format (eg
 * XML).
 *
 * @author Darren Smith
 */
public interface NRSMessageCallback
{
  /**
   * Handle receipt of the {@link Message} <tt>msg</tt>
   */
  public void handleMessage(Message msg);
}
