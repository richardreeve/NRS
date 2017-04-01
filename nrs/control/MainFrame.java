/*
 * Copyright (C) 2004 Edinburgh University
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.control;

import nrs.core.comms.FIFOCommsRoute;

import nrs.pml.PMLParser;
import nrs.pml.PML_Element_Registry;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;

import java.util.ArrayList;
import java.util.Properties;
import java.util.prefs.Preferences;

import nrs.toolboxes.SocketConnectionWindow;
import nrs.toolboxes.FIFOConnectionWindow;

/** GUI for Control component.
 *
 * @author Thoms French
 */
public class MainFrame {

    /** Singleton instance of <tt>MainFrame</tt> class. */
    private static MainFrame m_singletonInstance = null;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 100;
    private static final String WIN_X = "MAINF_BROSWER_DIALOG_WIN_X";
    private static final String WIN_Y = "MAINF_BROSWER_DIALOG_WIN_Y";
    private static final String WIN_WIDTH = "MAINF_BROSWER_DIALOG_WIN_WIDTH";
    private static final String WIN_HEIGHT = "MAINF_BROSWER_DIALOG_WIN_HEIGHT";

    private JPanel m_panel;
    private JFrame m_frame;
    private JMenuBar m_menuBar;
    private JMenu m_file, m_connect;
    private JMenuItem m_exit, m_fifo, m_socket;
    private AppManager m_appManager;

    private int m_unique_id = 0;

    private boolean connectedFIFOs = false;
    private boolean connectedSocket = false;

    /**
     * Return the singleton instance of class AppManager.
     *
     * @return singleton instance of class AppManager.
     */
    public static MainFrame getInstance(){
    	if (m_singletonInstance == null) new MainFrame();
	return m_singletonInstance;
    }

    /**
     * Constructor - uses singleton pattern.
     *
     * @param appManager reference to the {@link AppManager} instance
     */
    private MainFrame(){
   	if (m_singletonInstance == null) m_singletonInstance = this;

	m_appManager = AppManager.getInstance();

	m_frame = new JFrame("NRS.control");
	loadPrefs();
	
	//buildGUI();
    }

