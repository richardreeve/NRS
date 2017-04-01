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
import nrs.core.base.Node;

import nrs.ga.base.GenomeNode;
import nrs.ga.base.NumberGeneNode;
import nrs.ga.base.Chromosome;
import nrs.ga.base.Gene;
import nrs.ga.base.PackageLogger;
import nrs.ga.base.DependencyException;

import nrs.ga.replacement.*;

import java.util.ArrayList;

/** Class to represent SteadyStatePopNode.
 * This class implements a steady-state type of Genetic Algorithm, 
 * where only a few individuals (number supplied by user) are replaced each 
 * iteration of the GA. These individuals are then crossed-over and 
 * mutated, before being put back into population (according to some
 * replacement policy.
 *
 * @author Thomas French
*/

public class SteadyStatePopNode extends PopulationNode
{
  /** Counter for the Chromosome of population currently being evaluated. */
  private int m_popCounter;
  
  /** Temporary copy of population. */
  private ArrayList m_temp_population;
  
  /** ReplacementNode reference. */
  private ReplacementNode m_replacementNode;
  
  /** Number of individuals to replace. */
  private int m_individuals;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public SteadyStatePopNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "SteadyStatePopNode");
    
    m_popCounter = 0;
    m_individuals = 1;
  }
  
  //----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the PopulationNode var - StartRun.
   *
   * @param go value received
   *
   */
  public void handleMessageAt_StartRun(boolean go){
    PackageLogger.log.fine("Recieved message at StartRun with value: " 
                           + go);
    //start next generation.
    if ( go && !m_varStartRun.getValue() ){
      PackageLogger.log.fine("Starting GA epoch...");
      m_varStartRun.setValue(true);
      
      //if first generation - need to populate 
      if ( getInitial() ) {
        doInitial();
      }else{
        //Do selection, crossover, mutation and replacement
        sort(m_population); //sort individuals by evaluation score
        printBest(m_population);
	
        //add code to deal with length of genes == 1
        boolean use_crossover = checkCrossover(m_population);
        int individuals = m_individuals;
        if ( use_crossover )
          individuals *= 2;
        
        //System.out.println("Number of individuals: " + individuals);
        
        getSelectionNode().selection(m_population, m_temp_population, 
                                     individuals);
        //crossover
        if ( use_crossover ) 
          getCrossoverNode().crossover(m_temp_population,0,1);
        
        //mutation
        //System.out.println("Number of individuals to mutate: " 
        // + m_temp_population.size() );
        
        getMutationNode().mutate(m_temp_population, 0);	
      }
      
      doExperiment(); //evaluate new individuals
    }
    else if ( !go && m_varStartRun.getValue() ){
      //stop run now!
      m_varStartRun.setValue(false);
      printBest(m_population);
    }
  }
  
  //----------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the PopulationNode var - Evaluated.
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
    
    if ( getInitial() )
      c = (Chromosome) m_population.get(m_popCounter-1);
    else
      c = (Chromosome) m_temp_population.get(m_popCounter-1);
    
    c.setEvaluation(d);
    PackageLogger.log.fine("Evaluation score for Chromosome: " 
                           + (m_popCounter) + " is: " 
                           + c.getEvaluation());
    
    //set value of variable
    m_varEvaluated.setValue(d);
    
    //do next evaluation
    doExperiment();
  }
  
  //-----------------------------------------------------------------------
  
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at GenerationalPopNode!");

    Integer popSize = new Integer(Integer.parseInt(m.getField("popSize")));
    if ( popSize == null ){
      PackageLogger.log.warning("No population size defined!"
                                +" We'll use a size of 2.");
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
    
    //extract individuals attribute - must be greater than zero
    Integer i = new Integer(Integer.parseInt(m.getField("individuals")));
    if ( i != null && i.intValue() > 0 && i.intValue() < p ){
      m_individuals = i.intValue();
      PackageLogger.log.config("Replacing: " + m_individuals 
                               + " each iteration of GA."); 
    }
    else{ 
      m_individuals = 1;
      PackageLogger.log.config("Number of individuals to replace"
                               +" each iteration is, by default, 1.");
    }
  }
  //-----------------------------------------------------------------------//
  
  /** Generate population.
   * With population size number of Chromosomes, where each Chromosome has GeneNode number of Genes. 
   * Populate GeneNode values with a random number, unless a starting value has been specified.
   * Also, the initial random number can be specified to be within a two floats, e.g 0.0 and 1.0.
   */
  protected void doInitial(){
    ArrayList genes = getGenomeNode().getGeneNodes();
    int num_of_genes = genes.size();
    //PackageLogger.log.info("Number of genes is: " + num_of_genes);
    Chromosome c;
    
    //Create popSize new Chromosome objects, populate them with num_of_genes Gene objects.
    for ( int i = 1; i <= getPopSize(); i++){
      c = new Chromosome();
      c.addGenes(num_of_genes);
      m_population.add(c);
    }
    
    //generate initial random numbers for whole population
    //using RNGNode, if no initial value has been entered.
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
    
    //Steady-State GA needs replacement policy
    m_replacementNode.setRNGNode(getRNGNode()); 
  }
  
  //---------------------------------------------------------------------//
  
  //start a new experiment while there are new individuals
  protected void doExperiment(){
    Chromosome c;
    int limit;
    
    if ( getInitial() )
      limit = getPopSize();
    else
      limit = m_temp_population.size();
    
    PackageLogger.log.fine("m_popCounter: " + m_popCounter 
                           + " and limit is: " + limit);
    
    if ( m_popCounter < limit ){
      
      //new values need to be sent from GeneNodes to targets
      if ( getInitial() ) //no genetic operators or first run
        c = (Chromosome) m_population.get(m_popCounter);
      else
        c = (Chromosome) m_temp_population.get(m_popCounter);
      
      getGenomeNode().changeValues(c.getGenes());

      m_varStartEx.onEvent();
    }
    //end of run.
    else  if ( m_popCounter == limit )
      {
        PackageLogger.log.fine("Finished GA epoch...");
        if ( getInitial() ){
          finishedInitial();
          //printPopulation(m_population);
        }
        else{
          sort(m_temp_population); //sort before replacement
          //replacement - replace m_temp_population into m_population
          m_replacementNode.replace(m_temp_population, m_population);
          m_temp_population = new ArrayList();
          
          //printPopulation(m_population);
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
  
  /** Find first Chromosome with no evaluation score. 
   * @param l list of Chromosomes.
   * @return index to first Chromosome with no evaluation.
   * @deprecated
   */
  private int findNext(ArrayList l){
    Chromosome c;
    
    for ( int y = 0; y < l.size(); y++){
      c = (Chromosome) l.get(y);
      if ( c.getEvaluation() == 0.0 ){
        return y;
      }
    }
    return -1;  //no index point
  }
  
  public void setReplacementNode(ReplacementNode rNode){
    m_replacementNode = rNode;
  }
  
  public ReplacementNode getReplacementNode(){
    return m_replacementNode;
  }
  
  /* Check whether # of genes is not odd */
  private boolean checkCrossover(ArrayList pop){
    int size;
    
    //get number of genes each Chromsomes has.
    Chromosome c = (Chromosome) pop.get(0);
    size = c.getGenes().size();
    
    //only do crossover with even # of genes
    if ( (size % 2) != 0 ) return false;
    else return true;
  }
  
  /** Check dependencies for this node and all the nodes it contains. 
   * Override superclass.
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    //need to check for ReplacementNode
    ArrayList nodes = allNodes();
    int num = nodes.size();
    
    if ( num == 0 )
      throw new DependencyException("PopulationNode cannot find any subnodes.");
    Node n;
    for(int i = 0; i < num; i++){
      n = (Node) nodes.get(i);
      if ( n instanceof ReplacementNode ){
        ReplacementNode rn = (ReplacementNode) n;
        rn.checkDependencies();
        
        //call superclass' method to check variables, and other nodes
        super.checkDependencies();
        return;
      }
    }
    throw new DependencyException("PopulationNode cannot find ReplacemenNode.");
  }
  
}

