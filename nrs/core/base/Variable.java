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

import java.util.ArrayList;
import java.util.Iterator;
import nrs.core.message.DeleteLink;

/**
 * Base class for all classes which represent NRS-variables
 *
 * @author Darren Smith
 * @author Thomas French
 */
public abstract class Variable
{
  /** Unique numerical ID */
  private int m_vid;
  
  /** Unique string name */
  private String m_vname;
  
  /** Variable type. */
  private String m_type;

  /** Whether this Variable is state holding.*/
  private Boolean m_stateHolding;
  
  /** Whether this Variable is self updating.*/
  private Boolean m_selfUpdating;
  
  /** Store all Links where this Variable is the source.
   * Each object is of type {@link Link}.
   */
  protected ArrayList<Link> m_sourceLinks;
  
  /** Store all Links where this Variable is the target.
   * Each object is of type {@link Link}.
   */
  protected ArrayList<Link> m_targetLinks;
  
  /** VariableManager to send out messages through. */
  protected VariableManager m_vm;
  
  /** OutboundPipeline used by VariableManager.*/
  private OutboundPipeline m_out = null;

  /** Restriction of this variable. */
  private Restriction m_restriction = null;
  
  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param ID the unique variable ID to use for this variable
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @deprecated Do not use this constructor! Very out-dated!
   */
  public Variable(int ID, String name)
  {
    m_vid = ID;
    m_vname = name;
    
    PackageLogger.log.finer("Created NRS variable \""
                            + name + "\" with ID=" + ID);

    m_stateHolding = false;
    m_selfUpdating = false;
  }
  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @deprecated Variable must now declare their type at initialisation.
   */
  public Variable(VariableManager vm, String name)
  {
    m_vid = vm.suggestID(); //get suggested vnid

    m_vname = name;
    
    PackageLogger.log.finer("Created NRS variable \""
			      + name + "\" with ID=" + m_vid);
    
    // Automatically register this variable
    vm.add(this);
    
    m_vm = vm;
    m_out = m_vm.getOutboundPipeline();
    
    m_sourceLinks = new ArrayList<Link>();
    m_targetLinks = new ArrayList<Link>();
    
    m_stateHolding = false;
    m_selfUpdating = false;
  }
  
  /** Constructor for use by {@link Node}s
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @param type vnType for node
   */
  public Variable(VariableManager vm, String name, String type)
  {
    m_vid = vm.suggestID(); //get suggested vnid

    m_vname = name;
    m_type = type;
    
    PackageLogger.log.finer("Created NRS variable \""
			      + name + "\" with ID=" + m_vid);
    
    // Automatically register this variable
    vm.add(this);
    
    m_vm = vm;
    m_out = m_vm.getOutboundPipeline();
    
    m_sourceLinks = new ArrayList<Link>();
    m_targetLinks = new ArrayList<Link>();

    // nodes don't have these properties
    m_stateHolding = null;
    m_selfUpdating = null;
  }

  //----------------------------------------------------------------------
  /** Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @param stateHolding whether this variable is state holding
   *
   * @param selfUpdating whether this variable is self updating
   *
   * @deprecated Variable must now declare their type at initialisation.
   */
  public Variable(VariableManager vm, String name, boolean stateHolding, 
                  boolean selfUpdating)
  {
    m_vid = vm.suggestID(); //get suggested vnid
    
    m_vname = name;
    
    PackageLogger.log.finer("Created NRS variable \""
                            + name + "\" with ID=" + m_vid);
    
    // Automatically register
    vm.add(this);
    
    m_vm = vm;
    m_out = m_vm.getOutboundPipeline();
    
    m_sourceLinks = new ArrayList<Link>();
    m_targetLinks = new ArrayList<Link>();
    
    m_stateHolding = stateHolding;
    m_selfUpdating = selfUpdating;
  }
  //----------------------------------------------------------------------
 /** Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @param stateHolding whether this variable is state holding
   *
   * @param selfUpdating whether this variable is self updating
   *
   * @param type variable type
   */
  public Variable(VariableManager vm, String name, boolean stateHolding, 
                  boolean selfUpdating, String type)
  {
    m_vid = vm.suggestID(); //get suggested vnid
    
    m_vname = name;
    m_type = type;
    
    PackageLogger.log.finer("Created NRS variable \""
                            + name + "\" with ID=" + m_vid + " and type " 
                            + m_type);
    
    // Automatically register
    vm.add(this);
    
    m_vm = vm;
    m_out = m_vm.getOutboundPipeline();
    
    m_sourceLinks = new ArrayList<Link>();
    m_targetLinks = new ArrayList<Link>();
    
    m_stateHolding = stateHolding;
    m_selfUpdating = selfUpdating;
  }
  //----------------------------------------------------------------------
  /**
   * Deliver a {@link Message} to this variable
   */
  public abstract void deliver(Message m);
  
