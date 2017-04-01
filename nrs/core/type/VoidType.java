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

/** Class to represent NRS void variable.
 *
 * @author Thomas French
 */
public abstract class VoidType extends Variable
{
  /** Default Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   */
  public VoidType(VariableManager vm, String name){
    super(vm,name, false, false, "void");
  }
  
  /** Constructor
   *
   * @param vm the {@link VariableManager} to register with, so that
   * this new variable automatically gets an ID.
   *
   * @param name the string name for this variable (eg QueryCID)
   *
   * @param type derived variable-type
   */
  public VoidType(VariableManager vm, String name, String type){
    super(vm,name, false, false, type);
  }
  
  /** For on-board links, to send current value too.
   *     
   * @param target target of link
   */
  protected void send(Variable target){
    VoidType v = (VoidType) target;
    v.deliver();
  }
  
  /** Receive void call from variable within same component. 
   */
  public abstract void deliver();
  
  /** Get Message-type from derived class to send over links.
   *
   * @return Message to send over links.
   */
  protected Message createMessage(){
    return new Message(getVNType());
  }
  
  public void onEvent(){
    sendMessages();
  }
}
