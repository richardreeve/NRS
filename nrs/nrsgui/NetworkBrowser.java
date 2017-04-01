package nrs.nrsgui;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.JOptionPane;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;
import nrs.csl.ContainmentRule;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A window bare for displaying and manipulating network elements in a
 * graph-like view. This class is currently a very thin wrapped around
 * {@link BaseNetworkBrowser}; although, eventually this class might
 * evolve as extra GUI functionality is better suited here rather than
 * in the base class.
 *
 * @author Darren Smith
 */
public class NetworkBrowser extends BaseNetworkBrowser
{
  //---------------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param title title of the window
   *
   * @param context the {@link BrowserContext} to display
   */
  NetworkBrowser(String title, BrowserContext context,
                 BrowserManager browserMan)
  {
    super(title, context, new NetworkBrowserPopupMenu(), browserMan);

    addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          closeView();
        }
      });
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void closeView()
  {
    setVisible(false);
    m_browserMan.removeBrowser(this);
  }
}
