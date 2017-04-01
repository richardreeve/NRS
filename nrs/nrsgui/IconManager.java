// $Id: IconManager.java,v 1.10 2005/08/01 13:32:52 s0125563 Exp $
package nrs.nrsgui;

import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Utility class to simpified the process of getting {@link ImageIcon}s
 * from the file system.
 *
 * @author Darren Smith
 */
class IconManager
{
  private ImageIcon m_locked = null;
  private ImageIcon m_unlocked = null;
  private ImageIcon m_smallExit = null;
  private ImageIcon m_smallBlank = null;
  private ImageIcon m_smallClose = null;
  private ImageIcon m_link = null;
  private ImageIcon m_unlink = null;

  private ImageIcon m_network = null;
  private ImageIcon m_search = null;
  private ImageIcon m_viewtree = null;
  private ImageIcon m_closed = null;
  private ImageIcon m_open = null;

  private ImageIcon m_possible = null;
  private ImageIcon m_current = null;

  private static IconManager m_singleton = null;

  //----------------------------------------------------------------------
  /**
   * Return the singleton instance of this class
   */
  public static IconManager getInstance()
  {
    if (m_singleton == null) m_singleton = new IconManager();

    return m_singleton;
  }
  //----------------------------------------------------------------------
  ImageIcon locked() { return m_locked; }
  ImageIcon unLocked() { return m_unlocked; }
  ImageIcon smallClose() { return m_smallClose; }
  ImageIcon smallExit() { return m_smallExit; }
  ImageIcon smallBlank() { return m_smallBlank; }
  ImageIcon link() { return m_link; }
  ImageIcon unlink() { return m_unlink; }

  ImageIcon network() { return m_network; }
  ImageIcon search() { return m_search; }
  ImageIcon viewtree() { return m_viewtree; }
  ImageIcon closed() { return m_closed; }
  ImageIcon open() { return m_open; }

  ImageIcon possible(){ return m_possible; }
  ImageIcon current(){ return m_current; }
  //----------------------------------------------------------------------
  /**
   * Private constuctor - use factory method to create singleton instance
   */
  private IconManager()
  {
    m_locked = createImageIcon("icons/nuvola/22x22/encrypted.png");
    m_unlocked = createImageIcon("icons/nuvola/22x22/decrypted.png");
    m_smallExit = createImageIcon("icons/nuvola/16x16/exit.png");
    m_smallBlank = createImageIcon("icons/nuvola/16x16/blank.png");
    m_smallClose = createImageIcon("icons/nuvola/16x16/no.png");
    m_link = createImageIcon("icons/link.png");
    m_unlink = createImageIcon("icons/unlink.png");

    m_network = createImageIcon("icons/nuvola/16x16/network.png");
    m_search = createImageIcon("icons/nuvola/22x22/find.png");
    m_closed = createImageIcon("icons/nuvola/16x16/viewmag+.png");
    m_open = createImageIcon("icons/nuvola/16x16/viewmag-.png");

    m_possible = createImageIcon("icons/nuvola/16x16/ledred.png");
    m_current = createImageIcon("icons/nuvola/16x16/ledgreen.png");
  }
  //----------------------------------------------------------------------
  /**
   * Provide a service for retrieving {@link ImageIcon} instances
   */
  private ImageIcon createImageIcon(String path)
  {
    URL imgURL = ClassLoader.getSystemResource(path);
    if (imgURL != null)
    {
        return new ImageIcon(imgURL);
    }
    else
    {
      PackageLogger.log.warning("Failed to load image icon from file \""
                                + path + "\"");
      return null;
    }
  }

}
