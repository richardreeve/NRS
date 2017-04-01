package nrs.nrsgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;

import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;

/** Creates window for viewing, creating and deleting NRS connections. 
 * Through the use of a Java JTree widget it is possbile to view network 
 * structure as a tree. It is possible to apply different filters, e.g. 
 * see only links already created, etc, to help navigation and manipulation 
 * of the underlying network.
 *
 * @author Thomas French
 * @author Darren Smith
*/

public class NodeLinkMakerWindow 
  extends Toolbox 
  implements LinkManagerListener, TreeSelectionListener, MouseListener
{
  // GUI Components
  private JPanel m_mainPanel;  
  private NRSGUITree m_treePanel;
  private JSplitPane m_splitPane;
  
  // Tree stuff
  private JTree m_tree;
  private DefaultTreeModel m_model;
  private DefaultMutableTreeNode m_root;
  private DefaultMutableTreeNode m_last;
  private TreePath m_lastPath;

  /** PopupMenu for JTree. */
  private JPopupMenu m_popup;
  private AbstractAction m_expandAction;
  private AbstractAction m_linkAction;
  private Action m_expandAll;

  // label to show current variable
  private JLabel m_nameLabel;

  // vnName of current variable
  private String m_vnName = null;

  // view options
  private JRadioButton m_asSource, m_asTarget;
  private JButton m_create, m_delete;

  // MenuBar
  private JMenuBar m_menuBar;
  private JRadioButtonMenuItem m_all, m_current;
  private JCheckBoxMenuItem m_updateEvent, m_expandEvent;

  // reference to the main link manager
  private LinkManager m_linkMan;
  
  // reference to icon manager
  private IconManager m_iconMan;

  /** Title of the dialog */
  public static String TITLE = "Links";

  /** Reference to appliation main frame. */
  private MainFrame m_mainFrame;

  private final Dimension m_size;

  //----------------------------------------------------------------------
  /**
   * Creates a {@link NodeLinkMakerWindow} with a {@link Frame} owner
   * and specified {@link ToolboxParent}. The <code>linkMan</code>
   * parameter should not be null.
   */
  public NodeLinkMakerWindow(Frame owner,
                             ToolboxParent parent,
                             LinkManager linkMan)
  {
    super((Frame) owner, TITLE, parent, "NodeLinkMakerWindow_");
    m_linkMan = linkMan;
    linkMan.addObserver(this);

    try{
      m_mainFrame = (MainFrame) owner;
    }
    catch(ClassCastException cce){
      PackageLogger.log.warning("Not able to cast owner of " + 
                                "NodeLinkMakerWindow to a MainFrame object.");
      cce.printStackTrace();
    }

    //Create root node.
    m_root = new DefaultMutableTreeNode("Network");
    m_model = new DefaultTreeModel(m_root);
    m_tree = new JTree(m_model);

    m_iconMan = AppManager.getInstance().getIconManager();

    m_size = new Dimension(300,300);

    init();
  }
  //----------------------------------------------------------------------
  private void init()
  {
    // Tree configuration
    m_tree.setEditable(false);
    m_tree.setShowsRootHandles(true);
    m_tree.getSelectionModel().setSelectionMode
      (TreeSelectionModel.SINGLE_TREE_SELECTION);

    //Listen for when the selection changes.
    m_tree.addTreeSelectionListener(this);

    // configure custom renderer 
    DefaultTreeCellRenderer renderer = 
      new CustomTreeRenderer(m_iconMan.network(), m_iconMan.possible(), 
                             m_iconMan.current(), m_linkMan, this);
    renderer.setOpenIcon(m_iconMan.open());
    renderer.setClosedIcon(m_iconMan.closed());
    renderer.setBorderSelectionColor(Color.darkGray);
    renderer.setBackgroundSelectionColor(Color.lightGray);

    //set cell renderer
    m_tree.setCellRenderer(renderer);

    // Tree Panel
    m_treePanel = new NRSGUITree(m_model, m_tree);
    //m_treePanel.addPopup();

    buildMenuBar();

    //add custom popupmenu
    addCustomPopupMenu();

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    m_nameLabel = new JLabel("Variable:");
    panel.add(m_nameLabel);
    m_treePanel.add(panel, BorderLayout.SOUTH);

    //create toolbar panel
    JPanel toolbar = new JPanel(new BorderLayout());
    //toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));

    
    JPanel views = new JPanel();
    views.setBorder(BorderFactory.createTitledBorder(BorderFactory.
                                                  createEtchedBorder(), 
                                                  "View"));
    m_asSource = new JRadioButton("As Source");
    m_asSource.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          event();
        }
      });
    
    m_asTarget = new JRadioButton("As Target");
    m_asTarget.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          event();
        }
      });    

    ButtonGroup btGroup = new ButtonGroup();
    btGroup.add(m_asSource);
    btGroup.add(m_asTarget);
    m_asSource.setSelected(true); //set default
    
    JButton m_refresh = new JButton("Refresh Tree");
    m_refresh.setToolTipText("Refresh JTree");
    m_refresh.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          generateTree();

          if ( m_expandEvent.isSelected() )
            m_treePanel.expandAll(m_tree, true);
        }
      });

    JButton expand = new JButton("Expand All");
    expand.setToolTipText("Expand Out JTree.");
    expand.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          m_treePanel.expandAll(m_tree, true);
        }
      });

    views.add(m_asSource);
    views.add(m_asTarget);
    views.add(m_refresh);
    views.add(expand);
    toolbar.add(views, BorderLayout.CENTER);
       
    JPanel buttons = new JPanel();
    buttons.setBorder(BorderFactory.createTitledBorder(BorderFactory.
                                                     createEtchedBorder(), 
                                                     "Actions"));
    m_create = new JButton("Create Link");
    m_create.setEnabled(false);
    m_create.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          createLink();
        }
      });
    buttons.add(m_create);
    
    m_delete = new JButton("Delete Link");
    m_delete.setEnabled(false);
    m_delete.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          deleteLink();
        }
      });
    buttons.add(m_delete);
    
    buttons.setPreferredSize(new Dimension(100,100));
    toolbar.add(buttons, BorderLayout.SOUTH);

    //Add the scroll panes to a split pane.
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setRightComponent(m_treePanel);
    splitPane.setLeftComponent(toolbar);    
    splitPane.setPreferredSize(m_size);
    splitPane.setDividerSize(5);

    // not good to hardcore these values
    toolbar.setMinimumSize(new Dimension(150, 300));
    m_treePanel.setMinimumSize(new Dimension(175, 300));

    // Root panel
    Container root = getContentPane();
    root.setLayout(new BorderLayout());
    root.add(splitPane, BorderLayout.CENTER);
    root.setMinimumSize(m_size);
    root.setPreferredSize(m_size);

    getRootPane().setJMenuBar(m_menuBar);

    setMinimumSize(m_size);
    setPreferredSize(m_size);
    this.pack();

    setResizable(true);
  }
  //----------------------------------------------------------------------
  /** Add custom popup menu to JTree. */
  public void addCustomPopupMenu(){
    // create popupMenu
    m_popup = new JPopupMenu();
    
    // add abstract action for CreateLink & DeleteLink
    m_linkAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          //create or delete link
          Link ln = null;
          ln = m_linkMan.findLink(m_linkMan.getVariable(), 
                                  (NodeVariable) m_last.getUserObject());
          if ( ln == null )
            ln = m_linkMan.findLink((NodeVariable) m_last.getUserObject(),
                                    m_linkMan.getVariable());
          
          if ( ln == null )
            {
              // Createlink
              createLink();
            }
          else
            {
              // DeleteLink
              deleteLink();
            }
        }
      };
    m_popup.add(m_linkAction); 

    m_expandAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          TreePath lp = m_lastPath;

          if (lp == null)
            return;
          
          if (m_tree.isExpanded(lp))
            m_tree.collapsePath(lp);
          else
            m_tree.expandPath(lp);
        }
      };
    m_expandAll = new AbstractAction("Expand All") {
        public void actionPerformed(ActionEvent e) {
          m_treePanel.expandAll(m_tree, true);
	}
      };

    m_popup.addSeparator();
    m_popup.add(m_expandAction);
    m_popup.add(m_expandAll);
    
    // add popup menu to JTree
    m_tree.add(m_popup);
    m_tree.addMouseListener(this);
  }

  //----------------------------------------------------------------------
  private void buildMenuBar(){
    // Build MenuBar
    m_menuBar = new JMenuBar();
    
    JMenu views = new JMenu("View");
    
    m_all = new JRadioButtonMenuItem("All Links");
    m_all.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          event();
        }
      });
    m_current = new JRadioButtonMenuItem("Current Links Only");
    m_current.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          event();
        }
      });

    ButtonGroup group = new ButtonGroup();
    group.add(m_all);
    group.add(m_current);
    m_all.setSelected(true);    

    m_updateEvent = new JCheckBoxMenuItem("Update on Event", true);
    m_updateEvent.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          event();
        }
      });

    m_expandEvent = new JCheckBoxMenuItem("Expand on Event", true);
    m_expandEvent.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          event();
        }
      });

    views.add(m_all);
    views.add(m_current);
    
    JMenu viewMenu = new JMenu("Options");
    viewMenu.setMnemonic(KeyEvent.VK_O);
    viewMenu.add(views);
    viewMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
    viewMenu.add(m_updateEvent);
    viewMenu.add(m_expandEvent);
    
    m_menuBar.add(viewMenu);
  }
  //----------------------------------------------------------------------
  /* Triggered on event to update Tree model.*/
  private boolean event(){
    if ( m_updateEvent.isSelected()){
      rebuildLinkView();
      
      if ( m_expandEvent.isSelected() )
         m_treePanel.expandAll(m_tree, true);
      
      return true;
    }
    return false;
  }
  //----------------------------------------------------------------------
  /** Generate JTree from current network structure.*/
  private void generateTree(){
    if ( !m_all.isSelected() && !m_current.isSelected() ){
      m_root.removeAllChildren();
      m_model.nodeStructureChanged(m_root);
      return;
    }

    rebuildLinkView();
  }
  //----------------------------------------------------------------------
  /** Start off CreateLink procedure. */
  private void createLink(){
    if ( m_last == null ) return;

    //check if source or target
    if ( m_asSource.isSelected() )
      m_linkMan.makeLink(m_linkMan.getVariable(), 
               (NodeVariable) m_last.getUserObject());
    else
      m_linkMan.makeLink((NodeVariable) m_last.getUserObject(), 
               m_linkMan.getVariable());

    m_model.nodeStructureChanged(m_last);

    m_create.setEnabled(false);
    m_delete.setEnabled(true);
  }
  //----------------------------------------------------------------------
  /** Start off DeleteLink procedure. */
  private void deleteLink(){
    Link ln = null;

    //check if source or target
    if ( m_asSource.isSelected() )
      ln = m_linkMan.findLink(m_linkMan.getVariable(), 
               (NodeVariable) m_last.getUserObject());
    else
      ln = m_linkMan.findLink((NodeVariable) m_last.getUserObject(), 
               m_linkMan.getVariable());

    if ( ln != null ){
      m_linkMan.removeLink(ln);
      m_model.nodeStructureChanged(m_last);

      m_create.setEnabled(true);
      m_delete.setEnabled(false);
    }
  }

  //----------------------------------------------------------------------
  /** Selected variable has been changed. Update GUI elements.*/
  public void variableChanged()
  {
    if (m_linkMan.getVariable() != null)
    {
      // VNName of current variable
      m_vnName = m_linkMan.getVariable().VNName();
      
      m_nameLabel.setText("Variable: " + m_vnName);
      m_nameLabel.setToolTipText(m_vnName);
      
      // update event
      if ( !event() ){
        //clear network
        m_root.removeAllChildren();
        m_model.nodeStructureChanged(m_root);
      }
    }
    else
    {
      m_nameLabel.setText("Variable: ");
    }
  }
  //----------------------------------------------------------------------
  /**
   * Rebuild the TreeModel from the current network.
   * Generates a new tree everytime. Not effecient for large networks.
   *
   */
  private void rebuildLinkView()
  {
    // no variable clicked on
    if ( m_linkMan.getVariable() == null ) return;

    // start from scratch
    m_root.removeAllChildren();

    NodeCollection nc = m_mainFrame.getCollection();
    DefaultMutableTreeNode node = null;
    for(Iterator i = nc.nodeIterator(); i.hasNext(); ){
      node = new DefaultMutableTreeNode(i.next());

      node = buildTree(node); // recurse on node
      if ( node != null )
        m_root.add(node);
    }
    
    m_model.nodeStructureChanged(m_root);
  }

  /** Recursively build up TreeModel of network structure.*/
  private DefaultMutableTreeNode buildTree(DefaultMutableTreeNode node){
    Node n = (Node) node.getUserObject();
    
    // iterate through sub-nodes
    NodeCollection nc = n.getChildNodes();
    DefaultMutableTreeNode newnode;
    for(Iterator i = nc.nodeIterator(); i.hasNext(); ){
      newnode = new DefaultMutableTreeNode(i.next());
      
      newnode = buildTree(newnode); //recurse on node

      if ( newnode != null )
        node.add(newnode);
    }

    // iterate through variables
    VariableCollection vc = n.getVariables();
    for(Iterator i = vc.iterator(); i.hasNext(); ){
      NodeVariable vn = (NodeVariable) i.next();

      // check if Variable we are currently looking at.
      if ( vn.VNName().equals(m_vnName) )
        continue;

      newnode = new DefaultMutableTreeNode(vn);
      
      Link ln = null;
      // When view is show 'All Links', all links that are possible are added, 
      // and then the colour of the icon is determined by the cell renderer. 
      // Thus it is not necessary to differentiate between possible and 
      // already created links here.
      if ( m_all.isSelected() ){
        if ( m_asSource.isSelected() && m_linkMan.
             checkPossibleLink(m_linkMan.getVariable(), vn) ){
          node.add(newnode);
          continue;
        }
        else if ( m_asTarget.isSelected() && m_linkMan.
                  checkPossibleLink(vn, m_linkMan.getVariable())){
          node.add(newnode);
          continue;
        }
      }

      // show current only - now must find links that have been created.
      if ( m_current.isSelected() ){
        if ( m_asSource.isSelected() ){
          ln = m_linkMan.findLink(m_linkMan.getVariable(), vn);
        }
        else if ( m_asTarget.isSelected() ) {
          ln = m_linkMan.findLink(vn, m_linkMan.getVariable());
        }

        if ( ln != null && ln.getExists() ){
          node.add(newnode);
        }
      }
    }

    // don't show empty nodes
    if ( node.getChildCount() == 0 )
      return null;
    
    return (DefaultMutableTreeNode) node;
  }

  /** Required by TreeSelectionListener interface. 
   * Triggered when a new selection is made. 
   *
   * Store this information for possible use later on.
   */
  public void valueChanged(TreeSelectionEvent e) {
    // Store last selection and path
    m_last = (DefaultMutableTreeNode) m_tree.getLastSelectedPathComponent();
    m_lastPath = e.getPath();

    if ( m_last == null ) return;

    // change state of create and delete link buttons
    if ( !(m_last.getUserObject() instanceof NodeVariable) ){
      m_create.setEnabled(false);
      m_delete.setEnabled(false);
      return;
    }

    Link ln = null;
    
    if ( m_asSource.isSelected() ){
      ln = m_linkMan.findLink(m_linkMan.getVariable(), 
                              (NodeVariable) m_last.getUserObject());
    }
    else if ( m_asTarget.isSelected() ) {
      ln = m_linkMan.findLink((NodeVariable) m_last.getUserObject(), 
                              m_linkMan.getVariable());
    }
    
    if ( ln == null )
      {
        m_create.setEnabled(true);
        m_delete.setEnabled(false);
      }
    else
      {
        m_create.setEnabled(false);
        m_delete.setEnabled(true);
      }
  }

  /** Return whether currently set as Source.*/
  public boolean asSource(){
    return m_asSource.isSelected();
  }

  /** Return whether currently set as Target.*/
  public boolean asTarget(){
    return m_asTarget.isSelected();
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse button
   * has been clicked (pressed and released) on a component.
   */
  public void mouseClicked(MouseEvent e){
    if (e.isPopupTrigger() ) {
      popupMenu(e);
    }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when a mouse button
   * has been pressed on a component.
   */
  public void mousePressed(MouseEvent e){
    if ( e.isPopupTrigger() ){
        popupMenu(e);
    }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface.Invoked when a mouse button has
   * been released on a component.
   */
  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger() ) {
      popupMenu(e);
    }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse enters
   * a component.
   */
  public void mouseEntered(MouseEvent e) { }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse exits a
   * component.
   */
  public void mouseExited(MouseEvent e) { }
  //----------------------------------------------------------------------
  /** Popup appropriate menu. */
  private void popupMenu(MouseEvent e){
    int x = e.getX();
    int y = e.getY();
    TreePath path = m_tree.getPathForLocation(x, y);
    
    if (path == null)
    return;
    
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
      path.getLastPathComponent();
    
    m_last = node;
    
    // set expand/collapse menu entry
    if (m_tree.isExpanded(path))
    m_expandAction.putValue(Action.NAME, "Collapse");
    else
    m_expandAction.putValue(Action.NAME, "Expand");
    
    // set whether expand/collapse menu items should be enabled
    if ( node.isLeaf() ){
      m_expandAll.setEnabled(false);
        m_expandAction.setEnabled(false);
    }
    else
    {
      m_expandAll.setEnabled(true);            
      m_expandAction.setEnabled(true);
    }
      
    // whether create/delete link menu entries are enabled
    if ( node.getUserObject() instanceof NodeVariable )
    setLinkPopup();
    else
    {
      m_linkAction.putValue(Action.NAME, "");
        m_linkAction.setEnabled(false);
    }
    
    m_tree.setSelectionPath(path);
    m_tree.scrollPathToVisible(path);
    
      m_popup.show(m_tree, x, y);
  }
 
  /** Display appropriate popup entry - CreateLink or DeleteLink.*/
  private void setLinkPopup(){
    Link ln = null;
    
    if ( m_asSource.isSelected() ){
      ln = m_linkMan.findLink(m_linkMan.getVariable(), 
                              (NodeVariable) m_last.getUserObject());
    }
    else if ( m_asTarget.isSelected() ) {
      ln = m_linkMan.findLink((NodeVariable) m_last.getUserObject(), 
                            m_linkMan.getVariable());
    }
    
    if ( ln == null )
    {
      m_linkAction.putValue(Action.NAME, "CreateLink");
    }
    else
    {
      m_linkAction.putValue(Action.NAME, "DeleteLink");        
    }
    m_linkAction.setEnabled(true);
  }
}
