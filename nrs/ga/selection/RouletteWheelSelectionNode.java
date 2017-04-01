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

/** RouletteWheelSelectionNode.
 * Implements Roulette-wheel sampling selection, where each 
 * individual has a probability of being chosen proportionate to 
 * its fitness compared to the total fitness of the population.
 * 
 * @author Thomas French
 */

public class RouletteWheelSelectionNode extends SelectionNode
{
  private double[] m_fitness;
  private double m_sum_fitness;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public RouletteWheelSelectionNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "RouletteWheelSelectionNode");
  }
  
  /** Selection algorithm - Roulette-wheel sampling.
   *
   * @param population ordered list of Chromosomes in population.
   * @param new_population new list of Chromosomes selected for.
   * @param n number of individuals to select.
   */
  public void selection(ArrayList population, ArrayList new_population,int n){
    int i = 0, size = population.size(), index, j;
    double randDouble = 0.0, temp = 0.0;
    Chromosome c;
    RNGNode rng = getRNGNode();
    m_fitness = new double[size];
    
    //calculate fitness proportionate scores using evaluation score.
    evaluate(population);
    
    //test for elitism (only if using Generational GA)
    if ( !new_population.isEmpty() ){
      i += new_population.size();
    }
    
    //do roulette wheel selection for new population n-i 
    //(because of elitism) times.
    for(; i < n; i++){
      //get random double between 0.0 and 1.0
      randDouble = rng.getRandDouble(); //m_sum_fitness);
      
      //do roulette wheel sampling.
      for(j = 0; j < size; j++){
        
        temp += m_fitness[j];
        
        if ( randDouble <= temp ){
          //Copy to next population
          c = (Chromosome) population.get(j);
          //c.setEvaluation(0.0);
          new_population.add(c.clone());
          
          break;
        }
      }
      temp = 0.0;
    }
  }
  
  /** Calculate the proportion (p[x]) of the roulette wheel each chromosome 
   * (i) will take up.
   * p[i] = f[i] / (average) sum (i to N) f[i], where p is proportion of pie,
   * f is fitness 
   * and n is number of chromosomes.
   */
  private void evaluate(ArrayList l){
    m_sum_fitness = 0.0;
    Chromosome c;
    int size = l.size();
    
    //calculate total sum of fitness
    for(int i = 0; i < size; i++){
      c = (Chromosome) l.get(i);
      m_sum_fitness += c.getEvaluation();
    }
    
    //get proportionate fitness
    for(int i = 0; i < size; i++){
      c = (Chromosome) l.get(i);
      m_fitness[i] = c.getEvaluation() / m_sum_fitness;
    }
  }    
  
  //----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received msg at RouletteWheelSelectionNode!");   
    super.deliver(m); //pass to superclass.
  }
}
