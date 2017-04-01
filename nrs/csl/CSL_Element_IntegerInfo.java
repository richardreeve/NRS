package nrs.csl;

import java.util.HashMap;

/** Represent a NRS/CSL integer-info element.
 *
 * @deprecated
 **/
public class CSL_Element_IntegerInfo extends CSL_Element
{
  // Attributes
  private String m_minVal;
  private String m_maxVal;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "IntegerInfo";


  /** XML attribute to specify the minimum value */
  public static String ATTR_MINVAL = "minVal";

  /** XML attribute to specify the maximum value */
  public static String ATTR_MAXVAL = "maxVal";

  /** The text string which represents the value of minVal or maxVal if
   * an unbounded quantity is described. */
  public static String UNBOUNDED = "unbounded";

  //----------------------------------------------------------------------
  /** Constructor for prototype class. */
  CSL_Element_IntegerInfo()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor
   *
   * Used to create an object with a set of XML name & value attributes.
   *
   * @param atts a HashMap containing the set of name and value attributes.
   */
  public CSL_Element_IntegerInfo(HashMap atts)
  {
    super(NAME);

    // Set the minimum value
    if (atts.containsKey(ATTR_MINVAL))
      {
        m_minVal = (String) atts.get(ATTR_MINVAL);
      }
    else
      {
        m_minVal = UNBOUNDED;
      }

    // Set the maximum value
    if (atts.containsKey(ATTR_MAXVAL))
      {
        m_maxVal = (String) atts.get(ATTR_MAXVAL);
      }
    else
      {
        m_maxVal = UNBOUNDED;
      }

    if (m_minVal.equals(UNBOUNDED)) m_minVal = UNBOUNDED;
    if (m_maxVal.equals(UNBOUNDED)) m_maxVal = UNBOUNDED;
  }
  //----------------------------------------------------------------------
  /** Return a string representation of the minimum value
   *  constraint. The constant {@link #UNBOUNDED UNBOUNDED} is returned
   *  to represent values which are not bounded. */
  public String getIntegerInfoMinVal() { return m_minVal; }
  //----------------------------------------------------------------------
  /** Return a string representation of the maximum value
   *  constraint. The constant {@link #UNBOUNDED UNBOUNDED} is returned
   *  to represent values which are not bounded. */
  public String getIntegerInfoMaxVal() { return m_maxVal; }
  //----------------------------------------------------------------------
  /* Create and return an object of this class, with a specified set of
   * XML name & value attributes.
   *
   * @param atts a HashMap containing the set of name and value
   * attributes.
   */
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_IntegerInfo(atts);
  }
  //----------------------------------------------------------------------
  /** The number info element accepts no sub-elements. */
  public void addElement(CSL_Element element)
  {
    super.addElement(element);
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. */
  String diagString()
  {
    String retVal =  NAME + "[";

    if (m_minVal != null)  retVal += m_minVal + ", ";
    if (m_maxVal != null)  retVal += m_maxVal;

    retVal += "]";

   return retVal;
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    return NAME;
  }
  //----------------------------------------------------------------------
  /** Return a string which is used as a short identification string for
   * an instance, for use in debugging statements etc. */
  public String getIDString()
  {
    return NAME;
  }
  //----------------------------------------------------------------------
  /** Return a string which summarises the content of self */
  public String getSummary()
  {
    String retVal = "";

    if (m_minVal != null)  retVal += "minVal=" + m_minVal + ", ";
    if (m_maxVal != null)  retVal += "maxVal=" + m_maxVal;

    retVal += "\n";

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Compare self with another {@link CSL_Element_IntegerInfo}
   *
   * @return true if this and <code>lhs</code> effectively define the
   * same number-info (ie in terms of content rather than object
   * reference), false otherwise.
   */
  public boolean similarTo(CSL_Element_IntegerInfo lhs)
  {

    if (m_maxVal.compareTo(lhs.m_maxVal) != 0)
    {
      return false;
    }
    if (m_minVal.compareTo(lhs.m_minVal) != 0)
    {
      return false;
    }

    return true;
  }
}
