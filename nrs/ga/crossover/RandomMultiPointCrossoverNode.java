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
package nrs.ga.crossover;

import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.ga.rng.*;
import nrs.ga.base.Gene;
import nrs.ga.base.Chromosome;
import nrs.ga.base.PackageLogger;

import java.util.ArrayList;
import java.util.Arrays;

/** RandomMultipointCrossoverNode.
 * Implements random multiple point crossover, randomly selecting 
 * the parents, the number of crossover points and the points themselves.
 * 
 * @author Thomas French
 */

public class RandomMultiPointCrossoverNode extends CrossoverNode
{
  /** Constructor. Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   */
  public RandomMultiPointCrossoverNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "RandomMultiPointCrossoverNode");
  }

  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at RandomMultiPointCrossoverNode!");
    super.deliver(m);
  }
  //----------------------------------------------------------------------//
  
  /** Crossover
   *
   * @param population list of Chromosomes in population.
   * @param index to start from (deal with elistism)
   * @param number_of_children number of children each crossing-over will keep
   */
  public void crossover(ArrayList population, int index, 
                        int number_of_children){
    int counter = 1, popSize, index1, index2, cpoint1, cpoint2, size, x;
    int[] points;
    
    //get number of genes each Chromosome has.
    Chromosome c = (Chromosome) population.get(0);        
    size = c.getGenes().size();        

    //no crossover - just return and save some time.
    if ( getRate() == 0 || size == 1 ) return;
    
    //remove individuals through by elitism
    ArrayList elite = new ArrayList(index);
    for(int k = 0; k < index; k++){
      elite.add(population.remove(k));
    }
    popSize = population.size(); //shoudl be population size minus elite
    boolean[] crossed = new boolean[popSize];

    ArrayList child1, child2;
    //save time not having to make call everytime
    RNGNode rng = getRNGNode(); 
    Gene g;
    Gene[] temp;
    
    temp = new Gene[size];
    
    while ( counter <= Math.floor(popSize/2) ){
      
      //get mum
      index1 = rng.getRandInt(popSize);
      while ( crossed[index1] ){
        index1 = rng.getRandInt(popSize);
      }
      crossed[index1]=true;
      //get dad
      index2 = rng.getRandInt(popSize);
      while ( crossed[index2] ){
        index2 = rng.getRandInt(popSize);
      }
      crossed[index2]=true;

      //get copy of Genes from mum and dad Chromosomes.
      c = (Chromosome) population.get(index1);
      child1 = (ArrayList) c.getGenes().clone();
      c = (Chromosome) population.get(index2);
      child2 = (ArrayList) c.getGenes().clone();
      
      //random multiple point crossover
      //number of points to crossover is size-1.
      x = rng.getRandInt(size); //get 0(inc) to size(ex)
      
      if ( x > 1 ){
        points = new int[x];
	
        //generate random points - points.length of points
        for(int i = 0; i < points.length; i++){
          points[i] = rng.getRandInt(size-1)+1;
        }
        //sort in ascending order - guaranteed O(n*logn)
        Arrays.sort(points);
      }
      else if ( x == 1 ){
        points = new int[2];
        points[0] = rng.getRandInt(size-1)+1;
        points[1] = size;
      }
      else points = new int[0];
	    
      //if no crossover mum (child1) and dad (child2) copied directly
      if ( (rng.getRandInt(100)+1) <= getRate() ){
	
        //do actual crossover
        for(int k = 0; k < points.length-1; k++){
          cpoint1 = points[k];
          cpoint2 = points[k+1];
          
          for(int j = cpoint1; j < cpoint2; j++){
            g = (Gene) child1.get(j);
            //make a copy of object to store in temp
            temp[j] = (Gene) g.clone(); 
          }
          for(int i = cpoint1; i < cpoint2; i++){
            g = (Gene) child2.get(i);
            child1.set(i, g.clone());
            child2.set(i, temp[i]);
          }
        }

      }
      //add children to population	    
      if ( number_of_children == 1 ){
        x = rng.getRandInt(2) + 1;
        if ( x == 1 ){
          c = (Chromosome) population.get(index1);
          c.addGenes(child1);
          population.set(index2, null);
        }
        else{
          c = (Chromosome) population.get(index2);
          c.addGenes(child2);	    
          population.set(index1, null);
        }
      }
      else{
        c = (Chromosome) population.get(index1);
        c.addGenes(child1);   
        c = (Chromosome) population.get(index2);
        c.addGenes(child2);
      }
      counter++;
    }
    
    //remove null pointers
    for(int z = index; z < population.size(); z++){
      if ( population.get(z) == null )
        population.remove(z);
    }
    //add elite back to form array of size population+elite
    for(int v = 0; v < elite.size(); v++){
      population.add(elite.remove(v));
    }
  }
}
