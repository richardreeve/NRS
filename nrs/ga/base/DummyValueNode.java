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
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

/** Class to represent a DummyValueNode
 * 
 * This class implements a subclass of ValueNode which has no value, 
 * used when a user doesn't want to use a value node with an actual value.
 *
 * @author Thomas French
*/

class DummyValueNode extends ValueNode
{
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   */
  protected DummyValueNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "DummyValueNode");
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  
  //----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":DummyValueNode!");
  }
  //-----------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the Trigger variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Trigger(Message m, Variable v, boolean diff){ 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Trigger variable!");
    
    m_varConsistent.onEvent(true);
  }
  
  /**
   * Process the receipt of a message at the Trigger variable.
   */
  public void handleMessageAt_Trigger() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Trigger variable!");
    
    m_varConsistent.onEvent(true);
  }
    
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    return;
  }
}
