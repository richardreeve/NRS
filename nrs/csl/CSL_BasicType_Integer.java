package nrs.csl;

public class CSL_BasicType_Integer extends CSL_BasicType
{
  private static final Integer m_javaType = new Integer(0);

  /** String representation of this basic type. */
  public static String NAME = "integer";

  /**
   * Returns a <code>static</code> {@link Integer} type.
   */
  public Object getJavaType() { return m_javaType; }

  /**
   * Constructs and returns a {@link Integer} type.
   */
  public Object newJavaType() { return new Integer(0); }

  /**
   * Returns a {@link Integer} object holding the integer value
   * represented by the argument string <code>value</code>
   */
  public Object toJavaType(String value)
  {
    return Integer.valueOf(value);
  }
}
