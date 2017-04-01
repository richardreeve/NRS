package nrs.csl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import nrs.nrsgui.NRSComponent;   // _TODO_(Fix this import)

/** <p>An NRS application might have several sets of node, message and
 * unit libraries: typically one for each NRS component providing a CSL
 * file. The class brings those separate registries together to form the
 * global registry, which serves as the principle interface for other
 * parts of the code to aquire CSL related information.
 *
 * <p>This class works by regenerating its registry tables whenever a new
 * node, message or unit is added or removed. During this regeneration
 * any type conflicts are identified and logged.
 *
 * <p>The singleton pattern is implemented by this class, so there is no
 * need to create any instance. Instead just call {@link
 * #getInstance()}.
 *
 * <p>This class implements the observable patten by extending the
 * {@link Observable} class. Observers are notified when the registry
 * tables are rebuilt.
 *
 * <p>Note: if client code modifies the contents of a {@link
 * CSL_Unit_Registry} which has already been registered with this class,
 * then this global registry needs to be re-built.
 **/
public class CSL_Global_Registry extends Observable
{
  /** Set of pairs, consisting of a element name and an instance of the
    * class corresponding to that element: each element is of type
    * {@link CSL_Element_Unit}. */
  private HashMap m_units;

  /**  Set of pairs, consisting of a element name and an instance of the
    * class corresponding to that element: each element is of type
    * {@link CSL_Element_Message}. */
  private HashMap m_msgs;

  /** Collection of {@link CSL_Unit_Registry} instances that have been
   * registered with this class. */
  private ArrayList m_unitRegs;

  /** Collection of {@link CSL_Message_Registry} instances that have been
   * registered with this class. */
  private ArrayList m_msgRegs;

  /** Collection of {@link CSL_Node_Registry} instances that have been
   * registered with this class. */
  private ArrayList m_nodeRegs;

  /** Collection of the {@link CSL_Element_NodeDescription} elements
   * that can be created at root level. */
  private ArrayList m_rootNodes;

  private static CSL_Global_Registry m_instance;

