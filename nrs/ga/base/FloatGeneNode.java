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

import java.util.Iterator;
import java.util.Collection;

import nrs.core.type.FloatType;

/** Class to represent GeneNode.
 *
 * @author Thomas French
*/

public class FloatGeneNode extends NumberGeneNode{
  //Variable
  private FloatType m_floatValue;
  
  private String varName;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   * @param type type of this node
   */
  public FloatGeneNode(VariableManager vmMan, String vnName, String type){
    super(vmMan, vnName, type);
    varName = vnName+ ".Value";
    m_floatValue = new FloatType(vmMan, varName, true, true){
            public void deliver(Message m)
            {
                handleMessageAt_Value(m, this, checkType(m, this));
            }
            public void deliver(double d)
            {
                handleMessageAt_Value();
            }	
        };
    addVariable(varName, m_floatValue);
    
  }
  
  public void setVariable(Double value) {
      m_floatValue.setDefault(value.doubleValue());
      PackageLogger.log.fine("User supplied to use initial value: " 
                             + m_floatValue.getValue());
  }


  
  //-----------------------------------------------------------------------//
  
  /** Output new value for GeneNode.
   * Implicitly known about by its parent GenomeNode.
   *
   * @param v new value
   */
  public void setValue(double v){
      m_floatValue.onEvent(v);
      
  }
  
  public double getValue(){
      return m_floatValue.getValue();
  }
  
}
