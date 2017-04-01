package nrs.csl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Represent a NRS/CSL contains element */
public class CSL_Element_Contains extends CSL_Element
{
  ///// Attributes

  private String m_type;
  private int m_minOccurs;
  private int m_maxOccurs;

  ///// Attribute Names

  /** XML attribute to specify the element-type */
  public static String ATTR_TYPE = "type";

  /** XML attribute to specify minOccurs */
  public static String ATTR_MIN_OCCURS = "minOccurs";

  /** XML attribute to specify maxOccurs */
  public static String ATTR_MAX_OCCURS = "maxOccurs";

  ///// Sub-elements

  /** Sub-element 'attributes' within this contains section. Each
      element of the array is of type {@link
      nrs.csl.CSL_Element_Attribute}/ */
  private ArrayList m_attributes;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Contains";

  /** Value of the minOccurs/maxOccurs to use to represent an unbounded
   * number. */
  public static String ATTRVAL_UNBOUNDED = "unbounded";

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_Contains()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_Contains(HashMap atts)
  {
    super(NAME);

    m_type = (String) atts.get(ATTR_TYPE);

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
            PackageLogger.log.warning("Could not parse a value for attribute "
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
            PackageLogger.log.warning("Could not parse a value for attribute "
                          + ATTR_MIN_OCCURS
                          + " in XML element " + NAME );
          }
      }

    m_attributes = new ArrayList();
  }
  //----------------------------------------------------------------------
  /** Return the value associated with the 'minOccur' attribute */
  public int getMinOccurs() { return m_minOccurs; }
  //----------------------------------------------------------------------
  /** Return the value associated with the 'maxOccur' attribute, or -1
   * if the value represents unbounded. */
  public int getMaxOccurs() { return m_maxOccurs; }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of 'attribute' sub-elements. The
   * iterator is used to access objects of type {@link
   * nrs.csl.CSL_Element_Attribute}. */
  public Iterator getAttributesIterator()
  {
    return m_attributes.iterator();
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Contains(atts);
  }
  //----------------------------------------------------------------------
  /** Accept <code>Attribute</code> elements. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Attribute)
      {
        m_attributes.add(element);
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /**
   * Return the name of the NRS node to which this contains-rule applies
   */
  public String getType()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  /** Return the name of this element */
  public String toString()
  {
    return NAME;
  }
}
