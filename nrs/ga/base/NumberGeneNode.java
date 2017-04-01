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

/** Class to represent GeneNode.
 *
 * @author Thomas French
*/

public abstract class NumberGeneNode extends GeneNode{
  
  private double m_high;
  private double m_low;
  private String m_type;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   * @param type type of this node
   */
  public NumberGeneNode(VariableManager vmMan, String vnName, String type){
    super(vmMan, vnName, type);
  }
  
  /**
  * Sets the default value for the variable
  */
  public abstract void setVariable(Double value);


  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at GeneNode!");
    
    String type = m.getField("type");
    Boolean use_initial = new Boolean(m.getField("initial"));
    Double value = null;
    Double low = new Double(m.getField("min"));
    Double high = new Double(m.getField("max"));
   
    if ( type == null || low == null || high == null || 
         use_initial == null ){
      
      PackageLogger.log.severe("At least one of the compulsory "
                               +" attributes was not supplied!"
                               +" This is a major error.");
      return;
    }
    set_use_initial(use_initial.booleanValue());
    //use a non-random starting value supplied by user.
    if ( use_initial() ){
        value = new Double(m.getField("value"));
    }
    
    if ( use_initial() && value == null ){
      PackageLogger.log.warning("User supplied to use initial value,"
                                +" but no value was supplied. Will use"
                                +" default starting value of 0.0.");
    }else if ( use_initial() && value != null ){
        setVariable(value);
    }
    
    if ( !type.equals("") )
      m_type = type;
    else 
      PackageLogger.log.fine("User supplied empty type field!");
    
    if ( low.doubleValue() > high.doubleValue() )
      {
        PackageLogger.log.fine("User supplied low > high field!"
                               +" I'll swap them for you, or else"
                               +" send again.");
        m_low = high.doubleValue();
        m_high = low.doubleValue();
      }
    else
      {	
        m_low = low.doubleValue();
        m_high = high.doubleValue();
      }
    
  }
  //-----------------------------------------------------------------------//
  
  /** Output new value for GeneNode.
   * Implicitly known about by its parent GenomeNode.
   *
   * @param v new value
   */
  public abstract void setValue(double v);
      
  public abstract double getValue();
      
  
  public double getHigh(){
    return m_high;
  }
  
  public double getLow(){
    return m_low;
  }
  
 
  
}
