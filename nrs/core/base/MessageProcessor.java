package nrs.core.base;

/**
 * Define the base for classes which internally receive and process
 * {@link Message} objects.
 *
 * @author Darren Smith
 */
public abstract class MessageProcessor
{
  /** The default destination */
  private MessageProcessor m_dest;

  //----------------------------------------------------------------------
  /**
   * Deliver a {@link Message} object for processing and/or routing.
   *
   * @param m the {@link Message} being transferred
   * @param sender the {@link MessageProcessor} which is sending the message
   */
  public abstract void deliver(Message m, MessageProcessor sender);

  //----------------------------------------------------------------------
  /**
   * Set the default {@link MessageProcessor} where messages should be
   * passed to after being processed by this class, or <tt>null</tt> if
   * they should just be terminated.
   */
  public void setDest(MessageProcessor dest)
  {
    m_dest = dest;
  }
  //----------------------------------------------------------------------
  /**
   * Return the default destination. Can return null (which indicates
   * the messages go nowhere - ie they will be terminated).
   */
  public MessageProcessor getDest(MessageProcessor dest)
  {
    return m_dest;
  }
  //----------------------------------------------------------------------
  /**
   * Send the {@link Message} onto the default-destination (if there is
   * one). The default-destination is determined by first examining the
   * {@link Message} itself to see when one has been provided as part of
   * its auxillary data. If none is found, then the default-destination
   * specifed for this {@link MessageProcessor} instance will be
   * used. If, after these checks, no default-destination has been
   * found, the message is not sent anywhere.
   */
  public void next(Message m)
  {
    if (m.aux().getDest() != null)
    {
      m.aux().getDest().deliver(m, this);
    }
    else if (m_dest != null) m_dest.deliver(m, this);
  }
}
