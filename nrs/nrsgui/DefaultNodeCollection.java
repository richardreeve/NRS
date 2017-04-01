package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;
import nrs.csl.ContainmentRule;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard implementation of the {@link NodeCollection} interface.
 */
public class DefaultNodeCollection implements NodeCollection
{
  /** Reference to the list of nodes contained in this collection.  Each
   * element in the list is of type {@link Node}. Derived classes assign
   * to this member through the class constructor. */
  protected final Collection m_nodes;

  /** Reference to the parent of this node collection */
  protected final Node m_parent;

  /** Listeners, each of type NodeListener */
  private HashSet m_listeners = new HashSet();

  //----------------------------------------------------------------------
  /**
   * Constructor for creating a node which has a <code>collection</code>
   * of sub-nodes and parent <code>parent</code>
   */
  protected DefaultNodeCollection(Collection collection, Node parent)
  {
    m_nodes = collection;
    m_parent = parent;
  }
  //----------------------------------------------------------------------
  /**
   * Constructor for creating a node which has parent <code>parent</code>
   */
  protected DefaultNodeCollection(Node parent)
  {
    m_nodes = new ArrayList();
    m_parent = parent;
  }
  //----------------------------------------------------------------------
  /**
   * Constructor for creating a node which has no parent and no children
   */
  protected DefaultNodeCollection()
  {
    m_nodes = new ArrayList();
    m_parent = null;
  }
  //----------------------------------------------------------------------
  // Implements {@link NodeCollection} interface
  public Node getParent()
  {
    return m_parent;
  }
  //----------------------------------------------------------------------
  // Implements {@link NodeCollection} interface
  public Iterator nodeIterator()
  {
    return m_nodes.iterator();
  }
  //----------------------------------------------------------------------
  // Implements {@link NodeCollection} interface
  // _TODO_(add output of error messages from this guy)
  public void add(Node n)
  {
    if ((n == null) || (m_nodes.contains(n))) return;

    m_nodes.add(n);

    for (Iterator i = m_listeners.iterator(); i.hasNext(); )
    {
      NodeListener l = (NodeListener) i.next();

      l.nodeAdded(n);
    }
  }
  //----------------------------------------------------------------------
  // Implements {@link NodeCollection} interface
  // _TODO_(add output of error messages from this guy)
  public void remove(Node n)
  {
    if ((n == null) || (!m_nodes.contains(n))) return;

    m_nodes.remove(n);

    for (Iterator i = m_listeners.iterator(); i.hasNext(); )
    {
      NodeListener l = (NodeListener) i.next();

      l.nodeDeleted(n);
    }
  }
  //----------------------------------------------------------------------
  //Implements {@link NodeCollection} interface
  public int size()
  {
    return m_nodes.size();
  }
  //----------------------------------------------------------------------
  /**
   * Tests whether <tt>n</tt> new instances of the specified node type
   * can be added to this collection.
   */
  public boolean canAdd(CSL_Element_NodeDescription type, int n)
  {
    // if there is no parent, then don't allow anything to be created
    if (getParent() == null)
    {
      PackageLogger.log.warning("Can't create node " + type + " because "
                                + "there is no parent node");
      return false;
    }

    Collection rules = CSL_Global_Registry.getInstance().
      getContainmentRules(getParent().getType());

    // Look at each rule in turn until we find one that would be broken
    // if the new node instances were added. If we dont find one that
    // we'd break, then assume we can go ahead with the node addition.
    for (Iterator i = rules.iterator(); i.hasNext(); )
    {
      ContainmentRule rule = (ContainmentRule) i.next();

      // skip rules that specify no upper bound
      if (rule.getUpper() == ContainmentRule.UNBOUNDED) continue;

      // only consider rules which apply to the specified type
      if (rule.getPermittedList().contains(type))
      {
        int count = 0;

        for (Iterator j = nodeIterator(); j.hasNext(); )
        {
          Node child = (Node) j.next();
          if (rule.getPermittedList().contains(child.getType())) count++;
        }

        if ((count + n) > rule.getUpper())
        {
          PackageLogger.log.warning("Can't add " + type
                                    + " node - limit reached");
          return false;
        }
      }
    }

    return true;
  }
  //----------------------------------------------------------------------
  public void addNodeListener(NodeListener l)
  {
    m_listeners.add(l);
  }
  //----------------------------------------------------------------------
  public void removeNodeListener(NodeListener l)
  {
    m_listeners.remove(l);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to locate a named {@link Node} within this collection. If
   * not foun d, returns <tt>null</tt>.
   */
  private Node locateNode(String name)
  {
    for (Iterator i = m_nodes.iterator(); i.hasNext(); )
    {
      Node n = (Node) i.next();

      if (n.name().equals(name)) return n;
    }

    return null;
  }
  //----------------------------------------------------------------------
  public UserVN resolveVNName(String vnName)
  {
    if (vnName.length() == 0)
    {
      PackageLogger.log.warning("VNName resolution failure: vnName"
                                + " is empty in node collection");
      return null;
    }

    int dot = vnName.indexOf('.');

    if (dot != -1 )
    {
      // There is a dot, so locate the local segment, and the remaining
      // part of the VNName
      String segment = vnName.substring(0, dot);
      String remainingVNName = vnName.substring(dot+1);

      Node localNode = locateNode(segment);

      if (localNode == null)
      {
        PackageLogger.log.warning("VNName resolution failure: collection"
                                  + " has no node called "
                                  + segment);
        return null;
      }
      else
      {
        return localNode.resolveVNName(remainingVNName);
      }
    }
    else
    {
      // There is no dot, so the string must the name of a local node
      Node localNode = locateNode(vnName);
      if (localNode == null)
      {
        PackageLogger.log.warning("VNName resolution failure: collection"
                                  + " has no node called "
                                  + vnName);
        return null;
      }
      else
      {
        return localNode;
      }
    }
  }
}
