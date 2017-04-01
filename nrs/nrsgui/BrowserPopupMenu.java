package nrs.nrsgui;

import javax.swing.MenuElement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import nrs.csl.CSL_Element_Interface;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;

/**
 * Base class for the popup window that appears on windows which display
 * a graphical view of a nework of NRS node.
 *
 * @author Darren Smith
 */
abstract class BrowserPopupMenu extends JPopupMenu implements Observer
{
  /** Delegate to which some menu events are passed */
  private NetworkEventsListener m_eventHandler;

  // Submenus
  protected final JMenu m_newNodeMenu;
  protected final JMenu m_iconMenu;
  //  protected final JMenu m_fileMenu;
  protected final JMenuItem m_linkMenu;
  //protected final JMenuItem m_setSourceMenu;
  //protected final JMenuItem m_setTargetMenu;
  protected final JMenu m_generateMenu;

  // Menu items
  protected final JMenuItem m_delete;
  protected final JMenuItem m_open;
  protected final JMenuItem m_edit;
  //
  // Menu commands labels
  //
  protected final String DELETE = "Delete";
  protected final String SEND_TO_BACK = "Send To Back";
  protected final String BRING_TO_FRONT = "Bring To Front";
  protected final String PML_EXPORT = "Export PML";
  protected final String OPEN = "Open";
  protected final String EDIT = "Edit";
  protected final String CLOSE = "Close";
  protected final String MAKE_LINK = "Make Link";
  protected final String REBUILD = "Rebuild Nodes";
  protected final String VERIFY = "Verify";
  protected final String DO_LINKS = "Rebuild Links";
//   protected final String EXPORT_NETWORK = "Save Network";
//   protected final String LOAD_NETWORK = "Load Network";

  // Note, have to make a 'new' string here to prevent Java from giving me
  // the String object above which also has the text "Delete"
  protected final String REMOTE_DELETE = new String("Delete");

  /** Used to store the context of a node specific menu context. Should
   * be assigned to (or nulled) before the popup menu is triggered, and
   * read from if a context specific menu item is triggered. Note, only
   * one of the context variables should ever be non-null at any
   * instant.*/
  private Node m_menuEventContext;

  /** This has a similar use to m_menuEventContext, but is for
   * representing a context of type {@link NodeVariable}. Note, only one
   * of the context variables should ever be non-null at any instant. */
  private NodeVariable m_menuEventContext_NV;

  /** Utility context variable. It is assigned the {@link
   * DisplayDelegate} of whatever context variable exists in either
   * m_menuEventContext or m_menuEventContext_NV. */
  private DisplayDelegate m_menuEventContext_DD;

  NetworkCanvasDataContext m_context;

  NodeLinkMakerWindow m_linkWin = null;
  MainFrame m_mainFrame = null;

  /** Whether GUI is being used in debug mode. */
  boolean isDebugMode = false;

  // Inner class. Objects of this type are associated with those menu
  // items which represent an individual node type, typically used in
  // menus for selecting a node type to create. The class is responsible
  // for evoking the node construction code and providing it with the
  // actual type of the node to create; this information is not
  // avialable in the ActionEvent object because its command-string used
  // the displayName attribute of a node.
  private class NodeMenuActionListener implements ActionListener
  {
    private CSL_Element_NodeDescription m_type;

