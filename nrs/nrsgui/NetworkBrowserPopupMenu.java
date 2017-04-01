package nrs.nrsgui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;
import nrs.csl.ContainmentRule;

/**
 * A popup menu that appears in standard network browser windows, that
 * is, those which are not displaying the root level of the network.
 *
 * @author Darren Smith
 */
class NetworkBrowserPopupMenu extends BrowserPopupMenu
{
  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  NetworkBrowserPopupMenu()
  {
    super(true);
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

    if (getBrowserContext().getNode() != null)
    {
      Collection rules = CSL_Global_Registry.getInstance().
        getContainmentRules(getBrowserContext().getNode().getType());

      for (Iterator i = rules.iterator(); i.hasNext(); )
      {
        ContainmentRule rule = (ContainmentRule) i.next();

        List nodesPermitted = rule.getPermittedList();

        if (rule.getGroupName() == null)
        {
          CSL_Element_NodeDescription node
            = (CSL_Element_NodeDescription) nodesPermitted.get(0);

          m_newNodeMenu.add(makeNewNodeMenuItem(node));
        }
        else
        {
          JMenu submenu = new JMenu(rule.getGroupDisplayName());
          for (Iterator j = nodesPermitted.iterator(); j.hasNext(); )
          {
            CSL_Element_NodeDescription node
              = (CSL_Element_NodeDescription) j.next();

            submenu.add(makeNewNodeMenuItem(node));
          }
          m_newNodeMenu.add(submenu);
        }
      }
    }

    m_newNodeMenu.setEnabled(m_newNodeMenu.getItemCount() > 0);
  }
  //-------------------------------------------------------------------------
  /**
   * Handle the selection of a menu item which represents an event to
   * construct a particular NRS node.
   *
   * @param e the {@link ActionEvent} that triggered this request
   *
   * @param type the {@link CSL_Element_NodeDescription} type to
   * construct
   */
  protected void handleNodeConstructionCommand(ActionEvent e,
                                               CSL_Element_NodeDescription type)
  {
    /** _TODO_(Check node is allowed to be constructed) */

    // Delegate hard work to another routine
    handler().makeNode(type);
  }
}
