/* $Id: AppManager.java,v 1.1 2004/11/15 09:47:01 darrens Exp $ */
package nrs.tracker.robottrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import nrs.toolboxes.LogWindow;
import nrs.toolboxes.Toolbox;

/**
 * This class control application resources and and object
 * interation. It is designed according to the singleton pattern.
 *
 * @author  Darren Smith
 */
public class AppManager 
{
  /** The version number of the application. */
  public static final double VERSION = 0.1;
  
  /** Name of the application. */
  public static final String NAME = "NRS/RobotTrackerGUI";

  // Class logger
  private Logger m_log = Logger.getLogger("nrs.tracker.robottrack");

  // References to the class in which usstaer and system preferences will
  // be stored
  private Preferences m_prefs;
  
  /// The main window
  private RTMainFrame m_mainFrame;

  /// The log window
  private LogWindow m_logWin;

  /// List of windows to store settings for upon program termination
  private Collection m_windowsToPersist;

  /// Store the only instance of this class to be created
  private static AppManager m_singletonInstance = null;

  //----------------------------------------------------------------------
  /**
   * Return the singleton instance of this class
   */
  static public AppManager getInstance()
  {
    if (m_singletonInstance == null) m_singletonInstance = new AppManager();

    return m_singletonInstance;
  }
  //----------------------------------------------------------------------
  /**
   * The new improved constructor...
   *
   */
  private AppManager()
  {
    // Initialise data members first
    m_windowsToPersist = new ArrayList();

    configureLogging();
    initialisePreferences();

    // The main window, and the logging window, need to be created
    // early, so that early log records are not missed. They won't
    // initially be displayed however, but they will be stored within
    // the text component within the log 
    m_mainFrame = new RTMainFrame(this);
    m_logWin = new LogWindow(m_mainFrame, m_mainFrame);
    m_mainFrame.setLogWindow(m_logWin);

    Logger nrsLogger = Logger.getLogger("nrs");
    nrsLogger.addHandler(m_logWin.getHandler());
    
    persistWindowLater(m_logWin);
  }
  //----------------------------------------------------------------------
  /**
   * Start the application, including the display of the main window.
   *
   * /param args array of commandline arguments passed to application
   */
  public void startup(String[] args)
  {
    m_logWin.restoreSettings(m_prefs);
    m_mainFrame.restoreSettings(m_prefs);
    m_mainFrame.setVisible(true);
  }
  //---------------------------------------------------------------------
  /**
   * Return the appications preferences object. Client should use the
   * returned object to stored state between sessions (such as default
   * choices, positions of windows, font colours etc).
   */
  public Preferences getPreferences() 
  { 
    return m_prefs; 
  }
  //----------------------------------------------------------------------
  /** Configure the NRS namespace logger, and also the root logger.  The
   * NRS logger is detached away from the root ("") logger - i.e., will
   * not use any root handlers. Handlers are added to the NRS namespace
   * logger with logging level set to Level.ALL. The NRS namespace
   * logger is also set to Level.ALL. */
  private void configureLogging()
  {
    // Get hold of the namespace logger for "nrs"
    Logger nrsLogger = Logger.getLogger("nrs"); 

    // Remove any default handlers they may exist of the application logger
    Handler[] defaultHandlers = nrsLogger.getHandlers();
    for (int i = 0; i < defaultHandlers.length; i++)
      {
        System.out.println("Removing default handler...");
        nrsLogger.removeHandler(defaultHandlers[i]);
      }
    
    // Don't want to use parent handlers. Will assume control in this
    // class.
    nrsLogger.setUseParentHandlers(false);

    // Set initial level of the application logger
    nrsLogger.setLevel(Level.FINE);

    // Add a hanlder. Set the level to ALL, since we will control level
    // through the loggers and not through the handlers
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.ALL);
    nrsLogger.addHandler(ch);

    // Get hold of the namespace logger for "nrs.tracker"
    nrsLogger = Logger.getLogger("nrs.tracker.robottrack");
    nrsLogger.setLevel(null);
    nrsLogger.setUseParentHandlers(true);
  }
  //-------------------------------------------------------------------------
  // This is the routine called to terminate the application cleanly
  public void shutdown(int i)
  {
    m_log.info("Application shutting down");
    
    for (Iterator iter = m_windowsToPersist.iterator();
         iter.hasNext(); )
    {
      Object next = iter.next();

      if (next instanceof Toolbox)
      {
        Toolbox toolbox = (Toolbox) next;
        toolbox.saveSettings(m_prefs);
      }
    }
    
    // m_mainFrame is not an instance of Toolbox, so do manually
    m_mainFrame.saveSettings(m_prefs);

    System.exit(i);
  }
  //----------------------------------------------------------------------
  void persistWindowLater(Toolbox t)
  {
    m_windowsToPersist.add(t);
  }   
  //----------------------------------------------------------------------
  private void initialisePreferences()
  {
    m_log.fine("Retrieving preferences for node=" + getClass());
    m_prefs = Preferences.userNodeForPackage(getClass());
  }
}