  //----------------------------------------------------------------------
  /**
   * Constructor. This is private, because this class adopts the
   * singleton pattern. To use this class call the {@link
   * #getInstance()} method.
   */
  private CSL_Global_Registry()
  {
    m_units = new HashMap();
    m_msgs = new HashMap();
    m_rootNodes = new ArrayList();
    m_unitRegs = new ArrayList();
    m_msgRegs = new ArrayList();
    m_nodeRegs = new ArrayList();
  }
  //----------------------------------------------------------------------
  /**
   * Access the singleton instance.
   */
  static public CSL_Global_Registry getInstance()
  {
    if (m_instance == null) m_instance = new CSL_Global_Registry();

    return m_instance;
  }
  //----------------------------------------------------------------------
  /**
   * Add a {@link CSL_Unit_Registry}. After adding <tt>registry</tt> and
   * all of its units to internal lists, {@link #rebuild()} is called to
   * fully update the global registry.
   */
  public void addRegistry(CSL_Unit_Registry registry)
  {
    if ((registry != null) && (!m_unitRegs.contains(registry)))
    {
      m_unitRegs.add(registry);
      rebuild();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Add a {@link CSL_Message_Registry}. After adding <tt>registry</tt> and
   * all of its messages to internal lists, {@link #rebuild()} is called to
   * fully update the global registry.
   */
  public void addRegistry(CSL_Message_Registry registry)
  {
    if ((registry != null) && (!m_msgRegs.contains(registry)))
    {
      m_msgRegs.add(registry);
      rebuild();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Add a {@link CSL_Node_Registry}. After adding <tt>registry</tt> to
   * an internal list, {@link #rebuild()} is called to fully update the
   * global registry.
  */
  public void addRegistry(CSL_Node_Registry registry)
  {
    if ((registry != null) && (!m_nodeRegs.contains(registry)))
    {
      m_nodeRegs.add(registry);
      rebuild();
    }
    scanRootNodes(registry);
  }
  //----------------------------------------------------------------------
  /**
   * This allows all three registries to be updated in one call; thus,
   * it is equivalent to calling {@link #addRegistry(CSL_Unit_Registry)}
   * {@link #addRegistry(CSL_Message_Registry)} and {@link
   * #addRegistry(CSL_Node_Registry)} one after the other.
   */
  public void addRegisties(CSL_Unit_Registry uRegistry,
                           CSL_Message_Registry mRegistry,
                           CSL_Node_Registry nRegistry)
  {
    boolean rebuildNeeded = false;
    boolean scanRootNodesNeeded = false;

    if ((uRegistry != null) && (!m_unitRegs.contains(uRegistry)))
    {
      m_unitRegs.add(uRegistry);
      rebuildNeeded = true;
    }

    if ((mRegistry != null) && (!m_msgRegs.contains(mRegistry)))
    {
      m_msgRegs.add(mRegistry);
      rebuildNeeded = true;
    }

    if ((nRegistry != null) && (!m_nodeRegs.contains(nRegistry)))
    {
      m_nodeRegs.add(nRegistry);
      rebuildNeeded = true;
      scanRootNodesNeeded = true;
    }

    if (rebuildNeeded) rebuild();

    if (scanRootNodesNeeded) scanRootNodes(nRegistry);
  }
  //----------------------------------------------------------------------
  /**
   * <p>Scan for the new root nodes available and form a simple list of
   * their names for user-feedback purposes only.
   *
   * <p>This routine finishes by invoking the update method on its
   * observers with <tt>arg</tt> containing a {@link Collection} of
   * {@link String} objects, the elements of which are the names of new
   * root nodes available, except for the first element, which instead
   * contains the CID of the source component.
   */
  private void scanRootNodes(CSL_Node_Registry nRegistry)
  {
    ArrayList rootNames = new ArrayList();

    rootNames.add(nRegistry.getSourceCID());

    for (Iterator j = nRegistry.getNodeIterator(); j.hasNext(); )
    {
      CSL_Element_NodeDescription u = (CSL_Element_NodeDescription) j.next();
      if (u.getRootNode() == true)
      {
        rootNames.add(u.toString());
      }
    }

    if (rootNames.size() > 1)
    {
      setChanged();
      notifyObservers(rootNames);
    }
  }
  //----------------------------------------------------------------------
  /**
   * <p>Rebuild the unit, message and node registries. This is
   * automatically called whenever a client registry is added or removed
   * from this class; however, clients should call it if they modify
   * registries that have been added to this class.
   *
   * <p>This routine finishes by invoking the update method on its
   * observers with <tt>arg</tt> set to <tt>null</tt>.
   */
  public void rebuild()
  {
    PackageLogger.log.info("Rebuilding global registry");
    m_units.clear();
    m_msgs.clear();
    m_rootNodes.clear();

    // rebuild units
    for (Iterator i = m_unitRegs.iterator(); i.hasNext(); )
    {
      CSL_Unit_Registry ur = (CSL_Unit_Registry) i.next();

      for (Iterator j = ur.getUnitIterator(); j.hasNext(); )
      {
        CSL_Element_Unit u = (CSL_Element_Unit) j.next();
        m_units.put(u.getUnitName(), u);
      }
    }

    // rebuild messages
    for (Iterator i = m_msgRegs.iterator(); i.hasNext(); )
    {
      CSL_Message_Registry mr = (CSL_Message_Registry) i.next();

      for (Iterator j = mr.getMessageIterator(); j.hasNext(); )
      {
        CSL_Element_Message m = (CSL_Element_Message) j.next();
        m_msgs.put(m.getMessageName(), m);
      }
    }

    // rebuild root nodes
    for (Iterator i = m_nodeRegs.iterator(); i.hasNext(); )
    {
      CSL_Node_Registry ur = (CSL_Node_Registry) i.next();

      for (Iterator j = ur.getNodeIterator(); j.hasNext(); )
      {
        CSL_Element_NodeDescription u = (CSL_Element_NodeDescription) j.next();
        if (u.getRootNode() == true) m_rootNodes.add(u);
      }
    }

    setChanged();
    notifyObservers();
  }
  //----------------------------------------------------------------------
  /**
   * Return the list of root nodes. Each element in the list is of type
   * {@link CSL_Element_NodeDescription}.
   */
  public ArrayList getRootNodes()
  {
    return m_rootNodes;
  }
  //----------------------------------------------------------------------
  /**
   * Return a {@link Collection} of the root nodes provided by a
   * specified component. Will return <tt>null</tt> if the component
   * does not have a node-registry registered with this global registry.
   */
  public Collection getRootNodes(NRSComponent c)
  {
    ArrayList retVal = new ArrayList();

    for (Iterator i = m_nodeRegs.iterator(); i.hasNext(); )
    {
      CSL_Node_Registry nReg = (CSL_Node_Registry) i.next();

      if (nReg.getSourceCID().equals(c.getCID()))
      {
        for (Iterator j = nReg.getNodeIterator(); j.hasNext(); )
        {
          CSL_Element_NodeDescription node =
            (CSL_Element_NodeDescription) j.next();

          if (node.getRootNode() == true) retVal.add(node);
        }

        return retVal;
      }
    }

    //    PackageLogger.log.warning("Component "
    //                              + c
    //                              + " doesn't match with any node-registry"
    //                              + " in the global registry");
    return null;
  }
  //----------------------------------------------------------------------
  /**
   * Return a {@link Collection} of the possible nodes that be contained
   * within the specified node. Each element of the returned collection
   * is of type {@link ContainmentRule}. Can return <tt>null</tt> if no
   * registry was found which contained the node.
   */
  public Collection getContainmentRules(CSL_Element_NodeDescription parent)
  {
    ArrayList retVal = new ArrayList();
    CSL_Node_Registry reg = null;

    // First, find out which node registry this node belongs to
    for (Iterator i = m_nodeRegs.iterator(); i.hasNext(); )
    {
      reg = (CSL_Node_Registry) i.next();

      if (reg.contains(parent)) break; else reg = null;
    }

    if (reg == null)
    {
      PackageLogger.log.warning("No node registry appears to own node type : "
                                + parent);
      return null;
    }

    // Next, for each of the nodes contains...
    for (Iterator i = parent.getContainsIterator(); i.hasNext(); )
    {
      CSL_Element_Contains contains = (CSL_Element_Contains) i.next();

      // Now, assume it is a single group, search through the list of nodes
      CSL_Element_NodeDescription singleNode = reg.get(contains.getType());
      if (singleNode != null)
      {
        retVal.add(new ContainmentRule(singleNode, contains.getMinOccurs(),
                                       contains.getMaxOccurs()));
      }

      // Now, assume the contains refers to a group; so search through
      // each element in the node registry, and see if it's in a group
      // of this name
      ArrayList group = new ArrayList();
      String groupDisplayName = contains.getType(); /* its default value */
      for (Iterator j = reg.getNodeIterator(); j.hasNext(); )
      {
        CSL_Element_NodeDescription jNode =
          (CSL_Element_NodeDescription) j.next();

        for (Iterator k = jNode.getInGroupsIterator(); k.hasNext(); )
        {
          CSL_Element_InGroup kGroup = (CSL_Element_InGroup) k.next();
          if (kGroup.getInGroupName().equals(contains.getType()))
          {
            group.add(jNode);
            groupDisplayName = kGroup.toString();
          }
        }
      }
      if (group.size() > 0)
      {
        retVal.add(new ContainmentRule(group,
                                       contains.getMinOccurs(),
                                       contains.getMaxOccurs(),
                                       contains.getType(),
                                       groupDisplayName));
      }

      // Sanity check
      if ((singleNode != null) && (group.size() > 0))
      {
        PackageLogger.log.warning("<Contains> name " + contains.getType()
                                  + " in node "
                                  + parent.getNodeDescriptionName()
                                  + " matches to another node-name and to "
                                  + " an <InGroup> group of nodes");
      }
    }

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Attempts to find the {@link CSL_Element_NodeDescription} given its
   * name and {@link NRSComponent} which provides it.
   *
   * @return the {@link CSL_Element_NodeDescription} if a match found,
   * else <tt>null</tt>
   */
  public CSL_Element_NodeDescription findType(String name, NRSComponent c)
  {
    for (Iterator i = m_nodeRegs.iterator(); i.hasNext(); )
    {
      CSL_Node_Registry nodeReg = (CSL_Node_Registry) i.next();

      if (nodeReg.getSourceCID().equals(c.getCID()))
      {
        CSL_Element_NodeDescription type = nodeReg.get(name);

        if (type == null)
        {
          PackageLogger.log.warning("NRS component " + c
                                    + " does not support node type '"
                                    + name +"'");
        }
        return type;
      }
    }

    PackageLogger.log.warning("CSL global registry has no node registry "
                              + "for component " + c);
    return null;
  }
}
