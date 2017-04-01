// $Id: CSL_Element_Constraint.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.HashMap;
import java.util.logging.Logger;

/** Represent a NRS/CSL constraint element.  */
public class CSL_Element_Constraint extends CSL_Element
{
  private String m_segmentName;

  private double  m_upperBound;
  private boolean m_upperUnbounded;

  private double m_lowerBound;
  private boolean m_lowerUnbounded;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Constraint";

  ///// Attribute Names

  /** XML attribute to specify the segment name this initial-value
   * element applies to. */
  public static String ATTR_NAME = "segmentName";

  /** XML attribute to specify the minimum value */
  public static String ATTR_MIN_VAL = "minVal";

  /** XML attribute to specify the maximum value */
  public static String ATTR_MAX_VAL = "maxVal";

  /** XML value representing an unbounded constraint */
  private static String UNBOUNDED = "unbounded";

  //----------------------------------------------------------------------
  /** Constructor  */
  CSL_Element_Constraint()
  {
    super(NAME);

    m_upperUnbounded = true;
    m_lowerUnbounded = false;
  }
  //----------------------------------------------------------------------
  /** Constructor  */
  public CSL_Element_Constraint(HashMap atts)
  {
    super(NAME);

    m_segmentName = (String) atts.get(ATTR_NAME);

    String s;

    // Set the lower bounds
    if (atts.containsKey(ATTR_MIN_VAL))
      {
        m_lowerUnbounded = false;
        try
          {
            m_lowerBound = Double.parseDouble((String) atts.get(ATTR_MIN_VAL));
          }
        catch (NumberFormatException e)
          {
            // value is a string, so assume this means unbounded
            m_lowerUnbounded = true;

            // print a warning if the string isn't actually 'unbounded'
            if (!UNBOUNDED.equalsIgnoreCase((String) atts.get(ATTR_MIN_VAL)))
              {
                Logger.getLogger("nrs.csl")
                  .warning("Constraint attribute "
                           + ATTR_MIN_VAL
                           + " has unrecognised string value '"
                           + (String) atts.get(ATTR_MIN_VAL)
                           + "'. Being treated as unbounded.");
              }
          }
      }
    else
      {
        m_lowerUnbounded = true;
      }

    // Set the upper bounds
    if (atts.containsKey(ATTR_MAX_VAL))
      {
        m_upperUnbounded = false;
        try
          {
            m_upperBound = Double.parseDouble((String) atts.get(ATTR_MAX_VAL));
          }
        catch (NumberFormatException e)
          {
            // value is a string, so assume this means unbounded
            m_upperUnbounded = true;

            // print a warning if the string isn't actually 'unbounded'
            if (!UNBOUNDED.equalsIgnoreCase((String) atts.get(ATTR_MAX_VAL)))
              {
                Logger.getLogger("nrs.csl")
                  .warning("Constraint attribute "
                           + ATTR_MAX_VAL
                           + " has unrecognised string value '"
                           + (String) atts.get(ATTR_MAX_VAL)
                           + "'. Being treated as unbounded.");
              }
         }

      }
    else
      {
        m_upperUnbounded = true;
      }
  }
  //----------------------------------------------------------------------
  /** Return the name of the Segment sub-element for which this is a
      constraint */
  public String getSegmentName() { return m_segmentName;}
  //----------------------------------------------------------------------
  /** Virtual constructor - return an instance of the dynamic *
  class. Must be overidden by each base class. */
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Constraint(atts);
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. */
  String diagString()
  {
    String retVal =  NAME + "[";

    if (m_lowerUnbounded)
      retVal += "unbounded";
    else
      retVal += Double.toString(m_lowerBound);

    retVal += ", ";


    if (m_upperUnbounded)
      retVal += "unbounded";
    else
      retVal += Double.toString(m_upperBound);

    retVal += "]";

    return retVal;
  }
  //----------------------------------------------------------------------
  public String toString() { return diagString(); }
  //----------------------------------------------------------------------
  /** Return a string representation of the lower bound. */
  public String getLowerBoundStr()
  {
    if (m_lowerUnbounded)
      return "unbounded";
    else
      return Double.toString(m_lowerBound);
  }
  //----------------------------------------------------------------------
  /** Return a string representation of the upper bound. */
  public String getUpperBoundStr()
  {
    if (m_upperUnbounded)
      return "unbounded";
    else
      return Double.toString(m_upperBound);
  }

}
