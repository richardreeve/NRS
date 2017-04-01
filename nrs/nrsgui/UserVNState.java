package nrs.nrsgui;

import java.util.Observable;

/**
 * Represents the synchronized state of a {@link Node} instance. The
 * synchronized state indicates whether there are any local changes that
 * have not been propagated to the remote NRS component.
 */
class NodeState extends Observable
{

  /** State of this node. Will be one of the state constant contained
   * within thiis class. */
  private String m_state;

  private static final String S_LCONSTRUCTED = "Constructed locally";
  private static final String S_RCONSTRUCTED = "Constructed remotely";
  private static final String S_LMODIFIED = "Modified locally";
  private static final String S_RMODIFIED = "Modified remotely";

  //----------------------------------------------------------------------
  /**
   * Return the synchronised state. Returns <tt>true</tt> if this
   * instance is up-to-date with the remote NRS object, <tt>false</tt>
   * otherwise.
   */
  boolean getSyncState();
}
