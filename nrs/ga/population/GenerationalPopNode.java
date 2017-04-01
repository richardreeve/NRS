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
package nrs.ga.population;

import nrs.core.base.VariableManager;
import nrs.core.base.Message;
import nrs.core.base.Variable;

import nrs.ga.base.GenomeNode;
import nrs.ga.base.NumberGeneNode;
import nrs.ga.base.Chromosome;
import nrs.ga.base.Gene;
import nrs.ga.base.PackageLogger;

import java.util.ArrayList;

/** Class to represent GenerationalPopNode.
 * This class implements a generational type of Genetic Algorithm, 
 * where each generation is evaluated and then selected, crossed-over and mutated.
 *
 * @author Thomas French
*/

public class GenerationalPopNode extends PopulationNode
{
  /** Counter for number of Chromosomes of population have been evaluated. */
  private int m_popCounter;
  
  /** Whether using elitism (>0) or not (0). */
  private int m_elitism;
  
  /** Temporary copy of population. */
  private ArrayList m_temp_population;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public GenerationalPopNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "GenerationalPopNode");
    
    m_popCounter = 0;
  }
  
  //----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the PopulationNode variable - StartRun
   *
   * @param go value received
   *
   */
  public void handleMessageAt_StartRun(boolean go){
    PackageLogger.log.fine("Recieved message at StartRun with value: " 
                           + go);
    if ( go && !m_varStartRun.getValue() ){
      PackageLogger.log.fine("Starting GA epoch...");
      m_varStartRun.setValue(true);
      
      //if first generation - need to populate 
      if ( getInitial() ) {
        doInitial();
      }
      else{
        //Do selection, crossover and mutation.
        sort(m_population); //sorted into descending order
        //printPopulation(m_population);
        //printBest(m_population);
        
        if ( m_elitism > 0 ){
          //copy n (m_elitism) of the best across to new population - 
          Chromosome c;
          int pSize = m_population.size();
          for(int i = pSize-1; i >= (pSize-m_elitism); i--){
            c = (Chromosome) m_population.get(i);
            m_temp_population.add(c);
          }
        }
        //selection - select for whole new population
        //System.out.println("Do selection on " + m_population.size());
        getSelectionNode().selection(m_population, m_temp_population, 
                                     m_population.size());
        //crossover
        //System.out.println("Do crossover on " + m_temp_population.size());
        getCrossoverNode().crossover(m_temp_population, m_elitism, 2);
        
        //mutation
        //System.out.println("Do mutation on " + m_temp_population.size());
        
        getMutationNode().mutate(m_temp_population, m_elitism);
      }
      
      doExperiment(); //evaluate generation
    }
    else if ( !go && m_varStartRun.getValue() ){
      //stop run now!
      PackageLogger.log.fine("Stopping GA run!");
      m_varStartRun.setValue(false);
      printBest(m_population);
    }
  }
  //----------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the PopulationNode variable - Evaluated.
   *
   * @param d the value received
   */
  public void handleMessageAt_Evaluated(double d){
    //Store d value in message for current Chromosome.
    Chromosome c;
    
    if ( !m_varStartRun.getValue() ){
      PackageLogger.log.fine("Received message at Evaluated,"
                             +" but not running experiment!");
      return;
    }
    
    //get Chromosome that was just evaluated at m_popCounter - 1.
    if ( getInitial() )
      c = (Chromosome) m_population.get(m_popCounter-1);
    else
      c = (Chromosome) m_temp_population.get(m_popCounter-1);
    
    c.setEvaluation(d);
    PackageLogger.log.fine("Evaluation score for Chromosome: " 
                           + (m_popCounter-1) + " is: " 
                           + c.getEvaluation());
    
    //set value of variable
    m_varEvaluated.setValue(d);
    
    //do next evaluation
    doExperiment();
  }
  
  //---------------------------------------------------------------------//
  
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at GenerationalPopNode!");
    
    Integer popSize = new Integer(Integer.parseInt(m.getField("popSize")));
    if ( popSize == null ){
      PackageLogger.log.warning("No population size defined!"
                                + " We'll use a size of 2.");
      popSize = new Integer(2);
    }
    
    //to save having to make method call everytime.
    int p = popSize.intValue(); 
    
    if ( getPopSize() == 0 ){
      setPopSize(p);
      m_population = new ArrayList(p);
      m_temp_population = new ArrayList(1);
      PackageLogger.log.config("Population size: " + p);
    }
    else{
      setPopSize(p);
      m_population = new ArrayList(p);
      m_temp_population = new ArrayList(1);
      PackageLogger.log.config("Population size changed to: " 
                               + getPopSize());
    }
    
    //extract elitism attribute, by default it is 0 (not used)
    Integer i = new Integer(Integer.parseInt(m.getField("elitism")));
    if ( i != null && i.intValue() > 0 && i.intValue() < p ){
      m_elitism = i.intValue();
      PackageLogger.log.config("Using Elitism for top: " + m_elitism 
                               + " individuals."); 
    }
    else{ 
      m_elitism = 0;
      PackageLogger.log.config("Not using Elitism.");
    }
  }
  //-----------------------------------------------------------------------//
  
  /** Generate initial population.
   * With population size number of Chromosomes, where each has GeneNode 
   * number of Genes. 
   * Populate GeneNode values with a random number, unless a starting value 
   * has been specified.
   * Also, the initial random number can be specified to be within a two 
   * doubles, e.g 0.0 and 1.0.
   */
  protected void doInitial(){
    ArrayList genes = getGenomeNode().getGeneNodes();
    int num_of_genes = genes.size();
    Chromosome c;
    
    // Create popSize new Chromosome objects, 
    // populate them with num_of_genes Gene objects.
    for ( int i = 0; i < getPopSize(); i++){
      c = new Chromosome();
      c.addGenes(num_of_genes);
      
      m_population.add(c);
    }
    
    

    // generate initial random numbers for whole population
    // using RNGNode, if no initial value has been entered.
    for(int k = 0; k < num_of_genes;k++){
      NumberGeneNode gn = (NumberGeneNode) genes.get(k);
      double min = gn.getLow();
      double max = gn.getHigh();
      //get initial state - whether seeded with value or not.
      double value = gn.getValue(); 
      boolean initial = gn.use_initial();
      
      for(int m = 0; m < getPopSize(); m++){
        c = (Chromosome) m_population.get(m);
        Gene g = (Gene) c.getGenes().get(k);
	
        if ( !initial )
          {
            double d = getRNGNode().getRandDouble(min, max);
            PackageLogger.log.fine("Setting initial value to be: "
                                   + d);
            g.setValue(d);
          }
        else 
          g.setValue(value); //set default value
        
        g.setMin(min);
        g.setMax(max);
      }
    }
    
    //set required fields for genetic operators
    getSelectionNode().setRNGNode(getRNGNode());
    getCrossoverNode().setRNGNode(getRNGNode());
    getMutationNode().setRNGNode(getRNGNode());
  }
  
  //-----------------------------------------------------------------------//
  
  //start a new experiment until m_popCounter == population size.
  protected void doExperiment(){
    Chromosome c;
    
    //more Chromosomes to evaluate
    if ( m_popCounter < getPopSize() ){
      
      //new values need to be sent from GeneNodes to targets
      if ( getInitial() ) //no genetic operators on first run
        c = (Chromosome) m_population.get(m_popCounter);
      else
        c = (Chromosome) m_temp_population.get(m_popCounter);
      
      getGenomeNode().changeValues(c.getGenes());
      
      PackageLogger.log.fine("Next Chromosome to evaluate is: " 
                             + m_popCounter);
      
      m_varStartEx.onEvent();
    }
    //end of run.
    else if ( m_popCounter == getPopSize() ){
      PackageLogger.log.fine("Finished GA epoch...");
      //if was initial run - change flag
      if ( getInitial() ){
        finishedInitial();	
        //printPopulation(m_population);
      }
      else{
        //swap pointers - temporary population is new population.
        m_population = m_temp_population;
        m_temp_population = new ArrayList(getPopSize());
        
        // print out population or best in population
        //printPopulation(m_population);
        //printBest(m_population);
      }
      
      m_popCounter = 0;
      
      //Set StartRun variable value to false
      m_varStartRun.setValue(false);
      
      //Set FinishedRun variable value to true - one iteration done.
      m_varFinishedRun.onEvent();	   
      return;
    }
    m_popCounter++;    
  }
}
