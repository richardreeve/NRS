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
package nrs.ga.replacement;

import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.ga.rng.*;
import nrs.ga.base.PackageLogger;
import nrs.ga.base.Chromosome;

import java.util.ArrayList;

/** RandomReplacementNode -subclass of ReplacementNode
 * Implements 'replacement strategy' replacing random members of 
 * population with new members, if the new ones evaluation score is better.
 *
 * @author Thomas French
 */

public class RandomReplacementNode extends ReplacementNode
{
  /** Constructor. Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   */
  public RandomReplacementNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "RandomReplacementNode");
  }
  
  //----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at RandomReplacementNode!");
  }
  //----------------------------------------------------------------------//
  /** Replacement strategy - Random Replacement
   * Starts with trying to replace less fit individuals.
   * New members can, probabistically, replace each other.
   *
   * @param new_members potential new members (Chromosomes) to replace 
   * old individuals.
   * @param population list of Chromosomes in population.
   */
  public void replace(ArrayList new_members, ArrayList population){
    int size_new = new_members.size(), size_of_pop = population.size(), 
      index;
    Chromosome newC, oldC;
    RNGNode rng = getRNGNode();
    
    //loop through all for all new potential members
    for(int j = size_new-1; j >= 0; j--){	
      index = rng.getRandInt(size_of_pop);
      
      newC = (Chromosome) new_members.get(j);
      oldC = (Chromosome) population.get(index);
      
      if ( newC.getEvaluation() >  oldC.getEvaluation() ){
        population.set(index, newC);
      }
    }
  }
}
