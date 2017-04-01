/*
 * Copyright (C) 2006 Edinburgh University
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

package nrs.oscilloscope.nodes;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Variable;

import nrs.core.type.StringType;
import nrs.core.type.VoidType;

import nrs.core.unit.Time;

import java.io.File;
import java.io.IOException;

/** Represents top-level Node for this component
 * 
 * @author Theophile Gonos
 * 
*/

public class OscilloscopeNode extends Node
{
  // Variables
  private Time m_varTime; // output
  private VoidType m_varCounter; // input
  private Time m_varTimeStep;// input
  private VoidType m_varReset; // input & output
  
  private final static String type = "OscilloscopeNode";

  /** Constructor.
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public OscilloscopeNode(VariableManager vmMan, String vnName)
  {
    super(vmMan, vnName, type);
    
    // add variables to variable manager
    // register Time variable with VariableManager
    String varName2 = vnName+ ".Time"; 
    m_varTime = new Time(vmMan, varName2, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Time(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Time(d);
        }
      };
    // add Time variable to this Node localVars
    addVariable(varName2, m_varTime);
    
    // register Counter variable with VariableManager
    String varName3 = vnName+ ".Counter";
    m_varCounter = new VoidType(vmMan, varName3){
        public void deliver(Message m)
        {
          handleMessageAt_Counter(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Counter();
        }
      };
    // add Counter variable to this Node localVars
    addVariable(varName3, m_varCounter);
    
    // register TimeStep variable with VariableManager
    String varName4 = vnName+ ".TimeStep";
    m_varTimeStep = new Time(vmMan, varName4, true, false){
        public void deliver(Message m)
        {
          handleMessageAt_TimeStep(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_TimeStep(d);
        }
      };
    // add TimeStep variable to this Node localVars
    addVariable(varName4, m_varTimeStep);
    
    // register Reset variable with VariableManager
    String varName5 = vnName+ ".Reset";
    m_varReset = new VoidType(vmMan, varName5){
        public void deliver(Message m)
        {
          handleMessageAt_Reset(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Reset();
        }
      };
    // add Reset variable to this Node localVars
    addVariable(varName5, m_varReset);
  }
  //----------------------------------------------------------------------
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m)
  {
    PackageLogger.log.fine("Received a message at: " + getVNName());
    
    // initialise time value
    Double d = Double.parseDouble(m.getField("time"));
    if ( d == null )
      m_varTime.setDefault(0.0);
    else
      m_varTime.setDefault(d.doubleValue());
    
    // initialise timestep value
    d = Double.parseDouble(m.getField("timestep"));
    if ( d == null )
      m_varTimeStep.setDefault(1.0);
    else
      m_varTimeStep.setDefault(d.doubleValue());
    
  }
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  
  //--------------------------------------------------------------------// 
  
  /**
   * Process the receipt of a message at the Time variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Time(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Time variable!");
    
    Double d = m_varTime.extractData(m);
    if ( d != null )
      handleMessageAt_Time(d.doubleValue());
  }
  /**
   * Process the receipt of a message at the Time variable.
   */
  public void handleMessageAt_Time(double d) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Time variable!");
    
    m_varTime.setValue(d);
  } 
  
  /**
   * Process the receipt of a message at the Counter variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Counter(Message m, Variable v, boolean diff) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Counter variable!");
    
    handleMessageAt_Counter();
  }
  /**
   * Process the receipt of a message at the Counter variable.
   */
  public void handleMessageAt_Counter() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Counter variable!");
    
    // increment Time by TimeStep and update children nodes
    m_varTime.setValue( m_varTime.getValue().doubleValue() 
                        + m_varTimeStep.getValue().doubleValue());
  } 
  
  /**
   * Process the receipt of a message at the TimeStep variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_TimeStep(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":TimeStep variable!");
    
    Double d = m_varTimeStep.extractData(m);
    if ( d != null )
      handleMessageAt_TimeStep(d.doubleValue());
  }
  /**
   * Process the receipt of a message at the TimeStep variable.
   */
  public void handleMessageAt_TimeStep(double d) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":TimeStep variable!");
    
    m_varTimeStep.setValue(d);
  }
  
  /**
   * Process the receipt of a message at the Reset variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Reset(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Reset variable!");
    
    m_varReset.onEvent();
  }
  /**
   * Process the receipt of a message at the Reset variable.
   */
  public void handleMessageAt_Reset() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Reset variable!");
    m_varReset.onEvent();
  } 
}
