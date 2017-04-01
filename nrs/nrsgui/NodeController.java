package nrs.nrsgui;

import java.io.File;

/**
 * Represents the controller part of the model-controller-view framework
 * used for user and data interaction in NRS.GUI.
 *
 * @author Darren Smith, Thomas French
 */

class NodeController
{
  private NodeModel m_model = new NodeModel();

  //----------------------------------------------------------------------
  /**
   * Return the data model
   */
  NodeModel getModel()
  {
    return m_model;
  }
  //----------------------------------------------------------------------
  /**
   * Load a network from an existing file
   *
   * @throws an {@link IllegalStateException} if the underlying data
   * model already contains a network.
   */
  void loadNetwork(File file) throws IllegalStateException
  {
    if (m_model.getNetwork() == null)
    {
      throw new IllegalStateException("Can't load a network because"
                                      + " a network already exists");
    }

    // Create a new empty network
    RootNetwork network = new RootNetwork();

    // Load in the specified 'file', which contains a network, into the
    // new network created above.
    AppManager.getInstance().getFileStorageManager().loadNetwork(file,
                                                                 network,
                                                                 true);

    PackageLogger.log.fine("Network '" + network + "'loaded");
    network.resetChanged();

    m_model.setNetwork(network);
  }
}
