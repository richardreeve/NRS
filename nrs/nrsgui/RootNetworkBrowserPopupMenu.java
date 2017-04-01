package nrs.nrsgui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;

/**
 * Customized popup menu for use in the root network browser window
 */
class RootNetworkBrowserPopupMenu extends BrowserPopupMenu
{
  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  RootNetworkBrowserPopupMenu()
  {
    super(false);
  }
  //----------------------------------------------------------------------
  /**
   * Rebuilds the menu containing the list of node-types that can be
   * created. If the list is empty - ie if no nodes can be created with
   * the current network context - then the node menu is disabled.
   */
  protected void buildNodeMenu()
  {
    m_newNodeMenu.removeAll();


    NRSComponentManager cMan = AppManager.getInstance().getComponentManager();


    for (Iterator i = cMan.getComponents().iterator(); i.hasNext(); )
    {
      NRSComponent c = (NRSComponent) i.next();

      m_newNodeMenu.add(makeComponentSubmenu(c));
    }

    m_newNodeMenu.setEnabled(m_newNodeMenu.getItemCount() > 0);
  }
  //----------------------------------------------------------------------
  private JMenuItem makeComponentSubmenu(NRSComponent c)
  {
    JMenu retVal = new JMenu(c.toString());

    Collection rootNodes = CSL_Global_Registry.getInstance().getRootNodes(c);

    if (rootNodes != null)
    {
      for (Iterator i = rootNodes.iterator(); i.hasNext(); )
      {
        CSL_Element_NodeDescription node =
          (CSL_Element_NodeDescription) i.next();

        retVal.add(makeNewNodeMenuItem(node));
      }
    }

    retVal.setEnabled(retVal.getItemCount() > 0);

    return retVal;
  }
  //-------------------------------------------------------------------------
  /**
   * Handle the selection of a menu item which represents an event to
   * construct a particular NRS node.
   *
   * @param e the {@link ActionEvent} that triggered this request
   *
   * @param type the name of the {@link CSL_Element_NodeDescription}
   * type to construct
   */
  protected void handleNodeConstructionCommand(ActionEvent e,
                                             CSL_Element_NodeDescription type)
  {
    // Delegate hard work to another routine
    handler().makeNode(type);
  }
}