    NodeMenuActionListener(CSL_Element_NodeDescription type)
    {
      m_type = type;
    }
    public void actionPerformed(ActionEvent e)
    {
      handleNodeConstructionCommand(e, m_type);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param includeClose indicate whether the "Close" menu item should
   * be added;
   */
  BrowserPopupMenu(boolean includeClose)
  {
    IconManager iconMan = AppManager.getInstance().getIconManager();

    m_newNodeMenu = new JMenu("New node");
    m_iconMenu = new JMenu("Objects");
    //    m_fileMenu = new JMenu("File");
    m_linkMenu = new JMenuItem("Links");
    //m_setSourceMenu = new JMenuItem("As Source");
    //m_setTargetMenu = new JMenuItem("As Target");
    m_generateMenu = new JMenu("Remote");

    //    m_fileMenu.setIcon(iconMan.smallBlank());
    m_newNodeMenu.setIcon(iconMan.smallBlank());
    m_iconMenu.setIcon(iconMan.smallBlank());
    m_linkMenu.setIcon(iconMan.smallBlank());
    m_generateMenu.setIcon(iconMan.smallBlank());

    m_delete = makeMenuItem(DELETE, iconMan.smallBlank());
    m_open = makeMenuItem(OPEN, iconMan.smallBlank());
    m_edit = makeMenuItem(EDIT, iconMan.smallBlank());

    isDebugMode = AppManager.getInstance().isDebugMode();

    if ( isDebugMode ){
      m_iconMenu.add(makeMenuItem(BRING_TO_FRONT, null));
      m_iconMenu.add(makeMenuItem(SEND_TO_BACK, null));
      m_generateMenu.add(makeMenuItem(REBUILD, null));
      m_generateMenu.add(makeMenuItem(DO_LINKS, null));
      m_generateMenu.add(makeMenuItem(VERIFY, null));
      m_generateMenu.add(makeMenuItem(REMOTE_DELETE, null));
    }

    //    m_fileMenu.add(makeMenuItem(LOAD_NETWORK, null));
    //    m_fileMenu.add(makeMenuItem(EXPORT_NETWORK, null));
    //    m_fileMenu.addSeparator();
    //    m_fileMenu.add(makeMenuItem(PML_EXPORT, null));

    //m_linkMenu.add(makeMenuItem(MAKE_LINK, null));
    //m_linkMenu.addSeparator();
    //m_linkMenu.add(m_setSourceMenu);
    //m_linkMenu.add(m_setTargetMenu);
        
   //  m_setSourceMenu.addActionListener(new ActionListener()
//       {
//         public void actionPerformed(ActionEvent e)
//         { handleSetSource(e); }
//       });
    
//     m_setTargetMenu.addActionListener(new ActionListener()
//       {
//         public void actionPerformed(ActionEvent e)
//         { handleSetTarget(e); }
//       });
    m_linkMenu.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
        { handleSetLinkVariable(e); }
      });


    //    add(m_fileMenu);
    if ( isDebugMode ){
      add(m_generateMenu);
    }
    add(m_newNodeMenu);
    if ( isDebugMode ){
      add(m_iconMenu);
    }
    add(m_linkMenu);
    add(m_open);
    add(m_edit);
    add(m_delete);
    if (includeClose) add(makeMenuItem(CLOSE, iconMan.smallClose()));

    // Register as an observer on the global types registry
    CSL_Global_Registry.getInstance().addObserver(this);

    //get link window from AppManager
    m_linkWin = AppManager.getInstance().m_linkWin; 
    m_mainFrame = AppManager.getInstance().m_mainFrame;
  }
  //----------------------------------------------------------------------
  /**
   * Rebuilds the menu containing the list of node-types that can be
   * created. If the list is empty - ie if no nodes can be created with
   * the current network context - then the node menu is disabled.
   */
  abstract protected void buildNodeMenu();
  //----------------------------------------------------------------------
  /**
   * Handle the selection of a menu item which represents an event to
   * construct a particular NRS node.
   *
   * @param e the {@link ActionEvent} that triggered this request
   *
   * @param type the name of the {@link CSL_Element_NodeDescription}
   * type to construct
   */
  abstract protected void handleNodeConstructionCommand(ActionEvent e,
                                                        CSL_Element_NodeDescription type);

