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

import nrs.ga.selection.SelectionNode;
import nrs.ga.base.Chromosome;
import nrs.ga.base.PackageLogger;
import nrs.ga.rng.*;

import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import java.util.Random;
import java.util.ArrayList;

/** TournamentSelectionNode.
 * Implements Binary Tournament selection, where each 
 * two parents are chosen randomly, and with a certain probability, 
 * defined by the user the fitter one is chosen, otherwise the weaker 
 * one is selected.
 *
 * @author Thomas French
 */

public class TournamentSelectionNode extends SelectionNode
{
  /** Probabilty that fitter individual is chosen in tournament. */
  private int m_rate;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public TournamentSelectionNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "TournamentSelectionNode");
  }
  
  /** Selection algorithm - tournament selection using 2 parents
   *
   * @param population ordered list of Chromosomes in population.
   * @param new_population new list of Chromosomes selected for.
   * @param n number of individuals to select.
   */
  public void selection(ArrayList population, ArrayList new_population,int n){
    // Choose two individuals at random, then a random number r. 
    //If r < k (k a parameter greater than .5), 
    //select the fitter of the two; otherwise select the less fit.
    Chromosome c1, c2;
    int pop_size = population.size(), i1, i2, rand, i = 0;
    double d1, d2;
    
    RNGNode rng = getRNGNode();
    
    //test for elitism (only if using Generational GA)
    if ( !new_population.isEmpty() ){
      i += new_population.size();
    }
    
    for(; i < n; i++){
      i1 = rng.getRandInt(pop_size); //parent1
      i2 = rng.getRandInt(pop_size); //parent2
      
      c1 = (Chromosome) population.get(i1);
      c2 = (Chromosome) population.get(i2);
      
      d1 = c1.getEvaluation();
      d2 = c2.getEvaluation();
      
      c1 = (Chromosome) c1.clone();
      c1.setEvaluation(0.0);
      c2 = (Chromosome) c2.clone();
      c2.setEvaluation(0.0);
      
      rand = rng.getRandInt(100) + 1;
      //stronger wins!
      if ( rand <= m_rate ){
        //case c1 is fitter (or the rare case that they are equal)
        if ( d1 >= d2 ) 
          new_population.add(c1);
        else 
          new_population.add(c2);
      }
      //weaker wins!
      else{
        if ( d1 < d2 ) //case c1 is weaker
          new_population.add(c1);
        else //(d2 <= d1 )
          new_population.add(c2);
      }	    
    }  
  }
  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at TournamentSelectionNode!");
    
    //extract number of individuals in each tournament - default is 75
    Integer i = new Integer(Integer.parseInt(m.getField("rate")));
    if ( i != null && i.intValue() > 0 && i.intValue() <= 100 ){
      m_rate = i.intValue();
      PackageLogger.log.fine("Binary Tournament selection using rate of: "
                             + m_rate 
                             + " that fitter contestant will win each"
                             + " tournament."); 
    }
    else{ 
      m_rate = 75;
      PackageLogger.log.fine("Binary Tournament selection using default"
                             + " rate of: " + m_rate);
    }  
    super.deliver(m);
  }
}
