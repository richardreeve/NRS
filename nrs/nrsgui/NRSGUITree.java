package nrs.nrsgui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.*;

import java.util.Enumeration;
import java.util.ArrayList;

/** Generic Tree Panel for use with NRS GUI. 
 *
 * This class can be used to create a JPanel with a JTree panel and 
 * a search button and comboBox.
 * 
 * This class is not tied down to any particular framework. 
 * All that is required is to pass a TreeModel and a JTree.
 *
 * @author Thomas French
 */

class NRSGUITree extends JPanel
                         implements TreeSelectionListener
{
  private JPanel m_panel;
  private JScrollPane m_treeView;
  private JComboBox m_searchString;
  private JTree m_tree;

  private JPopupMenu m_popup;
  private Action m_expandAction;

  private DefaultTreeModel m_model;
  private DefaultMutableTreeNode m_last = null;
  private TreePath m_lastPath = null;

  private IconManager m_iconMan;

  //for search functionality
  private ArrayList<DefaultMutableTreeNode> m_lastSearchNodes;
  private String m_lastSearchString;
  
  /** Constructor. 
   *
   * @param model TreeDataModel to use
   * @param tree JTree object to use
   */
  public NRSGUITree(DefaultTreeModel model, JTree tree){
    super();

    m_model = model;
    m_tree = tree;

    setLayout(new BorderLayout());
    
    // Setup JTree inside JScrollPane
    m_treeView = new JScrollPane(m_tree);    

    m_iconMan = AppManager.getInstance().getIconManager();
    
    // create search panel
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    m_searchString = new JComboBox();
    m_searchString.setToolTipText("Search string");
    m_searchString.setEditable(true);
   //  m_searchString.addActionListener(new ActionListener(){
//         public void actionPerformed(ActionEvent e){
//           searchEvent((String) m_searchString.getSelectedItem());
//         }
//       });

    ImageIcon searchIcon = m_iconMan.search();
    JButton next = new JButton(searchIcon);
    next.setToolTipText("Find/ Find Next");
    next.setPreferredSize(new Dimension(searchIcon.getIconWidth(), 
                                        searchIcon.getIconHeight()));
    
    next.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          searchEvent((String) m_searchString.getSelectedItem());
        }
      });

    //searchPanel.add(searchLbl);
    searchPanel.add(m_searchString);
    searchPanel.add(next);

    add(searchPanel, BorderLayout.NORTH);
    add(m_treeView, BorderLayout.CENTER);
  }

  /** Add simple popup menu to panel, 
   * with only 'Expand/Collapse' and 'Expand All' options.
   *
   * To add custom popup menu add your own one. 
   * This menu is NOT added by default.
  */
  public void addPopup(){
    // create popupMenu
    m_popup = new JPopupMenu();
    m_expandAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          if (m_lastPath==null)
            return;
          
          if (m_tree.isExpanded(m_lastPath))
            m_tree.collapsePath(m_lastPath);
          else
            m_tree.expandPath(m_lastPath);
        }
      };
    m_popup.add(m_expandAction);
    Action a = new AbstractAction("Expand All") {
        public void actionPerformed(ActionEvent e) {
          expandAll(m_tree, true);
	}
      };
    
    m_popup.addSeparator();
    m_popup.add(a);
    
    // add popup menu to JTree
    m_tree.add(m_popup);
    m_tree.addMouseListener(new PopupTrigger()); //uses inner class
  }

  /** Required by TreeSelectionListener interface. */
  public void valueChanged(TreeSelectionEvent e) {
    m_last = (DefaultMutableTreeNode)
      m_tree.getLastSelectedPathComponent();
    m_lastPath = e.getPath();
  }
    

  /** Will expand or collapse JTree depending on expand parameter. */
  public void expandAll(JTree tree, boolean expand) {
    TreePath path;

    if ( m_lastPath == null )
      path = new TreePath((TreeNode)tree.getModel().getRoot());
    else
      path = m_lastPath;
    
    // Traverse tree from last selection
    expandAll(tree, path, expand);
  }

  private void expandAll(JTree tree, TreePath parent, boolean expand) {
    // Traverse children
    TreeNode node = (TreeNode)parent.getLastPathComponent();
    if (node.getChildCount() >= 0) {
      for (Enumeration e=node.children(); e.hasMoreElements(); ) {
        TreeNode n = (TreeNode)e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        expandAll(tree, path, expand);
      }
    }
    
    // Expansion or collapse must be done bottom-up
    if (expand) {
      tree.expandPath(parent);
    } else {
      tree.collapsePath(parent);
    }
  }

  /** Get last TreeNode from a TreePath.
   *
   * @param path TreePath to extract node from.
   * @return DefaultMutableTreeNode from path.
  */
  DefaultMutableTreeNode getTreeNode(TreePath path) {
    return (DefaultMutableTreeNode)(path.getLastPathComponent());
  }

  /** Search JTree for String <tt>s</tt>.
   * Stores successful search strings to display in ComboBox.
   *
   * Note: removes any spaces around search string.
   */
  public void searchEvent(String s){
    if ( s == null ) return;
    
    s = s.trim(); // trim spaces

    if ( s.equals("") ) return;
    
    boolean b = search(s);
    
     if ( b ){
       String st = null;

       // don't add if already in list
       for(int i = 0; i < m_searchString.getItemCount(); i++){
         st = (String) m_searchString.getItemAt(i);
         
         if ( st.equals(s) )
           return;
       }
       
       m_searchString.addItem(s);
     }
  }
  
  /** Finds first case where target startsWith (or equals) search string.
   * Uses breadth-first search heuristic. 
   * Also does 'Find Next' on multiple calls using same search string.
   * 
   * @param s String to search for
   * @returns whether succesful
   */
  private boolean search(String s){
    if ( m_lastSearchNodes == null )
      m_lastSearchNodes = new ArrayList<DefaultMutableTreeNode>();
    
    // search for new search string?
    if ( m_lastSearchNodes.size() > 0 && (m_lastSearchString != null && 
                                          !s.equals(m_lastSearchString)) ){
      m_lastSearchNodes.clear();
    }
    
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_model.getRoot();
    
    boolean found = false;
    DefaultMutableTreeNode n = null; // current node
    String st = "";
    
    // do breadth first search
    Enumeration e = root.breadthFirstEnumeration();
    
    // search for string
    while(e.hasMoreElements()){
      n = (DefaultMutableTreeNode) e.nextElement();
      
      // name of node or variable
      st = n.getUserObject().toString();

      if ( st.toLowerCase().startsWith(s.toLowerCase()) ){
        // check if already seen this path, continue if yes
        if ( m_lastSearchNodes.contains(n) ){     
          continue;
        }

        found = true;
        break;
      }
    }
    
    // search string has been found
    if ( found ){
      TreePath path = new TreePath(n.getPath());
      m_tree.scrollPathToVisible(path);
      m_tree.setSelectionPath(path);

      // add to nodes already seen
      if ( !m_lastSearchNodes.contains(n) )
        m_lastSearchNodes.add(n);
      
      // set last search string to be current search string
      if ( m_lastSearchString == null || !st.equals(m_lastSearchString) )
        m_lastSearchString = s;
      
      return true;
    }
    
    String info = "No results for: '" + s + "'";
    
    if ( s.equals(m_lastSearchString) )
      info = "No more results for: '" + s + "'";

    JOptionPane.showMessageDialog(m_panel, info, "Search result", 
                                  JOptionPane.INFORMATION_MESSAGE);

    return false;
  }
  
  /** Inner class used to trigger popup menu from mouse event. */
  class PopupTrigger extends MouseAdapter {
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
        int x = e.getX();
        int y = e.getY();
        TreePath path = m_tree.getPathForLocation(x, y);

        if (path == null)
          return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
          path.getLastPathComponent();
        
        if (m_tree.isExpanded(path))
          m_expandAction.putValue(Action.NAME, "Collapse");
        else
          m_expandAction.putValue(Action.NAME, "Expand");
        

        m_tree.setSelectionPath(path);
        m_tree.scrollPathToVisible(path);

        m_popup.show(m_tree, x, y);

        m_lastPath = path;
      }
    }
  }
}