  //----------------------------------------------------------------------
  void resetMenu()
  {
    buildNodeMenu();
  }
  //----------------------------------------------------------------------
  /**
   * Build the link sub menus based on the current context
   *
   * @deprecated
   */
  private void buildLinkSubMenus()
  {
    //m_setSourceMenu.removeAll();
    //m_setTargetMenu.removeAll();

    // priority given to a node variable context - if we have one, just
    // should its interfaces, otherwise if we have a node context,
    // should all interfaces its has.
    if (m_menuEventContext_NV != null)
    {
      buildLinkMenuItemsForVariable(m_menuEventContext_NV);
    }
    else if (m_menuEventContext != null)
    {
      for (Iterator i = m_menuEventContext.getVariables().variableIterator();
           i.hasNext(); )
        buildLinkMenuItemsForVariable((NodeVariable) i.next());
    }

    //m_setSourceMenu.setEnabled((m_setSourceMenu.getMenuComponentCount() > 0));
    //m_setTargetMenu.setEnabled((m_setTargetMenu.getMenuComponentCount() > 0));
  }
  //----------------------------------------------------------------------
  /**
   * For the specified node variable, build the menu items it should
   * have in the set-source and set-target submenus. This is a utility
   * method used by {@link #buildLinkSubMenus}
   *
   * @deprecated
   */
  private void buildLinkMenuItemsForVariable(NodeVariable nv)
  {      for (Iterator j = nv.getType().getInterfaceIterator(); j.hasNext(); )
      {
        CSL_Element_Interface intf = (CSL_Element_Interface) j.next();

     //    if (intf.canIn())
//           m_setTargetMenu.add(makeMI_LinkTarget(nv.getType().toString()));

//         if (intf.canOut())
//           m_setSourceMenu.add(makeMI_LinkSource(nv.getType().toString()));
      }
  }
  //-------------------------------------------------------------------------
  /**
   * Construct and return a JMenuItem object to represent the selection
   * of a particular node type. Such menu items occur under the
   * create-new-node sub menu, and are handled by a specific method,
   * {@link #handleNodeConstructionCommand}.
   */
  protected JMenuItem makeNewNodeMenuItem(CSL_Element_NodeDescription type)
  {
    JMenuItem newItem = new JMenuItem(type.toString());

    newItem.addActionListener(new NodeMenuActionListener(type));

    return newItem;
  }
  //-------------------------------------------------------------------------
  /**
   * Construct and return a JMenuItem object to represent the selection
   * of a normal menu item (eg, items such as 'cancel' etc), which use
   * the function {@link #handleMenuCommand} for event handling.
   */
  private JMenuItem makeMenuItem(String menuLabel, ImageIcon ii)
  {
    JMenuItem newItem;
    if (ii == null)
    {
      newItem = new JMenuItem(menuLabel);
    }
    else
    {
      newItem = new JMenuItem(menuLabel, ii);
    }

    newItem.addActionListener(
                              new ActionListener()
                              {
                                public void actionPerformed(ActionEvent e)
                                { handleMenuCommand(e); }
                              });

    return newItem;
  }
  //-------------------------------------------------------------------------
 //  /**
//    * Construct and return a JMenuItem object to represent the selection
//    * of a {@link JMenuItem} which represents a node variable which can
//    * be selected as the source for a potential link.
//    */
//   private JMenuItem makeMI_LinkSource(String menuLabel)
//   {
//     JMenuItem newItem = new JMenuItem(menuLabel);

//     newItem.addActionListener(
//                               new ActionListener()
//                               {
//                                 public void actionPerformed(ActionEvent e)
//                                 { handleSetSource(e); }
//                               });

//     return newItem;
//   }
  //-------------------------------------------------------------------------
  // /**
//    * Construct and return a JMenuItem object to represent the selection
//    * of a {@link JMenuItem} which represents a node variable which can
//    * be selected as the target for a potential link.
//    */
//   private JMenuItem makeMI_LinkTarget(String menuLabel)
//   {
//     JMenuItem newItem = new JMenuItem(menuLabel);

//     newItem.addActionListener(
//                               new ActionListener()
//                               {
//                                 public void actionPerformed(ActionEvent e)
//                                 { handleSetTarget(e); }
//                               });

//     return newItem;
//   }
  //----------------------------------------------------------------------
  
  //  /**
  //    * Handle the selection of a menu item which represents a variable to
  //    * choose as the potential source for a link.
  //    */
  //   private void handleSetSource(ActionEvent e)
  //   {
  //     // popup link window with JTree looking for links with current 
  //     // selection as source
  //     if (m_menuEventContext_NV != null)
  //       {
  //         System.out.println("As Source: " + m_menuEventContext_NV.VNName());
  //         if ( !m_linkWin.isVisible() ){
  //           m_linkWin.setVisible(true);
  //           m_mainFrame.populateViewMenu();
  //         }
  //       }
  //   }
  //   //----------------------------------------------------------------------
  //   /**
  //    * Handle the selection of a menu item which represents a variable to
  //    * choose as the potential target for a link.
  //    */
  //   private void handleSetTarget(ActionEvent e)
  //   {
  //     // popup link window with JTree looking for links with current 
  //     // selection as target
  //     if (m_menuEventContext_NV != null)
  //       {
  //         System.out.println("As Target: " + m_menuEventContext_NV.VNName());
  //         if ( !m_linkWin.isVisible() ){
  //           m_linkWin.setVisible(true);
  //           m_mainFrame.populateViewMenu();
  //         }
  //       }
  //   }
  
