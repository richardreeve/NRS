package nrs.nrsgui;

import java.util.HashMap;

/**
 * Manages the collections of network browsers the user has opened
 */
class BrowserManager
{
  /**
   * The keys are of type {@link Node}, and the values are of type
   * {@link NetworkBrowser}.
   */
  private HashMap m_windows;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  BrowserManager()
  {
    m_windows = new HashMap();
  }
  //----------------------------------------------------------------------
  /**
   * Returns whether a {@link NetworkBrowser} exists for the specified
   * {@link Node}
   */
  boolean browserExists(Node node)
  {
    return m_windows.containsKey(node);
  }
  //----------------------------------------------------------------------
  /**
   * Open a {@link NetworkBrowser} to browse the specified {@link
   * Node}. If a browser for that {@link Node} already exists, then it
   * is returned. Otherwise a new {@link NetworkBrowser} is created and
   * returned.
   */
  NetworkBrowser obtainBrowser(Node node)
  {
    if (browserExists(node))
    {
      PackageLogger.log.fine("### Reusing existing window");
      return (NetworkBrowser) m_windows.get(node);
    }
    else
    {
      NetworkBrowser browser = new NetworkBrowser(node.VNName(),
                                                  new BrowserContext(node),
                                                  this);
      m_windows.put(node, browser);
      return browser;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Remove a browser from this manager
   */
  void removeBrowser(NetworkBrowser browser)
  {
    m_windows.remove(browser.getNode());
  }
}
