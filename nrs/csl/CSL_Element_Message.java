package nrs.csl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

/** Represent a NRS/CSL message element */
public class CSL_Element_Message extends CSL_Element
{
  ///// Attributes

  private String m_name;

  // Is this message implicitly defined? True = yes, false = no
  private boolean m_implicit;

  ///// Sub-elements

  /** Sub-element description within this message. */
  private CSL_Element_Description m_desc;

  /** Sub-element unit segments within this message. Each element of the
      array is of type {@link nrs.csl.CSL_Element_Unit Element_Unit}/ */
  private ArrayList m_segments;

  /** Another way of storing segments, so that we can support name lookup */
  private HashMap m_segmentsMap;

  ///// Attribute Names

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Message";

  /** Psuedo-XML attribute to indicate if this element is implicit --
   * which means it hasn't been defined from CSL but instead is built
   * into the GUI. Thus the attribute is psuedo-XML since this attribute
   * should never appear in a CSL description of a unit. */
  public static String INT_ATTR_IMPLICIT = "_implicit_";

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_Message()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_Message(HashMap atts)
  {
    super(NAME);

   // Set the name, mandatory
    m_name = (String) atts.get(ATTR_NAME);

    // Set the implicitness state
    if (atts.containsKey(INT_ATTR_IMPLICIT))
      {
        m_implicit =
          Boolean.valueOf((String) atts.get(INT_ATTR_IMPLICIT)).booleanValue();
      }
    else
      {
        /* By default, we assume the unit has been defined in CSL */
        m_implicit = false;
      }

    m_segments = new ArrayList();
    m_segmentsMap = new HashMap();
  }
  //---------------------------------------------------------------------
  /** Return whether this message has been implicitly defined. A value
   * of <code>true</code> means is has, where-as <code>false</code>
   * means it was explicitiy defined in CSL. */
  public boolean isImplicit() { return m_implicit; }
  //----------------------------------------------------------------------
  /** Return the name of this element, as specified by the value of the
   * name attribute of the corresponding XML element. */
  public String getMessageName() { return m_name; }
  //----------------------------------------------------------------------
  /** Return the number of unit segments within this message type. */
  public int getSegmentCount() { return m_segments.size(); }
  //----------------------------------------------------------------------
  /**
   * Return the unit segment at position <code>i</code> within this
   * message type.
   *
   * @throws java.lang.IndexOutOfBoundsException if <code>i</code> does
   * not correspond to a unit segement within self.
   **/
  public CSL_Element_Segment getSegment(int i) throws IndexOutOfBoundsException
  {
    return (CSL_Element_Segment) m_segments.get(i);
  }
  //----------------------------------------------------------------------
  /**
   * Return the unit segment with the specified name, or
   * <code>null</code> if none found
   */
  public CSL_Element_Segment getSegment(String name)
  {
    if (m_segmentsMap.containsKey(name))
      return (CSL_Element_Segment) m_segmentsMap.get(name);
    else
      return null;
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the component segments. Objects accessed *
  through this iterator are of type {@link CSL_Element_Segment}. */
  public Iterator getSegmentIterator()
  {
    return m_segments.iterator();
  }
  //----------------------------------------------------------------------
  /** Return the {@link nrs.csl.CSL_Element_Description
   * CSL_Element_Description } instance associated with self, or null if
   * none was provided by the CSL. */
  public CSL_Element_Description getDesc() { return m_desc; }
  //----------------------------------------------------------------------
  /** Return a string which is used as a short identification string for
   * an instance, for use in debugging statements etc. */
  public String getIDString()
  {
    return getElementName() + "(name ='" + getMessageName() + "')";
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Message(atts);
  }
  //----------------------------------------------------------------------
  /** Resolve references to CSL units. This means this message will try
      to get a reference to an actual {@link CSL_Element_Unit}, instead
      of just holding the name to the unit. The list of units available
      for the process is supplied through the {@link CSL_Unit_Registry}
      parameter. */
  public void resolveUnits(CSL_Unit_Registry unitReg)
  {
    CSL_Element_Segment segment;

    // Loop over each of the segments belonging to this message type
    for (int i = 0; i < m_segments.size(); i++)
      {
        segment = (CSL_Element_Segment) m_segments.get(i);
        segment.resolveUnit(unitReg);
      }
  }
  //----------------------------------------------------------------------
  /** Accept <code>Description</code> and <code>Constraint</code>
   * elements. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Description)
      {
        m_desc = (CSL_Element_Description) element;
      }
    else if (element instanceof CSL_Element_Segment)
      {
        CSL_Element_Segment segment = (CSL_Element_Segment) element;

        m_segments.add(element);
        m_segmentsMap.put(segment.getSegmentName(), element);
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. */
  String diagString()
  {
    String retVal =  NAME + "[" + m_name;

    if (m_desc != null)  retVal += ", " + m_desc.diagString();
    if (m_implicit) retVal += ",  IMPLICIT";

    for (int i = 0; i < getSegmentCount(); i++)
      retVal += ", " + getSegment(i).diagString();

    retVal += "]";

   return retVal;
  }
  //----------------------------------------------------------------------
  /** Return the name of this message. */
  public String toString()
  {
    return m_name;
  }
  //----------------------------------------------------------------------
  /**
   * Compare self with another {@link CSL_Element_Message}
   * Note: an assumption is made that caller is source of possible connection.
   *
   * @return true if messages of the type specified by self can be
   * passed to the message type specified by <tt>rhs</tt>.
   */
  public boolean compatibleWith(CSL_Element_Message rhs)
  {
    if (rhs == null) return false;

    // HACK TO FIX PROBLEM WITH VOID NOT BEING A BASIC TYPE
    if ( getSegmentCount() == 0 && rhs.toString().equals("void")){
      // debug output
    //   PackageLogger.log.info("Checking compatibility of: " + m_name 
//                              + " and " + rhs.toString());
     //  PackageLogger.log.info("Diagnostics: \n" + diagString() 
//                              + "\n" + rhs.diagString());
    }
    // Reject is there is a different number of segments
    else if ( getSegmentCount() != rhs.getSegmentCount() ){
    //   PackageLogger.log.info("Different number of segments - lhs:" 
//                              + getSegmentCount() + " and rhs: " 
//                              + rhs.getSegmentCount());
      return false;
    }

    // Handle complex messages
    if ( getSegmentCount() > 1 )
    {
      for (Iterator i = getSegmentIterator(); i.hasNext(); )
      {
        CSL_Element_Segment segL = (CSL_Element_Segment) i.next();
        CSL_Element_Segment segR = rhs.getSegment(segL.getSegmentName());

        // Couldn't find a similarly named segment
        if (segR == null) return false;

        // _BUG_(Message type comparison is not finished!)
        //
        // Probably the best approach would be to define a global data
        // set, which is increased each time a new NRS component sends
        // its CSL to us. During such integration, all node, message,
        // and unit compatibilites will be carried out. This means that
        // for a unit type of a particular name, there will only be one
        // corresponding object. Note: this approach can loose data: ie,
        // consider two NRS components, A and B. Both provide, say,
        // "Voltage" message type, but B's definition contains more
        // description. Well, in the global registry, and array list to
        // both of these definitions could be maintained for the global
        // object - so a use can browser different definitions, as
        // provided by the different components. Note also, the good
        // thing about this whole approach is that consistency errors
        // are caught at CSL load time, rather that at some arbitrary
        // point in runtime when the user attempts to link a pair of
        // variables.

        // Compare segments by solely looking at the unit type
        CSL_Element_Unit unitL = segL.getSegmentResolvedUnit();
        CSL_Element_Unit unitR = segR.getSegmentResolvedUnit();

        if (!unitL.similarTo(unitR)) return false;
      }
    }
    // handle simple messages with one segment
    else if ( getSegmentCount() == 1 ){ 
      // get segments
      CSL_Element_Segment segL = getSegment(0);
      CSL_Element_Segment segR = rhs.getSegment(0);

      // get resolved units
      CSL_Element_Unit unit1 = segL.getSegmentResolvedUnit();
      CSL_Element_Unit unit2 = segR.getSegmentResolvedUnit();
      
      //PackageLogger.log.info("Compare segments: " + segL.toString() + " and "       // + segR.toString());
      //PackageLogger.log.info("Units to compare: " + unit1.getUnitType() 
      //+ " and " + unit2.getUnitType());
      
      // can't link down the unit hierarchy, 
      //e.g. can't link float to Time, but can link Time to float
      // (assumes caller is source of possible connection)
      if ( unit1.isImplicit() && !unit2.isImplicit() )
        return false;

      // check if basic types are compatible
      if ( !(unit1.getUnitType().equals(unit2.getUnitType())) )
        return false;

      // check if both units are implicit basic types and since we know
      // they are compatible (previous check) return true
      if ( unit1.isImplicit() && unit2.isImplicit() )
        return true;

      // otherwise need to check Unit restrictions compatibility
      // can go from restrictive to less restrictive, but not vice-versa

      // Compare string info
      if (unit1.getStringInfo() != null)
        {
          if( (unit2.getStringInfo() == null) ||
              (unit1.getStringInfo().similarTo(unit2.getStringInfo() )))
            {
              return true;
            }
        }
      else if( unit1.getStringInfo() == null && unit2.getStringInfo() != null)
        {
          return false;
        }
          
      // Compare integer info
      if (unit1.getIntegerInfo() != null)
        {
          if( (unit2.getIntegerInfo() == null) ||
              (unit1.getIntegerInfo().similarTo(unit2.getIntegerInfo() )))
            {
              return true;
            }
        }
      else if( unit1.getIntegerInfo() == null && unit2.getIntegerInfo() != null)
        {
          return false;
        }

      // Compare float info
      if (unit1.getFloatInfo() != null)
        {
          if( (unit2.getFloatInfo() == null) ||
              (unit1.getFloatInfo().similarTo(unit2.getFloatInfo() )))
            {
              return true;
            }
        }
      else if( unit1.getFloatInfo() == null && unit2.getFloatInfo() != null)
        {
          return false;
        }
    }
    // check void-type messages - always zero segments, by definition
    else { //if ( getSegmentCount() == 0 ){
      // PackageLogger.log.info("Zero segments: " + getMessageName() 
//                              + " and " + rhs.getMessageName());

      // can always go up unit hierarchy to void, or across if both 'void'
      if ( rhs.getMessageName().equals("void") )
        return true;
      
      // check if same message-type directly
      if ( !getMessageName().equals(rhs.getMessageName()))
        return false;
    }
          
    return true;
  }
}