  /**
   * Handle the selection of a menu item which represents a variable to
   * choose as the potential target for a link.
   */
  private void handleSetLinkVariable(ActionEvent e)
  {
    // popup link window with JTree looking for links with current 
    // selection as current variable
    if (m_menuEventContext_NV != null)
      {
        if ( !m_linkWin.isVisible() ){
          if (AppManager.getInstance().accessLinkManager() != null)
            {
              AppManager.getInstance().accessLinkManager()
                .setSelected(m_menuEventContext_NV);
            }
          m_linkWin.setVisible(true);
          m_mainFrame.populateViewMenu();
        }
      }
  }
  //-------------------------------------------------------------------------
  /**
   * Handle non-node construction menu commands
   */
  private void handleMenuCommand(ActionEvent e)
  {
    if (e.getActionCommand() == DELETE)
    {
      m_eventHandler.deleteNode(m_menuEventContext);
      return;
    }
    if (e.getActionCommand() == OPEN)
    {
      m_eventHandler.openNode(m_menuEventContext);
      return;
    }
    if (e.getActionCommand() == EDIT)
    {
      m_eventHandler.editNode(m_menuEventContext);
      return;
    }

    if ( isDebugMode ){
      if (e.getActionCommand() == SEND_TO_BACK)
      {
        m_eventHandler.setLowest(m_menuEventContext_DD);
        return;
      }
      if (e.getActionCommand() == BRING_TO_FRONT)
      {
        m_eventHandler.setHighest(m_menuEventContext_DD);
        return;
      }
    }
 //    if (e.getActionCommand() == PML_EXPORT)
//     {
//       //      AppManager.getInstance().exportPML();
//       return;
//     }
    if (e.getActionCommand() == CLOSE)
    {
      m_eventHandler.closeView();
      return;
    }

    if ( isDebugMode ){
      if ((e.getActionCommand() == REBUILD) && (m_menuEventContext != null))
      {
        m_eventHandler.remoteRebuild(m_menuEventContext);
        return;
      }
      if ((e.getActionCommand() == DO_LINKS) && (m_menuEventContext != null))
      {
        m_eventHandler.remoteRebuildLinks(m_menuEventContext);
        return;
      }
      if ((e.getActionCommand() == REMOTE_DELETE)
          && (m_menuEventContext != null))
      {
        m_eventHandler.remoteDelete(m_menuEventContext);
        return;
      }
      if ((e.getActionCommand() == VERIFY)
          && (m_menuEventContext != null))
      {
       m_eventHandler.remoteVerify(m_menuEventContext);
       return;
      }
    }
   //  if (e.getActionCommand() == LOAD_NETWORK)
//     {
//       m_eventHandler.loadNetwork();
//       return;
//     }

   //  if (e.getActionCommand() == EXPORT_NETWORK)
//     {
//       m_eventHandler.exportNetwork(m_context.getContext().network());
//       return;
//     }

      // removed by T. French
//     if ( e.getActionCommand() == MAKE_LINK )
//       {
//         Node n = m_menuEventContext;
//         NodeVariable nv = m_menuEventContext_NV;

//         // clicked on a variable
//         if ( nv != null ){
//           PackageLogger.log.fine("Variable: " + nv.name());
          
//           NodeLinkMakerWindow linkWin = AppManager.getInstance().
//             getLinkWindow();

//           //setup window
          

//           // show link window
//           linkWin.setVisible(true);
//         }
//         else if ( n != null )
//           PackageLogger.log.fine("Node: " + n.name());
//         return;
//       }
  }

