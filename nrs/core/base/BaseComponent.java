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
package nrs.core.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import nrs.core.comms.PortManager;
import nrs.core.comms.CommsRoute;
import nrs.core.message.*;

import static nrs.core.message.Constants.Fields.*;

/**
 * Base class for all NRS Java components. Creates and contains all the
 * of standard {@link Variable} objects that an NRS component must
 * possess. Message handlers for these variables are also provided;
 * derived classes will typically override some or all of them.
 *
 * Should use a naming convention for subclasses, e.g. SkeletonComponent.
 * Concatenate "Application name" with "Component".
 *
 * @author Thomas French
 * @author Darren Smith
 */
public class BaseComponent extends MessageProcessor
{
  /** Name of component. */
  private String m_Name;
  /** Component type. */
  private String m_CType;
  /** Component version. */
  private String m_Version;
  /** Whether this component has a CSL descriptor file. */
  private boolean m_hasCSL;
  /** Can the component speak BMF. */
  private boolean m_SpeaksBMF;
  /** Can the component speak PML. */
  private boolean m_SpeaksPML;

  /** VariableManager for this component. */
  protected VariableManager m_vMan;

  private ComponentInfo m_info;

  private MessageProcessor m_outDest;

  /**
   * Contains the set of default variables belonging to this node. Each
   * object in the collection is of type {@link Variable}.
     */
  protected HashMap m_localVars;

  /** PortManager for this component. */
  private PortManager m_portManager;

  /** RouteManager for this component. */
  protected RouteManager m_routeManager;

  /** List of root Nodes for this component. */
  private ArrayList<Node> m_rootNodes;

