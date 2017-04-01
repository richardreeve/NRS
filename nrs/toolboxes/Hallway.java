// $Id: Hallway.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.toolboxes;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.Preferences;

/** Provide a window which presents an initial list of options, each of
  * which represents a manner in which an NRS/GUI session can be
  * started. This will typically be displayed if the user has invoked
  * the NRS/GUI without any command line options.
  */
public class Hallway extends Toolbox implements ActionListener,
                                                ListSelectionListener
{
  private DefaultListModel m_listModel = new DefaultListModel();
  private HashMap m_options = new HashMap();

  String m_selection;

  // GUI elements
  //  private JLabel m_optionDesc = new JLabel("Nothing selected");
  private JList m_list = new JList(m_listModel);
  private JButton m_start = new JButton("Start");
  private JButton m_exit = new JButton("Exit");
  private JTextArea m_optionDesc = new JTextArea("Nothing selected", 2, 30); 

  ///// Name startup options
  public static String EXIT = "Exit";
  public static String SERVER_LOCAL = "Local Connect";
  public static String SERVER_REMOTE = "ServerConnect / Remote (NA)";
  public static String LOAD_DNL = "Load network (NA)";
  public static String LOAD_CSL = "CSL Restore";

  //----------------------------------------------------------------------
  /** Create a {@link Hallway} who's owner is a {@link Frame}.
   *
   * @param owner the {@link Frame} which owns this window
   *
   * @param parent the {@link ToolboxParent} to be notified of state
   * changes. Can be <tt>null</tt>.
   *
   * @param title window title
   */
  public Hallway(Frame owner, 
                 ToolboxParent parent,
                 String title)
  {
    // Set the title
    super(owner, title, parent, "Hallway_");

    // Default value is to exit. If a selection is actually chosen, then
    // the selected choice gets assigned to m_selection just before the
    // window is hidded. If the window is closed by either the exit
    // button or the window close icon, then this default value will
    // have the correct interpretation -- ie., that the user somehow
    // closed the dialog.
    m_selection = EXIT;
    
    // Build the list panel

    // ... configure the JList
    buildOptions();    
    m_list.setSelectedIndex(0);
    m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    m_optionDesc.setLineWrap(true);
    m_optionDesc.setWrapStyleWord(true);
    m_optionDesc.setEditable(false);
    m_optionDesc.setText((String) m_options.get(m_list.getSelectedValue()));

    JPanel listPanel = new JPanel(new BorderLayout());
    listPanel.add(new JScrollPane(m_list), BorderLayout.CENTER);
    listPanel.add(new JScrollPane(m_optionDesc), BorderLayout.SOUTH);
    
    // ...add borders
    listPanel.setBorder(BorderFactory.createTitledBorder(
                       "Choose startup method"));

    // Build the button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.add(m_start);
    buttonPanel.add(m_exit);

    // Add components to the root panel
    getContentPane().add(listPanel, BorderLayout.CENTER);;
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    getContentPane().add(new JPanel(), BorderLayout.WEST);
    getContentPane().add(new JPanel(), BorderLayout.EAST);
    getContentPane().add(new JPanel(), BorderLayout.NORTH);

    // Make dialog the minium size to display each element
    pack();

    // Set event managers
    m_list.addListSelectionListener(this);
    m_start.addActionListener(this);
    m_exit.addActionListener(this);
  }
  //----------------------------------------------------------------------
  /** Populate the HashMap with the options and descriptions */
  void buildOptions()
  {
    registerOption(SERVER_LOCAL, 
                   "Connect to a local NRS component using named pipes (FIFOs)");
    registerOption(SERVER_REMOTE, 
                   "Connect to a remote NRS server using TCP/IP");
    registerOption(LOAD_DNL, 
                   "Load an existing network specification");
    registerOption(LOAD_CSL, 
                   "Restore a previous CSL instead of connecting to a Server");
  }
  //----------------------------------------------------------------------
  private void registerOption(String key, String value)
  {
    m_options.put(key,value);
    m_listModel.addElement(key);
  }
  //----------------------------------------------------------------------
  public String getSelection()
  {
    return m_selection;
  }
  //----------------------------------------------------------------------
  /** Invoked when an action occurs on one of the GUI components.
   *  Inherited from ActionListener.
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_exit)
      {      
        m_selection = EXIT;
        hide();
      }
    
    if (e.getSource() == m_start)
      {
        m_selection = (String) m_list.getSelectedValue();
        hide();
      }
  }
  //----------------------------------------------------------------------
  /** Inherited from ListSelectionListener
   *
   */
  public void valueChanged(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() == false)
      {
        m_optionDesc.setText((String) 
                             m_options.get(m_list.getSelectedValue()));        
      }
  }
  //----------------------------------------------------------------------
  /**
   * Overridden the inherited method so that default size can be modified
   */
  protected void restoreDimension(Preferences props)
  {
    Dimension d = new Dimension();
    d.width = props.getInt(PREF_KEY_WIN_WIDTH, 300);
    d.height = props.getInt(PREF_KEY_WIN_HEIGHT, 400);
    setSize(d);
  }


}
