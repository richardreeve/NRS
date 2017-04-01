// $Id: SocketConnectionWindow.java,v 1.5 2005/06/23 11:12:02 s0125563 Exp $
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
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.io.File;

/**
 * Dialog window to allow user to enter the parameters for a Socket connection
 */
public class SocketConnectionWindow extends Toolbox implements ActionListener
{
    /** Dialog closed value if OK pressed */
    public static final String OK = "OK";
    
    /** Dialog closed value if Cancel pressed */
    public static final String CANCEL = "Cancel";
    
    //GUI components
    private JTextField m_name = new JTextField("name");
    private JTextField m_port = new JTextField("port");
    private JTextField m_host = new JTextField("host");
    private JTextField m_portNum = new JTextField("socket port");
    //private JButton m_browseIn = new JButton("Browse");
    //private JButton m_browseOut = new JButton("Browse");
    private JButton m_ok = new JButton(OK);
    private JButton m_cancel = new JButton(CANCEL);
    //private JFileChooser m_chooser;
    private JRadioButton m_server = new JRadioButton("Server");
    private JRadioButton m_client = new JRadioButton("Client");
    private ButtonGroup m_group = new ButtonGroup();
    
    /** Title of the dialog */
    public static String TITLE = "Open Remote Connection";
    
    private String m_retVal = CANCEL;
    
    private boolean m_defaultServer = false;
    
  //----------------------------------------------------------------------
  /**
   * Create a {@link SocketConnectionWindow} who's owner is a {@link Dialog}.
   *
   * @param owner the {@link Dialog} which owns this window
   *
   * @param parent the {@link ToolboxParent} to be notified of state
   * changes. Can be <tt>null</tt>.
   */
    public SocketConnectionWindow(Dialog owner, ToolboxParent parent, boolean defaultServer)
  {
    super((Dialog) owner, TITLE, parent, "SocketConnectionWindow_");
    m_defaultServer = defaultServer;
    init();
  }
  //----------------------------------------------------------------------
  /**
   * Create a {@link SocketConnectionWindow} who's owner is a {@link Frame}.
   *
   * @param owner the {@link Frame} which owns this window
   *
   * @param parent the {@link ToolboxParent} to be notified of state
   * changes. Can be <tt>null</tt>.
   */
    public SocketConnectionWindow(Frame owner, ToolboxParent parent, boolean defaultServer)
  {
    super((Frame) owner, TITLE, parent, "SocketConnectionWindow_");

    m_defaultServer = defaultServer;
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
      if ( m_defaultServer ){
	  m_server.setSelected(true);
	  m_host.setEditable(false);
      }
      else
	  m_client.setSelected(true);

      //m_browseIn.addActionListener(this);
      //m_browseOut.addActionListener(this);
      m_server.addActionListener(this);
      m_client.addActionListener(this);
      m_ok.addActionListener(this);
      m_cancel.addActionListener(this);
      
      //add radiobuttons to buttonGroup
      m_group.add(m_server);
      m_group.add(m_client);

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
      JPanel role = new JPanel(new BorderLayout());
      role.add(new JLabel("Role "), BorderLayout.WEST);
      JPanel buttons = new JPanel();
      buttons.add(m_client);
      buttons.add(m_server);
      role.add(buttons, BorderLayout.CENTER);

      JPanel hostname = new JPanel(new BorderLayout());
      hostname.add(new JLabel("Remote Hostname "), BorderLayout.WEST);
      hostname.add(m_host, BorderLayout.CENTER);
     
      JPanel portnumber = new JPanel(new BorderLayout());
      portnumber.add(new JLabel("Remote Port "), BorderLayout.WEST);
      portnumber.add(m_portNum, BorderLayout.CENTER);

      //JPanel error = new JPanel(new BorderLayout());
      LayerPanel center = new LayerPanel(role);
      center.addRow(hostname);
      center.addRow(portnumber);
      center.setBorder(BorderFactory.createCompoundBorder(
							  BorderFactory.createEmptyBorder(5,5,5,5),
							  BorderFactory.createTitledBorder(
											   BorderFactory.createEtchedBorder(),"Parameters")));
      
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
     * Set socket parameters
     *
     */
    public void setSocketParameters(String host, String port)
    {
	m_host.setText(host);
	m_portNum.setText(port);
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
	if (e.getSource() == m_server)
	    {
		m_host.setEditable(false);
		return;
	    }
	if (e.getSource() == m_client)
	    {
		m_host.setEditable(true);
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
     * Get the name of the remote host
     */
    public String getRemoteHost()
    {
	return m_host.getText();
    }
    //----------------------------------------------------------------------
    /**
     * Get the value of the remote port
     */
    public String getRemotePort()
    {
	return m_portNum.getText();
    }

    public boolean isClient(){
	boolean b = false;
	if ( m_client.isSelected() )
	    b = true;
	return b;
    }
}
