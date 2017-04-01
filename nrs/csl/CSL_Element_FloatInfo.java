package nrs.csl;

import java.util.HashMap;

/** Represent a NRS/CSL float-info element. */
public class CSL_Element_FloatInfo extends CSL_Element
{
  // Attributes
  private String m_scale;
  private String m_abbrev;
  private String m_minVal;
  private String m_maxVal;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "FloatInfo";

  /** XML attribute to specify the scale */
  public static String ATTR_SCALE = "scale";

  /** XML attribute to specify the abbreviation */
  public static String ATTR_ABBREV = "abbreviation";

  /** XML attribute to specify the minimum value */
  public static String ATTR_MINVAL = "minVal";

  /** XML attribute to specify the maximum value */
  public static String ATTR_MAXVAL = "maxVal";

  /** The text string which represents the value of minVal or maxVal if
   * an unbounded quantity is described. */
  public static String UNBOUNDED = "unbounded";

  //----------------------------------------------------------------------
  /** Constructor for prototype class. */
  CSL_Element_FloatInfo()
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
  public CSL_Element_FloatInfo(HashMap atts)
  {
    super(NAME);

    // Set the scale, optional
    if (atts.containsKey(ATTR_SCALE))
      {
        m_scale = (String) atts.get(ATTR_SCALE);
      }

    // Set the abbreviation, optional
    if (atts.containsKey(ATTR_ABBREV))
      {
        m_abbrev = (String) atts.get(ATTR_ABBREV);
      }

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
  /** Return value can be null, if scale has not been provided by the CSL
  * (e.g, values for learning rates etc.) */
  public String getFloatInfoScale() { return m_scale; }
  //----------------------------------------------------------------------
  /** Return value can be null, if abbreviation has not been provided by
   * the CSL (eg, values for learning rates etc.) */
  public String getFloatInfoAbbrev() { return m_abbrev; }
  //----------------------------------------------------------------------
  /** Return a string representation of the minimum value
   *  constraint. The constant {@link #UNBOUNDED UNBOUNDED} is returned
   *  to represent values which are not bounded. */
  public String getFloatInfoMinVal() { return m_minVal; }
  //----------------------------------------------------------------------
  /** Return a string representation of the maximum value
   *  constraint. The constant {@link #UNBOUNDED UNBOUNDED} is returned
   *  to represent values which are not bounded. */
  public String getFloatInfoMaxVal() { return m_maxVal; }
  //----------------------------------------------------------------------
  /* Create and return an object of this class, with a specified set of
   * XML name & value attributes.
   *
   * @param atts a HashMap containing the set of name and value
   * attributes.
   */
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_FloatInfo(atts);
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

    if (m_scale != null) retVal += m_scale + ", ";
    if (m_abbrev != null) retVal += m_abbrev + ", ";
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

    if (m_scale != null) retVal += "scale=" + m_scale + ", ";
    if (m_abbrev != null) retVal += "abbrev=" + m_abbrev + ", ";
    if (m_minVal != null)  retVal += "minVal=" + m_minVal + ", ";
    if (m_maxVal != null)  retVal += "maxVal=" + m_maxVal;

    retVal += "\n";

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Compare self with another {@link CSL_Element_FloatInfo}
   *
   * @return true if this and <code>lhs</code> effectively define the
   * same number-info (ie in terms of content rather than object
   * reference), false otherwise.
   */
  public boolean similarTo(CSL_Element_FloatInfo lhs)
  {
    if (m_scale == null)
    {
      if (lhs.m_scale != null)
      {
        return false;
      }
    }
    else
    {
      if ((lhs.m_scale == null) || (lhs.m_scale.compareTo(m_scale) != 0))
      {
        return false;
      }
    }

    if (m_abbrev == null)
    {
      if (lhs.m_abbrev != null)
      {
        return false;
      }
    }
    else
    {
      if ((lhs.m_abbrev == null) || (lhs.m_abbrev.compareTo(m_abbrev) != 0))
      {
        return false;
      }
    }

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
