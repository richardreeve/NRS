package nrs.core.base;

/**
 * A {@link MessageProcessor} which does not send received messages
 * anywhere. An optional name can be provided. If so, messages which are
 * are logged before they are lost forever.
 *
 * @author Darren Smith
 */
public class MessageStop extends MessageProcessor
{
  private static MessageStop m_static;

  private String m_name;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  public MessageStop()
  {
  }
  //----------------------------------------------------------------------
  /**
   * Constructor for a named object
   */
  public MessageStop(String name)
  {
    m_name = name;
  }
  //----------------------------------------------------------------------
  /**
   * The received message is not sent on to any other message-processor
   * objects. Thus there is no default-destination from this class, and
   * neither is any default-destination associated with the message
   * itself used.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    // do absolutely nothing.... if only all methods were this easy to write!

    if (m_name != null)
    {
      PackageLogger.log.finer("Message " + m + " terminated at MessageStop '"
                              + m_name +"'");
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return a shared instance of this class. This has been provided
   * because this class is so non-functional that it really doesn't make
   * sense to have separate instances.
   */
  public static MessageStop getInstance()
  {
    if (m_static == null) m_static = new MessageStop();

    return m_static;
  }
}
