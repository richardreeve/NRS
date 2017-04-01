package nrs.nrsgui;

import java.util.Iterator;
import nrs.core.base.ComponentInfo;
import nrs.csl.CSL_Element_NodeDescription;

/*
 * Basic abilities of a node instance
 *
 *
 * _TODO_(Does this need listeners, now that NodeCollection has them?)
 *
 * @author Darren Smith
 */
interface Node extends UserVN
{
  /**
   * Returns type information
   */
  CSL_Element_NodeDescription getType();

  /**
   * Returns an {@link Iterator} over the attributes associated with
   * this node. Each item accessed by the iterator is of type {@link
   * NodeAttribute}.
   */
  Iterator attributeIterator();

  /**
   * Returns the {@link DisplayDelegate} object used to paint this node
   */
  DisplayDelegate getPainter();

  /**
   * Return the collecion of child nodes contained by a node
   * instance. Should not return <code>null</code>
   */
  NodeCollection getChildNodes();

  /**
   * Get the collection of variables internal to this Node. May return
   * <code>null</code> if no variables are possessed.
   */
  VariableCollection getVariables();

  /**
   * Returns the {@link NodeCollection} this <tt>Node</tt> is part
   * of. Every <tt>Node</tt> should exist within such a structure.
   */
  NodeCollection getContainer();

  /**
   * Returns the {@link Node} which is the parent of self. The parent is
   * defined as the {@link Node} which contains this node.
   *
   * @return the parent {@link Node} or <tt>null</tt> if there is no
   * parent (which is the case for root nodes)
   */
  Node getParent();

  /**
   * Returns the local name of this node.
   */
  String toString();

  /**
   * Get {@link ComponentInfo} about the NRS component which this node
   * traces to.
   */
  ComponentInfo getComponent();

  /**
   * Delete this node and its child-nodes
   */
  void delete();

  /**
   * Indicate that this node has just had one or more of its attributes'
   * values modified.
   */
  void attrsModified();

  /**
   * Add a {@link NodeListener} to receive events related to this {@link
   * Node}
   */
  void addNodeListener(NodeListener l);

  /**
   * Remove a {@link NodeListener} from this {@link Node}
   */
  void removeNodeListener(NodeListener l);

  /**
   * Return the synchronized state of this {@link Node}. Returns
   * <tt>true</tt> if in-sync with the remote node, <tt>false</tt>
   * otherwise.
   */
  boolean inSync();

  /**
   * Return whether we know the a remote node corresponding to this
   * local node has been constructed.
   */
  boolean remoteCreated();

  /**
   * Find the {@link RootNetwork} which is the most fundamental container
   * for this <tt>node</tt>. This involves recursing up the heirarchy of
   * parent nodes. Can return <tt>null</tt>, which is a sign of an error
   * condition.
   */
  RootNetwork findRoot();

  /**
   * Resolve a VNName (a fully qualified node name). Attempts to find a
   * {@link UserVN} object contained with this node (including
   * recursively looking into all sub-collections, and variables of all
   * contained nodes, and all variables belonging to self).
   *
   * @return the {@link UserVN} located, or <tt>null</tt> if no match.
   */
  public UserVN resolveVNName(String vnName);
}
