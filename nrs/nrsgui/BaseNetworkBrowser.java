package nrs.nrsgui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import java.util.Observable;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import nrs.core.base.MessageException;
import nrs.core.message.Constants;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_Interface;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;
import nrs.util.job.Job;
import nrs.util.job.JobManager;

/**
 * Base class for browser-windows which display a view of an NRS
 * network.
 *
 * <p>A browser has two kinds of context: a {@link Node} and {@link
 * NodeCollection}.
 *
 * <p>Note, currently this code assumes only a single JFrame will exist
 * for displaying the contents of a particular node. If that were to
 * ever change, then this code will require careful review. A particular
 * issue will be home node related events initiated in one JFrame will
 * be displayed in another JFrame which also provides a view of the same
 * node-network.
 *
 * @author  Darren Smith
 */
abstract class BaseNetworkBrowser extends JFrame
  implements NetworkEventsListener,
             NetworkCanvasDataContext,
             NodeListener,
             ValidateNodeName

{
  /** Actual canvas which displays the network elements. */
  protected NetworkCanvas m_canvas;

  // Status bar
  private NetworkBrowserStatusBar m_statusBar;

  /** Popup menu for user commands. */
  protected BrowserPopupMenu m_popupMenu;

  /** Scroll bar which surrounds the canvas. */
  protected JScrollPane m_scrollPane;

  /** The context */
  BrowserContext m_ctxt;

  protected BrowserManager m_browserMan;

  //
  // Child objects
  //
  private AttributeEditorWrapper m_attrForm;
  private AttributeEditorWrapper m_attrEditor;

  /** Collection of the objects being displayed, which are a mix of
   * node, variables, links etc */
  private Collection m_dds;

  //----------------------------------------------------------------------
  /**
   * Constructor to create an empty browser
   */
  BaseNetworkBrowser(String title, BrowserPopupMenu popupMenu,
                     BrowserManager browserMan)
  {
    this(title, null, popupMenu, browserMan);
  }
  //---------------------------------------------------------------------------
  /**
   * Create a browser to inspect the specified context
   *
   * @param title title of the window
   *
   * @param context the {@link BrowserContext} to diplay
   *
   * @param popupMenu the {@link BrowserPopupMenu} to use on this
   * browser. This browser will have its event handler and data context
   * set to the class instance being constructed (self).
   *
   * @param browserMan the {@link BrowserManager} which provides
   * functions for opening new browsers.
   */
  BaseNetworkBrowser(String title,
                     BrowserContext context,
                     BrowserPopupMenu popupMenu,
                     BrowserManager browserMan)
  {
    super(title);

    m_browserMan = browserMan;
    m_popupMenu = popupMenu;
    m_canvas = new NetworkCanvas(this, this, this);
    m_dds = new ArrayList();

    setContext(context);

    ConfigParameterBoolean lineParam = (ConfigParameterBoolean)
      AppManager.getInstance().settings().get(NRSGUIConstants.Settings.ONLINE);

    m_statusBar = new NetworkBrowserStatusBar(lineParam);

    if (popupMenu != null)
    {
      popupMenu.setBrowserContext(this);
      popupMenu.setEventHandler(this);
    }

    rebuildObjectList();

    guiInit();

    m_canvas.setCollection(m_dds);

    guiInitFinish();

    addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          browserClosing();
        }
      });
  }
  //----------------------------------------------------------------------
  /**
   * Set the context of this browser
   */
  void setContext(BrowserContext context)
  {
    if (m_ctxt != null && m_ctxt.hasNodes())
    {
      m_ctxt.nodes().removeNodeListener(this);
    }

    m_ctxt = context;

    if (m_ctxt != null && m_ctxt.hasNodes())
    {
      m_ctxt.nodes().addNodeListener(this);
    }

    // use invokeLater here?
    rebuildObjectList();
    m_canvas.setCollection(m_dds);

    m_popupMenu.setEnabled(m_ctxt != null);
    m_canvas.setEnabled(m_ctxt != null);
  }
  //----------------------------------------------------------------------
  /**
   * Reflect network presence
   */
  void enableGUI(Network network)
  {
    boolean b = (network != null) ;

    m_canvas.setVisible(b);
    m_scrollPane.setVisible(b);
  }
  //----------------------------------------------------------------------
  /**
   * Build the initial list of objects to display, which is list of
   * variables and the list of child nodes.
   */
  private void rebuildObjectList()
  {
    m_dds.clear();

    if (m_ctxt == null) return;

    // add variables
    if (m_ctxt.hasNode())
    {
      for (Iterator i = m_ctxt.node().getVariables().variableIterator();
           i.hasNext();)
      {
        NodeVariable nv = (NodeVariable) i.next();
        m_dds.add(nv.getPainter());
      }
    }

    // add child nodes
    if (m_ctxt.hasNodes())
    {
      for (Iterator i = m_ctxt.nodes().nodeIterator(); i.hasNext(); )
      {
        Node n = (Node) i.next();
        m_dds.add(n.getPainter());
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Initialise the GUI. Note, add this naming convention to my list of
   * Java coding guidelines.
   */
  private void guiInit()
  {
    // Add menu to canvas
    m_popupMenu.resetMenu();

    ////////// Configure the root panel
    JPanel root = (JPanel) this.getContentPane();
    root.setLayout(new BorderLayout());

    m_scrollPane = new JScrollPane(m_canvas);
    root.add(m_scrollPane);
    root.add(m_statusBar, BorderLayout.SOUTH);

    // Add borders

    m_statusBar.setBorder(BorderFactory.createEtchedBorder());
  }
  //----------------------------------------------------------------------
  /**
   * Complete the initialisation of the GUI by performing placement,
   * sizing and packing.
   */
  private void guiInitFinish()
  {
    // Set size and location
    setSize(400,400);
    setLocation(100,100);
    this.pack();
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void objectSelected(Object o)
  {
    if (o == null)
    {
      //      m_statusBar.setText(" ");
      return;
    }

    if (o instanceof UserVN)
    {
      UserVN uvn = (UserVN) o;
      m_statusBar.setSelected(uvn);
    }
    else
    {
      m_statusBar.setSelected(null);
    }

    if (o instanceof Node)
    {
      Node n = (Node) o;

      RemoteVNInfo rInfo = n.remote();
      if ((rInfo != null) && (rInfo.isKnown()))
      {
        //        m_statusBar.setText("Node " + n +  " selected "
        //                            + "(VNID=" + rInfo.VNID() + ")");
      }
      else
      {
        //        m_statusBar.setText("Node " + n +  " selected");
      }

      // no longer required - T. French 
      //if (AppManager.getInstance().accessLinkManager() != null)
      //AppManager.getInstance().accessLinkManager().setSelected(n);
    }
    else if (o instanceof NodeVariable)
    {
      NodeVariable nv = (NodeVariable) o;
      //      m_statusBar.setText("Variable " + nv +  " selected "
      //                          + ", level=" + nv.getPainter().getDisplayHeight());

      if (AppManager.getInstance().accessLinkManager() != null)
      {
        AppManager.getInstance().accessLinkManager().setSelected(nv);
      }
      else PackageLogger.log.fine("no listener :-(");
    }
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void activatePopup(Node n, MouseEvent e)
  {
    m_popupMenu.customise(n);
    m_popupMenu.show(m_canvas, e.getX(), e.getY());
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void activatePopup(NodeVariable nv, MouseEvent e)
  {
    m_popupMenu.customise(nv);
    m_popupMenu.show(m_canvas, e.getX(), e.getY());
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void activatePopup(MouseEvent e)
  {
    m_popupMenu.customise();
    m_popupMenu.show(m_canvas, e.getX(), e.getY());
  }
  //----------------------------------------------------------------------
  private void autoLink(Node ni)
  {
//     System.out.println("In autolink");
    if (ni.getVariables() == null) return;  // no variables

    Node parent = ni.getParent();
    if (parent == null) return;             // no parent!

    VariableCollection pVars = parent.getVariables();
    if (pVars == null) return;              // parent has no variables

    if (ni.getVariables() == null) return;

    List<Link> linksToMake = new ArrayList<Link>();

    for (Iterator<NodeVariable> i = ni.getVariables().iterator(); i.hasNext();)
    {
      NodeVariable cVrbl = i.next();

      for (Iterator j = cVrbl.getType().getInterfaceIterator(); j.hasNext(); )
      {
        CSL_Element_Interface cif = (CSL_Element_Interface) j.next();

        if (cif.autoConnect())
        {

          for (Iterator<NodeVariable> k = pVars.iterator(); k.hasNext(); )
          {
            NodeVariable pVrbl = k.next();

            for (Iterator l = pVrbl.getType().getInterfaceIterator();
                 l.hasNext(); )
            {

              CSL_Element_Interface pif = (CSL_Element_Interface) l.next();

              if (cif.isIn() &&
                  pif.isOut() &&
                  pif.autoConnect() &&
                  pVrbl.name().equals(cVrbl.name()) &&
                  pVrbl.getType().getType() == cVrbl.getType().getType() )
              {
                linksToMake.add(new Link((NodeVariable) pVrbl,
                                         (NodeVariable) cVrbl,
                                         false));
              }
              else if (cif.isOut() &&
                       pif.isIn() &&
                       pif.autoConnect() &&
                       pVrbl.name().equals(cVrbl.name()) &&
                       pVrbl.getType().getType() == cVrbl.getType().getType() )
              {
                linksToMake.add(new Link((NodeVariable) cVrbl,
                                         (NodeVariable) pVrbl,
                                         false));
              }
            }
          }
        }
      }
    }

    if (linksToMake.size() > 0)
    {
      StringBuffer logStr = null;
      for (Link l : linksToMake)
      {
        if (logStr == null)
        {
          logStr = new StringBuffer("Autolinking: " + l.getSource());
        }
        else
        {
          logStr.append(", " + l.getSource());
        }
      }
      PackageLogger.log.fine(logStr.toString());

      // Make links
      for (Link l : linksToMake)
        AppManager.getInstance().accessLinkManager().makeLink(l);
    }
  }
  //----------------------------------------------------------------------
//   /**
//    * Determines whether <tt>n</tt> number of instances of the specified
//    * node type can be created within the current network. Returns
//    * <tt>true</tt> if <tt>n</tt> instances can be created, or
//    * <tt>false</tt> otherwise. The determination is made by evaluating
//    * all of the containment rules for the current network.
//    */
//   abstract boolean canCreate(CSL_Element_NodeDescription type, int n);
  //----------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void makeNode(final CSL_Element_NodeDescription nType)
  {
    /**
     * At some point this entire function, or at least most of it,
     * should be moved into a specific manager or utility class, so that
     * other classes needed interactive node creation can have
     * access. Same goes for the autoLink method.
     */
    if (m_ctxt.nodes() == null)
    {
      PackageLogger.log.warning("Can't create a " + nType + " node "
                                + "because there is no network context");
      return;
    }

    if (!m_ctxt.nodes().canAdd(nType, 1))
    {
      JOptionPane.
        showMessageDialog(this,
                          "Can't add " + nType + " node - limit reached",
                          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (m_attrForm == null)
    {
      m_attrForm = new AttributeEditorWrapper(this, "Attributes", this);
    }

    // thread this?
    ArrayList attrList = m_attrForm.runEditor(nType);
    
    // SwingUtilities.invokeLater();
    if (attrList == null) return;
    
    // Create the new guy and add to parent
    final DefaultNode ni = new DefaultNode(nType, attrList, 0, 0,
                                     m_ctxt.nodes(), m_ctxt.node());
    ni.addNodeListener(this);
    
    // Do remote create
    //AppManager.getInstance().getProgrammer().onlineCreate(ni);
    final Job job = new CreateNodeJob(AppManager.getInstance()
                                      .getProgrammer(), ni);
    // add Observer object callback
    // Attempt to make any automatic links (do after remote create)
    job.addObserver(new Observer(){
      public void update(Observable o, Object arg){
        if ( job.getPhase() == Job.COMPLETE ){
          autoLink(ni);
        }
      }
      });

    // add to jobManager
    AppManager.getInstance().getJobManager().add(job);
  }
  //----------------------------------------------------------------------
  /**
   * Add a newly created node to the display, using the specifed
   * coordinates to set its initial position and height
   */
  private void displayNewNode(Node node, int x, int y, int height)
  {
    node.getPainter().setDisplayHeight(m_dds.size()+1);
    node.getPainter().setLocation(x, y);

    displayNewNode(node);
  }
  //----------------------------------------------------------------------
  /**
   * Add a newly created node to the display, and set its coorindates to
   * appear in the top left window
   */
  private void displayNewNode(Node node)
  {
    m_dds.add(node.getPainter());
    m_canvas.setCollection(m_dds);

    //    NOT DOING THIS KIND OF PLACEMENT ANYMORE
    //    Point p = m_scrollPane.getViewport().getViewPosition();
    //    Rectangle r = node.getPainter().bounds(null);
    //    p.x -= r.x;
    //    p.y -= r.y;
    //    displayNewNode(node, p.x, p.y, m_dds.size()+1);
  }
  //----------------------------------------------------------------------
//   private String buildRowKey(CSL_Element_Attribute attr)
//   {
//     StringBuffer rowKey = new StringBuffer(attr.toString());

//     if ((attr.getResolvedUnit() != null)
//         && (attr.getResolvedUnit().getNumberInfo() != null) &&
//         (attr.getResolvedUnit().getNumberInfo().getNumberInfoAbbrev() != null))
//     {
//       rowKey.append(" (");

//       if (attr.getResolvedUnit().getNumberInfo().getNumberInfoScale() != null)
//       {
//         rowKey.append(attr.getResolvedUnit().getNumberInfo().
//                       getNumberInfoScale());
//       }

//       rowKey.append(attr.getResolvedUnit().getNumberInfo().
//                     getNumberInfoAbbrev());

//       rowKey.append(")");
//     }
//     return rowKey.toString();
//   }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface. A new {@link
   * NetworkBrowser} is created. It is given the same {@link
   * LinkManager} as self, before finally made visible.
   */
  public void openNode(Node n)
  {
    NetworkBrowser netBrowser = m_browserMan.obtainBrowser(n);

    netBrowser.setVisible(true);
    netBrowser.setState(Frame.NORMAL);
    netBrowser.requestFocus();

    RootNetwork root = n.findRoot();
    if (root != null)
    {
      root.addWindow(netBrowser);
    }
    else
    {
      PackageLogger.log.warning("Node " + n + " has no root network; "
                                + "There could be problems with window"
                                + " " + this.getTitle());
    }
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface.
   */
  public void editNode(final Node n)
  {
    if (m_attrEditor == null)
    {
      m_attrEditor = new AttributeEditorWrapper(this, "Attributes", null);
    }
    m_attrEditor.getComponent().setTitle(n.toString());

    new Thread(){
      public void run(){
        final ArrayList attrList = m_attrEditor.runEditor(n);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              
              // return if user cancelled the dialog
              if (attrList == null) return;
              
              // Now effect the changes tn the original node
              for (Iterator i = n.attributeIterator(); i.hasNext(); )
                {
                  NodeAttribute nSrc = (NodeAttribute) i.next();
                  
                  // find a corresponding attribute in the dialog's list
                  for (Iterator j = attrList.iterator(); j.hasNext(); )
                    {
                      NodeAttribute nNew = (NodeAttribute) j.next();
                      
                      if (nSrc.getCSL() == nNew.getCSL())
                        {
                          if (nSrc.getValue() != nNew.getValue())
                            {
                              nSrc.getValueDelegate().set(nNew.getValue());
                            }
                          
                          break;
                        }
                    }
                }
              // finally, indicate this change to the node
              n.attrsModified();
              
              //  remote action
              AppManager.getInstance().getProgrammer().onlineUpdate(n);
            }   
          });
      }
    }.start();
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void deleteNode(Node n)
  {
    if (n == null) return;

    // Check this Node isn't being browsed in another window
    if (m_browserMan.browserExists(n))
    {
       JOptionPane.showMessageDialog(this,
          "Please close open browsers before deleting",
          "Node error",
           JOptionPane.WARNING_MESSAGE);
      return;
    }

    if (JOptionPane.showConfirmDialog(this, "Really remove  '" + n + "'?",
                                      "Confirm delete",
                                      JOptionPane.YES_NO_OPTION)
        == JOptionPane.OK_OPTION)
    {
      // shift display heights above the guy being delete down by one
      for (Iterator i = m_dds.iterator(); i.hasNext(); )
      {
        DisplayDelegate painter = (DisplayDelegate) i.next();

        if (painter.getDisplayHeight() > n.getPainter().getDisplayHeight())
        {
          painter.setDisplayHeight(painter.getDisplayHeight() - 1);
        }
      }

      // ...okay, actually are deleting
      m_ctxt.nodes().remove(n);

      rebuildObjectList();
      m_canvas.setCollection(m_dds);

      n.delete();
    }
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void setHighest(DisplayDelegate np)
  {
    //
    // decrease all those display-heights above np's current
    // display-height by 1
    //
    for (Iterator i = m_dds.iterator(); i.hasNext(); )
    {
      DisplayDelegate dd = (DisplayDelegate) i.next();

      if (dd.getDisplayHeight() > np.getDisplayHeight())
      {
        dd.setDisplayHeight(dd.getDisplayHeight() - 1);
      }
    }
    np.setDisplayHeight(m_dds.size());

    // Update display
    m_canvas.setCollection(m_dds);
  }
  //-------------------------------------------------------------------------
  /**
   * Implements {@link NetworkEventsListener} interface
   */
  public void setLowest(DisplayDelegate np)
  {
    //
    // increase all those display-heights belows n's current
    // display-height by 1
    //
    for (Iterator i = m_dds.iterator(); i.hasNext(); )
    {
      DisplayDelegate dd = (DisplayDelegate) i.next();

      if (dd.getDisplayHeight() < np.getDisplayHeight())
      {
        dd.setDisplayHeight(dd.getDisplayHeight() + 1);
      }
    }
    // magic number: 1 is the lowest display height
    np.setDisplayHeight(1);

    // Update display
    m_canvas.setCollection(m_dds);
  }

  //----------------------------------------------------------------------
  /**
   * Return this browser's context.
   *
   * This has been made final because it would be an error for derived
   * classes to override this method and return an object other than an
   * internal field.
   */
  final public BrowserContext getContext()
  {
    return m_ctxt;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Node} opened in this browser. Can return null.
   *
   * This has been made final because it would be an error for derived
   * classes to override this method and return an object other than an
   * internal field.
   */
  final public Node getNode()
  {
    return m_ctxt.node();
  }
  //----------------------------------------------------------------------
  // Inherited
  /*
   * This has been made final because it would be an error for derived
   * classes to override this method and return an object other than an
   * internal field.
   *
   * @deprecated - use getNetwork() instead
   */
  final public NodeCollection getCollection()
  {
    return m_ctxt.nodes();
  }
  //----------------------------------------------------------------------
  // implements interface
  public void remoteRebuild(Node n)
  {
    AppManager.getInstance().getProgrammer().remoteRebuild(n);
  }
  //----------------------------------------------------------------------
  // implements interface
  public void remoteRebuildLinks(Node n)
  {
    try
    {
      AppManager.getInstance().getProgrammer().remoteRebuildLinks(n);
    }
    catch (MessageException e)
    {
      final String msg = e.toString();
      // Place feedback box in a separate thread
      Thread userFeedback = new Thread()
        {
          public void run()
          {
            JOptionPane.showMessageDialog(BaseNetworkBrowser.this,
                                          msg.toString(),
                                          "Warning",
                                          JOptionPane.ERROR_MESSAGE);
          }
        };
      userFeedback.start();
      PackageLogger.log.warning(e.toString());
    }
  }
  //----------------------------------------------------------------------
  // implements interface
  public void remoteDelete(Node n)
  {
    // delete all children and itself
    n.delete();
  }
  //----------------------------------------------------------------------
  // implements interface
  public void remoteVerify(Node n)
  {
    AppManager.getInstance().getProgrammer().remoteVerify(n);
  }
  //----------------------------------------------------------------------
  // Implements interface
  public void nodeAdded(Node node)
  {
    PackageLogger.log.fine("### callback: new node added");
    displayNewNode(node);
  }
  //----------------------------------------------------------------------
  // Implements interface
  public void nodeDeleted(Node node)
  {
  }
  //----------------------------------------------------------------------
  // Implements interface
  public void nodeModified(Node node)
  {
  }
  //----------------------------------------------------------------------
  // Implements interface
  public void nodeSyncChanged(Node node)
  {
    Rectangle r = new Rectangle();
    node.getPainter().bounds(r);
    m_canvas.repaint(r.x, r.y, r.width, r.height);
  }
  //----------------------------------------------------------------------
  /**
   * Removes the browser instance from any observer lists it may be
   * registered with.
   */
  void browserClosing()
  {
    for (Iterator i = m_ctxt.nodes().nodeIterator(); i.hasNext(); )
    {
      Node n = (Node) i.next();

      n.removeNodeListener(this);
    }
  }
  //----------------------------------------------------------------------
  // Implements ValidateNodeName interface
  public boolean nodeNameAcceptable(String name)
  {
    if (name == null) return false;

    if (name.trim().length() == 0) return false;

    for (Iterator i = m_ctxt.nodes().nodeIterator(); i.hasNext(); )
    {
      Node n = (Node) i.next();

      if (n.name().equals(name)) return false;
    }
    return true;
  }
  //----------------------------------------------------------------------
  // Implement interface
  public void exportNodes(List nlist)
  {
    PackageLogger.log.warning("exportNodes(...) not implemented");
  }
  //----------------------------------------------------------------------
  // Implement interface
  public void exportNetwork(Network network)
  {
    AppManager.getInstance().getFileStorageManager()
      .saveNetwork(network, new File("/home/darrens/test.xml"));
  }
  //----------------------------------------------------------------------
  // Implement interface
  public void loadNetwork()
  {
    //    AppManager.getInstance().getFileStorageManager()
    //      .loadNetwork(new File("/home/darrens/test.xml"));
  }
  //----------------------------------------------------------------------
  /** Clear Network status bar. */
  protected void clearStatusBar(){
    m_statusBar.setSelected(null);
  }
}
