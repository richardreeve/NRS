// $Id: RTMainFrame.java,v 1.2 2005/04/29 11:21:03 hlrossano Exp $
package nrs.tracker.robottrack;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.imageio.ImageIO;
import nrs.toolboxes.LogWindow;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import nrs.tracker.palette.PaletteWindow;
import nrs.tracker.palette.ScanWindow;
import nrs.tracker.palette.ExampleFileFilter;


public class RTMainFrame extends JFrame implements ToolboxParent
{
  /// The palette window
  private PaletteWindow m_palWin;

  /// The scan window
  private ScanWindow m_scanWin;
  
  /// The calibration window
  private CalibrationWindow m_calibWin;

  // Main menu bar
  private JMenuBar m_menuBar = new JMenuBar();
  
  // The canvases (for different purposes) // just one at the moment
  private CardLayout m_canvasManager;
  private CalibrationCanvas m_calCanvas = new CalibrationCanvas();
  private static String MODE_CALIBRATION = "Camera calibration mode";

  // File chooser for image in frame
  private JFileChooser m_fileChooser;

  
  // Menus
  private JMenu m_menuFile = new JMenu();
  private JMenu m_menuView = new JMenu();
  private JMenu m_menuWindows = new JMenu();

  // Menu options
  private final String EXIT = "Exit";
  private final String VIEW = "View";
  private final String WINDOWS = "Windows";
  private final String OPEN_IM = "Open image...";

  // Link to the application manager of the program
  private AppManager m_appManager;

  /// Logging window
  private LogWindow m_logWindow;

  ///// Name of entries in the properties file
  public static String WIN_X = "MAINFRAME_BROSWER_DIALOG_WIN_X";
  public static String WIN_Y = "MAINFRAME_BROSWER_DIALOG_WIN_Y";
  public static String WIN_WIDTH = "MAINFRAME_BROSWER_DIALOG_WIN_WIDTH";
  public static String WIN_HEIGHT = "MAINFRAME_BROSWER_DIALOG_WIN_HEIGHT";
  public static String WIN_VISIBLE = "MAINFRAME_BROSWER_DIALOG_WIN_VISIBLE";
  public static String CURRENT_DIR = "MAINFRAME_BROWSER_CURRENT_DIRECTORY";

  //-------------------------------------------------------------------------
  /**
   * Construct the main frame for the application represented by {@link
   * AppManager}
   **/
  public RTMainFrame(AppManager appManager)
  {
    m_appManager = appManager;

    m_palWin = new PaletteWindow(this, this);
    m_scanWin = new ScanWindow(this, this);
	m_calibWin = new CalibrationWindow(this, this, m_calCanvas);
    m_appManager.persistWindowLater(m_palWin);
    m_appManager.persistWindowLater(m_scanWin);
	m_appManager.persistWindowLater(m_calibWin);
    m_palWin.restoreSettings(m_appManager.getPreferences());
    m_scanWin.restoreSettings(m_appManager.getPreferences());
	m_calibWin.restoreSettings(m_appManager.getPreferences());
	

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    //try
    // {
        initialiseFrame();
    //  }
    //catch(Exception e)
    //  {
    //    e.printStackTrace();
    //  }
  }  
  //-------------------------------------------------------------------------
  /**
   * Provide a log window for with the main window.
   */
  public void setLogWindow(LogWindow logWindow)
  {
    m_logWindow = logWindow;

    // Now update menus to reflect changes
    updateMenus();
  }
  //-------------------------------------------------------------------------
  
  /** Initialize the UI in the frame. */
  void initialiseFrame()
  {
    // Title
	this.setTitle("NRS/TrackerGUI");

    // Content
	m_canvasManager = new CardLayout();
	JPanel contentPane = new JPanel(m_canvasManager);
	this.setContentPane(contentPane);
	
	contentPane.add(m_calCanvas, MODE_CALIBRATION);
	//done by .add automatically?  m_canvasManager.addLayoutComponent(m_calCanvas, MODE_CALIBRATION);		// show with canvasManager.show(getContentPane(),MODE_CALIBRATION)
    
	// Menu
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
	m_menuFile.add(new JSeparator());
    m_menuFile.add(makeMenuItem(EXIT));
	
	// Set up the file chooser
	m_fileChooser = new JFileChooser();
	m_fileChooser.addChoosableFileFilter(new ExampleFileFilter(new String[]{"jpg", "jpeg", "gif"}, "Image files"));

  }
  //-------------------------------------------------------------------------
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e)
  {
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
      {
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
    
    if (source.getText() == LogWindow.TITLE)
    {
      m_logWindow.setVisible(!m_logWindow.isVisible());
      updateMenus();
      return;
    }
    
    if (source.getText() == PaletteWindow.TITLE)
    {
      m_palWin.setVisible(!m_palWin.isVisible());
      updateMenus();
      return;
    }
    
    if (source.getText() == ScanWindow.TITLE)
    {
      m_scanWin.setVisible(!m_scanWin.isVisible());
      updateMenus();
      return;
    }
	
    if (source.getText() == CalibrationWindow.TITLE)
    {
      m_calibWin.setVisible(!m_calibWin.isVisible());
      updateMenus();
      return;
    }
	
	if (source.getText() == OPEN_IM)
    {
		// Open file chooser dialog
		if (m_fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
	  	{
			try
			{
				// Open image and show in canvas
				BufferedImage img = ImageIO.read(new FileInputStream(m_fileChooser.getSelectedFile()));
	        	m_calCanvas.setImage(img);
			}
			catch (IOException exception)
			{
				// FileInputStream constructor throws FileNotFoundException
				// ImageIO.read() throws IOException (superclass of FileNotFoundException)
				
				// create log entry!?
			}
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
    
    if (m_logWindow != null)
    {
      m_menuWindows.add(makeCBMenuItem(m_logWindow));
    }
    
    if (m_palWin != null)
    {
      m_menuWindows.add(makeCBMenuItem(m_palWin));
    }
    
    if (m_scanWin != null)
    {
      m_menuWindows.add(makeCBMenuItem(m_scanWin));
    }
	
	if (m_calibWin != null)
	{
	  m_menuWindows.add(makeCBMenuItem(m_calibWin));
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
	
	// Store directory for open file
	props.put(CURRENT_DIR, m_fileChooser.getCurrentDirectory().getAbsolutePath());

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
	
	String path = props.get(CURRENT_DIR, "");
	if (path != "") m_fileChooser.setCurrentDirectory(new java.io.File(path));
  }
  //----------------------------------------------------------------------
  /** Implement {@link ToolboxParent} interface. The object is added to
   * this class, so that the integration of the {@link Toolbox} to self
   * is automated. */
  public void toolboxAdded(Toolbox toolbox)
  {
    // TODO - implement this function properly - see how its done in the
    // MainFrame for NRS
  }
}
