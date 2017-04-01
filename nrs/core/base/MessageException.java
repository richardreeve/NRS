package nrs.core.base;

/**
 * Uses for exceptions when messages cannot be constructed or are
 * invalid.
 */
public class MessageException extends Exception
{
  private String m_messageType;
  private String m_messageField;
  private String m_reason;

  //----------------------------------------------------------------------
  /**
   * Constructs an exception for when a message field cannot be added
   * for some reason.
   */
  public MessageException(String messageType,
                          String messageField,
                          String reason)
  {
    m_messageType = messageType;
    m_messageField = messageField;
    m_reason = reason;
  }
  //----------------------------------------------------------------------
  /**
   * Get the message field
   */
  public String getField()
  {
    return m_messageField;
  }
  //----------------------------------------------------------------------
  /**
   * Get the message type
   */
  public String getType()
  {
    return m_messageType;
  }
  //----------------------------------------------------------------------
  /**
   * Get the reason why the message-field in the message-type caused an
   * exception
   */
  public String getReason()
  {
    return m_reason;
  }
  //----------------------------------------------------------------------
  /**
   * String representation
   */
  public String toString()
  {
    return "Couldn't construct field "
      + m_messageField + " for message type "
      + m_messageType + ": "
      + m_reason;
  }
}
