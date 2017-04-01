package nrs.nrsgui;

import java.util.ArrayList;

/**
 * Represents the model part of the model-controller-view framework used
 * for user and data interaction in NRS.GUI.
 *
 * <p>NRS classes which provide data views should register as listeners
 * to this class in order to receive events that indicate when a new
 * network has been created, or an existing one deleted.
 *
 * @author Darren Smith, Thomas French
 */
class NodeModel
{
  private ArrayList<ModelEventListener> m_listeners;
  private RootNetwork m_rootNetwork;

  //----------------------------------------------------------------------
  /**
   * Register an object to receive model events
   */
  void addListener(ModelEventListener l)
  {
    m_listeners.add(l);
  }
  //----------------------------------------------------------------------
  /**
   * Un-register an object, so that it no longer receives model events
   */
  void removeListener(ModelEventListener l)
  {
    m_listeners.remove(l);
  }
  //----------------------------------------------------------------------
  /**
   * Provide a new network object. Any existing network object will be
   * lost (and no 'removed' events will be triggered). A "networkAdded"
   * will be triggered by this call (unless n is null).
   */
  void setNetwork(RootNetwork n)
  {
    m_rootNetwork = n;

    if (m_rootNetwork != null ) raiseEvent_networkAdded(m_rootNetwork);
  }
  //----------------------------------------------------------------------
  /**
   * Returns the {@link RootNetwork} within the model. Can return null,
   * which indicates that the data model currently does not have a
   * network.
   */
  RootNetwork getNetwork()
  {
    return m_rootNetwork;
  }
  //----------------------------------------------------------------------
  private void raiseEvent_networkAdded(RootNetwork n)
  {
    for (ModelEventListener l : m_listeners)
    {
      l.networkAdded(this, n);
    }
  }
}
