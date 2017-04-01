//$Id: STMainFrame.java,v 1.3 2005/05/09 22:05:12 hlrossano Exp $
package nrs.tracker.stickgui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import nrs.toolboxes.LogWindow;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import nrs.tracker.jointmanager.JointWindow;
import nrs.tracker.palette.PaletteWindow;
import nrs.tracker.palette.ScanWindow;

public class STMainFrame extends JFrame implements ToolboxParent
{
  /// The windows
  private PaletteWindow m_palWin;
  private ScanWindow m_scanWin;
  private LogWindow m_logWin;
  private FindWindow m_findWin;
  private DisplayWindow m_displayWin;
  private JointWindow m_jointWin;

  // Main menu bar
  private JMenuBar m_menuBar = new JMenuBar();

  // Menus
  private JMenu m_menuFile = new JMenu();
  private JMenu m_menuView = new JMenu();
  private JMenu m_menuWindows = new JMenu();

  // Menu options
  private final String EXIT = "Exit";
  private final String VIEW = "View";
  private final String WINDOWS = "Windows";
  private final String OPEN_IM = "Open Image...";
  private final String CLOSE_IM = "Close";

  // Link to the application manager of the program
  private STAppManager m_appManager;

  // CANVAS
  private MyCanvas m_canvas;
  private final JFileChooser m_fc;

  ///// Name of entries in the properties file
  private static final String PREF_KEY_CURR_DIR = "STMAINFRAME_CURR_DIR";
  public static String WIN_X = "MAINFRAME_BROSWER_DIALOG_WIN_X";
  public static String WIN_Y = "MAINFRAME_BROSWER_DIALOG_WIN_Y";
  public static String WIN_WIDTH = "MAINFRAME_BROSWER_DIALOG_WIN_WIDTH";
  public static String WIN_HEIGHT = "MAINFRAME_BROSWER_DIALOG_WIN_HEIGHT";
  public static String WIN_VISIBLE = "MAINFRAME_BROSWER_DIALOG_WIN_VISIBLE";

  // List of toolboxes registered with self. Each object can be cast to
  // a Toolbox type. 
  private final ArrayList m_toolboxes = new ArrayList();;

  //-------------------------------------------------------------------------
  /**
   * Construct the main frame for the application represented by {@link
   * STAppManager}
   **/
  public STMainFrame(STAppManager appManager)
  {
    m_appManager = appManager;

    nrs.tracker.palette.ExampleFileFilter filter = new nrs.tracker.palette.ExampleFileFilter();
    filter.addExtension("jpg");
    filter.setDescription("Images");

	m_fc = new JFileChooser();
	m_fc.setFileFilter(filter);

    // create windows
    m_palWin = new PaletteWindow(this, this);
    m_scanWin = new ScanWindow(this, this);
    m_logWin = new LogWindow(this, this);
    m_findWin = new FindWindow(this, this);
	m_displayWin = new DisplayWindow(this, this);
	m_jointWin = new JointWindow(this, this);

    // register them for persistance
    m_appManager.persistWindowLater(m_palWin);
    m_appManager.persistWindowLater(m_scanWin);
    m_appManager.persistWindowLater(m_logWin);
    m_appManager.persistWindowLater(m_findWin);
    m_appManager.persistWindowLater(m_displayWin);
    m_appManager.persistWindowLater(m_jointWin);
	

    // restore their settings
    m_palWin.restoreSettings(m_appManager.getPreferences());
    m_scanWin.restoreSettings(m_appManager.getPreferences());
    m_logWin.restoreSettings(m_appManager.getPreferences());
    m_findWin.restoreSettings(m_appManager.getPreferences());
	m_displayWin.restoreSettings(m_appManager.getPreferences());
	m_jointWin.restoreSettings(m_appManager.getPreferences());

    // register the log window to receive logging events
    Logger.getLogger("nrs").addHandler(m_logWin.getHandler());

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
      {
        initialiseFrame();
      }
    catch(Exception e)
      {
        e.printStackTrace();
      }

  }  
  //-------------------------------------------------------------------------
  /**
   * Provide a log window for with the main window.
   */
  public void setLogWindow(LogWindow logWindow)
  {
    // Now update menus to reflect changes
    updateMenus();
  }
  //-------------------------------------------------------------------------
  /* Component initialisation */
  void initialiseFrame() throws Exception
  {
    /* Components of the GUI */
    JPanel contentPane = (JPanel) this.getContentPane();
	contentPane.setLayout(new BorderLayout());
	
	m_canvas = new MyCanvas(this);
	m_canvas.setBackground(java.awt.Color.white);
	contentPane.add(m_canvas, BorderLayout.CENTER);

    this.setSize(new Dimension(400, 300));
    this.setTitle(STAppManager.NAME);

    populateViewMenu();

    m_menuFile.setText("File");
    m_menuWindows.setText(WINDOWS);
    m_menuView.setText(VIEW);

    m_menuView.add(m_menuWindows);

    m_menuBar.add(m_menuFile);
    m_menuBar.add(m_menuView);
    this.setJMenuBar(m_menuBar);

    // Configure the main menu
    m_menuFile.add(makeMenuItem(OPEN_IM));
    m_menuFile.add(makeMenuItem(CLOSE_IM));
	m_menuFile.addSeparator();
    m_menuFile.add(makeMenuItem(EXIT));

  }
  //-------------------------------------------------------------------------
  public PaletteWindow getPalWin(){
	return m_palWin;
  }
  //-------------------------------------------------------------------------
  public ScanWindow getScanWin(){
	return m_scanWin;
  }
  //-------------------------------------------------------------------------
  public LogWindow getLogWin(){
	return m_logWin;
  }
  //-------------------------------------------------------------------------
  public FindWindow getFindWin(){
	return m_findWin;
  }
  //-------------------------------------------------------------------------
  public DisplayWindow getDisplayWin(){
	return m_displayWin;
  }
  