  //----------------------------------------------------------------------
  /**
   * Customize the popup menu so as to present options relating to the
   * specified {@link NodeVariable}. The {@link NodeVariable}
   * <code>nv</code> will be returned in the various {@link
   * NetworkEventsListener} callbacks.
   */
  void customise(NodeVariable nv)
  {
    setFocusContext(null, nv);

    if (!isEnabled())
    {
      disableAll();
      return;
    }

    m_delete.setEnabled(false);
    m_open.setEnabled(false);
    m_edit.setEnabled(false);
    if ( isDebugMode ){
      m_iconMenu.setEnabled(true);
      m_generateMenu.setEnabled(false);
    }
    m_newNodeMenu.setEnabled(false);

    m_linkMenu.setEnabled(true);

    //m_setSourceMenu.setEnabled(true);
    //m_setTargetMenu.setEnabled(true);
    //buildLinkSubMenus();  // needs calling because menus enabled
  }
  //----------------------------------------------------------------------
  /**
   * Disable all items
   */
  void disableAll()
  {
    MenuElement[] elements = getSubElements();

    for (int i = 0; i < elements.length; i++)
    {
      elements[i].getComponent().setEnabled(false) ;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Customize the popup menu so as to present options relating no selection.
   */
  void customise()
  {
    setFocusContext(null, null);

    if (!isEnabled())
    {
      disableAll();
      return;
    }

    m_delete.setEnabled(false);
    m_open.setEnabled(false);
    m_edit.setEnabled(false);
    if ( isDebugMode ){
      m_iconMenu.setEnabled(false);
      m_generateMenu.setEnabled(false);
    }
    m_newNodeMenu.setEnabled(true);

    m_linkMenu.setEnabled(false);

    //m_setSourceMenu.setEnabled(false);
    //m_setTargetMenu.setEnabled(false);
  }
  //----------------------------------------------------------------------
  /**
   * Customize the popup menu so as to present options relating to the
   * specified {@link Node}.  The <code>Node</code> <code>n</code> will
   * be returned in the various {@link NetworkEventsListener} callbacks.
   */
  void customise(Node n)
  {
    setFocusContext(n, null);

    if (!isEnabled())
    {
      disableAll();
      return;
    }

    m_delete.setEnabled(true);
    m_open.setEnabled(true);
    m_edit.setEnabled(true);
    if ( isDebugMode ){
      m_iconMenu.setEnabled(true);
      m_generateMenu.setEnabled(true);
    }
    m_newNodeMenu.setEnabled(false);

    m_linkMenu.setEnabled(false);

    //m_setSourceMenu.setEnabled(false);
    //m_setTargetMenu.setEnabled(false);
    //buildLinkSubMenus();  // needs calling because menus enabled
  }
  //----------------------------------------------------------------------
  /**
   * Set the current browser context, which is either the {@link Node}
   * or {@link NodeVariable} currently selected. Either or both of the
   * parameters can be <tt>null</tt>.
   */
  protected void setFocusContext(Node node, NodeVariable variable)
  {
    m_menuEventContext = node;
    m_menuEventContext_NV = variable;

    if (node != null)
    {
      m_menuEventContext_DD = node.getPainter();
    }
    else if (variable != null)
    {
      m_menuEventContext_DD = variable.getPainter();
    }
    else
    {
      m_menuEventContext_DD = null;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NetworkEventsListener} which handles the events
   * triggered by the menu.
   */
  protected NetworkEventsListener handler()
  {
    return m_eventHandler;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Node} which is the current context of the browser
   * view, or <tt>null</tt> if a node is not the context.
   */
  protected Node nodeContext()
  {
    return m_menuEventContext;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NodeVariable} which is the current context of the
   * browser view, or <tt>null</tt> if a variable is not the context.
   */
  protected NodeVariable variableContext()
  {
    return m_menuEventContext_NV;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link DisplayDelegate} which is the current context of
   * the browser view, or <tt>null</tt> if there is no node or variable
   * context.
   */
  protected DisplayDelegate ddContext()
  {
    return m_menuEventContext_DD;
  }
  //----------------------------------------------------------------------
  /**
   * Receive update events from the {@link CSL_Global_Registry}. When
   * such events occur, the buildNodeMenu() is called.
   *
   * Inhertied from {@link Observable}.
   */
  public void update(Observable o, Object arg)
  {
    if (o == CSL_Global_Registry.getInstance())
    {
      buildNodeMenu();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Set the event handler.
   *
   * @param eventHandler the {@link NetworkEventsListener} to which menu
   * events will be dispatched;
   */
  void setEventHandler(NetworkEventsListener eventHandler)
  {
    m_eventHandler = eventHandler;
  }
  //----------------------------------------------------------------------
  /**
   * Get the event handler.
   *
   * @return the {@link NetworkEventsListener} to which menu events will
   * be dispatched;
   */
  NetworkEventsListener getEventHandler()
  {
    return m_eventHandler;
  }
  //----------------------------------------------------------------------
  /**
   * Set the browser context. This specifies which network is currently
   * open in the browser.
   *
   * _TODO_(rename this routine; call it setCanvasContext )
   *
   */
  void setBrowserContext(NetworkCanvasDataContext context)
  {
    m_context = context;
  }
  //----------------------------------------------------------------------
  /**
   * Get the browser context.
   *
   * _TODO_(rename this routine)
   */
  NetworkCanvasDataContext getBrowserContext()
  {
    return m_context;
  }
}
