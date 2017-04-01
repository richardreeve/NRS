package nrs.csl;

/**
 * @deprecated void is not an NRS basic type.
*/
public class CSL_BasicType_Void extends CSL_BasicType
{
  private static final String m_javaType = "";

  /** String representation of this basic type. */
  public static String NAME = "void";

  /**
   * Returns an empty string
   */
  public Object getJavaType() { return m_javaType; }

  /**
   * Constructs and returns an empty string
   */
  public Object newJavaType() { return ""; }

  /**
   * Returns the empty string, to represent the void
   */
  public Object toJavaType(String value)
  {
    return "";
  }
}
