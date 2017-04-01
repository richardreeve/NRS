// $Id: CSL_Element_Root.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.logging.*;
import java.util.*;


/** Represent a root CSL element which, instead of corresponding to a
CSL XML element, instead is reponsible for the processing of other
CSL_Element units.

TODO - this class, which represents a root element, does not process or
store the XML attribute belonging to that element (and as defined in the
design doc). This should be done via the createInstance method
below. Although, currently the GUI assumes there is only ever going to
be a single root. But in reality there are going to be many, either
through several CSL files, or perhaps even nested
<Capabilities>. Perhaps each CSL_Element object corresponding to each
<Capabilities> could have its own set of unit, message and node
registries? That might be too cumbersome. Instead, perhaps I should
start keeping a registry of all the capabilities elements which have
been defined, together with their parent capabilities, and then for each
unit, message and node, I should store which <Capabilities> it was
stored in. This information will eventually be needed, because the user
might need to know which host is providing a particular simulation
service.

TODO - remove wildcard import statments

*/
public class CSL_Element_Root extends CSL_Element
{
  CSL_Unit_Registry m_unitReg;
  CSL_Message_Registry m_messageReg;
  CSL_Node_Registry m_nodeReg;

   /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Capabilities";

  //----------------------------------------------------------------------
  /** Constructor for prototype class. */
  CSL_Element_Root(CSL_Unit_Registry unitReg,
                   CSL_Message_Registry messageReg,
                   CSL_Node_Registry nodeReg)
  {
    super(NAME);

    m_unitReg = unitReg;
    m_messageReg = messageReg;
    m_nodeReg = nodeReg;
  }
  //----------------------------------------------------------------------
  /** Accept and process various kinds of CSL element. Those of type
   * <code>Unit</code> are added to a Unit registry. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Unit)
      {
        m_unitReg.add((CSL_Element_Unit) element);
      }
    else if (element instanceof CSL_Element_Message)
      {
        m_messageReg.add((CSL_Element_Message) element);
      }
    else if (element instanceof CSL_Element_NodeDescription)
      {
        m_nodeReg.add((CSL_Element_NodeDescription) element);
      }
    else
      {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /** No new elment is created. Instead the prototype instance (i.e.,
   * self) is returned. */
  public CSL_Element createInstance(HashMap atts)
  {
    return this;
  }
}
