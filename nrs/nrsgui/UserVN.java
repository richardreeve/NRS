package nrs.nrsgui;

import nrs.core.base.Message;

/**
 * Within NRS components nodes and are variables are, to a large part,
 * the same. The key difference is that nodes contain variables (and
 * other nodes), whereas variables cannot contain anything.
 *
 */
interface UserVN
{
  /**
   * Returns the {@link DisplayDelegate} object used to paint this
   * variable / node.
   */
  DisplayDelegate getPainter();

  /**
   * Returns the {@link Node} which is the parent of self. For
   * variables, the parent is defined as the {@link Node} in which it is
   * contained. For nodes, the parent is defined as the {@link Node}
   * which contains self, or <tt>null</tt> if there is no parent (which
   * is the case for root nodes).
   */
  Node getParent();

  /**
   * Returns the fully qualified node name (known in NRS as the VNName)
   */
  String VNName();

  /**
   * Returns the local node name, which is the final component of the
   * VNName.
   */
  String name();

  /**
   * Returns the local name of this node.
   */
  String toString();

  /**
   * Return the {@link RemoteVNInfo} which stores information known about
   * the remote NRS object corresponding to this GUI model.
   */
  RemoteVNInfo remote();

  /**
   * Receive and process a ReplyVNID message directed at this node
   */
  void handleReplyVNID(Message m);
}
