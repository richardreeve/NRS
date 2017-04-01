package nrs.toolboxes;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/** An extension of the Java {@link JTable} which allows for cells
 * contained within a column to use different cell editors, and also
 * custom editors (and renderers). Design and implementation of this
 * class is based on Java Tip 102 at www.javaworld.com, written by Tony
 * Colston. Currently cell editors / renderers can only be specified on
 * a per-row basis, which is suitable if this class is going to be used
 * to provide a Visual Basic style property editor, but not suitable the
 * table has many columns, and cell editors various from one column to
 * another, and from one row to another.
 *
 * A further addition to this class is the support for a row-specific
 * client data object, which a client can use to associate any
 * particular object with a row. A typical use for this is to provide a
 * description object for each row; when that row is then selected, the
 * description object can be retrieved and displayed.
 */
public class MultiTable extends JTable
{
  /** Map of the editor to use for each row */
  private HashMap m_rowEditors;

  /** Map of the renderers to use for each row */
  private HashMap m_rowRenderers;

  /** Map of the client objects associated with each row */
  private HashMap m_clientObjects;

  /** This is hack - used to communicate to getColumnClass() what class
   * to return */
  private Class m_rowClass;

  //----------------------------------------------------------------------
  /** Constructs a default {@link MultiTable} that is initialized with a
   * default data model, a default column model, and a default selection
   * model.
   */
  public MultiTable()
  {
    super();  // Call base constructor

    m_rowEditors = null;
    m_rowRenderers = null;

    init();
  }
  //----------------------------------------------------------------------
  /**  Constructs a {@link MultiTable} with numRows and numColumns of
   *  empty cells using DefaultTableModel. */
  public MultiTable(int numRows, int numColumns)
  {
    super(numRows, numColumns);  // Call base constructor

    m_rowEditors = null;

    init();
  }
  //----------------------------------------------------------------------
  /** Constructs a {@link MultiTable} to display the values in the two
   * dimensional array, rowData, with column names, columnNames.
   */
  public MultiTable(Object[][] rowData, Object[] columnNames)
  {
    super(rowData, columnNames);  // Call base constructor

    m_rowEditors = null;

    init();
  }
  //----------------------------------------------------------------------
  /** Constructs a {@link MultiTable} that is initialized with dm as the
   * data model, a default column model, and a default selection model.
   */
  public MultiTable(TableModel dm)
  {
    super(dm);  // Call base constructor

    m_rowEditors = null;

    init();
  }
  //----------------------------------------------------------------------
  /** Constructs a {@link MultiTable} that is initialized with dm as the
   *  data model, cm as the column model, and a default selection model.
   */
  public MultiTable(TableModel dm, TableColumnModel cm)
  {
    super(dm, cm);  // Call base constructor

    m_rowEditors = null;

    init();
  }
  //----------------------------------------------------------------------
  /** Constructs a {@link MultiTable} that is initialized with dm as the
   * data model, cm as the column model, and sm as the selection model.
   */
  public MultiTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
  {
    super(dm, cm, sm);  // Call base constructor

    m_rowEditors = null;

    init();
  }
  //----------------------------------------------------------------------
  /** Constructs a {@link MultiTable} to display the values in the
   * {@link Vector} of Vectors, rowData, with column names, columnNames.
   */
  public MultiTable(Vector rowData, Vector columnNames)
  {
    super(rowData, columnNames);  // Call base constructor

    m_rowEditors = null;

    init();
  }
  //----------------------------------------------------------------------
  public void addRowEditor(int row, TableCellEditor e)
  {
    if (m_rowEditors == null) m_rowEditors = new HashMap();

    m_rowEditors.put(new Integer(row), e);
  }
  //----------------------------------------------------------------------
  public void addRowRenderer(int row, TableCellRenderer e)
  {
    if (m_rowRenderers == null) m_rowRenderers = new HashMap();

    m_rowRenderers.put(new Integer(row), e);
  }
  //----------------------------------------------------------------------
  public void removeRowEditor(int row)
  {
    if (m_rowEditors == null) return;

    m_rowEditors.remove(new Integer(row));
  }
  //----------------------------------------------------------------------
  public void removeRowRenderer(int row)
  {
    if (m_rowRenderers == null) return;

    m_rowRenderers.remove(new Integer(row));
  }
  //----------------------------------------------------------------------
  public TableCellEditor getRowEditor(int row)
  {
    return (TableCellEditor) m_rowEditors.get(new Integer(row));
  }
  //----------------------------------------------------------------------
  public TableCellRenderer getRowRenderer(int row)
  {
    return (TableCellRenderer) m_rowRenderers.get(new Integer(row));
  }
  //----------------------------------------------------------------------
  /**
   * Override the default method for determining which {@link
   * TableCellEditor} to use for a particular cell. If a specific cell
   * editor has been provided for <code>row</code> then use that. If no
   * specific editor is located, then return the default editor for the
   * class of the value locate at the indicated cell.
   */
   public TableCellEditor getCellEditor(int row, int col)
   {
     if ((m_rowEditors != null) &&
         (m_rowEditors.containsKey(new Integer(row))))
       {
         return (TableCellEditor) m_rowEditors.get(new Integer(row));
       }
     else
       {
         /* Thanks to the Java source for showing me how to do this! */
         return getDefaultEditor(getValueAt(row,col).getClass());
       }
   }
  //----------------------------------------------------------------------
  /**
   * Overrides inherited <code>prepareEditor</code> from {@link JTable}
   * so that the subsequent call to {@link #getColumnClass(int)} can be
   * fooled into return a row specific value, rather than a column
   * specific one. Note: this design and implementation hinges on how
   * the internal {@link JTable} works. Acutally, there might be a bug
   * in the <code>JTable</code> itself, because when
   * <code>editor.getTableCellEditorComponent(...)</code> is called,
   * that method uses the <code>getColumnClass(...)</code> method to
   * determine the target type of the cell editor value, rather that
   * simply, and more correctly, just requesting the class type of the
   * default value provided in the
   * <code>editor.getTableCellEditorComponent(...)</code> call.
   *
   * This method works around that problem by setting a class member,
   * <code>m_rowClass</code>, with the name of actual row type to be
   * used. The subequent call to <code>getColumnClass(int)</code> caused
   * by the <code>super.prepareEditor(...)</code> call will return this
   * stored class value, and not the class value of the column.
   */
  public Component prepareEditor(TableCellEditor editor, int row, int column)
  {
    // store the actual class of the data being editted...
    m_rowClass = dataModel.getValueAt(row, column).getClass();

    Component retVal = super.prepareEditor(editor, row, column);

    // ... now remove what we stored, so that getColumnClass behaviour
    // is not effected more than necessary
    m_rowClass = null;

    return retVal;

  }
  //----------------------------------------------------------------------
  /**
   *
   */
  public Class getColumnClass(int column)
  {
    if (m_rowClass != null)
      {
        Class retVal = m_rowClass;
        m_rowClass = null;

        return retVal;
      }
    else
      return super.getColumnClass(column);
  }
  //----------------------------------------------------------------------
  /**
   * Override the default method for determining which {@link
   * TableCellRenderer} to use for a particular cell. If a specific cell
   * renderer has been provided for <code>row</code> then use that. If
   * no specific renderer is located, then return the default editor for
   * the class of the value locate at the indicated cell.
   */
  public TableCellRenderer getCellRenderer(int row, int col)
  {
    TableCellRenderer retVal;

    if ((m_rowRenderers != null) &&
        (m_rowRenderers.containsKey(new Integer(row))))
      {
        retVal =  (TableCellRenderer) m_rowRenderers.get(new Integer(row));
      }
    else
      {
        retVal = getDefaultRenderer(getValueAt(row,col).getClass());
      }


    // For Default renderers, we can play around with the colours
    if (retVal instanceof DefaultTableCellRenderer)
      {
        DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) retVal;

        // Set the default background, and override any previous colour setting
        dtcr.setBackground(Color.WHITE);

        // Now apply specific colouring for constant fields (ignoring
        // also the field names, which are constant)
        if ((col >= 0) && (dataModel.isCellEditable(row, col) == false))
          {
            dtcr.setBackground(SystemColor.control);
          }
     }

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Associate the client object <code>o</code> with row <code>row</code>
   */
  public void addClientObject(int row, Object o)
  {
    if (m_clientObjects == null) m_clientObjects = new HashMap();

    m_clientObjects.put(new Integer(row), o);
  }
  //----------------------------------------------------------------------
  /**
   * Remove the client object associated with row <code>row</code>
   */
  public void removeClientObject(int row)
  {
    if (m_clientObjects == null) return;

    m_clientObjects.remove(new Integer(row));
  }
  //----------------------------------------------------------------------
  /**
   * Return the client object associated with row <code>row</code>
   *
   * @return the cleint object, or <code>null</code> if none available
   */
  public Object getClientObject(int row)
  {
    if (m_clientObjects == null) return null;

    Integer rowI = new Integer(row);

    if (m_clientObjects.containsKey(rowI))
       return m_clientObjects.get(rowI);
    else
      return null;
  }

  /** Advance selected cell if column is currently selected.*/
  public void checkColumn(int row){
    if ( getSelectedColumn() == 0 ){
      changeSelection(row, 1, false, false);
    }
  }

  /** Transfer focus to OK button if TAB is clicked and on last row. */
  private void init(){
//     addKeyListener(new KeyAdapter() {
//         public void keyPressed(KeyEvent event) {
//           // look for tab keys
//           if(event.getKeyCode() == KeyEvent.VK_TAB
//              || event.getKeyChar() == '\t') {
//             System.out.println("Catch Tab key!");

//             if ( getSelectedColumn() == 1 && 
//                  (getSelectedRow()+1) == getRowCount() )
//               {
//                 System.out.println("Transfer focus!");
//                 transferFocus();
//               }
//           }
//         }
//       });
  }
}