  //----------------------------------------------------------------------
   /** Constructor
   *
   * @param vm {@link VariableManager} to register variables with
   * @param name name of this component
   * @param ctype CType fo this component
   * @param speaksBMF whether this component speaks BMF
   * @param speaksPML whether this component speaks PML
   * @param hasCSL whether this component has a CSL file
   * @param version version of this component
   *
   * @deprecated the <tt>name</tt> parameter is no longer used, so use a
   * constructor that does not have this argument.
   */
    public BaseComponent(VariableManager vm,
                       String name,
                       String ctype,
                       boolean speaksBMF,
                       boolean speaksPML,
                       boolean hasCSL,
                       String version)
  {
    this(vm,
         ctype,
         speaksBMF,
         speaksPML,
         hasCSL,
         version,
         null);
  }
  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param vm {@link VariableManager} to register variables with
   * @param ctype CType fo this component
   * @param speaksBMF whether this component speaks BMF
   * @param speaksPML whether this component speaks PML
   * @param hasCSL whether this component has a CSL file
   * @param version version of this component
   */
  public BaseComponent(VariableManager vm,
                       String ctype,
                       boolean speaksBMF,
                       boolean speaksPML,
                       boolean hasCSL,
                       String version)
  {
    this(vm,
         ctype,
         speaksBMF,
         speaksPML,
         hasCSL,
         version,
         null);
  }
  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param vm {@link VariableManager} to register variables with
   * @param name name of this component
   * @param ctype CType fo this component
   * @param speaksBMF whether this component speaks BMF
   * @param speaksPML whether this component speaks PML
   * @param hasCSL whether this component has a CSL file
   * @param version version of this component
   * @param cid the CID for this component
   *
   * @deprecated the <tt>name</tt> parameter is no longer used, so use a
   * constructor that does not have this argument.
   */
  public BaseComponent(VariableManager vm,
                       String name,
                       String ctype,
                       boolean speaksBMF,
                       boolean speaksPML,
                       boolean hasCSL,
                       String version,
                       String cid)
  {
    this(vm, ctype, speaksBMF, speaksPML, hasCSL, version, cid);
  }
  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param vm {@link VariableManager} to register variables with
   * @param ctype CType fo this component
   * @param speaksBMF whether this component speaks BMF
   * @param speaksPML whether this component speaks PML
   * @param hasCSL whether this component has a CSL file
   * @param version version of this component
   * @param cid the CID for this component
   *
   */
  public BaseComponent(VariableManager vm,
                       String ctype,
                       boolean speaksBMF,
                       boolean speaksPML,
                       boolean hasCSL,
                       String version,
                       String cid)
  {
    m_vMan = vm;

    m_CType = ctype;
    m_hasCSL = hasCSL;
    m_Version = version;
    m_SpeaksBMF = speaksBMF;
    m_SpeaksPML = speaksPML;

    m_info = new ComponentInfo(m_CType, cid, "", m_SpeaksBMF, m_SpeaksPML, m_Version);

    m_rootNodes = new ArrayList<Node>();

    m_localVars = new HashMap();

    m_localVars.put(Constants.MessageTypes.CreateNode,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.CreateNode)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_CreateNode(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.CreateNode;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.DeleteNode,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.DeleteNode)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_DeleteNode(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.DeleteNode;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.CreateLink,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.CreateLink)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_CreateLink(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.CreateLink;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.DeleteLink,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.DeleteLink)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_DeleteLink(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.DeleteLink;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryMaxLink,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryMaxLink)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryMaxLink(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryMaxLink;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyMaxLink,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyMaxLink)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyMaxLink(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyMaxLink;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryLink,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryLink)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryLink(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryLink;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyLink,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyLink)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyLink(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyLink;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryVNID,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryVNID)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryVNID(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryVNID;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyVNID,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyVNID)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyVNID(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyVNID;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryVNName,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryVNName)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryVNName(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryVNName;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyVNName,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyVNName)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyVNName(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyVNName;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryVNType,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryVNType)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryVNType(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryVNType;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyVNType,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyVNType)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyVNType(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyVNType;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryMaxVNID,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryMaxVNID)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryMaxVNID(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryMaxVNID;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyMaxVNID,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyMaxVNID)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyMaxVNID(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyMaxVNID;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryCID,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryCID)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryCID(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryCID;
                      }
                    });
    
    m_localVars.put(Constants.MessageTypes.ReplyCID,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyCID)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyCID(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyCID;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryMaxPort,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryMaxPort)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryMaxPort(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryMaxPort;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyMaxPort,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyMaxPort)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyMaxPort(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyMaxPort;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryCType,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryCType)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryCType(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryCType;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyCType,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyCType)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyCType(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyCType;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryCSL,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryCSL)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryCSL(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryCSL;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyCSL,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyCSL)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyCSL(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyCSL;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.QueryRoute,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryRoute)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryRoute(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryRoute;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyRoute,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyRoute)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyRoute(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyRoute;
                      }
                    });


    m_localVars.put(Constants.MessageTypes.QueryPort,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.QueryPort)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_QueryPort(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.QueryPort;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.ReplyPort,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.ReplyPort)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_ReplyPort(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.ReplyPort;
                      }
                    });

    m_localVars.put(Constants.MessageTypes.Reset,
                    new Variable(vm.suggestID(),
                                 Constants.MessageTypes.Reset)
                    {
                      public void deliver(Message m)
                      {
                        handleMessageAt_Reset(m, this, checkType(m, this));
                      }
                      public String getVNType(){
                        return Constants.MessageTypes.Reset;
                      }
                    });


    // Now register all these local variables with the application
    // variable manager - so it can route messages to these variables
    // soley using a VNID
    for (Iterator i = m_localVars.values().iterator(); i.hasNext(); )
    {
      vm.add((Variable) i.next());
    }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to route the received message to one of the {@link
   * nrs.core.base.Variable} objects within this node.
   *
   * @return true if the message was successfully routed, false otherwise
   */
  protected boolean dispatch(Message m)
    {
      Variable target = getVariable(m.getType());

	if (target != null)
	    {
		target.deliver(m);
		return true;
	    }
	else
	    {
		PackageLogger.log.warning("Failed to route message " + m
					  + " because no matching variable type found");
		return false;
	    }
    }
    //----------------------------------------------------------------------
    /**
     * Attempt to route the received message to one of the {@link
     * nrs.core.base.Variable} objects within this node. If the {@link
     * Message} could not be delivered it is passed to the next module.
     *
     * @see #setDest(MessageProcessor)
     */
  public void deliver(Message m, MessageProcessor sender)
  {
    if (dispatch(m) == false) next(m);
  }
  //----------------------------------------------------------------------
  /**
   * Retreive a local variable by its name, or <tt>null</tt> if there is
   * no match
   *
   * @param VNName the name of the variable to retrieve
   */
  public Variable getVariable(String VNName)
  {
    return (Variable) m_localVars.get(VNName);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the CreadeNode variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_CreateNode(Message m,
                                         Variable v,
                                         boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the DeleteNode variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_DeleteNode(Message m,
                                         Variable v,
                                         boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the CreateLink variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_CreateLink(Message m,
                                         Variable v,
                                         boolean diff){
    // Extract message fields
    int vnid, targetVNID;
    String cid, targetCID;
    boolean temporary, forSource;

    try{
      forSource = Boolean.parseBoolean(m.checkField(F_sourceNotTarget));
      cid = m.checkField(F_cid);
      vnid = Integer.parseInt(m.checkField(F_vnid));
      targetCID = m.checkField(F_targetCID);
      targetVNID = Integer.parseInt(m.checkField(F_targetVNID));
      temporary = Boolean.parseBoolean(m.checkField(F_temporary));
    }
    catch (FieldNotFoundException e){
      PackageLogger.log.warning(m.getType() + " message missing "
                                + e.fieldName() + " field; ignoring");
      return;
    }

    String route = null;
    String myCID = info().getCID();

    // Ignore create-link request if we are neither the source nor
    // target component
    if ( !cid.equals(myCID) && !targetCID.equals(myCID) ){
      PackageLogger.log.warning("This component is neither the source "
                                + "nor target for the CreateLink message"
                                + "; ignoring");
      return;
    }

    Variable source = findVar(cid, vnid);

    if ( forSource && source == null ){
      PackageLogger.log.warning("No matching source variable for"
                                + " cid=" + cid
                                + ", vnid=" + vnid);
      return;
    }

    Variable target = findVar(targetCID, targetVNID);

    Link lk;

    // create on-board link
    if ( source != null && target != null )
      {
        lk = new Link(source, target);
      }
    // create off-board link
    else
      {
        if ( forSource && cid.equals(myCID) )
          route = m_routeManager.resolveRoute(targetCID, true);
        else if ( !forSource && targetCID.equals(myCID) )
          route = m_routeManager.resolveRoute(cid, true);

        if ( route == null ) route = "";

        lk = new Link(cid, vnid, targetCID, targetVNID,
                      temporary, route, m_routeManager, myCID);
      }

    if ( forSource && cid.equals(myCID) )
      {
        PackageLogger.log.fine("CreateLink aimed at Source.");
        source.addLink(lk);
      }
    else if ( !forSource && targetCID.equals(myCID) )
      {
        PackageLogger.log.fine("CreateLink aimed at Target.");
        target.addLink(lk);
      }

    PackageLogger.log.fine("Link created");
  }
  //----------------------------------------------------------------------
  public void handleMessageAt_DeleteLink(Message m,
                                         Variable v,
                                         boolean diff)
  {
    // Extract message fields
    boolean forSource;
    int vnid, targetVNID;
    String cid, targetCID;

    try{
      forSource = Boolean.parseBoolean(m.checkField(F_sourceNotTarget));
      cid = m.checkField(F_cid);
      vnid = Integer.parseInt(m.checkField(F_vnid));
      targetCID = m.checkField(F_targetCID);
      targetVNID = Integer.parseInt(m.checkField(F_targetVNID));
    }
    catch (FieldNotFoundException e)
      {
        PackageLogger.log.warning(m.getType() + " message missing "
                                  + e.fieldName() + " field; ignoring");
        return;
      }

    String myCID = info().getCID();

    // Ignore delete-link request if we are neither the source nor
    // target component
    if ( !cid.equals(myCID) && !targetCID.equals(myCID) ){
      PackageLogger.log.warning("This component is neither the source "
                                + "nor target for the DeleteLink message"
                                + "; ignoring");
      return;
    }

    // Get variable
    Variable var = null;

    if ( forSource && cid.equals(myCID) )
      var = m_vMan.get(vnid); // I'm source variable
    else if ( !forSource && targetCID.equals(myCID) )
      var = m_vMan.get(targetVNID); // I'm target variable

    // Filter out problems
    if (var == null)
      {
        PackageLogger.log.warning("Can't process DeleteLink: no"
                                  + " variable found"
                                  + " with VNID="
                                  + ( forSource ? vnid : targetVNID) );
        return;
      }

    // remove Link from Variable
    if ( forSource && cid.equals(myCID) ){ // for source
      PackageLogger.log.fine("DeleteLink message aimed at link-source "
                             + "vnid: " + vnid
                             + ", cid: " + cid);
      var.deleteSourceLink(targetVNID, targetCID);
    }
    else if ( !forSource && targetCID.equals(myCID) ) { // for target
      PackageLogger.log.fine("DeleteLink message aimed at link-target "
                             + "vnid: " + targetVNID
                             + ", cid: " + targetCID);
      var.deleteTargetLink(vnid, cid);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryMaxLink variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryMaxLink(Message m,
                                         Variable v,
                                         boolean diff) {
    if (diff)
      {
        PackageLogger.log.warning("Unexpected message " + m + " received at"
                                  + " variable " + v + ": message ignored");
        return;
      }

    /* Extract field values */

    String route = MessageTools.findRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");
    Boolean sourceNotTarget = null;

    int vnid = -1;
    if (m.hasField(Constants.MessageFields.vnid))
    {
      try
      {
        vnid =
          Integer.parseInt(m.getField(Constants.MessageFields.vnid));
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning(Constants.MessageFields.vnid + " field in"
                                  + " " + m + " is not numeric: can't parse");
        // this error will be handled below
      }
    }

    if (vnid == -1)
      {
        PackageLogger.log.warning("No valid value for the "
                                  + Constants.MessageFields.vnid
                                  + " field in <" + m + "> message:"
                                + " unable to send ReplyMaxLink response");
        PackageLogger.log.warning("<" + m + "> message:" + m.diagString());
        return;
      }

    if ( m.hasField(Constants.MessageFields.sourceNotTarget) ){
      sourceNotTarget = Boolean.valueOf(m.getField(Constants.
                                                   MessageFields.
                                                   sourceNotTarget));
    }
    else {
      PackageLogger.log.warning("sourceNotTarget cannot be null");
      return;
    }

    Variable vFound = m_vMan.get(vnid);
    int links = 10;
    if (vFound == null)
      {
        PackageLogger.log.warning("Supplied vnid ("+vnid+") does not exist");

        // send failed acknowledge message
        return;
      }
    else
      {
        if ( sourceNotTarget )
          links = vFound.getSourceMaxLinks();
        else
          links = vFound.getTargetMaxLinks();
      }

    /* Build reply message */

    ReplyMaxLink r = new ReplyMaxLink();

    r.setFields(route, toVNID, replyMsgID, links);

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyMaxLink);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);

  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyMaxLink variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyMaxLink(Message m,
                                         Variable v,
                                         boolean diff) {
    PackageLogger.log.fine("Received ReplyMaxLink");
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryLink variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryLink(Message m,
                                         Variable v,
                                         boolean diff) {
     if (diff)
      {
        PackageLogger.log.warning("Unexpected message " + m + " received at"
                                  + " variable " + v + ": message ignored");
        return;
      }

    /* Extract field values */

    String route = MessageTools.findRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");
    Boolean sourceNotTarget = null;
    int link = -1;

    int vnid = -1;
    if (m.hasField(Constants.MessageFields.vnid))
    {
      try
      {
        vnid =
          Integer.parseInt(m.getField(Constants.MessageFields.vnid));
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning(Constants.MessageFields.vnid + " field in"
                                  + " " + m + " is not numeric: can't parse");
        // this error will be handled below
      }
    }

    if (vnid == -1)
      {
        PackageLogger.log.warning("No valid value for the"
                                  + Constants.MessageFields.vnid
                                  + " field in <" + m + "> message:"
                                + " unable to send ReplyVNType response");
        PackageLogger.log.warning("<" + m + "> message:" + m.diagString());
        return;
      }

    if ( m.hasField(Constants.MessageFields.sourceNotTarget) ){
      sourceNotTarget = Boolean.valueOf(m.getField(Constants.
                                                   MessageFields.
                                                   sourceNotTarget));
    }
    else {
      PackageLogger.log.warning("sourceNotTarget cannot be null");
      return;
    }

    if ( m.hasField(Constants.MessageFields.link) ){
      try
        {
          link =
            Integer.parseInt(m.getField(Constants.MessageFields.link));
        }
      catch (NumberFormatException e)
        {
          PackageLogger.log.warning(Constants.MessageFields.link + " field in"
                                    + " " + m + " is not numeric: can't parse");
          // this error will be handled below
        }
    }
    else{
      PackageLogger.log.warning("Illegal QueryLink message: no link field"
                                + " specified");
      return;
    }

    if ( link < 1 ){
      PackageLogger.log.warning("Received QueryLink message with illegal"
                                + " link value");
      return;
    }

    Variable vFound = m_vMan.get(vnid);
    Link ln = null;
    if (vFound == null)
      {
        PackageLogger.log.warning("Supplied vnid ("+vnid+") does not exist");

        // send failed acknowledge message
        return;
      }
    else
      {
        if ( sourceNotTarget )
          ln = vFound.getSourceLink(link);
        else
          ln = vFound.getTargetLink(link);
      }

    if ( ln == null ){
      PackageLogger.log.warning("No link found with link index: " + link);

      // send failed acknowledge message

      return;
    }

    /* Build reply message */

    ReplyLink r = new ReplyLink();

    String linkCID;
    int linkVNID;
    String logPort = "";
    boolean resolved = true;

    // check if on-board
    if ( ln.getSourceRef() != null && ln.getTargetRef() != null ){
      Variable other;
      if ( sourceNotTarget ){
        other = ln.getTargetRef();
        linkVNID = other.getVNID();
      }
      else{
        other = ln.getSourceRef();
        linkVNID = other.getVNID();
      }
      linkCID = info().getCID(); // both on this component
    }
    else { // off-board
      if ( sourceNotTarget )
        {
          linkCID = ln.getTargetCID();
          linkVNID = ln.getTargetVNID().intValue();
        }
      else
        {
          linkCID = ln.getSourceCID();
          linkVNID = ln.getSourceVNID().intValue();
        }
    }

    r.setFields(route, toVNID, replyMsgID, linkCID, linkVNID,
                logPort, resolved);

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyLink);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyLink variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyLink(Message m,
                                         Variable v,
                                         boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryVNID variable. Default implementation is to retreive
   * the VNID of the variable named in the message, and send back a
   * reply containing that information.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryVNID(Message m,
                                        Variable v,
                                        boolean diff)
  {
    if (diff)
    {
      PackageLogger.log.warning("Unexpected message " + m + " received at"
                                + " variable " + v + ": message ignored");
      return;
    }

    if (m_outDest != null) try
    {
      /* Build message */
      Message r = m_vMan.buildReplyToQueryVNID(m);

      /* Send */
      m_outDest.deliver(r, this);
    }
    catch (FieldNotFoundException fe)
    {
      PackageLogger.log.warning("Unable to send "
                                + Constants.MessageTypes.ReplyVNID
                                + " : "
                                + fe.getMessage());
    }
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyVNID variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyVNID(Message m,
                                        Variable v,
                                        boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryVNName variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryVNName(Message m,
                                          Variable v,
                                          boolean diff) {
      m_vMan.replyToQueryVNName(m);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyVNName variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyVNName(Message m,
                                          Variable v,
                                          boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Accepts <tt>QueryVNType</tt> messages and attempts to issue an
   * appropriate response by interogating the available variable manager
   * to find the type of the variable that is identified in the message.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryVNType(Message m,
                                          Variable v,
                                          boolean diff)
  {
    if (diff == true)
    {
      PackageLogger.log.warning("Variable " + v + " does not support messages "
                                + " of type " + m + "; message ignored");
      return;
    }

    /* Extract field values */

    String route = MessageTools.findRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");
    String vnType;

    int vnid = -1;
    if (m.hasField(Constants.MessageFields.vnid))
    {
      try
      {
        vnid =
          Integer.parseInt(m.getField(Constants.MessageFields.vnid));
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning(Constants.MessageFields.vnid + " field in"
                                  + " " + m + " is not numeric: can't parse");
        // this error will be handled below
      }
    }

    if (vnid == -1)
    {
      PackageLogger.log.warning("No valid value for the"
                                + Constants.MessageFields.vnid
                                + " field in <" + m + "> message:"
                                + " unable to send ReplyVNType response");
      PackageLogger.log.warning("<" + m + "> message:" + m.diagString());
      return;
    }

    Variable vFound = m_vMan.get(vnid);
    if (vFound == null)
    {
      vnType = "";
    }
    else
    {
      vnType = vFound.getVNType();
    }

    /* Build message */

    ReplyVNType r = new ReplyVNType();

    r.setFields(route, toVNID, replyMsgID, vnType);

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyVNType);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyVNType variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyVNType(Message m,
                                          Variable v,
                                          boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryMaxVNID variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryMaxVNID(Message m,
                                           Variable v,
                                           boolean diff) {
    if (diff)
    {
      PackageLogger.log.warning("Unexpected message " + m + " received at"
                                + " variable " + v + ": message ignored");
      return;
    }

    if (m_vMan == null)
    {
      PackageLogger.log.warning("BaseComponent cannot reply to QueryMaxVNID"
                                + " because no VariableManager object has been"
                                + " provided");
      return;
    }

    /* Extract field values */

    String route = MessageTools.findReturnRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");

    /* Build message */

    ReplyMaxVNID r = new ReplyMaxVNID();

    r.setFields(route,
                toVNID,
                replyMsgID,
                m_vMan.getMaxID());

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyMaxVNID);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyMaxVNID variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyMaxVNID(Message m,
                                           Variable v,
                                           boolean diff) { }
  //---------------------------------------------------------------------- 
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryCID variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryCID(Message m,
                                       Variable v,
                                       boolean diff) {

      String cid = info().getCID();
      if ( cid == null || cid.equals("") )
	  info().setCID(m.getField(Constants.MessageFields.cid));

      ReplyCID r = new ReplyCID();

      String route = m.getNRSField(Constants.MessageFields.iReturnRoute);
      if (route == null)
	  {
	      route = m.getField(Constants.MessageFields.returnRoute);
	  }
      if (route == null)
	  {
	      PackageLogger.log.warning("No return route in " + m + " query:"
					+ " unable to reply");
	      return;
	  }

      int toVNID;
      String toVNName;

      if (m.hasField(Constants.MessageFields.returnToVNID)){
	      try
	        {
	            toVNID =
	       	  Integer.parseInt(m.getField(Constants.MessageFields.returnToVNID));
	             toVNName = null;
	         }
	      catch (NumberFormatException e)
		  {
		      PackageLogger.log.warning(Constants.MessageFields.toVNID
						+ " field in"
						+ " " + m
						+ " is not numeric: can't parse"
						+ "; unable to reply");
		      return;
		  }
      }
      else{
	  toVNID = 0;  /* zero is reserved; means null */
	  toVNName = m.getField(Constants.MessageTypes.ReplyCID);
      }

      // the source message should provide a msg ID: otherwise we can't
      // send back a replyMsgID
      String replyMsgID = m.getField(Constants.MessageFields.msgID);
      if (replyMsgID == null) replyMsgID = "";

      String port = m.getField(Constants.MessageFields.port);
      if (port == null) port = "";

      r.setFields(route,
		  toVNID,
		  m.getField(Constants.MessageFields.msgID),
		  info().getCID());

      if (toVNName != null){
	  r.setNRSField(Constants.MessageFields.iTargetVNName,
		     toVNName);
	  r.setNRSField(Constants.MessageFields.intelligent, "true");
      }

      //      m_outPipeline.submit(r);
      if (getOutboundDest() != null) getOutboundDest().deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyCID variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyCID(Message m,
                                       Variable v,
                                       boolean diff) { }
  //----------------------------------------------------------------------
    /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryCType variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryCType(Message m,
                                         Variable v,
                                         boolean diff) {

      ReplyCType r = new ReplyCType();

      String route = m.getNRSField(Constants.MessageFields.iReturnRoute);
      if (route == null)
	  {
	      route = m.getField(Constants.MessageFields.returnRoute);
	  }
      if (route == null)
	  {
	      PackageLogger.log.warning("No return route in " + m + " query:"
					+ " unable to reply");
	      return;
	  }

      int toVNID;
      String toVNName;

      if (m.hasField(Constants.MessageFields.returnToVNID)){
	      try
	        {
	            toVNID =
	       	  Integer.parseInt(m.getField(Constants.MessageFields.returnToVNID));
	             toVNName = null;
	         }
	      catch (NumberFormatException e)
		  {
		      PackageLogger.log.warning(Constants.MessageFields.toVNID
						+ " field in"
						+ " " + m
						+ " is not numeric: can't parse"
						+ "; unable to reply");
		      return;
		  }
      }
      else{
	  toVNID = 0;  /* zero is reserved; means null */
	  toVNName = m.getField(Constants.MessageTypes.ReplyCType);
      }

      // the source message should provide a msg ID: otherwise we can't
      // send back a replyMsgID
      String replyMsgID = m.getField(Constants.MessageFields.msgID);
      if (replyMsgID == null) replyMsgID = "";

      r.setFields(route,
		  toVNID,
		  m.getField(Constants.MessageFields.msgID),
		  m_CType,
		  m_Version);

      if (toVNName != null){
	  r.setNRSField(Constants.MessageFields.iTargetVNName,
		     toVNName);
	  r.setNRSField(Constants.MessageFields.intelligent, "true");
      }

      //      m_outPipeline.submit(r);
      if (getOutboundDest() != null) getOutboundDest().deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyCType variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyCType(Message m,
                                         Variable v,
                                         boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryCSL variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryCSL(Message m,
                                       Variable v,
                                       boolean diff) {

      ReplyCSL r = new ReplyCSL();

       String route = m.getNRSField(Constants.MessageFields.iReturnRoute);
      if (route == null)
	  {
	      route = m.getField(Constants.MessageFields.returnRoute);
	  }
      if (route == null)
	  {
	      PackageLogger.log.warning("No return route in " + m + " query:"
					+ " unable to reply");
	      return;
	  }

      int toVNID;
      String toVNName;

      if (m.hasField(Constants.MessageFields.returnToVNID)){
	      try
	        {
	            toVNID =
	       	  Integer.parseInt(m.getField(Constants.MessageFields.returnToVNID));
	             toVNName = null;
	         }
	      catch (NumberFormatException e)
		  {
		      PackageLogger.log.warning(Constants.MessageFields.toVNID
						+ " field in"
						+ " " + m
						+ " is not numeric: can't parse"
						+ "; unable to reply");
		      return;
		  }
      }
      else{
	  toVNID = 0;  /* zero is reserved; means null */
	  toVNName = m.getField(Constants.MessageTypes.ReplyCSL);
      }

      // the source message should provide a msg ID: otherwise we can't
      // send back a replyMsgID
      String replyMsgID = m.getField(Constants.MessageFields.msgID);
      if (replyMsgID == null) replyMsgID = "";

      r.setFields(route,
		  toVNID,
		  m.getField(Constants.MessageFields.msgID));

      if (toVNName != null){
	  r.setNRSField(Constants.MessageFields.iTargetVNName,
		     toVNName);
	  r.setNRSField(Constants.MessageFields.intelligent, "true");
      }

      //need to read in CSL file
      //File csl_file = new File(AppManager.getInstance().getCSLFile());
      //r.setContents();

      //      m_outPipeline.submit(r);
      if (getOutboundDest() != null) getOutboundDest().deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyCSL variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyCSL(Message m,
                                       Variable v,
                                       boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Accepts <tt>QueryRoute</tt> messages and attempts to issue an
   * appropriate reponse.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryRoute(Message m,
                                         Variable v,
                                         boolean diff)
  {
    if (diff)
    {
      PackageLogger.log.warning("Unexpected message " + m + " received at"
                                + " variable " + v + ": message ignored");
      return;
    }

    /* Extract field values */

    String route = MessageTools.findRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");
    String forwardRoute = MessageTools.findForwardRoute(m);
    String returnRoute = MessageTools.findReturnRoute(m);
    String translationCount = MessageTools.findTranslationCount(m);

    /* Verify values */

    if (MessageTools.verify(Constants.MessageFields.iForwardRoute,
                            forwardRoute, m)) return;
    if (MessageTools.verify(Constants.MessageFields.iReturnRoute,
                            returnRoute, m)) return;

    /* Build message */

    ReplyRoute r = new ReplyRoute();

    r.setFields(route,
                toVNID,
                replyMsgID,
                forwardRoute,
                returnRoute,
                translationCount);

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyRoute);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyRoute variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyRoute(Message m,
                                         Variable v,
                                         boolean diff) {
      m_routeManager.deliver(m, this);
  }
  //----------------------------------------------------------------------
  /**
   * If a {@link PortManager} object has been supplied then a reply to
   * this message will be generated. Otherwise no action is taken.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryMaxPort(Message m,
                                           Variable v,
                                           boolean diff)
  {
    if (diff)
    {
      PackageLogger.log.warning("Unexpected message " + m + " received at"
                                + " variable " + v + ": message ignored");
      return;
    }

    if (m_portManager == null)
    {
      PackageLogger.log.warning("BaseComponent cannot reply to QueryMaxPort"
                                + " because no PortManager object has been"
                                + " provided");
      return;
    }

    /* Extract field values */

    String route = MessageTools.findReturnRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");

    /* Build message */

    ReplyMaxPort r = new ReplyMaxPort();

    r.setFields(route,
                toVNID,
                replyMsgID,
                m_portManager.getMaxPortID());

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyMaxPort);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyMaxPort variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyMaxPort(Message m,
                                           Variable v,
                                           boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the QueryPort variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_QueryPort(Message m,
                                        Variable v,
                                        boolean diff)
  {
    if (diff)
    {
      PackageLogger.log.warning("Unexpected message " + m + " received at"
                                + " variable " + v + ": message ignored");
      return;
    }

    if (m_portManager == null)
    {
      PackageLogger.log.warning("BaseComponent cannot reply to " + m
                                + " because no PortManager object has been"
                                + " provided");
      return;
    }

    /* Extract field values */

    if (!m.hasField(F_port))
    {
      PackageLogger.log.warning(m +" message lacks field"
                                + " " + F_port + "; ignoring");
      return;
    }

    String route = MessageTools.findReturnRoute(m);
    int toVNID = MessageTools.findToVNID(m);
    String replyMsgID = MessageTools.findReplyMsgID(m, "");
    int port = Integer.parseInt(m.getField(F_port));

    /* Build message */

    ReplyPort r = new ReplyPort();

    CommsRoute cr = m_portManager.getPort(port);
    String encoding = "";
    if ( cr != null ) encoding = cr.getID().getEncoding();
    
    r.setFields(route,
                toVNID,
                replyMsgID,
                encoding);

    /* Fixes */

    MessageTools.fixToVNID(r, Constants.MessageTypes.ReplyPort);
    MessageTools.fixRoute(r, m);

    /* Verify fields */

    if (MessageTools.verifyRoute(r)) return;

    /* Send */

    if (m_outDest != null) m_outDest.deliver(r, this);
  }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the ReplyPort variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_ReplyPort(Message m,
                                        Variable v,
                                        boolean diff) { }
  //----------------------------------------------------------------------
  /**
   * Override in inherited classes to process the receipt of a message
   * at the Reset variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Reset(Message m,
                                        Variable v,
                                        boolean diff) {
    // iterate through root nodes
    for(Node n : m_rootNodes)
      n.reset();
  }
  //----------------------------------------------------------------------
  /**
   * Return {@link ComponentInfo} associated with this component
   */
  public ComponentInfo info()
  {
    return m_info;
  }
  //----------------------------------------------------------------------
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v)
  {
    return !(m.getType().equals(v.getVNType()));
  }

  /**
   * Get the cType of this NRS component.
   *
   * @deprecated Use <tt>info().getCType()</tt> instead.
   *
   * @return the name of the component or null if the name is not yet known
   */
  public String getName()
  {
    return m_info.getCType();
  }

  /**
   * Get whether this component has a CSL file.
   */
  public boolean getHasCSL(){
    return m_hasCSL;
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link MessageProcessor} this class will use when it needs
   * to submit {@link Message} objects for transmission to other NRS
   * components.
   */
  public void setOutboundDest(MessageProcessor o)
  {
    m_outDest = o;
  }
  //----------------------------------------------------------------------
  /**
   * Get the {@link MessageProcessor} this class will use when it needs
   * to submit {@link Message} objects for transmission to other NRS
   * components.
   */
  public MessageProcessor getOutboundDest()
  {
    return m_outDest;
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link PortManager} object.  If a {@link PortManager}
   * object is available then this class will automatically be able to
   * handle QueryMaxPort requests.
   */
  public void setPortManager(PortManager portManager)
  {
    m_portManager = portManager;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link PortManager} object assigned this class. Can
   * return null if none has been assigned.
   */
  public PortManager getPortManager()
  {
    return m_portManager;
  }

  //----------------------------------------------------------------------
  /**
   * Provide a {@link RouteManager} object.
   */
  public void setRouteManager(RouteManager routeManager)
  {
    m_routeManager = routeManager;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link RouteManager} object assigned this class. Can
   * return null if none has been assigned.
   */
  public RouteManager getRouteManager()
  {
    return m_routeManager;
  }
  //----------------------------------------------------------------------

    /** Check whether a Variable with vnid or vname exists in this component.
     *
     * @param cid cid of component with variable vnid or vname
     * @param vnid vnid of variable being queried
     * @param vname vname of variable being queried
     *
     * @return null if Variable not found
     */
    protected Variable findVar(String cid, int vnid, String vname){
	if ( !m_info.getCID().equals(cid) )
	    return null;

	if ( vnid == 0 && (vname == null || vname.equals("")) )
	    return null;

	Variable v = m_vMan.get(vnid);
	if ( v != null )
	    return v;

	v = m_vMan.get(vname);
	if ( v != null )
	    return v;

	return null;
    }

    /** Check whether a Variable with vnid exists in this component.
     *
     * @param cid cid of component with variable vnid or vname
     * @param vnid vnid of variable being queried
     *
     * @return null if Variable not found
     */
    protected Variable findVar(String cid, int vnid)
    {
	if ( !m_info.getCID().equals(cid) ) return null;

	if ( vnid == 0 )  return null;

	return m_vMan.get(vnid);
    }
    //----------------------------------------------------------------------

  /** Add a root {@link Node} to this component. */
  protected void addRootNode(Node n){
    if ( !m_rootNodes.contains(n))
    m_rootNodes.add(n);
  }

  /** Remove a root {@link Node} from this component. */
  protected boolean removeRootNode(Node n){
    return m_rootNodes.remove(n);
  }
}