  public JointWindow getJointWin(){
  	return m_jointWin;
  }
  //-------------------------------------------------------------------------
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e)
  {
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
      {
		m_canvas.exit();
        m_appManager.shutdown(0);
      }

    // I have deliberately placed this after the call to
    // m_appManager.shutdown(0); because this way the shutdown(...)
    // routine first saves the correct applicaton state to the
    // properties file. If I do this the other way around, then when
    // first calling processWindowEvent, the subwindows belonging to
    // this Frame are close - hence they are no longer visible, and
    // because of this the visibility state which always get stored for
    // those windows in the properties files is always false - even if
    // they were visible just before application shutdown.
    super.processWindowEvent(e);
  }
  //-------------------------------------------------------------------------
  /**
   * Handle a menu event
   */
  private void handleMenuEvent(ActionEvent e)
  {
    JMenuItem source = (JMenuItem)(e.getSource());
    
    // Defensive programming
    if (source == null) return;

    if (source.getText() == EXIT)
      {
        m_appManager.shutdown(0);
        return;
      }
    if (source.getText() == CLOSE_IM)
      {
        m_canvas.removeImg();
        return;
      }
    if (source.getText() == OPEN_IM)
      {
		if (m_fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
			String _filename = m_fc.getSelectedFile().getAbsolutePath();
		if (_filename.length() == 0) return;
			m_canvas.setImage(_filename);
        return;
      }    
    // Now see if the action event is related to one of the toolboxes
    for (Iterator i = m_toolboxes.iterator(); i.hasNext(); )
    {
      Toolbox tb = (Toolbox) i.next();
      if (source.getText() == tb.getTitle())
      {
        tb.setVisible(!tb.isVisible());
        updateMenus();
        return;
      }
    } 
  }
  //-------------------------------------------------------------------------
  /**
   * Utility method to turn a menu item string (the parameter) into a
   * {@link JMenuItem} which displays that string and has a callback to
   * this class registered.
   */
  private JMenuItem makeMenuItem(String menuLabel)
  {
    JMenuItem newItem = new JMenuItem(menuLabel);

    newItem.addActionListener(
                              new ActionListener()
                              {
                                public void actionPerformed(ActionEvent e)
                                { handleMenuEvent(e); }
                              });

    return newItem;
  }
  //-------------------------------------------------------------------------
  /**
   * Utility method to represent a dialog (the parameter) into a {@link
   * JCheckBoxMenuItem} which displays the dialog title and has a
   * callback to this class registered.
   */ 
  private JCheckBoxMenuItem makeCBMenuItem(JDialog dialog)
  {
    JCheckBoxMenuItem newItem = new JCheckBoxMenuItem(dialog.getTitle(),
                                                      dialog.isVisible());

    newItem.addActionListener(
                              new ActionListener()
                              {
                                public void actionPerformed(ActionEvent e)
                                { handleMenuEvent(e); }
                              });

    return newItem;
  }
  //----------------------------------------------------------------------
  /** 
   * Add options to the windows menu depending on whether corresponding
   * components are currently visible or not.
   **/
  private void populateViewMenu()
  {
    m_menuWindows.removeAll(); // we will do a complete rebuild

    for (Iterator i = m_toolboxes.iterator(); i.hasNext(); )
    {
      Toolbox tb = (Toolbox) i.next();

      m_menuWindows.add(makeCBMenuItem(tb));
    }
  }
  //----------------------------------------------------------------------
  /**
   * Instruct the main window to update its menus, to take into effect
   * any changes in availability or state in dependent components.
   */
  public void updateMenus()
  {
    populateViewMenu();
  }
  //----------------------------------------------------------------------
  /** 
   * Implement {@link ToolboxParent} interface
   */
  public void toolboxVisibilityChanged(Toolbox toolbox)
  {
    updateMenus();
  }
  //----------------------------------------------------------------------
  /** 
   * Persist window size and position values to the supplied {@link
   * java.util.prefs.Preferences} object.
   */
  public void saveSettings(Preferences props)
  {
    // Store the location and size of the unit browser toolbox
    Point p = getLocation();
    props.putInt(WIN_X, p.x);
    props.putInt(WIN_Y, p.y);

    Dimension d = getSize();
    props.putInt(WIN_WIDTH, d.width);
    props.putInt(WIN_HEIGHT, d.height);

	 props.put(PREF_KEY_CURR_DIR,
              m_fc.getCurrentDirectory().getAbsolutePath());
  }
  //----------------------------------------------------------------------
  /** 
   * Restore window size and position values from the supplied {@link
   * java.util.prefs.Preferences} object.
   */
  public void restoreSettings(Preferences props)
  {
    Point p = new Point();
    p.x = props.getInt(WIN_X, 0);
    p.y = props.getInt(WIN_Y, 0);
    setLocation(p);

    Dimension d = new Dimension();
    d.width = props.getInt(WIN_WIDTH, 300);
    d.height = props.getInt(WIN_HEIGHT, 200);
    setSize(d);

	String path = props.get(PREF_KEY_CURR_DIR, "");
	if (path != "") m_fc.setCurrentDirectory(new java.io.File(path));
  }
  //----------------------------------------------------------------------
  /** Implement {@link ToolboxParent} interface. The object is added to
   * this class, so that the integration of the {@link Toolbox} to self
   * is automated. */
  public void toolboxAdded(Toolbox toolbox)
  {
    if ((toolbox == null) || (m_toolboxes.contains(toolbox))) return;
    m_toolboxes.add(toolbox);
  }
}
