package nrs.csl;

import java.util.*;
import java.util.logging.*;
import nrs.core.message.Constants;

/** Register the various NRS unit types defined in CSL input */
public class CSL_Unit_Registry
{
  /** Sorted set of pairs, each consisting of a unit name and an
      instance of {@link nrs.csl.CSL_Element_Unit
      CSL_Element_Unit.}. Note, a sorted map is being used because it is
      desirable to have the list of units appear in alphabetical order
      in the list-of-units dialog. */
  Map m_map;

  // Class logger
  Logger m_log = Logger.getLogger("nrs.csl");

  /** The source CID of the NRS component which provided the units */
  private String m_sourceCID;

  //----------------------------------------------------------------------
  /** Constructor when CID is not known*/
  public CSL_Unit_Registry()
  {
    m_map = new TreeMap(); // sorted map, see comments above
    m_sourceCID = "";
  }
  //----------------------------------------------------------------------
  /** Constructor when CID is known */
  public CSL_Unit_Registry(String sourceCID)
  {
    m_map = new TreeMap(); // sorted map, see comments above
    m_sourceCID = sourceCID;
  }
  //----------------------------------------------------------------------
  /** Add a new unit type to the registry. */
  public void add(CSL_Element_Unit eUnit)
  {
   if (m_map.containsKey(eUnit.getUnitName()))
   {
     // okay, so map contains a unit of this name... next we need to
     // find it and make a more detailed check... Note, we only assume
     // there is one element in the map with the same name (which is a
     // fair assumption, since the point of this return is to ensure
     // that is the case).
     if (eUnit.similarTo((CSL_Element_Unit) m_map.get(eUnit.getUnitName())))
     {
       Logger.getLogger("nrs.csl")
         .fine("Ignoring attempt to add unit-type '"
               + eUnit.getUnitName()
               + "' to registry since it already exists.");
     }
     else
     {
       Logger.getLogger("nrs.csl")
         .warning("Failed to add unit-type '" + eUnit.getUnitName()
                  + "' to registry because"
                  + " it has been multiply defined. I.e.,"
                  +" another unit-type with same name"
                  + " but DIFFERENT meaning already exists.");
     }
   }
   else
   {
     m_map.put(eUnit.getUnitName(), eUnit);
   }
  }
  //----------------------------------------------------------------------
  /** Add the implicit units to registry. The <code>elementReg</code> is
   * used to construct each unit. The implicit units are those which
   * simply encapsule the basic types without adding any further
   * meaning. */
  public void addImplicitUnits(CSL_Element_Registry elementReg)
  {
    addImplicitUnit(CSL_BasicType_Float.NAME, elementReg);
    addImplicitUnit(CSL_BasicType_String.NAME, elementReg);
    addImplicitUnit(CSL_BasicType_Boolean.NAME, elementReg);
    addImplicitUnit(CSL_BasicType_Integer.NAME, elementReg);
    addImplicitUnit(CSL_BasicType_Route.NAME, elementReg);
    addImplicitUnit(CSL_BasicType_Void.NAME, elementReg);
  }
  //----------------------------------------------------------------------
  /** A a single implicit unit. */
  void addImplicitUnit(String basicType,
                       CSL_Element_Registry elementReg)
  {
    HashMap atts = new HashMap();
    atts.put(CSL_Element_Unit.ATTR_NAME, basicType);
    atts.put(CSL_Element_Unit.ATTR_TYPE, basicType);

    // This unit is implicit, so we add an appropriate indicator into
    // the list of attributes.
    atts.put(CSL_Element_Unit.INT_ATTR_IMPLICIT, Boolean.toString(true));

    CSL_Element newUnit = elementReg.buildElement(Constants.Namespace.NRS_CSL,
                                                  CSL_Element_Unit.NAME,
                                                  atts);

    // Finally add the new unit just created to the internal list
    add((CSL_Element_Unit) newUnit);

    m_log.fine("Added implicit unit with name='"
               + basicType
               + "'");

  }
  //----------------------------------------------------------------------
  /** Return a unit object by name, or return null if no unit with the
   * specified name is found in the registry. */
  public CSL_Element_Unit getUnit(String name)
  {
    return (CSL_Element_Unit) m_map.get(name);
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the units within the registry. The type of
   * each object iterated over is {@link CSL_Element_Unit}. */
  public Iterator getUnitIterator()
  {
    return m_map.values().iterator();
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    String retVal = "Unit Registry contents (" + m_map.size() + "):\n";

    Iterator it = m_map.values().iterator();
    while (it.hasNext())
      {
        CSL_Element_Unit eu = (CSL_Element_Unit) it.next();
        retVal += eu.getUnitName() + "\n";
     }

    return retVal;
  }
  //----------------------------------------------------------------------
  /** Return the number of units defined in the registry. */
  public int size()
  {
    return m_map.size();
  }
  //----------------------------------------------------------------------
  /**
   * Return the CID of the NRS component which is the source for the
   * units in this registry.
   */
  public String getSourceCID()
  {
    return m_sourceCID;
  }
  //----------------------------------------------------------------------
  /**
   * Set the CID of the NRS component which is the source for the
   * units in this registry.
   */
  public void setSourceCID(String cid)
  {
    m_sourceCID = cid;
  }
}
