// $Id: FIFOConnectionWindow.java,v 1.1 2005/06/14 17:05:20 s0125563 Exp $
package nrs.toolboxes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.io.File;

/**
 * Dialog window to allow user to enter the parameters for a FIFO connection
 */
public class FIFOConnectionWindow extends Toolbox implements ActionListener
{
  /** Dialog closed value if OK pressed */
  public static final String OK = "OK";

  /** Dialog closed value if Cancel pressed */
  public static final String CANCEL = "Cancel";

  ///// GUI components
  private JTextField m_name = new JTextField("name");
  private JTextField m_port = new JTextField("port");
  private JTextField m_in = new JTextField("inbound");
  private JTextField m_out = new JTextField("outbound");
  private JButton m_browseIn = new JButton("Browse");
  private JButton m_browseOut = new JButton("Browse");
  private JButton m_ok = new JButton(OK);
  private JButton m_cancel = new JButton(CANCEL);
  private JFileChooser m_chooser;

  /** Title of the dialog */
  public static String TITLE = "Open Local Connection";

  private String m_retVal = CANCEL;

  private String m_currentDir;

  //----------------------------------------------------------------------
  /**
   * Create a {@link FIFOConnectionWindow} who's owner is a {@link Dialog}.
   *
   * @param owner the {@link Dialog} which owns this window
   *
   * @param parent the {@link ToolboxParent} to be notified of state
   * changes. Can be <tt>null</tt>.
   */
  public FIFOConnectionWindow(Dialog owner, ToolboxParent parent)
  {
    super((Dialog) owner, TITLE, parent, "FIFOConnectionWindow_");
    init();
  }
  //----------------------------------------------------------------------
  /**
   * Create a {@link FIFOConnectionWindow} who's owner is a {@link Frame}.
   *
   * @param owner the {@link Frame} which owns this window
   *
   * @param parent the {@link ToolboxParent} to be notified of state
   * changes. Can be <tt>null</tt>.
   */
  public FIFOConnectionWindow(Frame owner, ToolboxParent parent)
  {
    super((Frame) owner, TITLE, parent, "FIFOConnectionWindow_");
    init();
  }
  //----------------------------------------------------------------------
  /**
   * Get the return value of a recently closed dialog
   *
   * @see #OK
   * @see #CANCEL
   */
  public String getReturnValue()
  {
    return m_retVal;
  }
  //----------------------------------------------------------------------
  private void init()
  {
    m_browseIn.addActionListener(this);
    m_browseOut.addActionListener(this);
    m_ok.addActionListener(this);
    m_cancel.addActionListener(this);

    ////////// Configure the north panel
    JPanel north = new JPanel(new GridLayout(1,2));
    JPanel northFirst = new JPanel(new BorderLayout());
    JPanel northSecond = new JPanel(new BorderLayout());
    north.add(northFirst);
    north.add(northSecond);
    northFirst.add(new JLabel("Name "), BorderLayout.WEST);
    northFirst.add(m_name, BorderLayout.CENTER);
    northSecond.add(new JLabel("   Port Number "), BorderLayout.WEST);
    northSecond.add(m_port, BorderLayout.CENTER);
    north.setBorder(
     BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5,5,5,5),
        BorderFactory.createTitledBorder(
                                     BorderFactory.createEtchedBorder()
                                     ,"Port description")));
    ////////// Configure the center panel
    JPanel incoming = new JPanel(new BorderLayout());
    incoming.add(new JLabel("Inbound File "), BorderLayout.WEST);
    incoming.add(m_in, BorderLayout.CENTER);
    incoming.add(m_browseIn, BorderLayout.EAST);
    JPanel outgoing = new JPanel(new BorderLayout());
    outgoing.add(new JLabel("Outbound File "), BorderLayout.WEST);
    outgoing.add(m_browseOut, BorderLayout.EAST);
    outgoing.add(m_out, BorderLayout.CENTER);
    JPanel error = new JPanel(new BorderLayout());
    LayerPanel center = new LayerPanel(incoming);
    center.addRow(outgoing);
    center.setBorder(
     BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5,5,5,5),
        BorderFactory.createTitledBorder(
                                     BorderFactory.createEtchedBorder()
                                     ,"Parameters")));

    ////////// Configure the south panel
    JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    south.add(m_ok);
    south.add(m_cancel);

    ////////// Configure the root panel
    JPanel root = (JPanel) this.getContentPane();
    root.setLayout(new BorderLayout());

    root.add(north, BorderLayout.NORTH);
    root.add(center, BorderLayout.CENTER);
    root.add(south, BorderLayout.SOUTH);

    this.pack();
  }
  //----------------------------------------------------------------------
  /**
   * Set the directory where the browse dialog will start at
   */
  public void setBrowseDir(String dir)
  {
    m_currentDir = dir;

    if (m_chooser == null) m_chooser = new JFileChooser();

    m_chooser.setCurrentDirectory(new File(dir));
  }
  //----------------------------------------------------------------------
  /**
   * Get the current-directory that the browse dialog is using (so it
   * can be perhaps saved for future use)
   */
  public String getBrowseDir()
  {
    if (m_chooser == null) m_chooser = new JFileChooser();
    
    return m_chooser.getCurrentDirectory().getAbsolutePath();
  }
  //----------------------------------------------------------------------
  /**
   * Set port description, and optionally lock
   *
   * @param lock set to <tt>true</tt> to prevent the name and port from
   * being changed by the user.
   */
  public void setPortDescription(String name, int port, boolean lock)
  {
    m_name.setText(name);
    m_port.setText(Integer.toString(port));

    m_name.setEditable(!lock);
    m_port.setEditable(!lock);
  }
  //----------------------------------------------------------------------
  /**
   * Set port parameters
   *
   */
  public void setPortParameters(String inbound, String outbound)
  {
    m_in.setText(inbound);
    m_out.setText(outbound);
  }
  //----------------------------------------------------------------------
  /** Invoked when an action occurs on one of the GUI components. */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_ok)
    {
      m_retVal = OK;
      setVisible(false);
      return;
    }
    if (e.getSource() == m_cancel)
    {
      m_retVal = CANCEL;
      setVisible(false);
      return;
    }
    if (e.getSource() == m_browseIn)
    {
      if (m_chooser == null) m_chooser = new JFileChooser();
      m_chooser.setDialogTitle("Select Inbound File");
      int returnVal = m_chooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
        m_in.setText(m_chooser.getSelectedFile().getAbsolutePath());
      }
      return;
    }
    if (e.getSource() == m_browseOut)
    {
      if (m_chooser == null) m_chooser = new JFileChooser();
      m_chooser.setDialogTitle("Select Outbound File");
      int returnVal = m_chooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
        m_out.setText(m_chooser.getSelectedFile().getAbsolutePath());
      }
      return;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Get the value of the port name
   */
  public String getName()
  {
    return m_name.getText();
  }
  //----------------------------------------------------------------------
  /**
   * Get the value of the port number
   */
  public int getPortNumber() throws NumberFormatException
  {
    return Integer.parseInt(m_port.getText());
  }
  //----------------------------------------------------------------------
  /**
   * Get the value of the inbound file
   */
  public String getInbound()
  {
    return m_in.getText();
  }
  //----------------------------------------------------------------------
  /**
   * Get the value of the outbound file
   */
  public String getOutbound()
  {
    return m_out.getText();
  }
}
