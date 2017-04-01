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
package nrs.ga.base;

import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;
import nrs.core.type.VoidType;

/** Abstract class to represent a ValueNode.
 * 
 * Derived classes are used by {@link EvaluationNode}s as values to be sent 
 * to system to set particular parameters (ones not being evolved, but still
 * needing to be changed/ set). 
 * 
 * This functionality is particularly useful in multi-objective evaluation.
 *
 * @author Thomas French
*/

abstract class ValueNode extends Node
{
  //Variables
  private VoidType m_varTrigger;
  protected BooleanType m_varConsistent;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   * @param type VNType of this node
   */
  protected ValueNode(VariableManager vmMan, String vnName, String type){
    super(vmMan, vnName, type);
    
    //register Trigger variable with VariableManager
    String varName = vnName+ ".Trigger";
    m_varTrigger = new VoidType(vmMan, varName){
        public void deliver(Message m)
        {
          handleMessageAt_Trigger(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Trigger();
        }
      };
    //add Trigger variable to this Node localVars
    addVariable(varName, m_varTrigger);
    
    String varName2 = vnName+ ".Consistent";
    m_varConsistent = new BooleanType(vmMan, varName2, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Consistent(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_Consistent(b);
        }
      };
    //add Consistent variable to this Node localVars
    addVariable(varName2, m_varConsistent);
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }

  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":ValueNode!");
  }
  
  //----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the Trigger variable.
   * Must be overriden in subclass.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Trigger(Message m, Variable v, boolean diff){
    PackageLogger.log.warning("Handle Trigger event must be overriden"
                              +" by subclass");
  }
  
  /**
   * Process the receipt of a message at the Trigger variable.
   * Must be overriden in subclass.
   */
  public void handleMessageAt_Trigger() {
    PackageLogger.log.warning("Handle Trigger event must be overriden"
                              +" by subclass");
    
  }
  
  //-----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the Consistent variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Consistent(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Consistent variable!"
                           + " Used only as an output variable.");
  }
  /**
   * Process the receipt of a message at the Consistent variable.
   * @param b the received value
   */
  public void handleMessageAt_Consistent(boolean b) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Consistent variable!"
                           + " Used only as an output variable.");
  }
  //-----------------------------------------------------------------------//
  
  /** Check dependencies for this node and all the nodes it contains. 
   * Can be overwritten by subclass should need be
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    return;
  }
}
