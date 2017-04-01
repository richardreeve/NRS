package nrs.csl;

public class CSL_BasicType_Float extends CSL_BasicType
{
  private static final Double m_javaType = new Double(0.0);

  /** String representation of this basic type. */
  public static String NAME = "float";

  /**
   * Returns a <code>static</code> {@link Double} type.
   */
  public Object getJavaType() { return m_javaType; }

  /**
   * Constructs and returns a {@link Double} type.
   */
  public Object newJavaType() { return new Double(0.0); }

  /**
   * Returns a {@link Double} object holding the integer value
   * represented by the argument string <code>value</code>
   */
  public Object toJavaType(String value)
  {
    return Double.valueOf(value);
  }
}
