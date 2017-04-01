package nrs.csl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/** Registery of the various node-types which have been declared and
 * defined by the CSL */
public class CSL_Node_Registry
{
  /** Sorts a set of pairs, each consisting of a node name and an
      instance of {@link CSL_Element_NodeDescription}. */
  Map m_map;

  // Class logger
  Logger m_log = Logger.getLogger("nrs.csl");

  /** The source CID of the NRS component which provided the node */
  private String m_sourceCID;

  //----------------------------------------------------------------------
  /** Constructor for when CID is not known */
  public CSL_Node_Registry()
  {
    m_map = new HashMap();
    m_sourceCID = "";
  }
  //----------------------------------------------------------------------
  /** Constructor for when CID is known */
  public CSL_Node_Registry(String sourceCID)
  {
    m_map = new HashMap();
    m_sourceCID = sourceCID;
  }
  //----------------------------------------------------------------------
  /**
   * Add a new node type to the registry. The specified node also gets
   * tagged with the source CID associated with this node registry.
   */
  public void add(CSL_Element_NodeDescription eNode)
  {
    // Note, this is one way of setting the cid for individual
    // nodes. The other is via the setSourceCID() method of this class
    eNode.setSourceCID(m_sourceCID);

    if (m_map.containsKey(eNode.getNodeDescriptionName()))
      {
        m_log.warning("Ignoring attempt to add node-type '"
                   + eNode.getNodeDescriptionName()
                   + "' to registry since it already exists.");
      }
    else
      {
        m_map.put(eNode.getNodeDescriptionName(), eNode);
      }
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the nodes within the registry. Objects
   * accessed through the iterator are of type
   * {@link CSL_Element_NodeDescription}. */
  public Iterator getNodeIterator()
  {
    return m_map.values().iterator();
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to locate a node type in the registry by its typename
   *
   * @return the {@link CSL_Element_NodeDescription} associated with
   * <code>name</code>, or <code>null</code> if no such object found.
   *
   */
  public CSL_Element_NodeDescription get(String name)
  {
    if (m_map.containsKey(name))
      return (CSL_Element_NodeDescription) m_map.get(name);
    else
      return null;
  }
  //----------------------------------------------------------------------
  /**
   * Causes each nodetype in the registry to instruct children
   * attributes to resolved their units types.  This means each {@link
   * CSL_Element_Attribute} owned by each nodetype will try to get a
   * reference to an actual {@link CSL_Element_Unit}, instead of just
   * holding the name to a unit. The list of units available for this
   * process is supplied through the {@link CSL_Unit_Registry}
   * parameter.
   */
  public void resolveAttributes(CSL_Unit_Registry unitReg)
  {
    for (Iterator it = m_map.values().iterator(); it.hasNext(); )
    {
      CSL_Element_NodeDescription e
        = (CSL_Element_NodeDescription) it.next();

      e.resolveAttributes(unitReg);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Instructs each node type in the registry to instruct its child
   * variables to resolve their message types using the supplied message
   * type registry.
   */
  public void resolveMessageTypes(CSL_Message_Registry msgReg)
  {
    for (Iterator it = m_map.values().iterator(); it.hasNext(); )
    {
      CSL_Element_NodeDescription e
        = (CSL_Element_NodeDescription) it.next();

      e.resolveMessageTypes(msgReg);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Write a log entry which lists all of the elements available in this
   * registry.
   */
  public String toString()
  {
    String retVal = "Node Registry contents (" + m_map.size() + "):\n";

    Iterator it = m_map.values().iterator();
    while (it.hasNext())
      {
         CSL_Element_NodeDescription n =
           (CSL_Element_NodeDescription) it.next();

        retVal += n.getNodeDescriptionName() + "\n";
     }

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Return the CID of the NRS component which is the source for the
   * nodes in this registry.
   */
  public String getSourceCID()
  {
    return m_sourceCID;
  }
  //----------------------------------------------------------------------
  /**
   * Set the CID of the NRS component which is the source for the node
   * in this registry. Each node description in this registry will also
   * be given this value.
   */
  public void setSourceCID(String cid)
  {
    m_sourceCID = cid;

    Iterator it = m_map.values().iterator();
    while (it.hasNext())
    {
      CSL_Element_NodeDescription n =
        (CSL_Element_NodeDescription) it.next();

      n.setSourceCID(cid);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Returns <tt>true</tt> if the specified node exists within this
   * registry, <tt>false</tt> otherwise.
   */
  public boolean contains(CSL_Element_NodeDescription n)
  {
    return m_map.values().contains(n);
  }
}
