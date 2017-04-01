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

/** GaussianMutationNode.
 * This node implements a Gaussian GA mutation operator, 
 * where each Chromosome is mutated by adding a random value 
 * to its current value, using a random number generated from a 
 * Gaussian distribution, using the Gene's value as the mean and 
 * the standard deviation are specified as a node attribute.
 *
 * @author Thomas French
*/
public class GaussianMutationNode extends MutationNode
{
  /** Standard deviation, set by user. */
  private double m_std;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public GaussianMutationNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "GuassianMutationNode");
  }
  
  //----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at SimpleMutationNode!");
    
    //extract standard deviation
    Double d = new Double(m.getField("std"));
    if ( d != null && d.doubleValue() > 0 ){
      m_std = d.doubleValue();
      PackageLogger.log.fine("Using " + m_std 
                             + " as standard deviation.");
    }
    else{ 
      m_std = 1.0;
      PackageLogger.log.fine("Using default " + m_std 
                             + " as standard deviation.");
    }
    
    super.deliver(m); //pass to superclass.
  }
  //----------------------------------------------------------------------//
  
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
    double newD;
    
    for(int i = index; i < population.size(); i++){
      c = (Chromosome) population.get(i);
      genes = c.getGenes();
      
      //mutate
      for(int j = 0; j < num_genes; j++){
        g = (Gene) genes.get(j);
        rand = rn.getRandInt(100); 
        
        if ( rand <= getRate() ){
          newD = rn.getRandGaussian(g.getValue(),  m_std);
          
          if ( newD < g.getMin() )
            newD = g.getMin();
          else if ( newD > g.getMax() )
            newD = g.getMax();
          
          // replace current value with Gaussian random value 
          // between min & max
          g.setValue(newD);
        }
      }
    }
  }
}
