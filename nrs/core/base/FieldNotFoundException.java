package nrs.core.base;

/**
 * Can be thrown by some routines which attempt field access in {@link
 * Message} objects.
 */
public class FieldNotFoundException extends Exception
{
  private String m_fieldName;

  /**
   * Create a new exception to indicate the <tt>fieldname</tt> was not
   * found. It constructs an exception messasge of the format:
   *
   * <p>"Field " + <tt>fieldName</tt> + " not found"
   */
  public FieldNotFoundException(String fieldName)
  {
    super("Field " + fieldName + " not found");
    m_fieldName = fieldName;
  }

  /**
   * Return the fieldname associated with this this exception
   */
  public String fieldName()
  {
    return m_fieldName;
  }
}
