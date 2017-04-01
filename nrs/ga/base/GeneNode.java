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

/** Class to represent GeneNode.
 *
 * @author Thomas French
*/

public abstract class GeneNode extends Node{


  private boolean m_use_initial;

  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   * @param type type of this node
   */
  public GeneNode(VariableManager vmMan, String vnName, String type){
    super(vmMan, vnName, type);
  }


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
    PackageLogger.log.fine("Received message at Value variable!"+
                           " Used only as an output variable.");
  }
  
  /**
   * Process the receipt of a message at the Value variable.
   *
   */
  public void handleMessageAt_Value() { 
    PackageLogger.log.fine("Received message at Value variable!"+
                           " Used only as an output variable.");
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  

 /** {@link Message} to deliver to this Node. */
  public abstract void deliver(Message m);
   

 /** Check dependencies for this node and all the nodes it contains. 
  *
  * @throws DependencyException thrown if any dependencies are not satisfied 
  */
  public void checkDependencies() throws DependencyException {
    Variable v;
    String vName;
    String name = this.getVNName() + ".";
    int links;
    
    //need to check variables and links
    Collection variables = allVariables();
    for(Iterator i = variables.iterator(); i.hasNext(); )
      {
        v = (Variable) i.next();
        vName = v.getVNName();
        links = v.allLinks().size();
        
        if ( vName.equals(name + "Output") && links < 1 ){
          throw new DependencyException(name+"Output variable must"
                                        +" have at least ONE link.");
        }
      }
  }

  public boolean use_initial(){
    return m_use_initial;
  }

  public void set_use_initial(boolean b) {
      m_use_initial = b;
  }

}
