package nrs.nrsgui;

import java.util.Iterator;
import nrs.csl.CSL_Element_NodeDescription;

/*
 * Basic abilities of a node instance
 */
public interface NodeCollection
{
  /**
   * Returns the parent node of this collection.
   */
  Node getParent();

  /**
   * Returns an iterator to sub-nodes contained within this node.  Each
   * item accessed by the iterator is of type {@link Node}.
   */
  Iterator nodeIterator();

  /**
   * Remove {@link Node} <code>node</code> from this collection. Missing
   * and null instances are checked for and ignored.
   */
  void remove(Node n);

  /**
   * Add a {@link Node} to this collection. Duplicate and
   * <code>null</code> instances are checked for and ignored.
   */
  void add(Node n);

  /**
   * Returns the number of sub-nodes
   */
  int size();

  /**
   * Determines whether <tt>n</tt> number of instances of the specified
   * node type can be created within the current network. Returns
   * <tt>true</tt> if <tt>n</tt> instances can be created, or
   * <tt>false</tt> otherwise. The determination is made by evaluating
   * all of the containment rules for the current network.
   */
  boolean canAdd(CSL_Element_NodeDescription type, int n);

  /**
   * Add a {@link NodeListener} to receive events that occur on the
   * {@link Node} instances belonging to this collection
   */
  void addNodeListener(NodeListener l);

  /**
   * Remove a {@link NodeListener}
   */
  public void removeNodeListener(NodeListener l);

  /**
   * Resolve a VNName (a fully qualified node name). Attempts to find a
   * {@link UserVN} object contained with this collection (including
   * recursively looking into all sub-collections, and variables of all
   * contained nodes).
   *
   * @return the {@link UserVN} located, or <tt>null</tt> if no match.
   */
  public UserVN resolveVNName(String vnName);
}
