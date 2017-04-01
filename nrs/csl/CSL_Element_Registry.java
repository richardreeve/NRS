package nrs.csl;

import java.util.*;

/** Register the various classes which correspond to CSL XML
 * elements. An important function provided by this class is the
 * construction of a CSL object if given the name an XML element and its
 * list of attributes. */
public class CSL_Element_Registry
{
  /** Set of pairs, consisting of a element name and an instance of the
   * class corresponding to that element (i.e., base class of each
   * element is CSL_Element. */
  HashMap m_map;

  //----------------------------------------------------------------------
  /** Constructor

      Todo - this is not nice, having to provide various registries to
      the constructor of CSL_Element_Registry. They are actually
      required by CSL_Element_Root. So really I should sent them
      directly there, instead of through this call. Perhaps it would be
      better for the CSL_Element_Root to create them itself, and
      providing accessor methods to there for the rest of the program to
      use.

      Also note - although these four registries have similar format
      names, there is an importance difference between this one, and the
      other three. This registry stores a list of *known* CSL XML
      element types, whereas the other three store the results form the
      processing of the CSL file. This registry is used to create
      instances of other objects.

      It initially seems plausible to design CSL_Element_Root as a
      singleton - which owns the three dynamic registries. However,
      during the course of the GUI session, several different CSL files
      might be loaded from different sources, so making it a singleton
      isn't such a good idea. Also, this leads to the question: do we
      want separate sources of CSL inputs to result in separate sets of
      the dynamic-registries? Probably no - easier just to stick with
      three dynamic registries.*/
  public CSL_Element_Registry(CSL_Unit_Registry unitReg,
                              CSL_Message_Registry messageReg,
                              CSL_Node_Registry nodeReg)
  {
    m_map = new HashMap();

    createPrototypes(unitReg, messageReg, nodeReg);
  }
  //----------------------------------------------------------------------
  /** Create one prototype of each element class, which is the act of
   * registering a class with the registry, so further instances of that
   * class can be constructed.  */
  private void createPrototypes(CSL_Unit_Registry unitReg,
                        CSL_Message_Registry messageReg,
                        CSL_Node_Registry nodeReg)
  {
    ///// Add special elements (that require constructor arguments)
    registerPrototype(new CSL_Element_Root(unitReg, messageReg, nodeReg));

    ///// Add all other types of element
    registerPrototype(new CSL_Element_Description());
    registerPrototype(new CSL_Element_Constraint());
    registerPrototype(new CSL_Element_Segment());
    registerPrototype(new CSL_Element_Unit());
    registerPrototype(new CSL_Element_Message());
    registerPrototype(new CSL_Element_ListMember());
    registerPrototype(new CSL_Element_NodeDescription());
    registerPrototype(new CSL_Element_InGroup());
    registerPrototype(new CSL_Element_Attribute());
    registerPrototype(new CSL_Element_Contains());
    registerPrototype(new CSL_Element_Interface());
    registerPrototype(new CSL_Element_InitialValue());
    registerPrototype(new CSL_Element_Variable());
    registerPrototype(new CSL_Element_IntegerInfo());
    registerPrototype(new CSL_Element_FloatInfo());
    registerPrototype(new CSL_Element_StringInfo());
  }
  //----------------------------------------------------------------------
  /** Add the 'protoType' element to the internal store of prototypes. */
  private void registerPrototype(CSL_Element protoType)
  {
    m_map.put(protoType.getQName(), protoType);
  }
  //----------------------------------------------------------------------
  /** Return true if there exists a class registered the corresponds to
   * the specified XML element. */
  public boolean elementSupported(String URI, String localName)
  {
    return m_map.containsKey(formQName(URI, localName));
  }
  //----------------------------------------------------------------------
  /** Return an instance of the class corresponding to the named CSL XML
   * element, or null if an element with name <code>name</code> is not
   * recognised. */
  public CSL_Element buildElement(String URI, String name, HashMap atts)
  {
    CSL_Element element = (CSL_Element) m_map.get(formQName(URI, name));

    if (element == null)
      {
        return null;
      }
    else
      {
        return element.createInstance(atts);
      }
  }
  //----------------------------------------------------------------------
  /** STandard toString conversion used to provide diagnostic
   * output. The returned string is a list of the classes registered in
   * the registry, together with the qualified name of the corresponding
   * XML element. */
  public String toString()
  {
    Iterator it = m_map.values().iterator();
    CSL_Element e;
    String retVal = "Registered CSL Element Classes\n";
    while (it.hasNext())
      {
        e = (CSL_Element) it.next();
        retVal += "<" + e.getElementName() + ">, " + e.getQName() + "\n";
      }

    return retVal;
  }
  //----------------------------------------------------------------------
  /** Combine an XML elment namespace URI and local name in the
  conventional way to allow comparison to internal and other URI/name
  pairs. */
  String formQName(String URI, String localName)
  {
    return  URI.toUpperCase() + localName.toUpperCase();
  }
}
