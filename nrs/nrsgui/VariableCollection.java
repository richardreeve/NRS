package nrs.nrsgui;

import java.util.Iterator;

/*
 * Collection of the internal variables belonging to a {@link Node} instance
 */
interface VariableCollection
{
  /**
   * Returns the node which owns these variables
   */
  Node getParent();

  /**
   * Returns an {@link Iterator} over the variables. Each non
   * <code>null</code> object reference returned by the iterator is of
   * type {@link NodeVariable}.
   *
   * @deprecated use <tt>iterator</tt> instead
   */
  Iterator variableIterator();

  /**
   * Returns an {@link Iterator} over the variables
   */
  Iterator<NodeVariable> iterator();

  /**
   * Returns the number of variables
   */
  int size();

  /**
   * Add a variable to this collection. Duplicate entries are detected
   * and silently ignored.
   */
  void add(NodeVariable nv);

  /**
   * Resolve a VNName (a fully qualified node name). Attempts to find a
   * {@link NodeVariable} object contained with this collection.
   *
   * @return the located variable, or <tt>null</tt> if no match.
   */
  public NodeVariable resolveVNName(String vnName);
}
