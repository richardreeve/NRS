package nrs.nrsgui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import nrs.core.base.ComponentInfo;
import nrs.core.base.discovery.DiscoveryProcess;
import nrs.core.comms.FIFOCommsRoute;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;
import nrs.pml.PMLParser;
import nrs.pml.PML_Element_Registry;
import nrs.toolboxes.FIFOConnectionWindow;
import nrs.toolboxes.SocketConnectionWindow;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import nrs.util.ExampleFileFilter;

/**
 * The main NRS.gui application window.
 *
 * @author Darren Smith
 */
public class MainFrame extends RootNetworkBrowser implements ToolboxParent,
                                                             Observer
{

  // Main menu bar
  JMenuBar m_menuBar = new JMenuBar();

  // Menus
  JMenu m_menuFile = new JMenu();
  JMenu m_menuView = new JMenu();
  JMenu m_menuWindows = new JMenu();
  JMenu m_connect = new JMenu();

  // Menu options
  private final String FILE = "File";
  private final String CONNECT = "Connect";
  private final String VIEW = "View";
  private final String WINDOWS = "Windows";

  private final JMenuItem m_miNew = new JMenuItem("New...", KeyEvent.VK_N);
  private final JMenuItem m_miOpen = new JMenuItem("Open...", KeyEvent.VK_O);
  private final JMenuItem m_miEdit = new JMenuItem("Edit Info", KeyEvent.VK_E);
  private final JMenuItem m_miSave = new JMenuItem("Save", KeyEvent.VK_S);
  private final JMenuItem m_miSaveAs = new JMenuItem("Save As...",
                                                     KeyEvent.VK_A);
  private final JMenuItem m_miClose = new JMenuItem("Close", KeyEvent.VK_C);
  private final JMenuItem m_miExit = new JMenuItem("Exit", KeyEvent.VK_X);
  private final JMenuItem m_miLocal = new JMenuItem("Local (FIFO)...",
						    KeyEvent.VK_L);
  private final JMenuItem m_miSockets = new JMenuItem("Remote (TCP/IP)...",
						      KeyEvent.VK_T);
  private final JMenuItem m_miDiscov = new JMenuItem("Discover (ALPHA)",
                                                     KeyEvent.VK_D);

  /** Browsers */
  //  UnitBrowserDialog m_unitBrowserDialog;
  //  MessageBrowserDialog m_messageBrowserDialog;
  //  NodeBrowserDialog m_nodeBrowserDialog;
  //  LogWindow m_logWindow;

  ///// Name of entries in the properties file
  private static final String WIN_X = "MAINF_BROSWER_DIALOG_WIN_X";
  private static final String WIN_Y = "MAINF_BROSWER_DIALOG_WIN_Y";
  private static final String WIN_WIDTH = "MAINF_BROSWER_DIALOG_WIN_WIDTH";
  private static final String WIN_HEIGHT = "MAINF_BROSWER_DIALOG_WIN_HEIGHT";
  private static final String DEFAULT_FIFO_OUT = "DEFAULT_FIFO_OUT";
  private static final String DEFAULT_FIFO_IN = "DEFAULT_FIFO_IN";
  private static final String DEFAULT_FIFO_DIR = "DEFAULT_FIFO_DIR";
  private static final String DEFAULT_REMOTE_HOST = "DEFAULT_REMOTE_HOST";
  private static final String DEFAULT_PORT = "DEFAULT_PORT";
  private static final String LOAD_SAVE_DIR = "MAINFRAME_LOAD_SAVE_DIR";

  // List of toolboxes registered with self. Each object can be cast to
  // a Toolbox
  private final ArrayList m_toolboxes = new ArrayList();

  private static JFileChooser m_fileDialog;

  private DiscoveryProcess m_discovProcess = null;

  //-------------------------------------------------------------------------
  /* Constructor
   *
   * @param browserMan reference to {@link BrowserManager}
    */
  public MainFrame(BrowserManager browserMan)
  {
    super(browserMan);

    // Register as an observer on the global types registry
    CSL_Global_Registry.getInstance().addObserver(this);

    if (m_fileDialog == null)
    {
      m_fileDialog = new JFileChooser();
      ExampleFileFilter filter = new ExampleFileFilter();
      filter.addExtension(FileStorageManager.NETWORK_EXT);
      filter.setDescription("XML Networks");
      m_fileDialog.setFileFilter(filter);
    }

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
    {
      guiInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  //----------------------------------------------------------------------
  /* Component initialisation */
  private void guiInit() throws Exception
  {
    populateViewMenu();

    IconManager icons = AppManager.getInstance().getIconManager();

    m_menuFile.setText(FILE);
    m_menuFile.setMnemonic(KeyEvent.VK_F);
    m_menuWindows.setText(WINDOWS);
    m_menuWindows.setMnemonic(KeyEvent.VK_W);
    m_menuView.setText(VIEW);
    m_menuView.setMnemonic(KeyEvent.VK_V);
    m_connect.setText(CONNECT);
    m_connect.setMnemonic(KeyEvent.VK_C);

    m_menuView.add(m_menuWindows);

    m_menuBar.add(m_menuFile);
    m_menuBar.add(m_connect);
    m_menuBar.add(m_menuView);
    this.setJMenuBar(m_menuBar);

    // Configure the connections menu
    m_connect.add(initMenuItem(m_miLocal, icons.smallBlank()));
    m_connect.add(initMenuItem(m_miSockets, icons.smallBlank()));
    m_connect.addSeparator();
    m_connect.add(initMenuItem(m_miDiscov, icons.smallBlank()));

    // Configure the main menu
    m_menuFile.add(initMenuItem(m_miNew, icons.smallBlank()));
    m_menuFile.add(initMenuItem(m_miOpen, icons.smallBlank()));
    m_menuFile.addSeparator();
    m_menuFile.add(initMenuItem(m_miEdit, icons.smallBlank()));
    m_menuFile.addSeparator();
    m_menuFile.add(initMenuItem(m_miSave, icons.smallBlank()));
    m_menuFile.add(initMenuItem(m_miSaveAs, icons.smallBlank()));
    m_menuFile.addSeparator();
    m_menuFile.add(initMenuItem(m_miClose, icons.smallBlank()));
    m_menuFile.add(initMenuItem(m_miExit, icons.smallExit()));
  }
  //----------------------------------------------------------------------
  /**
   * Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e)
  {
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      if ( !shutdown() )
        return;
    }

    // I have deliberately placed this after the call to
    // AppManager.getInstance().shutdown(0); because this way the shutdown(...)
    // routine first saves the correct applicaton state to the
    // properties file. If I do this the other way around, then when
    // first calling processWindowEvent, the subwindows belonging to
    // this Frame are close - hence they are no longer visible, and
    // because of this the visibility state which always get stored for
    // those windows in the properties files is always false - even if
    // they were visible just before application shutdown.
    super.processWindowEvent(e);
  }
  //----------------------------------------------------------------------
  /**
   * Handle the user's request to save a network. The network's filename
   * setting is used for the file to save to. If the file save was
   * successful then the has-changed property of the network is reset.
   */
  private void saveNetwork(final Network network, boolean saveAs)
  {
    if ((network.getFilename() == null) || (saveAs))
    {
      if (network.getFilename() != null)
      {
        m_fileDialog.setSelectedFile(new File(network.getFilename()));
      }

      m_fileDialog.setDialogTitle("Save");
      if (m_fileDialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
      {
        File file = m_fileDialog.getSelectedFile();
        network.setFilename(FileStorageManager
                            .addExtension(file.getName(),
                                          file.getAbsolutePath(),
                                          FileStorageManager.NETWORK_EXT));
        setTitle(file.getName());
      }
      else
      {
        return;
      }
    }

    // Thread this purely for reasons of responsiveness.
    new Thread(){
      public void run(){
        AppManager.getInstance().getFileStorageManager().saveNetwork(network);
        network.resetChanged();
        JOptionPane.showMessageDialog(MainFrame.this, 
                                      "Network saved to:\n" 
                                      + network.getFilename(),
                                      "Network saved", 
                                  JOptionPane.INFORMATION_MESSAGE);
      }}.start();
  }
  //-------------------------------------------------------------------------
  /**
   * Handle the user event to create a new network
   */
  private void createNewNetwork()
  {
    if (getContext() != null)
    //    if (AppManager.getInstance().getNetwork() != null)
    {
      JOptionPane.showMessageDialog(null,
                                    "Please close existing network first",
                                    "Create Network",
                                    JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    // removed by T. French - for the purpose of staying sane! :)
    // NetworkInfoForm networkForm =
//       new NetworkInfoForm(this,
//                           "New Network",
//                           "",
//                           "",
//                           "");
//     networkForm.setVisible(true);

//     if (networkForm.userOKed())
//     {
//       Network n = new RootNetwork(networkForm.getNetworkName(),
//                                   networkForm.getAuthor(),
//                                   networkForm.getDescription());

//       setContext(new BrowserContext(n));
//       updateMenus();
//     }

    Network n = new RootNetwork();
    setContext(new BrowserContext(n));
    updateMenus();

//     networkForm.dispose();
  }
  //----------------------------------------------------------------------
  /**
   * Handle user's request to close an existing network
   */
  private void closeNetwork()
  {
    if (getContext() == null) return;

    final Network n = getContext().network();
    if (n.hasChanged())
    {
       Object[] options = { "Save", "Discard", "Cancel" };
       int choice = JOptionPane.showOptionDialog(this,
                                                 "This network has unsaved changes.\nDo you want to save?",
                                                 "Warning",
                                                 JOptionPane.DEFAULT_OPTION,
                                                 JOptionPane.WARNING_MESSAGE,
                                                 null, options,
                                                 options[0]);

       // process user choice
       switch (choice)
       {
         case 0 :
         {
           // user selected "Save"
           saveNetwork(n, false);
           if (n.hasChanged()) return;  // indicates user cancelled the save!
           break;
         }
         case 1:
         {
           // user selected "Discard"
           break;
         }
         default:
         {
           // user selected "Cancel"
           return;
         }
       }
    }

    // Close all views if the current network is root (which it should be)
    if (n instanceof RootNetwork)
    {
      RootNetwork root = (RootNetwork) n;
      root.closeAllViews();
    }

    // delete all root nodes - recursively deleting all their child nodes, etc
    new Thread(){
        public void run(){
          Node nd;
          for (Iterator<Node> i = n.nodeIterator(); i.hasNext(); )
          {
            nd = i.next();
            //getContext().nodes().remove(nd); // not necessary?
            nd.delete();
          }

          // invoke on the GUI dispatching thread
          SwingUtilities.invokeLater(new Runnable(){
              public void run(){
                setContext(null);
                updateMenus();
                setTitle("");
                clearStatusBar();
              }
            });
        }
      }.start();
  }
  //----------------------------------------------------------------------
  /**
   * Handle user's request to open an existing network
   */
  private void openNetwork()
  {
    if (getContext() != null)
    {
      JOptionPane.showMessageDialog(null,
                                    "Please close existing network first",
                                    "Open Network",
                                    JOptionPane.WARNING_MESSAGE);
      return;
    }

    m_fileDialog.setDialogTitle("Open");
    if (m_fileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    {
      loadNetwork(m_fileDialog.getSelectedFile());
    }
    else
    {
      return;
    }
  }
  //----------------------------------------------------------------------
  void loadNetwork(File file)
  {
    // Create a new empty network
    Network network = new RootNetwork();
    setContext(new BrowserContext(network));
    updateMenus();

    // Load in the specified 'file', which contains a network, into the
    // new network created above.
    AppManager.getInstance().getFileStorageManager().loadNetwork(file,
                                                                 network,
                                                                 true);

    PackageLogger.log.fine("Network '" + network + "'loaded from file "
                           + file.getAbsolutePath());

    network.setFilename(file.getAbsolutePath()); //bug fix by T.French
    network.resetChanged();

    setTitle(file.getName());
  }
  //-------------------------------------------------------------------------
  /**
   * Handle the user event to edit the network information:
   * Author, Network Name and Description
   */
  private void editNetworkInfo()
  {
    Network n = getContext().network();

    NetworkInfoForm networkForm =
      new NetworkInfoForm(this,
                          "New Network",
                          n.getName(),
                          n.getAuthor(),
                          n.getDescription());
    networkForm.setVisible(true);

    // info has been edited
    if (networkForm.userOKed())
      {
        n.setName(networkForm.getNetworkName());
        n.setAuthor(networkForm.getAuthor());
        n.setDescription(networkForm.getDescription());

        setContext(new BrowserContext(n));
      }

    networkForm.dispose();
  }
  //-------------------------------------------------------------------------
  void handleMenuEvent(ActionEvent e)
  {
    JMenuItem source = (JMenuItem)(e.getSource());

    // Defensive programming
    if (source == null) return;

    if (e.getSource() == m_miSave)
    {
      saveNetwork(getContext().network(), false);
      return;
    }

    if (e.getSource() == m_miOpen)
    {
      openNetwork();
      return;
    }

    if (e.getSource() == m_miClose)
    {
      closeNetwork();
      return;
    }

    if (e.getSource() == m_miSaveAs)
    {
      saveNetwork(getContext().network(), true);
      return;
    }

    if (e.getSource() == m_miExit)
    {
      shutdown();
      return;
    }

    if (e.getSource() == m_miNew)
    {
      createNewNetwork();
      return;
    }

    // add Edit Info action, T. French
    if (e.getSource() == m_miEdit)
    {
      editNetworkInfo();
      return;
    }

    if (e.getSource() == m_miLocal)
      {
        configureLocalConnection();
        return;
      }

    if ( e.getSource() == m_miSockets )
      {
        configureRemoteConnection();
        return;
      }

    if ( e.getSource() == m_miDiscov )
      {
        runDiscovery();
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
  //----------------------------------------------------------------------
  /**
   * Registers the provided {@link JMenuItem} with this class's event
   * handler, and also sets the icon.
   *
   * <pThe provided menu item returned, so that this function can be
   * used in the follow pattern:
   *
   * <p><tt>m_menuFile.add(initMenuItem(m_miNew, icons.smallBlank()));</tt>
   */
  private JMenuItem initMenuItem(JMenuItem item, ImageIcon icon)
  {
    item.addActionListener(
                           new ActionListener()
                           {
                             public void actionPerformed(ActionEvent e)
                             { handleMenuEvent(e); }
                           });
    item.setIcon(icon);

    return item;
  }
  //-------------------------------------------------------------------------
  private JMenuItem makeMenuItem(String menuLabel, ImageIcon icon)
  {
    JMenuItem newItem;
    if (icon == null)
    {
      newItem = new JMenuItem(menuLabel);
    }
    else
    {
      newItem = new JMenuItem(menuLabel, icon);
    }

    newItem.addActionListener(
                              new ActionListener()
                              {
                                public void actionPerformed(ActionEvent e)
                                { handleMenuEvent(e); }
                              });

    return newItem;
  }
  //-------------------------------------------------------------------------
  JCheckBoxMenuItem makeCBMenuItem(JDialog dialog)
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
  /** Add options to the m_menuView depending on whether their
   * corresponding component is currently visible or not.  */
  void populateViewMenu()
  {
    m_menuWindows.removeAll();

    for (Iterator i = m_toolboxes.iterator(); i.hasNext(); )
    {
      Toolbox tb = (Toolbox) i.next();

      m_menuWindows.add(makeCBMenuItem(tb));
    }
  }
  //----------------------------------------------------------------------
  public void updateMenus()
  {
    populateViewMenu();

    boolean b = (getContext() != null) && (getContext().network() != null);

    m_miSave.setEnabled(b);
    m_miSaveAs.setEnabled(b);
    m_miEdit.setEnabled(b);
    m_miClose.setEnabled(b);
  }
  //----------------------------------------------------------------------
  /** Notify the owner of a toolbox window that the toolbox has had its
   * visibility altered */
  public void toolboxVisibilityChanged(Toolbox toolbox)
  {
    updateMenus();
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
  //----------------------------------------------------------------------
  /** Provide a {@link java.util.prefs.Preferences} object which allows self
   * to save various settings for future sessions. */
  public void saveSettings(Preferences props)
  {
    props.put(LOAD_SAVE_DIR,
              m_fileDialog.getCurrentDirectory().getAbsolutePath());

    // Store the location and size of the unit browser toolbox
    Point p = getLocation();
    props.putInt(WIN_X, p.x);
    props.putInt(WIN_Y, p.y);

    Dimension d = getSize();
    props.putInt(WIN_WIDTH, d.width);
    props.putInt(WIN_HEIGHT, d.height);
  }
  //----------------------------------------------------------------------
  /** Provide a {@link java.util.prefs.Preferences} object which allows self
   * to restore various settings saved from previous sessions. */
  public void restoreSettings(Preferences props)
  {
    m_fileDialog.setCurrentDirectory(new File(props.get(LOAD_SAVE_DIR, "")));

    Point p = new Point();
    p.x = props.getInt(WIN_X, 0);
    p.y = props.getInt(WIN_Y, 0);
    setLocation(p);

    Dimension d = new Dimension();
    d.width = props.getInt(WIN_WIDTH, 300);
    d.height = props.getInt(WIN_HEIGHT, 200);
    setSize(d);

    // Retrieve the visibility
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
  private boolean configureLocalConnection()
  {
    Preferences prefs = AppManager.getInstance().getPreferences();

    FIFOConnectionWindow conWin = new FIFOConnectionWindow(this, null);
    conWin.restoreSettings(prefs);

    int ID = AppManager.getInstance().getPortManager().getFreePortID();

    conWin.setPortDescription("Local FIFO", ID, true);
    conWin.setPortParameters(prefs.get (DEFAULT_FIFO_IN,
                                        "/tmp/NRS_fifo_to_gui"),
                             prefs.get (DEFAULT_FIFO_OUT,
                                        "/tmp/NRS_fifo_from_gui"));
    conWin.setBrowseDir(prefs.get(DEFAULT_FIFO_DIR, "/tmp"));
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
      }
      else if (conWin.getReturnValue() == FIFOConnectionWindow.OK)
      {
        try
        {
          portNumber = conWin.getPortNumber();
          enterAgain = false;

          // save these settings
          prefs.put(DEFAULT_FIFO_IN, conWin.getInbound());
          prefs.put(DEFAULT_FIFO_OUT, conWin.getOutbound());
          prefs.put(DEFAULT_FIFO_DIR, conWin.getBrowseDir());
        }
        catch (NumberFormatException e)
        {
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
        // Create a port
        FIFOCommsRoute fifo = new FIFOCommsRoute(ID,
                                                 conWin.getName(),
                                                 conWin.getInbound(),
                                                 conWin.getOutbound());

        PMLParser pmlParser = new PMLParser();
        fifo.setMesssageListener(pmlParser);

        // Tell the parser which objects its should use for both message
        // preprocessing and processing
        pmlParser.setMessageCallbacks(fifo,
                                      AppManager.getInstance().
                                      getInboundPipeline());

        AppManager.getInstance().getPortManager().addPort(fifo);

        fifo.open();
        return false;
      }
      catch (Exception e)
      {
        PackageLogger.log.warning("Failed to open local connection, "
                                   + e.toString());
        e.printStackTrace();
      }
    }
    return true;
  }
    //----------------------------------------------------------------------
  /**
   * Coordinate the start up of objects and processes to initiate
   * connection to remote NRS component using Sockets.
   *
   * @return <tt>true</tt> if the user selected the cancel button,
   * <tt>false</tt> otherwise or if the selected connection could not be
   * opened.
   */
  private boolean configureRemoteConnection()
  {
    Preferences prefs = AppManager.getInstance().getPreferences();

    SocketConnectionWindow socWin = new SocketConnectionWindow(this, null, true);
    socWin.restoreSettings(prefs);

    int ID = AppManager.getInstance().getPortManager().getFreePortID();

    socWin.setPortDescription("Remote Socket", ID, true);
    socWin.setSocketParameters(prefs.get (DEFAULT_REMOTE_HOST,
                                        "localhost"),
                             prefs.get (DEFAULT_PORT,
                                        "5000"));
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
	  prefs.put("DEFAULT_REMOTE_HOST", socWin.getRemoteHost());
	  prefs.put("DEFAULT_PORT", socWin.getRemotePort());
        }
        catch (NumberFormatException e)
        {
          JOptionPane.showMessageDialog(null,
                                        "\"Port Number\" must a number",
                                        "Invalid entry",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    if (socWin.getReturnValue() == SocketConnectionWindow.OK )
    {
      try
      {
	  if ( socWin.isClient() )
	      AppManager.getInstance().
		  getPortManager().openClient(ID,
					      socWin.getRemoteHost(),
					      Integer.parseInt(socWin.
							       getRemotePort()));
	  else
	      AppManager.getInstance().
		  getPortManager().openServer(
					      Integer.parseInt(socWin.
							       getRemotePort()));


	  return false;
      }
      catch (Exception e)
      {
        PackageLogger.log.warning("Failed to open remote connection, "
                                   + e.toString());
        e.printStackTrace();
      }
    }
    return true;
  }
  //----------------------------------------------------------------------
  /**
   * Receive update events from the {@link CSL_Global_Registry}. When
   * such events occur, the list of root nodes available is obtained and
   * passed to the GUI controls.
   *
   * Also receives events from {@link NodeState} objects under
   * observation.
   *
   * Inhertied from {@link Observable}.
   */
  public void update(Observable o, Object arg)
  {
    if (o == CSL_Global_Registry.getInstance())
    {

      if (arg == null)
      {
        /*
        m_nodesPermitted.clear();

        m_nodesPermitted.add(CSL_Global_Registry.getInstance().getRootNodes());
        */
      }
      else if (arg instanceof Collection)
      {
        Collection rootNames = (Collection) arg;
        final StringBuffer msg
          = new StringBuffer("Root nodes available");

        /*
            _BUG_( if there are many new names, then the dialog box could
            become to big, preventing the user from clicking
            close. Create a dedicated a GUI which contains a list box. )
        */
        Iterator i = rootNames.iterator();

        ComponentInfo source = AppManager.getInstance().
          getComponentManager().getComponent(i.next().toString());

        if (source != null)
        {
          msg.append(" on ");
          msg.append(source.toString());
        }
        msg.append(" : ");

        for (; i.hasNext(); )
        {
          String rootName = (String) i.next();
          msg.append("\n\t");
          msg.append(rootName);
        }

        // Inform the user of the new nodes availalbe. Do this in a
        // separate thread (this is the first ever time I have used this
        // pattern!)
        Thread userFeedback = new Thread()
          {
            public void run()
            {
              JOptionPane pane
                = new JOptionPane(msg,
                                  JOptionPane.INFORMATION_MESSAGE,
                                  JOptionPane.DEFAULT_OPTION);

              JDialog dialog = pane.createDialog(MainFrame.this,
                                                 "Information");

              dialog.setModal(false);
              dialog.show();

              //              JOptionPane.showMessageDialog(null,
              //                                            msg.toString(),
              //                                            "Information",
              //                                            JOptionPane.INFORMATION_MESSAGE);
            }
          };
        userFeedback.start();
      }
    }
  }
  //----------------------------------------------------------------------
  private void runDiscovery()
  {
    /* TODO - I should allow for multiple discovery processes to be in
     * operation, and each would maybe run in its own thread? But what I
     * still need to do is manage the termination of a discovery process
     * (during which time it should deregister its variables) and also
     * the termination of the enclosing thread.
     *
     */
    if (m_discovProcess != null)
    {
      PackageLogger.log.info("A discovery process already exists");
      return;
    }

    m_discovProcess =
      new DiscoveryProcess(AppManager.getInstance().getPortManager(),
                           AppManager.getInstance().getVariableManager(),
                           AppManager.getInstance().getCIDManager(),
                           AppManager.getInstance().getOutboundPipeline());

    PackageLogger.log.info("Starting component discovery");

    //  Do this in a separate thread
    Thread discoveryThread = new Thread()
      {
        public void run()
        {
          m_discovProcess.startDiscovery();
        }
      };
    discoveryThread.start();
  }

  /** Shutdown procedure for when window is killed or 'Exit' menu option 
   * clicked on. 
   *
   * This is quite ugly, but seems to work for now. (T. French)
   */
  private boolean shutdown(){
    // shutdown immediately if no network context
    if ( getContext() == null ){
      AppManager.getInstance().shutdown(0);
      return true; // redundant
    }
    else{ // need to check status of ports and sockets.
      StringBuffer sb = new StringBuffer();
      String a = "Are you sure you want to continue?";
      int s = AppManager.getInstance().getPortManager().numOfServers();
      int p = AppManager.getInstance().getPortManager().numOpenPorts();

      // if acting as Sockets Server and using more than one Ports.
      if ( p > 1 )
        sb.append("NRS.gui has " + p + " ports open.\n");
      if ( s > 0 && p > 0 ) 
        sb.append("NRS.gui is acting as " + s + " server(s) with " + p 
                  + " port(s) open.\n");
                  
      if ( sb.length() != 0 ) sb.append("\n");
      sb.append(a);

      int resp = JOptionPane.showConfirmDialog(this, sb.toString(),
                                               "Exit",
                                               JOptionPane.YES_NO_OPTION);
      if ( resp == JOptionPane.YES_OPTION ){
        browserClosing();
        AppManager.getInstance().shutdown(0);
        return true; // redundant, but required by compiler
      }
      else
        return false;
    }
  }
}
