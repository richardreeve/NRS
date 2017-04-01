package nrs.nrsgui;

/**
 * Implemented by {@link NetworkBrowser} and used by {@link
 * NetworkCanvas} so that the former can pass to the latter the current
 * network objects it should be displaying.
 */
interface NetworkCanvasDataContext
{
  /**
   * Return the {@link BrowserContext} instance which described the
   * nodes and variables accessible by a network view.
   */
  BrowserContext getContext();

  /**
   * Return the {@link Node} whose internal variables will be visible on
   * the canvas
   *
   * @deprecated - use getContext
   */
  Node getNode();

  /**
   * Return the {@link NodeCollection} whose sub-nodes will be visible
   * on the canvas, and to which new nodes will be added.
   *
   * @deprecated - use getContext
   */
  NodeCollection getCollection();
}

