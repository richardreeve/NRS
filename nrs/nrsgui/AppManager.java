package nrs.nrsgui;

import java.awt.*;
import java.io.*;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.JOptionPane;
import nrs.core.base.BroadcastHandler;
import nrs.core.base.InboundPipeline;
import nrs.core.base.MessageIDStamp;
import nrs.core.base.MessageStorage;
import nrs.core.base.OutboundMessageCache;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.RouteManager;
import nrs.core.base.CIDManager;
import nrs.core.base.BaseAppManager;
import nrs.core.base.BaseComponent;
import nrs.core.base.VariableManager;
import nrs.core.comms.PortManager;
import nrs.core.base.Router;
import nrs.core.message.Constants;
import nrs.core.message.QueryConnectedCIDs;
import nrs.csl.*;
import nrs.toolboxes.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import nrs.util.ArgumentFielder;
import nrs.util.ArgumentException;
import nrs.util.job.JobManager;
import nrs.util.job.JobWindow;

/**
 * The main application manager, controlling all important resources,
 * their initialisation and coordination. Designed according to the
 * singleton pattern.
 *
 * <p>Note about logging. The Java logging API is used. The log levels
 * <tt>warning</tt> and <tt>severe</tt> should be used for program
 * warnings are error. The level <tt>fine</tt> is used for output
 * notable changes in state or occurance of significant
 * events. Importantly it is also being used to output the messages sent
 * and received from communications ports. The level above,
 * <tt>finer</tt>, is used to provide lower level details of standard
 * events, while <tt>finest</tt> catches everything else, included debug
 * statements.
 *

 * @author  Darren Smith
 */
public class AppManager extends BaseAppManager
{
  /** The version number of the application. */
  public static final double VERSION = 0.1;

  /** Name of the application. */
  public static final String NAME = "NRS.gui";

  // TODO - move these, finally, to somewhere else
  /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
  static final String NAMESPACES_FEATURE_ID
    = "http://xml.org/sax/features/namespaces";

  /** Namespace prefixes feature id
   * (http://xml.org/sax/features/namespace-prefixes). */
  static final String NAMESPACE_PREFIXES_FEATURE_ID
    = "http://xml.org/sax/features/namespace-prefixes";

  /** Default parser name. */
  protected static final String DEFAULT_PARSER_NAME
    = "org.apache.xerces.parsers.SAXParser";

  /**
   * List of windows to store settings for upon program termination
   */
  private Collection m_windowsToPersist = new ArrayList();

  LogWindow m_logWin;
  JobWindow m_jobWin;
  NodeLinkMakerWindow m_linkWin;
  MainFrame m_mainFrame;
  
  private PML_Exporter m_pmlExporter;
  private LinkManager m_linkMan;
  
  private NodeManager m_nodeMan;

  private FileStorageManager m_fileMan;

  private Settings m_settings;

  private NRSComponentManager m_compMan;

  private CIDManager m_cidManager;

  private Programmer m_programmer;

  int m_fileLoadDelay;

  private JobManager m_jobManager;

  /** When GUI is being used in debug mode. */
  private boolean m_debugMode = false;