  //----------------------------------------------------------------------
  /**
   * Get the unqiue variable ID of this variable
   */
  public int getVNID()
  {
    return m_vid;
  }
  //----------------------------------------------------------------------
  /**
   * Get the string name of this variable
   */
  public String getVNName()
  {
    return m_vname;
  }
  //----------------------------------------------------------------------
  /**
   * Get the variable type of this variable.
   */
  public String getVNType(){
    return m_type;
  }
  //----------------------------------------------------------------------
  /** Called when a Reset message is received at Component.
   * StateHolding Variables must override this method.
   */
  public void reset(){ 
    if ( m_stateHolding && getTargetMaxLinks() == 0 ){
      PackageLogger.log.warning("Reset: State-Holding variables, with no " 
                                + " input links, must override this method");
    }
  }
  //----------------------------------------------------------------------
  /**
   * Returns the name of this variable
   */
  public String toString()
  {
    return m_vname;
  }
  //---------------------------------------------------------------------
  /** Is this variable state-holding.
   *
   * @return whether this variable is state-holding or not.
   */
  protected boolean isStateHolding(){
    return m_stateHolding;
  }
  //----------------------------------------------------------------------
  /** Is this variable self-updating.
   *
   * @return whether this variable is self-updating or not.
   */
  protected boolean isSelfUpdating(){
    return m_selfUpdating;
  }
  //----------------------------------------------------------------------
  /** Get restriction of this variable.
   * Returns null if no restriction.
   */
  public Restriction getRestriction(){
    return m_restriction;
  }
  //----------------------------------------------------------------------
  /** Set restriction of this variable. */
  public void setRestriction(Restriction restriction){
    m_restriction = restriction;
  }
  //----------------------------------------------------------------------
  /** Add {@link Link} contained by this {@link Variable}.
   * Note: it is possible to add multiple links between Variables.
   *
   * @param lk {@link nrs.core.base.Link} to add, which is contained by
   * this Node.
   *
   * @return whether addition of link was successful or not.
   *
   */
  public boolean addLink(Link lk) {
    if ( lk == null ){
      PackageLogger.log.warning("Link is null, cannot add");
      return false;
    }
    
    // add to source links
    if ( lk.isSource(getVNID()) ){
      m_sourceLinks.add(lk);
      PackageLogger.log.finer("Link added to sourceLinks of variable: " 
                             + getVNID());
      
      // send my current state
      if ( m_stateHolding )
        sendState(lk);

      PackageLogger.log.finer("Number of source links is: " 
                             + getSourceMaxLinks());      
    }
    // otherwise add to target links
    else
      {
        m_targetLinks.add(lk);
        PackageLogger.log.finer("Link added to targetLinks of variable: " 
                               + getVNID());
      }
    
    return true;
  }
  //---------------------------------------------------------------------
  /** Remove a {@link nrs.core.base.Link} contained by this Variable, 
   * by VNID, as the source of the link.
   *
   * @param vnid VNID to search for
   * @param cid CID to search for
   */
  public void deleteSourceLink(int vnid, String cid){
    Variable target;
    Integer targetVNID;
    String targetCID;
    
    Link ln;
    for(Iterator<Link> i = m_sourceLinks.iterator(); i.hasNext();){
      ln = i.next();
      
      // check if off-board
      targetVNID = ln.getTargetVNID();
      targetCID = ln.getTargetCID();
      
      if ( targetVNID != null && targetCID != null && 
           targetVNID.intValue() == vnid && targetCID.equals(cid) ){
        i.remove();
        PackageLogger.log.fine("Off-board Link removed from variable " 
                               + getVNName() );
        return; // remove one link at a time
      }
      
      // check if on-board
      target = ln.getTargetRef();
      
      if ( target != null && target.getVNID() == vnid ){
        i.remove();
        PackageLogger.log.fine("On-board link removed from variable " 
                               + getVNName());
        return; // remove one link at a time
      }
    }
    
    PackageLogger.log.fine("No source link found with vnid: " + vnid);
  }
  //---------------------------------------------------------------------
  /** Remove a {@link nrs.core.base.Link} contained by this Variable, 
   * by VNID, as the target of the link.
   *
   * @param vnid VNID to search for
   * @param cid CID to search for
   */
  public void deleteTargetLink(int vnid, String cid){
    Variable source;
    Integer sourceVNID;
    String sourceCID;
    
    Link ln;
    for(Iterator<Link> i = m_targetLinks.iterator(); i.hasNext();){
      ln = i.next();
      
      // check if off-board
      sourceVNID = ln.getSourceVNID();
      sourceCID = ln.getSourceCID();
      
      if ( sourceVNID != null && sourceCID != null && 
           sourceVNID == vnid && sourceCID.equals(cid) ){
        i.remove();
        PackageLogger.log.fine("Off-board Link removed from variable " 
                               + getVNName() );
        return; // remove one link at a time
      }
      
      // check if on-board
      source = ln.getSourceRef();
      
      if ( source != null && source.getVNID() == vnid ){
        i.remove();
        PackageLogger.log.fine("On-board link removed from variable " 
                               + getVNName());
        return; // remove one link at a time
      }
    }
    
    PackageLogger.log.fine("No link found with vnid: " + vnid);
  }
  //---------------------------------------------------------------------
  /** Remove all Links this Variable this has.
   */
  public void removeAllLinks(){
    // send deleteLinks for all links - for source and target of links
    DeleteLink dl = null;
    Variable source = null, target = null;
    
    ArrayList<Link> m_allLinks = new ArrayList<Link>();
    m_allLinks.addAll(m_sourceLinks);
    m_allLinks.addAll(m_targetLinks);
    
    // remove all links
    for(Link ln : m_allLinks){
      source = ln.getSourceRef();
      target = ln.getTargetRef();
      
      // on-board component
      // delete source link
      if ( target != null && ln.isSource(getVNID()) ){
        target.deleteTargetLink(getVNID(), null);
        continue;
      }
      // delete target link
      else if ( source != null && !ln.isSource(getVNID()) ){
        source.deleteSourceLink(getVNID(), null);
        continue;
      }
      
      // check if off-component
      else if ( source == null && target == null ){ 
        dl = new DeleteLink();
	
        PackageLogger.log.fine("Deleting link between vnid: " + 
                               ln.getSourceVNID().intValue() 
                               + ", cid: " + ln.getSourceCID() 
                               + " and target vnid: " 
                               + ln.getTargetVNID().intValue() 
                               + ", cid: " + ln.getTargetCID());
        
        // get details for Message
        ln.fillDeleteLinkMsg(dl, ln.isSource(getVNID()) );
	
        if ( m_out == null ){
          PackageLogger.log.warning("OutboundPipeline is null, "
                                    + " can't send DeleteLinks.");
          break;
        }
        else // send message out.
          m_out.submit(dl);   
      }
    }
    
    // finally, remove all links
    m_sourceLinks.clear();
    m_targetLinks.clear();
    PackageLogger.log.fine("All links removed");
  }
  //---------------------------------------------------------------------
  /** Return all links this variable has.
   * @return joint list of source and target links.
   */
  public ArrayList<Link> allLinks(){
    ArrayList<Link> m_allLinks = new ArrayList<Link>();
    m_allLinks.addAll(m_sourceLinks);
    m_allLinks.addAll(m_targetLinks);

    return m_allLinks;
  }
  //---------------------------------------------------------------------
  /** Return all source links this Variable has.*/
  public ArrayList<Link> allSourceLinks(){
    return m_sourceLinks;
  }
  //---------------------------------------------------------------------
  /** Return all target links this Variable has.*/
  public ArrayList<Link> allTargetLinks(){
    return m_targetLinks;
  }
  //---------------------------------------------------------------------    
  /** Return highest number of source links this variable has.*/
  public int getSourceMaxLinks(){
    return m_sourceLinks.size();
  }
  //---------------------------------------------------------------------
  /** Return highest number of target links this variable has.*/
  public int getTargetMaxLinks(){
    return m_targetLinks.size();
  }
  //---------------------------------------------------------------------
  /** Return link at index into source links.*/
  public Link getSourceLink(int l){
    PackageLogger.log.fine("getSourceLink: " + l + " with number of" 
                           + " source links: " + m_sourceLinks.size());
    if ( l < 1 || l > m_sourceLinks.size() )
      return null;

    return m_sourceLinks.get(l-1);
  }
  //---------------------------------------------------------------------
  /** Return link at index into target links.*/
  public Link getTargetLink(int l){
    PackageLogger.log.fine("getTargetLink: " + l + " with number of" 
                           + " target links: " + m_targetLinks.size());
    if ( l < 1 || l > m_targetLinks.size() )
      return null;
    
    return m_targetLinks.get(l-1);
  }
  //---------------------------------------------------------------------
  
