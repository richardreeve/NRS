package nrs.csl;

import java.util.logging.*;
import java.util.*;

/** Represent a NRS/CSL interface element */
public class CSL_Element_Interface extends CSL_Element
{
  ///// Attributes

  private String m_dir;
  private int m_minOccurs;
  private int m_maxOccurs;
  private boolean m_autoConnect = false;

  ///// Attribute Names

  /** XML attribute to specify the direction of this interface */
  public static String ATTR_DIRECTION = "direction";

  /** XML attribute to specify minOccurs */
  public static String ATTR_MIN_OCCURS = "minOccurs";

  /** XML attribute to specify maxOccurs */
  public static String ATTR_MAX_OCCURS = "maxOccurs";

  /** XML attribute to specify autoConnect */
  public static String ATTR_AUTO_CONNECT = "autoConnect";

  ///// Sub-elements

  /** Sub-element 'ingroups' within this interface elment. Each element
      of the array is of type {@link nrs.csl.CSL_Element_InGroup}/ */
  private ArrayList m_groups;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Interface";

  /** Value of the direction attribute to represent inwards */
  public static String ATTRVAL_IN = "in";

  /** Value of the direction attribute to represent outwards */
  public static String ATTRVAL_OUT = "out";

  /** Value of the direction attribute to represent a log-role */
  public static String ATTRVAL_LOG = "log";

  /** Value of the minOccurs/maxOccurs to use to represent an unbounded
   * number. */
  public static String ATTRVAL_UNBOUNDED = "unbounded";

  /** Logger for this class */
  private Logger m_log = Logger.getLogger("nrs.csl");

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_Interface()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_Interface(HashMap atts)
  {
    super(NAME);

    m_dir = (String) atts.get(ATTR_DIRECTION);

    // Extract the minOccurs number
    String temp = (String) atts.get(ATTR_MIN_OCCURS);
    if (temp.equalsIgnoreCase(ATTRVAL_UNBOUNDED))
      {
        m_minOccurs = -1;
      }
    else
      {
        try
          {
            m_minOccurs = Integer.parseInt(temp);
          }
        catch (NumberFormatException e)
          {
            m_log.warning("Could not parse a value for attribute "
                          + ATTR_MIN_OCCURS
                          + " in XML element " + NAME );
          }
      }

    // Extract the maxOccurs number
    temp = (String) atts.get(ATTR_MAX_OCCURS);
    if (temp.equalsIgnoreCase(ATTRVAL_UNBOUNDED))
      {
        m_maxOccurs = -1;
      }
    else
      {
        try
          {
            m_maxOccurs = Integer.parseInt(temp);
          }
        catch (NumberFormatException e)
          {
            m_log.warning("Could not parse a value for attribute "
                          + ATTR_MIN_OCCURS
                          + " in XML element " + NAME );
          }
      }

    temp = (String) atts.get(ATTR_AUTO_CONNECT);
    if (temp != null)
    {
      m_autoConnect = Boolean.parseBoolean(temp);
    }

    m_groups = new ArrayList();
  }
  //----------------------------------------------------------------------
  /**
   * Return true if this interface represents an inwards
   * direction. Note, you probably want to use the canIn() utility
   * method instead.
   *
   * @see #canIn()
   */
  public boolean isIn() { return m_dir.equalsIgnoreCase(ATTRVAL_IN); }
  //----------------------------------------------------------------------
  /**
   * Return true if this interface represents an outwards direction.
   * Note, you probably want to use the canOut() utility method instead.
   *
   * @see #canOut()
   */
  public boolean isOut() { return m_dir.equalsIgnoreCase(ATTRVAL_OUT); }
  //----------------------------------------------------------------------
  /** Return true if this interface represents a logger */
  public boolean isLog() { return m_dir.equalsIgnoreCase(ATTRVAL_LOG); }
  //----------------------------------------------------------------------
  /** Return <tt>true</tt> if this interface should attempt auto
   * connnect, or <tt>false</tt> */
  public boolean autoConnect() { return m_autoConnect; }
  //----------------------------------------------------------------------
  /** Return the value associated with the 'maxOccur' attribute, or -1
   * if the value represents unbounded. */
  public int getMaxOccurs() { return m_maxOccurs; }
  //----------------------------------------------------------------------
  /**
   * Return true if this interfaces represents an inwards direction and
   * the associated maxOccurs value is non-zero. This is a utility
   * method which saves the user the slight hassle of combining a pair
   * of the other related methods.
   */
  public boolean canIn()
  {
    return (isIn() && (m_maxOccurs != 0));
  }
  //----------------------------------------------------------------------
  /**
   * Return true if this interfaces represents an outwards direction and
   * the associated maxOccurs value is non-zero. This is a utility
   * method which saves the user the slight hassle of combining a pair
   * of the other related methods.
   */
  public boolean canOut()
  {
    return (isOut() && (m_maxOccurs != 0));
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of 'InGroup' sub-elements. The
   * iterator is used to access objects of type {@link
   * nrs.csl.CSL_Element_InGroup}. */
  public Iterator getInGroupIterator()
  {
    return m_groups.iterator();
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Interface(atts);
  }
  //----------------------------------------------------------------------
  /** Accept <code>Attribute</code> elements. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_InGroup)
      {
        m_groups.add(element);
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /** Return the name of this element */
  public String toString()
  {
    return NAME;
  }
}