  //----------------------------------------------------------------------
  /**
   * Return the singleton instance of this class
   */
  static AppManager getInstance()
  {
    if (BaseAppManager.instance() == null) new AppManager();

    return (AppManager) BaseAppManager.instance();
  }
  //----------------------------------------------------------------------
  /**
   * Constructor. This creates member objects which are provided as
   * services (ie that can be access via appropriate getXXX or accessXXX
   * methods).
   *
   */
  private AppManager()
  {
    m_nodeMan = new NodeManager();
    m_settings = new Settings();
    initialiseSettings();

    m_programmer = new Programmer(m_settings);

    m_jobManager = new JobManager("main");
    JobManager.startJobManagerThread(m_jobManager).start();

    m_linkMan = new LinkManager(m_jobManager);

    // Don't create the exporter... it is created upon demand
    m_pmlExporter = null;

    // configuration
    m_programmer.setOutboundPipeline(getOutboundPipeline());
    m_compMan.setOutboundPipeline(getOutboundPipeline());
    m_compMan.setComponentNode(getComponent());

    PortManager portMan = instance().getPortManager();
    portMan.setCIDManager(m_cidManager);
    portMan.setComponentNode(getComponent());
    portMan.setIsServer();

    m_fileMan = new FileStorageManager(null);
    m_argumentFielders.add(m_fileMan);
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  private void lateApplicationConfiguration()
  {
    m_mainFrame = new MainFrame(new BrowserManager());
    m_mainFrame.restoreSettings(m_prefs);
    m_mainFrame.setContext(null);
    
    m_fileMan.setGUI(m_mainFrame);

    m_logWin = new LogWindow (m_mainFrame, m_mainFrame);
    m_jobWin = new JobWindow (m_mainFrame, m_mainFrame, m_jobManager);
    
    Logger.getLogger("nrs").addHandler(m_logWin.getHandler());

    // Used for recording which windows should remember their state
    persistWindowLater(m_logWin);
    persistWindowLater(m_jobWin);
  }
  //----------------------------------------------------------------------
  /**
   * Start the application.
   *
   * @param args array of commandline arguments passed to application
   */
  protected void startup(String[] args)
  {
    super.startup(args);

    // call config stuff that can been delayed from the constructor
    lateApplicationConfiguration();

    if (getCSLRepository(true) != null)
    {
      PackageLogger.log.info("Using CSL repository: "+getCSLRepository(false));
    }

    // show the main window
    m_mainFrame.setVisible(true);

    // Create the link manager window
    m_linkWin = new NodeLinkMakerWindow (m_mainFrame,
                                         m_mainFrame,
                                         m_linkMan);
    persistWindowLater (m_linkWin);
    
    m_linkWin.restoreSettings (m_prefs);
    m_logWin.restoreSettings(m_prefs);
    m_jobWin.restoreSettings(m_prefs);
  }
  //-------------------------------------------------------------------------
  /**
   * Causes the NRS.gui to terminate
   *
   * @param i a status code; by convention, a nonzero status code
   * indicates abnormal termination
   */
  void shutdown(int i)
  {
    PackageLogger.log.info ("Application shutting down");

    for (Iterator iter = m_windowsToPersist.iterator (); iter.hasNext ();)
      {
	Object next = iter.next ();

	if (next instanceof Toolbox)
	  {
	    Toolbox toolbox = (Toolbox) next;
	    toolbox.saveSettings (m_prefs);
	  }
      }

    m_mainFrame.saveSettings (m_prefs);
    System.exit (i);
  }
  //----------------------------------------------------------------------
  private void initialiseSettings()
  {
    m_settings.add(new ConfigParameterBoolean(NRSGUIConstants.Settings.ONLINE,
                                              true));
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Settings} object that stores session configuration.
   */
  Settings settings()
  {
    return m_settings;
  }
  //-------------------------------------------------------------------------
  String requestCSL_local ()
  {
    // TODO: This method will later be replaced with thread
    // implementation - so that I can have a timeout and progress bar
    // etc.

    // TODO: also make these filenames configurable

    PackageLogger.log.info ("Attempting to connect to the server... ");

    // open fifo for writing
    FileWriter outf;
    try
    {
      PackageLogger.log.fine ("Opening fifo_out...");
      outf = new FileWriter ("/home/darrens/fifo_out");
    }
    catch (IOException e)
    {
      PackageLogger.log.warning ("Couldn't open fifo for writing. IOException=" + e);
      return null;
    }

    // attempt to write to fifo
    try
    {
      String command = "Please send me CSL...";
      outf.write (command, 0, command.length ());
    }
    catch (IOException e)
    {
      PackageLogger.log.warning ("Couldn't write to fifo. IOException=" + e);
      return null;
    }

    // attempt to close the fifo
    try
    {
      outf.close ();
    }
    catch (IOException e)
    {
      PackageLogger.log.warning ("Couldn't close fifo. IOException=" + e);
      return null;
    }

    // now open the fifo for reading - i.e., to check the response
    FileReader inf;
    BufferedReader bf;
    try
    {
      PackageLogger.log.fine ("Opening fifo_in...");
      inf = new FileReader ("/home/darrens/fifo_in");
      bf = new BufferedReader (inf);
    }
    catch (IOException e)
    {
      PackageLogger.log.warning ("Couldn't open fifo_in for writing. IOException=" + e);
      return null;
    }

    String cslContent = "";
    try
    {
      int i;
      do
	{
	  i = bf.read ();
	  if (i != -1)
	    cslContent += (char) i;
	}
      while (i != -1);
    }
    catch (IOException e)
    {
      PackageLogger.log.warning ("Failed while reading from  fifo_in. " + e);
      return null;
    }

    try
    {
      bf.close ();
    }
    catch (IOException e)
    {
      PackageLogger.log.warning ("Failed while closing fifo_in. " + e);
      return null;
    }

    PackageLogger.log.info ("CSL acquired. Comms complete.");
    return cslContent;
  }

  //----------------------------------------------------------------------
  public void persistWindowLater(Toolbox t)
  {
    m_windowsToPersist.add (t);
  }
  //----------------------------------------------------------------------
  public void persistWindowImmediately(Toolbox t)
  {
    // todo
  }
  //----------------------------------------------------------------------
  IconManager getIconManager()
  {
    return IconManager.getInstance();
  }
  //----------------------------------------------------------------------
  /**
   * Return the location of the local CSL repository, or <tt>null</tt>
   * if the NRS_CSL_REPOSITORY variable has been passed to the Java
   * command, or if its value could not be obtained for other reason.
   * The CSL repository is the file system directory in which CSL can be
   * stored and retrieved.
   *
   * @param showError set to <tt>true</tt> to log warnings
   */
  public String getCSLRepository(boolean showError)
  {
    String retVal = null;
    try
    {
      retVal = System.getProperty("NRS_CSL_REPOSITORY");

      if ((retVal == null) || (retVal.length() == 0))
      {
	  retVal = NRSCSLRepository.CSL_DIR;
	  if (showError)
	      {
		  PackageLogger.log.info( "Using default CSL repository: "
					  + retVal );
	      }
      }

    }
    catch (Exception e)
    {
      if (showError)
      {
        PackageLogger.log.warning("Error when attempting to access "
                                  + "env. variable NRS_CSL_REPOSITORY: "
                                  + e.getMessage());
        e.printStackTrace();
      }
    }

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Return then {@link NRSComponentManager} this application uses
   */
  NRSComponentManager getComponentManager()
  {
    return m_compMan;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Programmer} instance used by the application.
   */
  Programmer getProgrammer()
  {
    return m_programmer;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link JobManager} instance used by the application.
   */
  JobManager getJobManager(){
    return m_jobManager;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NodeManager} instance used to manage all of the
   * user constructed {@link Node} instances
   */
  NodeManager getNodeManager()
  {
    return m_nodeMan;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link FileStorageManager} used to manage export to, and
   * import from, files.
   */
  FileStorageManager getFileStorageManager()
  {
    return m_fileMan;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NodeLinkMakerWindow} used to manage links.
   */
  NodeLinkMakerWindow getLinkWindow()
  {
    return m_linkWin;
  }


  //----------------------------------------------------------------------
  public void displayHelp()
  {
    super.displayHelp();

    System.out.println("\t-d|--delay SEC \t\t\tdelay loading network");
    System.out.println("\t--debug \t\t\tuse debug mode");
    System.out.println("\t<filename>\t\t\tnetwork file to open");
  }
  //----------------------------------------------------------------------
  public java.util.List getOptions()
  {
    java.util.List options = super.getOptions();

    options.add("d");
    options.add("delay");
    options.add("debug");

    return options;
  }
  //----------------------------------------------------------------------
  public boolean processLeftover(String option, String [] args, int index)
  {
    final String localOption = option;
    // Attempt to open the specifed file
    if (m_fileLoadDelay > 0)
    {
      Thread openJob = new Thread()
        {
          public void run()
          {
            PackageLogger.log.fine("Delaying file open for "
                                   + m_fileLoadDelay + " sec.");
            try{
                sleep(m_fileLoadDelay * 1000);
            }
            catch (InterruptedException e)
            {
            }

            m_mainFrame.loadNetwork(new File(localOption));
          }
        };
      openJob.start();
    }
    else
    {
      //PackageLogger.log.fine("Leftover argument is: " + option);
      //m_mainFrame.loadNetwork(new File(option));
    }

    return true;
  }
  //----------------------------------------------------------------------
  public void processOption(String option, String [] args, int index)
    throws ArgumentException
  {
    super.processOption(option, args, index);

    if (option.equals("d") || option.equals("delay"))
      {
        // ensure there are enough options
        if (index + 1 < args.length)
          {
            try
              {
                m_fileLoadDelay = Integer.parseInt(args[index+1]);
              }
            catch (NumberFormatException e)
              {
                PackageLogger.log.warning("Bad delay value, can't parse \""
                                          + args[index+1] + "\":  using 0 instead");
                m_fileLoadDelay = 0;
              }
            args[index+1] = null;
          }
        else
          {
            throw new ArgumentException(option, index, ArgumentException.MISSING);
          }
      }
    else if ( option.equals("debug") )
    {
      m_debugMode = true;
    }
    
  }
  //----------------------------------------------------------------------
  public CIDManager getCIDManager()
  {
    return m_cidManager;
  }
  //----------------------------------------------------------------------

  /** Called by superclass constructor.*/
  protected BaseComponent buildComponent(VariableManager vmMan)
  {
    // Make object assignments now, because NRSGUIComponentNode is going
    // to require them (and AppManager is still unders construction at
    // this point!)
    m_compMan = new NRSComponentManager(getCSLRepository(false));
    m_cidManager = new CIDManager();
    return new NRSGUIComponentNode(m_compMan,
                                   vmMan,
                                   getRouteManager());
  }
  //----------------------------------------------------------------------
  /**
   * Access the {@link LinkManager}
   */
  LinkManager accessLinkManager()
  {
    return m_linkMan;
  }

  /** Whether GUI is being run in debug mode. */
  boolean isDebugMode()
  { 
    return m_debugMode; 
  }
}