    /** Builds GUI for <code>MainFrame</code> */
    public void buildGUI(){
	m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	//Add window listener to shutdown properly
	m_frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    shutdown(0);
		}
	    });

	//build menubar, menu and menuitems
	m_menuBar = new JMenuBar();
	m_file = new JMenu("File");
	m_file.setMnemonic(KeyEvent.VK_F);

	m_connect = new JMenu("Connect");
	m_connect.setMnemonic(KeyEvent.VK_C);

	m_fifo = new JMenuItem("Local (FIFO)...", KeyEvent.VK_L);
	m_fifo.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if ( !connectedFIFOs ){
			if ( !setupLocalConnection() ){
			    return;			   
			}
			//else{
			//  fifosUp();
			//}
		    }
		}
	    });

	m_socket = new JMenuItem("Remote (TCP/IP)...", KeyEvent.VK_S);
	m_socket.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if ( !connectedSocket ){
			if ( !setupRemoteConnection() ){
			    return;			   
			}
			//else{
			//  socketsUp();
			//}
		    }
		}
	    });

	m_exit = new JMenuItem("Exit", KeyEvent.VK_E);
	m_exit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    shutdown(0);
		}
	    });
	
	//setup content pane and box layout
	m_panel = new JPanel();
	m_panel.setLayout(new BoxLayout(m_panel, BoxLayout.Y_AXIS));
	m_panel.setBorder(BorderFactory.createEmptyBorder(
						       10, //top
						       10, //left
						       10, //bottom
						       10) //right
		       );



	m_file.add(m_exit);
	m_connect.add(m_fifo);
	m_connect.add(m_socket);

	m_menuBar.add(m_file);
	m_menuBar.add(m_connect);

	m_frame.setJMenuBar(m_menuBar);
	m_frame.setContentPane(m_panel);
	m_frame.setSize(MainFrame.WIDTH,MainFrame.HEIGHT);
	m_frame.setMinimumSize(new Dimension(MainFrame.WIDTH,MainFrame.HEIGHT));
	//m_frame.setPreferredSize(new Dimension(MainFrame.WIDTH,MainFrame.HEIGHT));
	m_frame.setVisible(true);
    }

    public void setVisible(){
	m_frame.setVisible(true);
    }

    public void setTitle(String title){
	m_frame.setTitle(title);
    }

    public void addBt(JComponent b){
	m_panel.add(b);
	m_panel.add(Box.createRigidArea(new Dimension(0, 10)));
	m_frame.pack();
    }
    public void removeBt(JComponent b){
	m_panel.remove(b);

	removeBoxes();

	if ( m_panel.getComponentCount() == 0 ){
	    //m_frame.setSize(MainFrame.WIDTH,MainFrame.HEIGHT);
	    m_frame.validate();
	}
	else
	    m_frame.pack();
    }

    private void removeBoxes(){
	Component[] c = m_panel.getComponents();
	
	//only need to remove one Box component
	if ( (c[c.length-1] instanceof Box) && !(c[c.length-2] instanceof Box) ){
	    m_panel.remove(c.length-1);
	    return;
	}

	//probably need to remove all objects
	for(int i = 0; i < c.length;i++){
	    if ( !(c[i] instanceof Box) ) return;
	}
	m_panel.removeAll();
    }

    public void packFrame(){
	m_frame.pack();
    }

    private void shutdown(int i){
	PackageLogger.log.info("Application shutting down");
	savePrefs();
	System.exit(i);
    }
    
    private void savePrefs(){
	Preferences prefs = AppManager.getInstance().getPreferences();
	// Store the location and size of the unit browser toolbox
	Point p = m_frame.getLocation();
	prefs.putInt(MainFrame.WIN_X, p.x);
	prefs.putInt(MainFrame.WIN_Y, p.y);
    }

    private void loadPrefs(){
	Preferences prefs = AppManager.getInstance().getPreferences();
	Point p = new Point();
	p.x = prefs.getInt(MainFrame.WIN_X, 0);
	p.y = prefs.getInt(MainFrame.WIN_Y, 0);
	m_frame.setLocation(p);
    }

    /** Case user supplies fifos in command line arguments. */
    public void fifosUp(){
	connectedFIFOs = true;
	m_fifo.setEnabled(false);
    }
    /** Case user supplies socket commands as command line arguments. */
    public void socketsUp(){
	connectedSocket = true;
	m_socket.setEnabled(false);
    }
    //----------------------------------------------------------------------
    /**
     * Coordinate the start up of objects and processes to initiate
     * connection to local NRS component using FIFOs.
     *
     * @return <tt>true</tt> if the user selected the cancel button,
     * <tt>false</tt> otherwise or if the selected connection could not be
     * opened.
     */
    private boolean setupLocalConnection()
    {
	int ID = m_appManager.getPortManager().getFreePortID();

	Preferences prefs = m_appManager.getPreferences();

	FIFOConnectionWindow conWin = new FIFOConnectionWindow(m_frame, null);
	conWin.restoreSettings(prefs);

	conWin.setPortDescription("Local FIFO", ID, true);
	conWin.setPortParameters(prefs.get ("DEFAULT_FIFO_IN",
					    "/tmp/NRS_PML_nsim2skel"),
				 prefs.get ("DEFAULT_FIFO_OUT",
					    "/tmp/NRS_PML_skel2nsim"));
	conWin.setBrowseDir(prefs.get("DEFAULT_FIFO_DIR", "/tmp"));
	conWin.setModal(true);

	int portNumber;
	boolean enterAgain = true;

	while (enterAgain)
	    {
		// show dialog
		conWin.setVisible(true);

		// immediately save its new position
		conWin.saveSettings(prefs);

		if (conWin.getReturnValue() == FIFOConnectionWindow.CANCEL)
		    {
			enterAgain = false;
		    }		else if (conWin.getReturnValue() == FIFOConnectionWindow.OK)
		    {
			try
			    {
				portNumber = conWin.getPortNumber();
				enterAgain = false;

				// save these settings
				prefs.put("DEFAULT_FIFO_IN", conWin.getInbound());
				prefs.put("DEFAULT_FIFO_OUT", conWin.getOutbound());
				prefs.put("DEFAULT_FIFO_DIR", conWin.getBrowseDir());
			    }
			catch (NumberFormatException e){
			    JOptionPane.showMessageDialog(null,
							  "\"Port Number\" must a number",
							  "Invalid entry",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
	    }
	
	if (conWin.getReturnValue() == FIFOConnectionWindow.OK)
	    {
		try
		    {
			m_appManager.getPortManager().
			    openFifoPml(conWin.getInbound(), 
					conWin.getOutbound());

			return true;
		    }
		catch (Exception e)
		    {
			e.printStackTrace();
		    }
	    }
	return false;
    }

    //----------------------------------------------------------------------
    /**
     * Coordinate the start up of objects and processes to initiate
     * a remote connection to other NRS component using TCP/IP
     *
     * @return <tt>true</tt> if the user selected the cancel button,
     * <tt>false</tt> otherwise or if the selected connection could not be
     * opened.
     */
    private boolean setupRemoteConnection()
    {
	int ID = m_appManager.getPortManager().getFreePortID();
	Preferences prefs = m_appManager.getPreferences();

	SocketConnectionWindow socWin = new 
	    SocketConnectionWindow(m_frame, null, false);
	socWin.restoreSettings(prefs);

	socWin.setPortDescription("Remote Socket", ID, true);
	socWin.setSocketParameters(prefs.get ("DEFAULT_REMOTE_HOST", 
					      "localhost"),
				 prefs.get ("DEFAULT_PORT", "5000"));
	socWin.setModal(true);

	int portNumber;
	boolean enterAgain = true;

	while (enterAgain)
	    {
		// show dialog
		socWin.setVisible(true);

		// immediately save its new position
		socWin.saveSettings(prefs);

		if (socWin.getReturnValue() == SocketConnectionWindow.CANCEL)
		    {
			enterAgain = false;
		    }
		else if (socWin.getReturnValue() == SocketConnectionWindow.OK)
		    {
			try
			    {
				portNumber = socWin.getPortNumber();
				enterAgain = false;

				// save these settings
				prefs.put("DEFAULT_REMOTE_HOST", 
					  socWin.getRemoteHost());
				prefs.put("DEFAULT_PORT", 
					  socWin.getRemotePort());
			    }
			catch (NumberFormatException e){
			    JOptionPane.showMessageDialog(null,
							  "\"Port Number\" must a number",
							  "Invalid entry",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
	    }
	
	if (socWin.getReturnValue() == SocketConnectionWindow.OK)
	    {
		try
		    {
			if ( socWin.isClient() )
			    m_appManager.getPortManager().openClient(ID, 
								     socWin.getRemoteHost(), Integer.parseInt(socWin.getRemotePort()));
			else
			    m_appManager.getPortManager().openServer(Integer.parseInt(socWin.getRemotePort()));
								     

			return true;
		    }
		catch (Exception e)
		    {
			e.printStackTrace();
		    }
	    }
	return false;
    }
    
}
