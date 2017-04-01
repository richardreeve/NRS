// $Id: CSL_Element_InitialValue.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.HashMap;
import java.util.logging.Logger;

/** Represent a NRS/CSL contains element */
public class CSL_Element_InitialValue extends CSL_Element
{ 
  ///// Attributes

  private String m_segmentName;
  private String m_attributeReference;
  
  ///// Attribute Names

  /** XML attribute to specify the segment name this initial-value
   * element applies to. */
  public static String ATTR_NAME = "segmentName";

  /** XML attribute to specify the attribute-reference this
   * initial-value element applies to. */
  public static String ATTR_ATTRIBUTE_REF = "attributeReference";

 /** Name of the XML element to which this class corresponds. */  
  public static String NAME = "InitialValue";
  
  /** Logger for this class */
  private Logger m_log = Logger.getLogger("nrs.csl");

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_InitialValue()
  {
    super(NAME);
  } 
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_InitialValue(HashMap atts)
  {
    super(NAME);

    m_segmentName = (String) atts.get(ATTR_NAME);
    m_attributeReference = (String) atts.get(ATTR_ATTRIBUTE_REF);
  }
  //----------------------------------------------------------------------
  /** Return the name of the Segment sub-element of which this is the
   * initial value. */
  public String getSegmentName() { return m_segmentName;}
  //----------------------------------------------------------------------
  /** Return the name of the Attribute sub-element of which will store
   * the initial value. */
  public String getAttributeReference() { return m_attributeReference;}
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts) 
  { 
    return new CSL_Element_InitialValue(atts); 
  }
  //----------------------------------------------------------------------
  /** Return the name of this element */
  public String toString() 
  { 
    return NAME; 
  }
}
