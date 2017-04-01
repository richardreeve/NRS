package nrs.core.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import nrs.core.base.BaseComponent;
import nrs.core.base.CIDManager;
import nrs.core.base.Pipeline;
import nrs.core.base.Message;
import nrs.core.base.MessageProcessor;
import nrs.core.base.MessageTools;
import nrs.core.base.OutboundPipeline;
import nrs.core.message.Constants;
import nrs.core.message.QueryCID;
import nrs.pml.PMLParser;
import nrs.util.ArgumentException;
import nrs.util.ArgumentFielder;

/**
 * <p>Manage the interrogation of an available port by sending the
 * appropriate sequences of PML messages.
 *
 * <h3>Registering Ports</h3>
 *
 * <p>To register a port for use with a {@link PortManager} :
 *
 * <ol>
 *
 * <p><li>First, construct a port object, which should be an instance of
 * a class derived from {@link CommsRoute}. The method {@link
 * #getFreePortID()} should be used for assigning a port ID.</li>
 *
 * <p><li>Second, call {@link #addPort(CommsRoute)} to register the unopened
 * port with a {@link PortManager}</li>
 *
 * <p><li>Finally, invoke {@link CommsRoute#open()} on the port</li>
 *
 * </ol>
 *
 * <p>The <tt>PortManager</tt> can be created in two modes: server and
 * client. They operate essentially the same, except for the handling of
 * on-connection events. Server objects respond to such events by
 * issuing a QueryCID to the new connection; client connections make no
 * such query.
 *
 * <h3>Sending Messages</h3>
 *
 * <code>PortManger</code> is used for sending messages to other NRS
 * components. A message is sent by passing it to {@link
 * #deliver(Message, MessageProcessor)}.
 *
 */
public class PortManager extends MessageProcessor
  implements PortConnectedListener, ArgumentFielder

