package nrs.nrsgui;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import nrs.csl.CSL_Element_NodeDescription;
import java.awt.Window;

/**
 * This class represents the lowest layer of an NRS simulation
 * network.
 *
 * <p>It is a specialisation of the {@link Network} class. The change is
 * that this class implements different rules for adding new nodes to
 * the network; this is essentially due to node of this network being of
 * <i>root</i> type.
 *
 * <p>This class also contains a list of {@link Window} instances that
 * are presenting views onto various subnetworks belonging to this
 * network. These will all be programmatically closed if/when the user
 * closes a root network.
 *
 * @author Darren Smith
 */
public class RootNetwork extends Network
{
  private Set m_views = new HashSet();

  //----------------------------------------------------------------------
  /**
   * Creates a network
   */
  public RootNetwork()
  {
    super("", "", "");
  }
  //----------------------------------------------------------------------
  /**
   * Creates a network with the specified information
   */
  public RootNetwork(String name,
                     String author,
                     String description)
  {
    super(name, author, description);
  }
  //----------------------------------------------------------------------
  /**
   * Determine whether <tt>n</tt> new instances of the supplied node
   * type can be inserted at the root level. This evalutes the root
   * containment rules specified in the CSL in order to arrive at its
   * result.
   *
   * @return Will return false if <tt>type</tt> does not describe a
   * root node, or if the maximum number of root instances for that type
   * already exist at the root level, or if <tt>n</tt> extra nodes would
   * exceed the CSL defined limits.
   */
  public boolean canAdd(CSL_Element_NodeDescription type, int n)
  {
    if (type.getRootNode() == false) return false;

    if (type.getRootMaxOccurs() == -1) return true; // ie, unbounded

    return (count(type) + n) <= type.getRootMaxOccurs();
  }
  //----------------------------------------------------------------------
  /**
   * Returns the number of instances of the given {@link
   * CSL_Element_NodeDescription} that exist in this network
   */
  private int count(CSL_Element_NodeDescription nType)
  {
    int count = 0;

    for (Iterator iter = m_nodes.iterator();
         iter.hasNext(); )
    {
      Node node = (Node) iter.next();
      if (node.getType() == nType) count++;
    }

    return count;
  }
  //----------------------------------------------------------------------
  /**
   * Register a {@link Window} that is providing a view of this network
   */
  void addWindow(Window window)
  {
    PackageLogger.log.fine("### window added");
    m_views.add(window);
  }
  //----------------------------------------------------------------------
  /**
   * De-register a {@link Window} that is providing a view of this
   * network
   */
  void removeWindow(Window window)
  {
    m_views.remove(window);
  }
  //----------------------------------------------------------------------
  /**
   * Close all views (windows) that exist for this network
   */
   void closeAllViews()
   {
     for (Iterator i = m_views.iterator(); i.hasNext(); )
     {
       Window window = (Window) i.next();
       window.hide();
     }
   }
}
