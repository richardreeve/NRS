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
package nrs.core.unit;

import nrs.core.type.StringType;
import nrs.core.type.StringRestriction;
import nrs.core.base.VariableManager;

/** Class to represent the Filename unit.
 *
 * @author Thomas French
 */

public abstract class Filename extends StringType
{
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
  public Filename(VariableManager vm, String name, 
                boolean stateHolding, boolean selfUpdating){
    super(vm, name, stateHolding, selfUpdating, "filename");
    
    // set Filename restriction
    setRestriction(new StringRestriction(StringRestriction.Type.Filename));
  }

  /** Receive String from variable within same component. 
   *     
   * @param s String value
   */
  public abstract void deliver(String s);
}
