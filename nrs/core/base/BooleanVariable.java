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

import nrs.core.message.BooleanMessage;

/** Class to represent NRS boolean variables.
 *
 * @deprecated Using new type framework.
 *
 * @author Thomas French
 */

public abstract class BooleanVariable extends Variable
{ 
  private Boolean m_value = null;
  private Boolean m_default = null;
  
  /** Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   */
  public BooleanVariable(VariableManager vm, String name,
                         boolean stateHolding, boolean selfUpdating){
    super(vm, name, stateHolding, selfUpdating);
    
    //default initialisation state
    if ( stateHolding ){
      m_default = Boolean.FALSE;
      setValue(m_default.booleanValue());
    }
  }

  /** Set default value.*/
  public void setDefault(Boolean b){
    if ( isStateHolding() ){
      m_default = b;
      setValue(m_default.booleanValue());
    }
    else
      throw new IllegalStateException("Cannot set Variable: " 
                                      + getVNName() + " default value since"
                                      + " it is not state holding.");
  }
  
  /** For on-board links, to send current value too.
   *     
   * @param target target of link
   */
  protected void send(Variable target){
    BooleanVariable b = (BooleanVariable) target;
    b.deliver(m_value.booleanValue());
  }
  
  /** Receive boolean from variable within same component. 
   *     
   * @param b boolean value
   */
  public abstract void deliver(boolean b);
  
  /** Get current value for this {@link Variable}
   *
   * @return current value for variable. Null if not set yet.
   */
  public Boolean getValue(){
    if ( isStateHolding() )
      return m_value;
    else
      throw new IllegalStateException("Cannot get Variable: " 
                                      + getVNName() + " value since"
                                      + " it is not state holding.");
  }
  
  /** Set new value if different from previous value. 
   * Also trigger sendMessages event.
   *
   * @param b new value
   */
  public void setValue(boolean b){
    if ( !isStateHolding() )
      throw new IllegalStateException("Cannot set Variable: " 
                                      + getVNName() + " value since"
                                      + " it is not state holding.");
    
    if ( m_value == null || b != m_value.booleanValue() ){
      m_value = Boolean.valueOf(b);
      sendMessages();
    }
  }
  
  public void onEvent(boolean b){
    m_value = b;
    sendMessages();
  }
  
  /** Get Message from derived class to send over links.
   *
   * NOTE: Derived classes should create new instances of the Message 
   * on each call.
   *
   * @return Message to send over links.
   */
  protected Message getMessage(){
    if (m_value == null ) return null;
    return new BooleanMessage(m_value);
  }
  
  /**
   * Get the variable type of this variable.
   */
  public String getVNType(){
    return "boolean";
  }

  /** Reset variable value to default, given that it is state-holding and
   * had no input links (since its state will be set by another variable).
   */
  public void reset(){
    if ( isStateHolding() && getTargetMaxLinks() == 0 )
      setValue(m_default);
  }
}
