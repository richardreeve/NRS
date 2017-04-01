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
package nrs.ga.mutation;

import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.ga.base.PackageLogger;
import nrs.ga.base.Gene;
import nrs.ga.base.Chromosome;
import nrs.ga.rng.*;

import java.util.ArrayList;

/** SimpleMutationNode.
 * This node implements a simple GA mutation operator, 
 * where each Chromosome is mutated at a specified rate,
 * and a Gene's value is set to a random number between 
 * the min and max values.
 *
 * @author Thomas French
*/
public class SimpleMutationNode extends MutationNode
{
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public SimpleMutationNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "SimpleMutationNode");
  }
  
  //------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at SimpleMutationNode!");  
    super.deliver(m);
  }
  //------------------------------------------------------------------//
  /** Mutate
   *
   * @param population list of Chromosomes in population.
   * @param index for elite (only with generational GA).
   */
  public void mutate(ArrayList population, int index){
    if ( getRate() == 0 ) return; //no mutation specified.
    
    RNGNode rn = getRNGNode();
    Gene g;
    ArrayList genes;
    Chromosome c = (Chromosome) population.get(index);
    int rand, num_genes = c.getNumGenes();
    
    for(int i = index; i < population.size(); i++){
      c = (Chromosome) population.get(i);
      genes = c.getGenes();
      
      //mutate at each gene
      for(int j =0; j < num_genes; j++){
        g = (Gene) genes.get(j);
        rand = rn.getRandInt(100);
        
        if ( rand <= getRate() ){
          //replace current with random value between min and max for the Gene
          g.setValue(rn.getRandDouble(g.getMin(), g.getMax()));
        }
      }
    }
  }
}
