package nrs.util;

/**
 * Represents an error associated with argument processing
 *
 * @author Darren Smith
 */
public class ArgumentException extends Exception
{
  public static final String INVALID = "Argument is not valid";
  public static final String UNKNOWN = "Unknown argument";
  public static final String UNEXPECTED = "Unexpected argument";
  public static final String MISSING = "Missing parameters to argument";

  private final String m_rawString;
  private final int m_position;

  //----------------------------------------------------------------------
  public ArgumentException(String rawString,
                           int position,
                           String errorMessage)
  {
    super(errorMessage + " \"" + rawString + "\"");

    m_rawString = rawString;
    m_position = position;
  }
  //----------------------------------------------------------------------
  public String getRawArgument()
  {
    return m_rawString;
  }
  //----------------------------------------------------------------------
  public int getPosition()
  {
    return m_position;
  }
}
