package nrs.toolboxes;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import nrs.toolboxes.MultiTable;

/**
 *  A specialisation of {@link Toolbox} for showing and
 *  manipulating the attributes associated with a node
 */
public class AttributeEditor extends Toolbox implements ListSelectionListener,
                                                        ActionListener
{
  //
  // GUI components
  //
  private MultiTable m_table;
  private JTextArea m_descPane;
  private JSplitPane m_splitPane;

  /** Verification object */
  private Verifier m_verifier;

  //
  // Button labels
  //
  private final String OK = "OK";
  private final String CANCEL = "Cancel";

  //
  // Preference keys
  //
  private static final String PREF_DIVIDER_LOCATION = "PREF_DIVIDER_LOCATION";

  /** Store the return state of the dialog. True if the user clicked on
   * OK, or false if some kind of cancel button was pressed.  */
  private boolean m_dialogOKed;

  //---------------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param owner      owner of this dialog
   * @param title      name of the window
   * @param table      the {@link MultiTable} to be displayed
   * @param verifier   the optional {@link Verifier} to be called after
   *                   the user clicks OK. Can be null.
   */
  public AttributeEditor(Frame owner,
                         String title,
                         MultiTable table,
                         Verifier verifier)
  {
    super(owner, title, null, "AttributeEditor_");

    m_table = table;
    m_verifier = verifier;

    initGUI();
  }
  //---------------------------------------------------------------------------
  /**
   * Constructor for use by derived classes. The <code>owner</code>,
   * <code>title</code>, <code>parent</code> and <code>prefPrefix</code>
   * parametes are based to the base class constuctor.
   *
   * @param owner      owner of this dialog
   * @param title      title of the dialog, passed to base constructor
   * @param table      the {@link MultiTable} to be displayed
   * @param parent     the parent of this dialog, which will receive toolbox
   *                   events. Can be <code>null</code>, in which case
   *                   toolbox events are not generated.
   * @param prefPrefix unique String for prefixing preference entries
   * @param verifier   the optional {@link Verifier} to be called after
   *                   the user clicks OK. Can be null.
   */
  protected AttributeEditor(Frame owner,
                            String title,
                            MultiTable table,
                            ToolboxParent parent,
                            String prefPrefix,
                            Verifier verifier)
  {
    super(owner, title, parent, prefPrefix);

    m_table = table;
    m_verifier = verifier;

    initGUI();
  }
  //----------------------------------------------------------------------
  /**
   * Called whenever the value of the selection changes. Calls
   * <code>rowSelected(int row)</code>.
   */
  public void valueChanged(ListSelectionEvent e)
  {
    // Ignore extra messages
    if (e.getValueIsAdjusting()) return;

    ListSelectionModel lsm = (ListSelectionModel) e.getSource();

    if (lsm.isSelectionEmpty())
      {
        // ... no rows are selected, so nothing to do
      }
    else
      {
        int r = lsm.getMinSelectionIndex();
        //check column selection to advance selected cell, if necessary
        m_table.checkColumn(r);

        // Delegate row selection
        rowSelected(r);
      }
  }
  //----------------------------------------------------------------------
  /**
   * Perform extra processing whenever the row selection changes
   */
  protected void rowSelected(int row)
  {
    Object clientObject = m_table.getClientObject(row);

    if (clientObject != null)
      {
        m_descPane.setText(clientObject.toString());
      }
    else
      {
        m_descPane.setText("");
      }
  }
  //----------------------------------------------------------------------
  /**
   * Construct and arrange the GUI components
   */
  protected void initGUI()
  {
    // Create a JPanel to be the content pane (this is the preferred
    // method to my older style of casting the return from
    // getContentPane). This JPanel must be opaque.
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setOpaque(true);
    setContentPane(contentPane);

    m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_table.getSelectionModel().addListSelectionListener(this);
    m_table.setColumnSelectionAllowed(false);

    m_descPane = new JTextArea();
    m_descPane.setEditable(false);
    m_descPane.setWrapStyleWord(true);
    m_descPane.setLineWrap(true);

    JScrollPane scrollPaneTable = new JScrollPane(m_table);
    JScrollPane scrollPaneDesc = new JScrollPane(m_descPane);

    m_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                 scrollPaneTable,
                                 scrollPaneDesc);

    JButton okBtn = newButton(OK);
    JButton cancelBtn = newButton(CANCEL);

    JPanel buttonPane = new JPanel();
    buttonPane.add(okBtn);
    buttonPane.add(cancelBtn);

    contentPane.add(m_splitPane);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    // Set size and location
    setSize(400,400);
    setLocation(100,100);
    this.pack();
  }
  //----------------------------------------------------------------------
  /**
   * Utility method to return {@link JButton} objects with an installed
   * action listener
   */
  protected JButton newButton(String label)
  {
    JButton button = new JButton(label);
    button.addActionListener(this);
    return button;
  }
  //----------------------------------------------------------------------
  /**
   * Finalise table editting, as might be required if an OK button is
   * selected, by validating any partial input in a cell that is
   * currently being editted. Returns false if the editting could not be
   * finalised. This method should not be used for an event caused by
   * CANCEL button, since for that case, it is not important that the
   * current cell under edit is validated.
   */
  protected boolean finaliseEditing()
  {
    if (m_table.isEditing())
      return m_table.getCellEditor().stopCellEditing();
    else
      return true;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link ActionListener} listener
   */
  public void actionPerformed(ActionEvent e)
  {
    // Identify which button caused the event
    if (e.getActionCommand() == OK)
      {
        // check field values are fine
        if (!finaliseEditing()) return;

        // check verification object agrees
        if ((m_verifier != null) && (!m_verifier.isOkay(m_table))) return;

        m_dialogOKed = true;
        hide();

        return;
      }
    if (e.getActionCommand() == CANCEL)
      {
        m_dialogOKed = false;
        hide();
        return;
      }
  }
  //----------------------------------------------------------------------
  public MultiTable getTable()
  {
    return m_table;
  }
  //----------------------------------------------------------------------
  /**
   * Inquire the return state of the dialog. Returns <code>true</code>
   * if the user closed the dialog by selecting an OK button, for
   * <code>false</code> if a CANCEL button was selected.
   */
  public boolean getCloseState()
  {
    return m_dialogOKed;
  }
  //----------------------------------------------------------------------
  /**
   * Override {@link Toolbox} base to persist the location of the divider
   **/
  public void saveSettings(Preferences props)
  {
    props.putInt(PREF_DIVIDER_LOCATION, m_splitPane.getDividerLocation());

    super.saveSettings(props);
  }
  //----------------------------------------------------------------------
  /**
   * Override {@link Toolbox} base to persist the location of the
   * divider. This methods avoids setting the visibility of the
   * attribute editor object, because doing so may reveal the window
   * before it has had its data loaded.
   *
   */
  public void restoreSettings(Preferences props)
  {
    // base class method
    restoreLocation(props);

    m_splitPane.setDividerLocation(props.getInt(PREF_DIVIDER_LOCATION, 100));
  }
 }
