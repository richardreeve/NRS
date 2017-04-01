package nrs.toolboxes.multitable;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import nrs.nrsgui.AccessDelegate;
import nrs.toolboxes.MultiTable;

/** Non-public class. All fields are public, because this is serving as
 * an extremely light-weight data structure.
 *
 * TODO - this class should not be under multitable, since it references
 * NRS classes. Move to that package.
 */
class RowData
{
  public String key;
  public boolean editable;
  public AccessDelegate delegate;
}

/** Non-public class */
class PropertyEditorModel extends AbstractTableModel
{
  private final int m_columnCount = 2;
  private final String[] m_columnNames;
  private final ArrayList m_rows;  // each item of type RowData

  ///// Methods

  //----------------------------------------------------------------------
  /** Constructor */
  PropertyEditorModel(String columnKey, String columnValue)
  {
    super(); // base constructor

    m_columnNames = new String[2];
    m_columnNames[0] = columnKey;
    m_columnNames[1] = columnValue;

    m_rows = new ArrayList();
  }
  //----------------------------------------------------------------------
  /**
   * Accept a new row of data. Return the index at which this new row
   * resides
   */
  public int addRow(RowData rowData)
  {
    m_rows.add(rowData);
    return getRowCount() - 1;
  }
  //----------------------------------------------------------------------
  /** Return number of columns */
  public int getColumnCount() { return m_columnCount; }
  //----------------------------------------------------------------------
  /**
   * Return number of rows
   */
  public int getRowCount() { return m_rows.size(); }
  //----------------------------------------------------------------------
  /**
   * Parameter <code>col</code> should be either 0 or 1 - anything else
   * will cause an exception to be thrown
   */
  public String getColumnName(int col) { return m_columnNames[col]; }
  //----------------------------------------------------------------------
  /**
   * Parameter <code>col</code> should be either 0 or 1 - anything else
   * will cause null to be returned
   */
  public Object getValueAt(int row, int col)
  {
    RowData rowData = (RowData) m_rows.get(row);

    if (col == 0)
      return rowData.key;
    else
      return rowData.delegate.get();
  }
  //----------------------------------------------------------------------
  /**
   * <code>JTable</code> and derivatives, use this method to determine the
   * default renderer / editor for each cell, but {@link MultiTable}
   * should not need to rely on it, since <code>MultiTable</code>
   * support differing classes throughout an individual column.
   */
  public Class getColumnClass(int c) { return getValueAt(0, c).getClass(); }
  //----------------------------------------------------------------------
  /** Parameter <code>col</code> should be either 0 or 1 */
  public boolean isCellEditable(int row, int col)
  {
    if (col == 0)
      {
        return false;
      }
    else
      {
        RowData rowData = (RowData) m_rows.get(row);
        return rowData.editable;
      }
  }
  //----------------------------------------------------------------------
  /**
   * Implements {@link AbstractTableModel} interface
   */
  public void setValueAt(Object value, int row, int col)
  {
    //    System.out.println("Setting value at " + row + "," + col
    //                       + " to " + value
    //                       + " (an instance of "
    //                       + value.getClass() + ")");
    // only for col == 1
    if (col == 1)
      {
        RowData rowData = (RowData) m_rows.get(row);

        rowData.delegate.set(value);

        fireTableCellUpdated(row, col);
      }
  }
  //----------------------------------------------------------------------
  /**
   * Delete all rows from this data model
   */
  public void removeAll()
  {
    m_rows.clear();
  }
}

/** This class facilitates the building of a {@link MultiTable} object
 * with a pair of columns. The left column contains property keys, or
 * names, and the right column contains the corresponding value. */
public class MultiTableBuilder
{
  private final MultiTable m_table;
  private final PropertyEditorModel m_model;

  //----------------------------------------------------------------------
  /**
   * Constructor. Create the initial {@link MultiTable} instance and a
   * supporting table model which provides for a two-column name/value
   * kind of table.
   */
  public MultiTableBuilder()
  {
    m_model = new PropertyEditorModel("Name", "Value");
    m_table = new MultiTable(m_model);
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link MultiTable} under constructed and maintained by
   * this class. The same object is always returned.
   */
  public MultiTable getTable() { return m_table; }
  //----------------------------------------------------------------------
  /**
   * Add a new row, and use a specific editor for the value field
   */
  public void addRow(String key,
                     AccessDelegate delegate,
                     TableCellEditor editor,
                     boolean editable,
                     Object clientObject)
  {
    // First add to data model
    RowData rowData = new RowData();

    rowData.key = key;
    rowData.editable = editable;
    rowData.delegate = delegate;

    int rowIndex = m_model.addRow(rowData);

    // ...and second, add to the MultiTable any specific editor provided
    if (editor != null) m_table.addRowEditor(rowIndex, editor);

    // ...and also register the client object
    if (clientObject != null) m_table.addClientObject(rowIndex, clientObject);
  }
  //----------------------------------------------------------------------
  /**
   * Add a new row, and use the default editor for the value field
   */
  public void addRow(String key,
                     AccessDelegate delegate,
                     boolean editable,
                     Object clientObject)
  {
    addRow(key, delegate, null, editable, clientObject);
  }
  //----------------------------------------------------------------------
  /**
   * Clear the current contents of the table
   */
  public void removeAll()
  {
    m_model.removeAll();
  }
}