{
  /** Set of ports being managed, each value is of type {@link
   *  CommsRoute}, while the key is a string representing the encoded
   *  route */
  private HashMap<String, CommsRoute> m_ports;

  /** Used and reused for constructing messasges */
  private ByteArrayOutputStream m_msg = new ByteArrayOutputStream();

  /** Needed for access VNIDs */
  private BaseComponent m_defNode;

  /** Pipeline to use for sending messages */
  private OutboundPipeline m_outPipeline;

  /** Next available port ID number. Is incremented whenever such a
   * number is requested, or if a port is added with a greater number,
   * in which case it is incremented past that number. */
  private int m_nextPortID = 0;// 1;

  /**
   * Default inbound pipeline to pass to port objects that are
   * automatically created by this class.
   */
  private Pipeline m_defaultInPipe;

  /**
   * Is this a client or a server connection? Server connections (such
   * as used by the main NRS.gui) will query ports as they become
   * connected. Client connections will not issue this query.
   */
  private boolean m_isServer = false;
    
  private int m_servers = 0;

  private CIDManager m_cidManager;

  //----------------------------------------------------------------------
  /**
   * Create a {@link PortManager} that provides a client mode connection
   */
  public PortManager()
  {
    m_cidManager = null;

    m_defNode = null;
    m_isServer = false;
    m_ports = new HashMap<String, CommsRoute>();
  }
  //----------------------------------------------------------------------
  /**
   * Create a {@link PortManager} that provides a server-mode connection
   *
   * @param node a {@link BaseComponent} object. Because this port
   * manager is operating in server-mode, it will, open detecting
   * connections, issues a <tt>QueryCID</tt> to the new connection. That
   * message will illicit a reply; this <tt>node</tt> argument specifies
   * the component object which will handle the reply.
   *
   * @param cidManager in server mode, this PortManager might allocate
   * CIDs to newly discovered components; hence it needs the object that
   * will provide unique CID values.
   */
  public PortManager(BaseComponent node,
                     CIDManager cidManager)
  {
    m_cidManager = cidManager;

    m_defNode = node;
    m_isServer = true;
    m_ports = new HashMap<String, CommsRoute>();
  }
  //----------------------------------------------------------------------
  /**
   * Provide the {@link Pipeline} that should be used for any
   * {@link CommsRoute} objects created by this class
   */
  public void setDefaultInboundPipeline(Pipeline p)
  {
    m_defaultInPipe = p;
  }
  //----------------------------------------------------------------------
  /**
   * Get a free port ID to use
   */
  public int getFreePortID()
  {
    return m_nextPortID++;
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link OutboundPipeline} this class will use when it needs
   * to submit {@link Message} objects for transmission to other NRS
   * components.
   */
  public void setOutboundPipeline(OutboundPipeline o)
  {
    m_outPipeline = o;
  }
  //----------------------------------------------------------------------
  /**
   * Add a new port to be managed
   */
  public void addPort(CommsRoute port)
  {
    if (port == null) return;

    m_ports.put(port.getID().getEncoding(), port);

    PackageLogger.log.fine("Port " + port + " registered with ID="
                           +  port.getID().getEncoding());

    port.addConnectionListener(this);

    if (port.getID().getValue() > m_nextPortID) { // >= to >
      m_nextPortID = port.getID().getValue();// removed +1 to value
    }
  }
  //----------------------------------------------------------------------
  /**
   * Remove a port from this management
   */
  public void removePort(CommsRoute port)
  {
    if (port == null) return;
    
    m_ports.remove(port.getID().getEncoding());
  }
  //----------------------------------------------------------------------
  /**
   * Send a QueryCID message down the port, to see what NRS component is
   * sitting at the other end.
   */
  private void queryPort(CommsRoute port)
  {
    PackageLogger.log.fine("Querying port \"" + port + "\"");

    QueryCID message = new QueryCID();

    String route = port.getID().getEncoding();

    String cid;
    if (m_cidManager.isAvailable(route))
    {
      cid = route;
    }
    else
    {
      cid = m_cidManager.obtain();

      PackageLogger.log.warning("Unable to use route " + route +
                                " for suggested"
                                + " CID. It seems to be already in use. Using "
                                + cid + " instead");
    }

    message.setFields(
        route,
        0,  // not using this
        "", // empty return route
        m_defNode.getVariable(nrs.core.message.Constants.MessageTypes.ReplyCID).getVNID(),
        "-1",  /* make negative */
        cid);

    // set optional intelligent message fields
    message.setNRSField(nrs.core.message.Constants.MessageFields.iForwardRoute, "");
    message.setNRSField(nrs.core.message.Constants.MessageFields.iReturnRoute, "");
    message.setNRSField(nrs.core.message.Constants.MessageFields.iTargetVNName,
                     nrs.core.message.Constants.MessageTypes.QueryCID);
    message.setNRSField(nrs.core.message.Constants.MessageFields.intelligent, "true");

    // set auxillary information
    message.aux().setSenderPort(port);  /* port to send on */

    if (m_outPipeline != null)
    {
      m_outPipeline.submit(message);
    }
    else
    {
      PackageLogger.log.severe("No outbound pipeline present for"
                               + " submitting messages");
    }
  }
  //----------------------------------------------------------------------
  /** Called when the <code>port</code> has connected
   *
   * @param port the {@link CommsRoute} which has just connected to
   * another NRS component
   **/
  public void portConnected(CommsRoute port)
  {
    // first thing to do is discover who we are connected to
    PackageLogger.log.fine("Port \"" + port + "\" connected");

    if (m_isServer)
    {
      PackageLogger.log.fine("Querying new connection, \"" + port + "\"");
      queryPort(port);
    }
  }
  //----------------------------------------------------------------------
  /** Indicates the <code>port</code> has disconnected */
  public void portDisconnected(CommsRoute port)
  {
    removePort(port); // remove port
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to route the received {@link Message} through an existing
   * port.
   *
   * <p>Before sending, the auxillary message field <tt>SendStatus</tt>
   * is set to false.  During the sending process this field will be
   * updated to reflect the success or failure in attempting to transmit
   * the message.
   *
   * <p>During sending, the first task is to identify an outbound port
   * to use.This is done by looking for the <tt>route</tt> field, and
   * taking the first hop. Thus the message must contain a valid
   * <tt>route</tt> field.
   *
   * <p>As part of the sending process, the value of the <tt>route</tt>
   * field is altered: the initial hop is chopped off. However, because
   * other parts of an application my later need the orginial value of
   * <tt>route</tt>, before it is altered it is optionally copied into
   * the message's auxillary data. If the {@link
   * nrs.core.base.AuxillaryInfo#getRoute()} is presently <tt>null</tt>
   * then {@link nrs.core.base.AuxillaryInfo#setRoute(String)} is called
   * with the original route value.

   * @see nrs.core.base.AuxillaryInfo#getRoute()
   * @see nrs.core.base.AuxillaryInfo#setRoute(String)
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    m.aux().setSendStatus(false);

    transmitMessage(m);

    next(m);
  }
  //----------------------------------------------------------------------
  /**
   * Handle the sending of a broadcast message
   */
  private synchronized void sendBroadcastMessage(Message m)
  {
    // Deal with hop count
    if (!m.hasNRSField(Constants.MessageFields.iHopCount))
    {
      PackageLogger.log.warning("Broadcast message lacks a hop count field"
                                + ": " + m);
      m.setNRSField(Constants.MessageFields.iHopCount, "5");
    }
    else if (MessageTools.decrementHop(m)) return;

    // Store the current value of iForwardRoute, if present
    String iForwardRoute = null;
    if (m.hasNRSField(Constants.MessageFields.iForwardRoute))
    {
      iForwardRoute = m.getNRSField(Constants.MessageFields.iForwardRoute);
    }

    // Attempt send on each port
    for (Iterator i = m_ports.values().iterator(); i.hasNext(); )
    {
      CommsRoute port = (CommsRoute) i.next();

      // Don't send on the received port
      if (port == m.aux().getReceivedPort()) continue;

      // Set the intelligent-field iForwardRoute
      if (iForwardRoute != null)
      {
        m.setNRSField(Constants.MessageFields.iForwardRoute,
                   iForwardRoute + port.getID().getEncoding());
      }

      try
      {
        // _BUG_(This will fail is a port is BMF)
        m.getPML(m_msg, true);
        PackageLogger.log.fine("Sending " + m_msg + " on port:" + port);
        port.write(m_msg);
        m.aux().setSendStatus(true);
        
        m_msg.reset();
      }
      catch (Exception e)
      {
        PackageLogger.log.severe("Failed to send message: " + e);
        e.printStackTrace();
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Perform the work of sending the message <tt>m</tt> on a port. The
   * <tt>route</tt> determines which port is used to send the message.
   *
   * <p>The message <tt>m</tt> should contain a {@link
   * nrs.core.message.Constants.MessageFields#route} field which
   * contains the route to the destination NRS component. This method
   * uses that route to identify which local port, out of however many
   * may be available, to actually transmit the message
   * through. Importantly, the value of the {@link
   * nrs.core.message.Constants.MessageFields#route} field is altered by
   * this method : the intial port hop will be chopped off.
   *
   * <p>However, because other parts of an application my later need the
   * orginial value of {@link
   * nrs.core.message.Constants.MessageFields#route}, before it is
   * altered it is copied into the message's auxillary data. If {@link
   * nrs.core.base.AuxillaryInfo#getRoute()} is presently <tt>null</tt>
   * then {@link nrs.core.base.AuxillaryInfo#setRoute(String)} is called
   * with the original route value.
   *
   * This method is synchronized to prevent multiple threads accessing it, and 
   * causing problems. 
   */
  private synchronized void transmitMessage(Message m)
  {
    m_msg.reset();

    // Handle broadcast messages

    if (MessageTools.isBroadcast(m))
    {
      sendBroadcastMessage(m);
    }
    else
    {
      final String msgRoute = m.getNRSField(Constants.MessageFields.route);

      try
      {
        // Discover the port
        CommsRoute port =
          (CommsRoute) m_ports.get(PortNumber.firstHop(msgRoute));

        if (port == null)
        {
          PackageLogger.log.severe("Unable to send message " + m
                                   + " because a local port can't be "
                                   + "identified with route:" + msgRoute);
          return;
        }

        // Optionally store original route before it is altered
        if (m.aux().getRoute() == null) m.aux().setRoute(msgRoute);

        // Alter the route field, so that this hop has been remove
        m.setNRSField(Constants.MessageFields.route,
                      PortNumber.chop(msgRoute));

        // Update the intelligent forward route field if its present
        if (m.hasNRSField(Constants.MessageFields.iForwardRoute))
        {
          m.setNRSField(Constants.MessageFields.iForwardRoute,
                        m.getNRSField(Constants.MessageFields.iForwardRoute)
                        + port.getID().getEncoding());
        }

        m.getPML(m_msg, true);
        PackageLogger.log.fine("Sending " + m_msg
                               + " on port:" + port);
        port.write(m_msg);
        m.aux().setSendStatus(true);
      }
      catch (IllegalArgumentException ae)
      {
        PackageLogger.log.severe("Failed to send message due to lack of"
                                 + " idenfiable port in route field:"
                                 + m.diagString()
                                 + "Exception:" + ae);
        ae.printStackTrace();
      }
      catch (Exception e)
      {
        PackageLogger.log.severe("Failed to send message: " + e);
        e.printStackTrace();
      }
    }
  }
  //----------------------------------------------------------------------
  public void displayHelp()
  {
    System.out.println("\t-F|--fifo-pml <in> <out>\tcommunicate in PML using two fifos");
    System.out.println("\t-S <port>\tcommunicate in PML using Sockets - "
		       + "act as server listening on <port>");
    System.out.println("\t-C <host> <port>\tcommunicate in PML using Sockets - "
		       +"act as client connecting to server at <host>:<port>");
  }
  //----------------------------------------------------------------------
  public List getOptions()
  {
    List options = new ArrayList();

    options.add("F");
    options.add("fifo-pml");

    options.add("S");
    options.add("C");

    return options;
  }
  //----------------------------------------------------------------------
  public void processOption(String option,
                            String [] args,
                            int index) throws ArgumentException
  {
    if (option.equals("F") || option.equals("fifo-pml"))
    {
      // ensure there are enough options
      if (index + 2 < args.length)
      {
        String fifoIn = args[index+1];
        String fifoOut = args[index+2];

        args[index+1] = null;
        args[index+2] = null;


        openFifoPml(fifoIn, fifoOut);
      }
      else
      {
        throw new ArgumentException(option, index, ArgumentException.MISSING);
      }
    }
    else if ( option.equals("S") ){
	// ensure there are enough options
	if (index + 1 < args.length){
	    int port = Integer.parseInt(args[index+1]);

	    args[index+1] = null;

	    openServer(port);
	}
	else{
	    throw new ArgumentException(option, index,
					ArgumentException.MISSING);
	}
    }
    else if ( option.equals("C") ){
	// ensure there are enough options
	if (index + 2 < args.length)
	    {
		String host= args[index+1];
		int port = Integer.parseInt(args[index+2]);

		args[index+1] = null;
		args[index+2] = null;

		openClient(host, port);
	    }
	else
	    {
		throw new ArgumentException(option, index,
					    ArgumentException.MISSING);
	    }

    }
  }
  //----------------------------------------------------------------------
    public void openFifoPml(String fifoIn, String fifoOut)
	throws ArgumentException
    {
	int ID = getFreePortID();

	try
	    {
		final FIFOCommsRoute fifo = new FIFOCommsRoute(ID,
							       "Local FIFO",
							       fifoIn,
							       fifoOut);

		PMLParser pmlParser = new PMLParser();
		fifo.setMesssageListener(pmlParser);

		pmlParser.setMessageCallbacks(fifo, m_defaultInPipe);

		addPort(fifo);

		fifo.open();
	    }
	catch (Exception e)
	    {
		PackageLogger.log.warning("Failed to open local connection, "
					  + e.toString());
		e.printStackTrace();
	    }
    }
    /** */
    public void openFifoPml(int id, String fifoIn, String fifoOut){
	try
	    {
		final FIFOCommsRoute fifo = new FIFOCommsRoute(id,
							       "Local FIFO",
							       fifoIn,
							       fifoOut);

		PMLParser pmlParser = new PMLParser();
		fifo.setMesssageListener(pmlParser);

		pmlParser.setMessageCallbacks(fifo, m_defaultInPipe);

		addPort(fifo);

		fifo.open();
	    }
	catch (Exception e)
	    {
		PackageLogger.log.warning("Failed to open local connection, "
					  + e.toString());
		e.printStackTrace();
	    }

    }
    //----------------------------------------------------------------------
    public void openServer(int port){
	try{
	    ServerSockets server = new ServerSockets(this,
						     m_defaultInPipe,
						     port);
            m_servers++;
	}
	catch(IOException ioe){
	    PackageLogger.log.warning(ioe.getMessage());
	    ioe.printStackTrace();
	}
    }
    //----------------------------------------------------------------------
    public void openClient(String host, int port){
	int ID = getFreePortID();

	try{
	    final SocketCommsRoute socket = new SocketCommsRoute(ID,
							   "Socket_"+ID,
							   host,
							   port);

	    PMLParser pmlParser = new PMLParser();
	    socket.setMesssageListener(pmlParser);

	    pmlParser.setMessageCallbacks(socket, m_defaultInPipe);

	    addPort(socket);

	    socket.open();
	}
	catch(Exception e){
	    PackageLogger.log.warning(e.getMessage());
	    e.printStackTrace();
	}
    }
    public void openClient(int id, String host, int port){
	try{
	    final SocketCommsRoute socket = new SocketCommsRoute(id,
							   "Socket_"+id,
							   host,
							   port);

	    PMLParser pmlParser = new PMLParser();
	    socket.setMesssageListener(pmlParser);

	    pmlParser.setMessageCallbacks(socket, m_defaultInPipe);

	    addPort(socket);

	    socket.open();
	}
	catch(Exception e){
	    PackageLogger.log.warning(e.getMessage());
	    e.printStackTrace();
	}
    }
  //----------------------------------------------------------------------
  /**
   * Return the highest number assigned to ports.  <b>Note</b>: do not
   * use this method for obtaining new port IDs. Use {@link
   * #getFreePortID()} instead.
   */
  public int getMaxPortID()
  {
    //return m_nextPortID-1;
    return m_nextPortID;
  }
  //----------------------------------------------------------------------
  /**
   * Lookup a {@link CommsRoute} port by the numerical ID
   *
   * @return the matched {@link CommsRoute} or null if the ID did not
   * match with any port
   */
  public CommsRoute getPort(int id)
  {
    Collection<CommsRoute> ports = m_ports.values();

    for (Iterator<CommsRoute> i = ports.iterator(); i.hasNext(); )
    {
      CommsRoute port = i.next();

      if (port.getID().getValue() == id) return port;
    }

    return null;
  }


  public void setCIDManager(CIDManager m_cidMan){
    m_cidManager = m_cidMan;
  }

  public void setComponentNode(BaseComponent node){
    m_defNode = node;
  }

  public void setIsServer(){
    m_isServer = true;
  }

  public int numOfServers(){
    return m_servers;
  }

  public int numOpenPorts(){
    return m_ports.size();
  }
}
