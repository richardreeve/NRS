/* $Id: LayerPanel.java,v 1.1 2004/11/15 09:47:01 darrens Exp $ */

package nrs.toolboxes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/** 
 * A psuedo layout manager. A {@link LayerPanel} is used to hold other
 * panels such that each panel is laid out one above the other (acting
 * kind of like a vertical layout manager). The vertical layering is
 * achieved by using nested panels each using the {@link
 * BorderLayout}. Each "row" panel added to (or constructed by) a {@link
 * LayerPanel} is placed within the north area of a {@link
 * BorderLayout}, thus ensuring it appears in its preferred or minimum
 * size.
 *
 * @author Hugo L. Rosano, Darren Smith
 */
public class LayerPanel extends JPanel
{
  // The panel which contains the current row (see next). Whenever a new
  // row is created, the center area of the m_RowBottom is used to hold
  // the next row and next bottom panel
  private JPanel m_RowBottom;

  // current row available for placing of components
  private JPanel m_row;

  //----------------------------------------------------------------------
  /** Construct a {@link LayerPanel} which uses the {@link BorderLayout}
   *
   * @param border this parameter is ignored, the default is {@link
   * BorderLayout}
   */
  public LayerPanel(BorderLayout border){
    this();
  }

  //----------------------------------------------------------------------
  /** Constructor 
   *
   * Creates an intial {@link JPanel} row which uses the {@link FlowLayout}
   **/
  public LayerPanel()
  {
    super(new BorderLayout());
    m_RowBottom = this;
    m_row = new JPanel(new FlowLayout(FlowLayout.LEFT));
    m_RowBottom.add(m_row, BorderLayout.NORTH);
  }

  //----------------------------------------------------------------------
  /** Constructor 
   *
   * Uses <tt>top</tt> as the first layer
   **/
  public LayerPanel(JPanel top)
  {
    super(new BorderLayout());
    m_RowBottom = this;
    m_row = top;
    m_RowBottom.add(m_row, BorderLayout.NORTH);
  }

  //----------------------------------------------------------------------
  /** Adds the {@link JComponent} to the current row
   *	
   * @param comp {@link JComponent} to be added
   */
  public void add(JComponent comp){
    m_row.add(comp);
  }

  //----------------------------------------------------------------------
  /**
   * Return the current row being used for containing new components. A
   * new row is created by calling {@link #createRow()}.
   */
  public JPanel getRow()
  {
    return m_row;
  }
  
  //----------------------------------------------------------------------
  //  public void addOneResizable(JComponent comp){
  //    m_row = new JPanel(new BorderLayout());
  //    m_RowBottom.add(m_row, BorderLayout.NORTH);
  //    m_row.add(comp, BorderLayout.NORTH);
  //  }

  //----------------------------------------------------------------------
  //  public void addHorizontalSplitPane(JComponent one, JComponent two){
  //    addHorizontalSplitPane(one,two,BorderLayout.NORTH);
  //  }
  
  public void addHorizontalSplitPane(JComponent one, JComponent two, String borderLayout){
    JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    jsp.setLeftComponent(one);
    jsp.setRightComponent(two);
    m_RowBottom.remove(m_row);
    m_RowBottom.add(jsp, borderLayout);
  }

  //----------------------------------------------------------------------
  /** 
   * Adds a new layer by creating a new {@link JPanel} containing a
   * {@link FlowLayout} layout manager.
   */
  public JPanel createRow()
  {
    JPanel newBase = new JPanel(new BorderLayout());
    m_RowBottom.add(newBase, BorderLayout.CENTER);
    m_RowBottom = newBase;
    JPanel newRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    m_RowBottom.add(newRow, BorderLayout.NORTH);
    m_row = newRow;
    return m_row;
  }
  //----------------------------------------------------------------------
  /**
   * Adds the {@link JPanel} <tt>row</tt> as the next layer
   *
   * @return the {@link JPanel} just added
   */
  public JPanel addRow(JPanel row)
  {
    JPanel newBase = new JPanel(new BorderLayout());
    m_RowBottom.add(newBase, BorderLayout.CENTER);
    m_RowBottom = newBase;
    m_RowBottom.add(row, BorderLayout.NORTH);
    m_row = row;
    return m_row;
  }
  //----------------------------------------------------------------------
  /** Adds default border to the current row */
  public void rowSetBorder(){
    m_row.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createEmptyBorder(5,5,5,5),
                              BorderFactory.createEtchedBorder()));
  }

  //----------------------------------------------------------------------
  /** Adds default border to this LayerPanel*/
  public void setBorder(){
    this.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createEmptyBorder(5,5,5,5),
                              BorderFactory.createEtchedBorder()));
  }
};
