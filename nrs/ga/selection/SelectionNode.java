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
package nrs.ga.selection;

import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.ga.rng.*;
import nrs.ga.base.PackageLogger;
import nrs.ga.base.DependencyException;

import java.util.ArrayList;

/** Class to represent a SelectionNode.
 *
 * @author Thomas French
*/

public abstract class SelectionNode extends Node
{
  private RNGNode m_rng;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   * @param type vnType of this node
   */
  public SelectionNode(VariableManager vmMan, String vnName, String type){
    super(vmMan, vnName, type);
  }
  
  //---------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at SelectionNode!");
  }
  //---------------------------------------------------------------------//
  
  /** Selection - implemented by subclass.
   *
   * @param population list of Chromosomes in population.
   * @param new_population new list of Chromosomes selected for.
   * @param number number of individuals to be selected
   */
  public abstract void selection(ArrayList population, 
                                 ArrayList new_population, int number);
  
  protected final RNGNode getRNGNode(){
    return m_rng;
  }
  
  public final void setRNGNode(RNGNode rng){
    m_rng = rng;
  }
  
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    return; //has no dependencies
  }  
}
