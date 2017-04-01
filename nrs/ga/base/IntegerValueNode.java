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

import nrs.core.type.IntegerType;

/** Class to represent IntegerValueNode
 * 
 * This class implements a subclass of ValueNode whose value is a integer type.
 *
 * @author Thomas French
*/

class IntegerValueNode extends ValueNode
{
  //Variables
  private IntegerType m_varOutput, m_varInput, m_varValue;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   */
  protected IntegerValueNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "IntegerValueNode");
    
    String varName = vnName+ ".Output";
    m_varOutput = new IntegerType(vmMan, varName, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Output(m, this, checkType(m, this));
        }
        public void deliver(int i){
          handleMessageAt_Output(i);
        }
      };
    addVariable(varName, m_varOutput);
    
    String varName2 = vnName+ ".Input";
    m_varInput = new IntegerType(vmMan, varName2, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Input(m, this, checkType(m, this));
        }
        public void deliver(int i){
          handleMessageAt_Input(i);
        }
      };
    addVariable(varName2, m_varInput);
    
    String varName3 = vnName+ ".Value";
    m_varValue = new IntegerType(vmMan, varName3, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Value(m, this, checkType(m, this));
        }
        public void deliver(int i){
          handleMessageAt_Value(i);
        }
      };
    addVariable(varName3, m_varValue);
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
                           + ":IntegerValueNode!");
    
    //extract value for node
    Integer i = Integer.parseInt(m.getField("value"));
    if ( i != null )
      m_varValue.setDefault(i.intValue());
  }
  //-----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the Output variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Output(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Output variable!"
                           + " Used only as an output variable.");
  }
  /**
   * Process the receipt of a message at the Output variable.
   * @param i the received value
   */
  public void handleMessageAt_Output(int i) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Output variable!"
                           + " Used only as an output variable.");
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
    
    m_varOutput.onEvent(m_varValue.getValue().intValue());
  }
  
  /**
   * Process the receipt of a message at the Trigger variable.
   */
  public void handleMessageAt_Trigger() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Trigger variable!");
    
    m_varOutput.onEvent(m_varValue.getValue().intValue());
  }
  
  //-----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the Input variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Input variable!");
    //extract value
    Integer i = m_varInput.extractData(m);
    if ( i != null ){
      handleMessageAt_Input(i.intValue());
    }
  }
  /**
   * Process the receipt of a message at the Input variable.
   * @param i the received value
   */
  public void handleMessageAt_Input(int i) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Input variable!");
    m_varInput.setValue(i);
    
    setConsistent();
  }
  //---------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the Value variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Value(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Value variable!");
  }
  /**
   * Process the receipt of a message at the Value variable.
   * @param i the received value
   */
  public void handleMessageAt_Value(int i) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Value variable!");
  }
  //-----------------------------------------------------------------------//
  
  private void setConsistent(){
    boolean b = false;
    
    if ( m_varValue.getValue().intValue() == 
         m_varInput.getValue().intValue() )
      b = true;
    
    m_varConsistent.onEvent(b);
  }
  
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    return;
  }
}
