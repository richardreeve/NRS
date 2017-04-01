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

import nrs.core.type.FloatType;

/** Class to represent FloatValueNode
 * 
 * This class implements a subclass of ValueNode whose value is a float type.
 *
 * @author Thomas French
*/

class FloatValueNode extends ValueNode
{
  //Variables
  private FloatType m_varOutput, m_varInput, m_varValue;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   */
  protected FloatValueNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "FloatValueNode");
    
    String varName = vnName+ ".Output";
    m_varOutput = new FloatType(vmMan, varName, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Output(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Output(d);
        }
      };
    addVariable(varName, m_varOutput);
    
    String varName2 = vnName+ ".Input";
    m_varInput = new FloatType(vmMan, varName2, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Input(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Input(d);
        }
      };
    addVariable(varName2, m_varInput);
    
    String varName3 = vnName+ ".Value";
    m_varValue = new FloatType(vmMan, varName3, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Value(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Value(d);
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
                           + ":FloatValueNode!");
    
    //extract value for node
    Double d = Double.parseDouble(m.getField("value"));
    if ( d != null )
      m_varValue.setDefault(d.doubleValue());
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
   * @param d the received value
   */
  public void handleMessageAt_Output(double d) { 
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
    
    m_varOutput.onEvent(m_varValue.getValue().doubleValue());
  }
  
  /**
   * Process the receipt of a message at the Trigger variable.
   */
  public void handleMessageAt_Trigger() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Trigger variable!");
    
    m_varOutput.onEvent(m_varValue.getValue().doubleValue());
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
    Double d = m_varInput.extractData(m);
    if ( d != null ){
      handleMessageAt_Input(d.doubleValue());
    }
  }
  /**
   * Process the receipt of a message at the Input variable.
   * @param d the received value
   */
  public void handleMessageAt_Input(double d) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Input variable!");
    m_varInput.setValue(d);
    
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
   * @param d the received value
   */
  public void handleMessageAt_Value(double d) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Value variable!");
  }
  //-----------------------------------------------------------------------//
  
  private void setConsistent(){
    boolean b = false;
    
    if ( m_varValue.getValue().doubleValue() == 
         m_varInput.getValue().doubleValue() )
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
