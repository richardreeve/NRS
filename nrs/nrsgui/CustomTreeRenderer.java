package nrs.nrsgui;

import java.awt.Component;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.JTree;
import javax.swing.Icon;

class CustomTreeRenderer extends DefaultTreeCellRenderer 
{
  private Icon m_root, m_possible, m_created;
  private LinkManager m_linkMan;
  private NodeLinkMakerWindow m_linkWin;
  
  public CustomTreeRenderer(Icon root, Icon possible, Icon created, 
                            LinkManager linkMan, NodeLinkMakerWindow win) {
    m_root = root;
    m_possible = possible;
    m_created = created;
    m_linkMan = linkMan;
    m_linkWin = win;
  }
  
  public Component getTreeCellRendererComponent(
                                                JTree tree,
                                                Object value,
                                                boolean sel,
                                                boolean expanded,
                                                boolean leaf,
                                                int row,
                                                boolean hasFocus) {
    
    super.getTreeCellRendererComponent(tree, value, sel,
                                       expanded, leaf, row,
                                       hasFocus);

    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) 
      tree.getModel().getRoot();

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    
    // if root icon
    if ( tn == node ) setIcon(m_root);
    else if ( node.getUserObject() instanceof NodeVariable ){
      NodeVariable selected = m_linkMan.getVariable();
      NodeVariable current = (NodeVariable) node.getUserObject();

      Link ln;
      if ( m_linkWin.asSource() )
        ln = m_linkMan.findLink(selected, current);
      else //as target
        ln = m_linkMan.findLink(current, selected);

      if ( ln != null && ln.getExists() ){
        setIcon(m_created);
      }
      else 
        setIcon(m_possible);
    }
    return this;
  }
}
