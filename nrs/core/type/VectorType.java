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

/** Represent an NRS vector variable. 
 *
 * _TODO_ Still need to finish this class!
 *
 * @author Thomas French
 */

public abstract class VectorType extends Variable
{
  private byte[] m_value;
  
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
  public VectorType(VariableManager vm, String name,
                 boolean stateHolding, boolean selfUpdating){
    super(vm, name, stateHolding, selfUpdating, "vector");
    
    // default restriction
    setRestriction(new VectorRestriction());
  }

  /** Get restriction of this variable.
   * Returns null if no restriction.
   */
  public VectorRestriction getRestriction(){
    return (VectorRestriction) super.getRestriction();
  }
}
