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
package nrs.wrapper;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import nrs.core.base.BaseComponent;
import nrs.core.base.Link;
import nrs.core.base.Message;
import nrs.core.base.FieldNotFoundException;
import nrs.core.base.MessageTools;
import nrs.core.base.Node;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.RouteManager;
import nrs.core.message.*;

import static nrs.core.message.Constants.Fields.*;

/**
 * Root Component class for wrapper component, 
 * which handles all message handling.
 *
 * @author Thomas French
 */
public class WrapperComponent extends BaseComponent
{
  /** Name of component. */
  private final static String m_Name = "NRS.wrapper";
  /** Component type. */
  private final static String m_CType = "wrapper";
  /** Component version. */
  private final static String m_Version = "1.0";
  /** Whether this component has a CSL descriptor file. */
  private final static boolean m_HasCSL = true;
  /** Can the component speak BMF. */
  private final static boolean m_SpeaksBMF = false;
  /** Can the component speak PML. */
  private final static boolean m_SpeaksPML = true;
  
  /** Root node for this component. */
  private WrapperNode m_rootNode;
  
  //----------------------------------------------------------------------
  /**Constructor
   *
   * @param vMan reference to {@link VariableManager} object.
   */
  public WrapperComponent(VariableManager vMan)
  {
    super(vMan, m_Name, m_CType, m_SpeaksBMF, m_SpeaksPML, m_HasCSL, 
          m_Version);
    
    m_vMan = vMan;
  }
  
  /**
   * Process the receipt of a message at the CreateNode variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_CreateNode(Message m, Variable v, boolean diff) 
  {
    
    // Extract message fields
    int vnid;
    String vnName, vnType;
    
    try{
      vnName = m.checkField(F_vnName);
      vnType = m.checkField(F_vnType);
      vnid = Integer.parseInt(m.checkField(F_vnid));
    }
    catch (FieldNotFoundException e){
      PackageLogger.log.warning(m.getType() + " message missing "
                                + e.fieldName() + " field; ignoring");
      return;
    }
    
    PackageLogger.log.fine("Received a CreateNode message, for type: "
                           + vnType + " with name: " + vnName);
    
    //receive message to create root node - ControlNode
    if ( vnType.equals("WrapperNode")){
      if ( m_rootNode != null ){
        PackageLogger.log.warning("Root node WrapperNode already created");
        return;
      }
      
      m_rootNode = new WrapperNode(m_vMan, vnName);
      
      addRootNode(m_rootNode);
      
      PackageLogger.log.fine("Created a WrapperNode with name "
                             + vnName);
    }
    
    //create Execute Node
    else if ( vnType.equals("ExecuteNode") ){

      m_rootNode.addNode(new ExecuteNode(m_vMan, vnName));

    }
    
    else{
      PackageLogger.log.warning("CreateNode message received for" 
                                + " unknown node type: " + vnType);
    }
    
  }
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
                                         boolean diff){
    // extract vnid from message
    int vnid;
    try{
      vnid = Integer.parseInt(m.checkField(F_vnid));
    }
    catch (FieldNotFoundException e){
      PackageLogger.log.warning(m.getType() + " message missing "
                                + e.fieldName() + " field; ignoring");
      return;
    }
    
    PackageLogger.log.fine("Received a DeleteNode message for vnID: "
                           + vnid);
    
    // remove root node from m_vMan
    if ( vnid == m_rootNode.getVNID() ){
      // remove all children nodes
      m_rootNode.removeAll();
      
      // remove this node from Variable Manager`
      m_vMan.remove(m_rootNode);

      removeRootNode(m_rootNode);
      
      m_rootNode = null;
      
      PackageLogger.log.fine("Deleted root Wrapper Node!");
    }
    else{
      Node n;
      // delete node
      if ( vnid > 0 ){
        n = (Node) m_vMan.get(vnid);
      }else{
        PackageLogger.log.warning("Error deleting Node - no VNID"
                                  +" specified!");
        n = null;
      }
      
      // need to remove all nodes and variables node contains
      // and remove it from Variable Manager
      if ( n != null){
        n.removeAll();
        
        // remove from VariableManager
        m_vMan.remove(n);

        // remove from root node
        m_rootNode.removeNode(n);
        
        PackageLogger.log.fine("Deleted Node!");
      }
      else{
        PackageLogger.log.warning("DeleteNode message received for"
                                  +" unknown node with vnID: " + vnid);
      }
    }
  }
}
