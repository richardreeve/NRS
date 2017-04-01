package nrs.nrsgui;

/**
 * The interface for receiving {@link Node} events. Classes that are
 * interested in processing {@link Node} events should implement this
 * interface and register with an appropriate event source.
 */
interface NodeListener
{
  /**
   * Invoked when a {@link Node} has been added.
   */
  void nodeAdded(Node n);

  /**
   * Invoked when a {@link Node} is in the processed of being deleted.
   */
  void nodeDeleted(Node n);

  /**
   * Invoked when a {@link Node} has had its attributes modified.
   */
  void nodeModified(Node n);

  /**
   * Invoked when a {@link Node} has its synchronized status altered.
   */
  void nodeSyncChanged(Node n);
}
