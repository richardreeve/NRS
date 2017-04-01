package nrs.csl;

/**
 * Represents the NRS basic-type <tt>route</tt>.
 */
public class CSL_BasicType_Route extends CSL_BasicType
{
  private static final String m_javaType = "";

  /** String representation of this basic type. */
  public static String NAME = "route";

  /**
   * Returns a <code>static</code> {@link String} type.
   */
  public Object getJavaType() { return m_javaType; }

  /**
   * Constructs and returns a {@link String} type.
   */
  public Object newJavaType() { return new String(); }
  
  /**
   * Returns the argument supplied
   */
  public Object toJavaType(String value)
  {
    return value;
  }
}
