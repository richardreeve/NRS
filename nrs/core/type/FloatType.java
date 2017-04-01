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
package nrs.core.type;

import nrs.core.base.Message;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/** Represent a Float variable. 
 * Can have a double or float value. We only use
 * a double value on links call, because floats are doubles.
 *
 * @author Thomas French
 */

public abstract class FloatType extends Variable
{
  private Double m_value = null;
  private Double m_default = null;

  /** Default Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @param stateHolding whether this variable is state-holding
   *
   * @param selfUpdating whether this variable is self-updating
   */
  public FloatType(VariableManager vm, String name,
               boolean stateHolding, boolean selfUpdating){
    super(vm, name, stateHolding, selfUpdating, "float");
    
    //default initialisation state
    if ( stateHolding ){
      m_default = new Double(0.0);
      setValue(m_default.doubleValue());
    }
  }

  /** Default Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @param stateHolding whether this variable is state-holding
   *
   * @param selfUpdating whether this variable is self-updating
   *
   * @param type derived variable-type
   */
  public FloatType(VariableManager vm, String name,
                   boolean stateHolding, boolean selfUpdating,
               String type){
    super(vm, name, stateHolding, selfUpdating, type);
    
    //default initialisation state
    if ( stateHolding ){
      m_default = new Double(0.0);
      setValue(m_default.doubleValue());
    }
  }

  /** Set default value.*/
  public void setDefault(Double d){
    if ( isStateHolding() ){
      m_default = d;
      setValue(m_default.doubleValue());
    }
    else
      throw new IllegalStateException("Cannot set Variable: " 
                                      + getVNName() + " default value since"
                                      + " it is not state holding.");
  }

  /** For on-board links, send current value.
   *     
   * @param target target of link
   */
  protected void send(Variable target){
    FloatType f = (FloatType) target;
    f.deliver(m_value.doubleValue());
  }
  
  /** Receive double from variable within same component. 
   *
   * @param d value received
   */
  public abstract void deliver(double d);
  
  /** Set new value if different from previous value. 
   * Also trigger sendMessages event.
   *
   * @param f new value
   */
  public void setValue(float f){
    setValue((double) f);
  }
  
  /** Set new value if different from previous value. 
   * Also trigger sendMessages event.
   *
   * @param d new value
   */
  public void setValue(double d){
    if ( !isStateHolding() )
      throw new IllegalStateException("Cannot set Variable: " 
                                      + getVNName() + " value since"
                                      + " it is not state holding.");
    
    if ( m_value == null || d != m_value.doubleValue() ){
      m_value = Double.valueOf(d);
      sendMessages();
    }
  }
  
  /** Get current value for this {@link Variable}
   *
   * @return current value for variable. Null if not set yet.
   */
  public Double getValue(){
    if ( !isStateHolding() )
      throw new IllegalStateException("Cannot get Variable: " 
                                      + getVNName() + " value since"
                                      + " it is not state holding.");
    return m_value;
  }
  
  public void onEvent(float f){
    onEvent((double) f);
  }
  
  public void onEvent(double d){
    m_value = d;
    sendMessages();
  }
  
  /** Get Message-type from derived class to send over links.
   *
   * @return Message to send over links.
   */
  protected Message createMessage(){
    if (m_value == null ) return null;

    Message m = new Message(getVNType());
    m.setField(getVNType(), m_value.toString());

    return m;
  }

  /** Reset variable value to default, given that it is state-holding and
   * had no input links (since its state will be set by another variable).
   */
  public void reset(){
    if ( isStateHolding() && getTargetMaxLinks() == 0 )
      setValue(m_default);
  }
  
  /** Get restriction of this variable.
   * Returns null if no restriction.
   */
  public FloatRestriction getRestriction(){
    return (FloatRestriction) super.getRestriction();
  }

  /** Extract value from message. 
   * Note: returns null if errors occurs.
   */
  public Double extractData(Message m){
    HashMap<String, String> map = m.getPayload();

    int size = map.size();
    if ( size > 1 ){
      PackageLogger.log.warning("Received message at " + getVNName() + " with"
                                + " more than one data segment (" + size 
                                + "). Expected only one.");
      return null;
    }
    else if ( map.isEmpty() ){
      PackageLogger.log.warning("Received message at " + getVNName() + " with"
                                + " zero segments. Expected only one.");
      return null;
    }
    
    Collection<String> c = map.values();
    Iterator<String> i = c.iterator();
    
    Double d = null;
    try
      {
        d = Double.valueOf(i.next());
      }
    catch(NumberFormatException nfe){
      PackageLogger.log.warning("Received message at " + getVNName() + " with"
                                + " an illegal data segment.");
      //nfe.printStackTrace();
    }

    return d;
  }
}
