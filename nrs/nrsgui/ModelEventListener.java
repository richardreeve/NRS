package nrs.nrsgui;

/**
 * These are the events triggered by {@link NodeModel}.
 *
 * @author Darren Smith, Thomas French
 */
interface ModelEventListener
{
  /**
   * Invoked when an existing network has been closed.  Classes which
   * may be providing a view should cease to do so.
   *
   * @param source the {@link NodeModel} which raised the event
   * @param n the {@link RootNetwork} which has been removed
   */
  public void networkRemoved(NodeModel source, RootNetwork n);

  /**
   * Invoked when the NodeModel has had a network added to it.
   *
   * @param source the {@link NodeModel} which raised the event
   * @param n the {@link RootNetwork} which has been added
   */
  public void networkAdded(NodeModel source, RootNetwork n);
}
