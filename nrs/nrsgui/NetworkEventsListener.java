package nrs.nrsgui;

import java.awt.event.MouseEvent;
import java.util.List;
import nrs.csl.CSL_Element_NodeDescription;


/**
 * The interface for communicating user triggered events and requests
 * ocurring on GUI elements within a network view. The idea of this
 * class is to represent the source of events on network elements in an
 * implementation independent way. I.e., the events might be generated
 * by either a {@link NetworkBrowserPopupMenu} instance, or perhaps from
 * <code>JList</code> kind of interface.
 *
 * @author Darren Smith
 */
public interface NetworkEventsListener
{
  /**
   * Indicates that the user selected an {@link Object}. If null is
   * passed, indicated that a mouse clicked occured resulting in
   * no-selection. Note, rather than an {@link Object} type, the
   * argument used to be of type {@link Node}. However I now need to
   * indicate that {@link NodeVariable} objects can be selected. So
   * there are several ways of doing this: perform instanceof operations
   * before this call, in which case we would have several overloaded
   * objectSelected(...) methods; or pass a base class. Currently the
   * latter is being done for this method (and the former is done for
   * other methods below). Perhaps, as a TODO, I could introduce a new
   * base class common to Node and NodeVariable, and other selectable
   * objects, that is higher that Object. Or maybe I could just add
   * several methods, such as nodeSelected(), variableSelected() etc.
   */
  void objectSelected(Object n);

  /**
   * Handle request to create a {@link Node} of type {@link
   * CSL_Element_NodeDescription}
   */
  void makeNode(CSL_Element_NodeDescription nType);

  /**
   * Handle request to delete {@link Node} <code>n</code>
   */
  void deleteNode(Node n);

  /**
   * Handle request to open a view onto a {@link Node} <code>n</code>
   */
  void openNode(Node n);

  /**
   * Handle request to edit the defaults of {@link Node} <code>n</code>
   */
  void editNode(Node n);

  /**
   * Indicate that a context aware popup menu for the {@link Node}
   * should be activated. The parameter <code>n</code> refers to the
   * node which the popup menu should be customized for. <code>n</code>
   * can be <code>null</code>, in which case the popup menu can't
   * present any context dependent options.
   *
   * @param n a {@link Node} refering to the node which the
   * menu should be customized for.
   *
   * @param e the {@link MouseEvent} which triggered the popup.
   *
   **/
  void activatePopup(Node n, MouseEvent e);

  /**
   * Indicate that a context aware popup menu for the {@link
   * NodeVariable} should be activated. The parameter <code>nv</code>
   * refers to the node variable which the popup menu should be
   * customized for. <code>nv</code> can be <code>null</code>, in which
   * case the popup menu can't present any context dependent options.
   *
   * @param nv a {@link NodeVariable} refering to the node variable
   * which the menu should be customized for.
   *
   * @param e the {@link MouseEvent} which triggered the popup.
   *
   **/
  void activatePopup(NodeVariable nv, MouseEvent e);

  /**
   * Indicate that an unspecified popup menu should be activated.
   *
   * @param e the {@link MouseEvent} which triggered the popup.
   *
   **/
  void activatePopup(MouseEvent e);

  /**
   * Object <code>n</code> should be set to the highest display layer
   * within its encapsulating network.
   *
   * @param n {@link DisplayDelegate} to set to highest layer. Might be
   * <code>null</code>, in which case this request should be ignored.
   */
  void setHighest(DisplayDelegate n);

  /**
   * Object <code>n</code> should be set to the lowest display layer
   * within its encapsulating network.
   *
   * @param n {@link DisplayDelegate} to set to lowest layer. Might be
   * <code>null</code>, in which case this request should be ignored.
   */
  void setLowest(DisplayDelegate n);

  /**
   * Represents a user request to close a network view
   */
  void closeView();

  /**
   * Handle the users request to rebuild the specified node on a remote
   * NRS component.
   */
  void remoteRebuild(Node n);

  /**
   * Handle the users request to rebuild the links of a specified node
   * on a remote NRS component.
   */
  void remoteRebuildLinks(Node n);

  /**
   * Handle the users request to delete the specified node on a remote
   * NRS component.
   */
  void remoteDelete(Node n);

  /**
   * Handle the users request to verify the specified node on a remote
   * NRS component.
   */
  void remoteVerify(Node n);

  /**
   * Export a network selection. The parameter is a collection of
   * nodes. Typically it will either be a single node, or, a selection
   * of nodes within a particular network.
   */
  void exportNodes(List nlist);

  /**
   * Export an entire network to disk
   */
  void exportNetwork(Network network);

  /**
   * Load an entire network from
   */
  void loadNetwork();
}