  /** Derived classed should override thid method to return a 
   * {@link Message} of their type.
   *
   * NOTE: For safety reasons, derived classes should create new instances 
   * of the Message object on each call.
   *
   * @deprecated No longer using this method. Now generate {@link Message}s 
   * on the fly. Use {#see createMessage} instead.
   *
   * @return Message to send over links.
   */
  protected Message getMessage(){
    PackageLogger.log.warning("Using default implementation of"
                              + " Variable.getMessage():"
                              + " no operation taken");
    return null;
  }
  //---------------------------------------------------------------------
 /** Derived classed should override thid method to return a 
   * {@link Message} of their type.
   *
   * NOTE: For safety reasons, derived classes should create new instances 
   * of the Message object on each call.
   *
   * @return Message to send over links.
   */
  protected Message createMessage(){
    PackageLogger.log.warning("Using default implementation of"
                              + " Variable.createMessage():"
                              + " no operation taken");
    return null;
  }
  //---------------------------------------------------------------------
  protected void send(Variable v){
    PackageLogger.log.warning("Using default implementation of"
                              + " Variable.send(): no operation taken");
    return;
  }
  //---------------------------------------------------------------------
  /** Send messages over links, if any present. */
  protected void sendMessages(){
    for(Link ln : m_sourceLinks )
      sendState(ln);
  }
  //---------------------------------------------------------------------
  /** Send message over link.
   *
   * @param link link to send message over.
   */
  private void sendState(Link ln){
    if ( !ln.isSource(getVNID()) ) return; // only send if source of link
    
    Variable v = ln.getTargetRef();
    
    // if link is internal to component
    if ( v != null ){
      try{
        send(v);
      }
      catch(ClassCastException e){
        PackageLogger.log.warning("Cannot send message. Source"
                                  + " and Target variables are"
                                  + " incompatible. Source-type: "
                                  + this + "; Target-type: " + v
                                  + ". Class cast expection: " + e);
      }
      return;
    }
    
    // link is off-component
    //Message msg = getMessage(); // get message from derived class
    Message msg = createMessage();
    
    // case default implemenation is used, 
    // or derived class hasn't set a value yet.
    if ( msg == null ) return; 
    
    // get details for Message from Link
    ln.fillMsg(msg);
    
    if ( m_out == null )
      PackageLogger.log.warning("OutboundPipeline is null," 
                                + " can't send messages out.");
    else // send message out.
      m_out.submit(msg);   
  }
  //---------------------------------------------------------------------
}
