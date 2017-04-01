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

import nrs.core.message.StringMessage;

/** Class to represent string typed variables.
 *
 * @deprecated Using new type framework.
 *
 * @author Thomas French
 */

public abstract class StringVariable extends Variable
{ 
  public enum StringRestrictionType {List, Token, Filename, VNName,
      NoStringRestriction};
  
  private String m_value = null;
  private String m_default = null;
    
  /** Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   */
  public StringVariable(VariableManager vm, String name, 
                        boolean stateHolding, boolean selfUpdating){
    super(vm, name, stateHolding, selfUpdating);
    
    //default initialisation state
    m_default = "";
    setValue(m_default);
  }
  
  /** Set default value.*/
  public void setDefault(String s){
    if ( isStateHolding() ){
      m_default = s;
      setValue(m_default);
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
    StringVariable s = (StringVariable) target;
    s.deliver(m_value);
  }
  
  /** Receive String from variable within same component. 
   *     
   * @param s String value
   */
  public abstract void deliver(String s);
  
  /** Get current value for this {@link Variable}
   *
   * @return current value for variable. Null if not set yet.
   */
  public String getValue(){
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
   * @param s new value
   */
  public void setValue(String s){
    if ( !isStateHolding() )
      throw new IllegalStateException("Cannot set Variable: " 
                                      + getVNName() + " value since"
                                      + " it is not state holding.");
    if ( s == null ) return;
    
    if ( m_value == null || !s.equals(m_value) ){
      m_value = s;
      sendMessages();
    }
  }
  
  public void onEvent(String s){
    if ( s == null ) 
      throw new NullPointerException("Value can't be null");
    
    m_value = s;
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
    return new StringMessage(m_value);
  }
  
  /**
   * Get the variable-type of this variable.
   */
  public String getVNType(){
    return "string";
  }

  /** Reset variable value to default, given that it is state-holding and
   * had no input links (since its state will be set by another variable).
   */
  public void reset(){
    if ( isStateHolding() && getTargetMaxLinks() == 0 )
      setValue(m_default);
  }
}
