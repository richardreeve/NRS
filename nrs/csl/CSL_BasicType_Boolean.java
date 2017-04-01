// $Id: CSL_BasicType_Boolean.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

public class CSL_BasicType_Boolean extends CSL_BasicType
{
  private static final Boolean m_javaType = new Boolean(false);

  /** String representation of this basic type. */
  public static String NAME = "boolean";

  /**
   * Returns a <code>static</code> {@link Boolean} type.
   */
  public Object getJavaType() { return m_javaType; }

  /**
   * Constructs and returns a {@link Boolean} type.
   */
  public Object newJavaType() { return new Boolean(false); }

  /**
   * Returns a {@link Boolean} object holding the boolean value
   * represented by the argument string <code>value</code>
   */
  public Object toJavaType(String value)
  {
    return Boolean.valueOf(value);
  }
}
