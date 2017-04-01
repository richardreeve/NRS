package nrs.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A {@link JPanel} with that uses a {@link GridBagLayout} layout
 * manager. Convenience methods are provided to facilitate the process
 * of arranging component on the panel.
 *
 * <p>This code borrows heavily from the the <tt>GriddedPanel.java</tt>
 * example in the book <i>Swing</i> (Manning Publications Co.) by
 * Matthew Robinson and Pavel Vorobiev (and the relevant chapter, number
 * 4, is attributed to James Tan).
 *
 * @author James Tan
 * @author Darren Smith
 */
public class GridPanel extends JPanel
{
  /**
   * Only a single instance of GridBagConstraints is used, in order to
   * conserve memory.
   */
  private GridBagConstraints m_constraints;

  // Default constraints values definitions
  private static final int C_HORIZ  = GridBagConstraints.HORIZONTAL;
  private static final int C_NONE   = GridBagConstraints.NONE;
  private static final int C_WEST   = GridBagConstraints.WEST;
  private static final int C_WIDTH  = 1;
  private static final int C_HEIGHT = 1;

  //----------------------------------------------------------------------
  /**
   * Creates an instance using a {@link GridBagLayout} with a default
   * insets constraint.
   */
  public GridPanel()
  {
    this(new Insets(2, 2, 2, 2));
  }
  //----------------------------------------------------------------------
  /**
   * Creates an instance using a {@link GridBagLayout} with the
   * specified insets
   */
  public GridPanel(Insets insets)
  {
    super(new GridBagLayout());
    m_constraints = new GridBagConstraints();
    m_constraints.anchor = C_WEST;
    m_constraints.insets = insets;
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column (both use a zero
   * based index)
   */
  public void addComponent(JComponent component, int row, int column)
  {
    addComponent(component, row, column, C_WIDTH, C_HEIGHT, C_WEST, C_NONE);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column, spanning across a
   * specified number of columns and rows
   */
  public void addComponent(JComponent component, int row, int column,
                           int width, int height)
  {
    addComponent(component, row, column, width, height, C_WEST, C_NONE);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column, using a specified
   * anchor constraint
   */
  public void addAnchoredComponent(JComponent component, int row, int column,
                                   int anchor)
  {
    addComponent(component, row, column,
                 C_WIDTH, C_HEIGHT, anchor, C_NONE);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column, spanning across
   * the specified number of columns and rows, and using a a specified
   * anchor constraint
   */
 public void addAnchoredComponent(JComponent component, int row, int column,
                                  int width, int height, int anchor)
  {
    addComponent(component, row, column, width, height, anchor, C_NONE);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column, filling the column
   * horizontally
   */
  public void addFilledComponent(JComponent component, int row, int column)
  {
    addComponent(component, row, column, C_WIDTH, C_HEIGHT, C_WEST, C_HORIZ);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column with the specified
   * fill constraint
   */
  public void addFilledComponent(JComponent component, int row, int column,
                                 int fill)
  {
    addComponent(component, row, column, C_WIDTH, C_HEIGHT, C_WEST, fill);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column, spanning across a
   * specified number of columns and rows, and with the specified fill
   * constraint
   */
  public void addFilledComponent(JComponent component, int row, int column,
                                 int width, int height, int fill)
  {
    addComponent(component, row, column, width, height, C_WEST, fill);
  }
  //----------------------------------------------------------------------
  /**
   * Add a component to the specified row and column, spannign across a
   * specified number of columns and rows, with the specified fill and
   * anchor constraints
   */
  public void addComponent(JComponent component, int row, int column,
                           int width, int height,
                           int anchor, int fill)
  {
    m_constraints.gridx = column;
    m_constraints.gridy = row;
    m_constraints.gridwidth = width;
    m_constraints.gridheight = height;
    m_constraints.gridheight = height;
    m_constraints.anchor = anchor;
    m_constraints.fill = fill;

    double weightx = 0.0;
    double weighty = 0.0;

    // Only use extra horizontal space or vertical space if a component
    // spans more than one column and/or row
    if (width > 1)  weightx = 1.0;
    if (height > 1) weighty = 1.0;

    switch (fill)
    {
      case GridBagConstraints.HORIZONTAL :
      {
        m_constraints.weightx = weightx;
        m_constraints.weighty = 0.0;
        break;
      }
      case GridBagConstraints.VERTICAL :
      {
        m_constraints.weightx = 0.0;
        m_constraints.weighty = weighty;
        break;
      }
      case GridBagConstraints.BOTH :
      {
        m_constraints.weightx = weightx;
        m_constraints.weighty = weighty;
        break;
      }
      case GridBagConstraints.NONE :
      {
        m_constraints.weightx = 0.0;
        m_constraints.weighty = 0.0;
        break;
      }
      default : break;
    }

    add(component, m_constraints);
  }
}

