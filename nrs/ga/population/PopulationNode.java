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

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;
import nrs.core.type.FloatType;
import nrs.core.type.VoidType;

import nrs.ga.mutation.*;
import nrs.ga.crossover.*;
import nrs.ga.selection.*;
import nrs.ga.rng.*;
import nrs.ga.base.GenomeNode;
import nrs.ga.base.Gene;
import nrs.ga.base.PackageLogger;
import nrs.ga.base.Chromosome;
import nrs.ga.base.DependencyException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/** Class to represent PopulationNode.
 *
 * @author Thomas French
*/

public abstract class PopulationNode extends Node
{
  //attributes: size of population 
  //variables: 2 input - StartRun(from GANode->StartRun), Evaluated(from GenomeNode->Finished), 
  //2 output - Start (to GenomeNode->NexEx), Finished (to GANode->FinishedRun)
  
  /** Size of population to use. */
  private int m_population_size;
  
  /** Population of chromosomes. */
  protected ArrayList m_population;
  
  /** Whether on initial run of GA. */
  private boolean m_initial_run;
  
  ///** Variables. */
  protected BooleanType m_varStartRun; //input
  protected FloatType m_varEvaluated; //input
  protected VoidType m_varStartEx; //output
  protected VoidType m_varFinishedRun; //output
  
  //references to genetic operator nodes, which this node contains.
  private CrossoverNode m_crossoverNode;
  private MutationNode m_mutationNode;
  private SelectionNode m_selectionNode;
  private GenomeNode m_genomeNode;
  
  /** Reference to random number generator. */
  private RNGNode m_rng;

  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   * @param type vnType of this node.
   */
  public PopulationNode(VariableManager vmMan, String vnName, String type){
    super(vmMan, vnName, type);

    String var1 = vnName+ ".StartRun";
    m_varStartRun = new BooleanType(vmMan, var1, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_StartRun(m, this, checkType(m, this));
        }
        public void deliver(boolean b)
        {
          handleMessageAt_StartRun(b);
        }
      };
    addVariable(var1, m_varStartRun);
    
    String var2 = vnName+ ".Evaluated";
    m_varEvaluated = new FloatType(vmMan, var2, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Evaluated(m, this, checkType(m, this));
        }
        public void deliver(double d)
        {
          handleMessageAt_Evaluated(d);
        }
      };
    addVariable(var2, m_varEvaluated);
    
    String var3 = vnName+ ".StartEx";
    m_varStartEx = new VoidType(vmMan, var3){
        public void deliver(Message m)
        {
          handleMessageAt_StartEx(m, this, checkType(m, this));
        }
        public void deliver()
        {
          handleMessageAt_StartEx();
        }
      };
    addVariable(var3, m_varStartEx);
    
    String var4 = vnName+ ".FinishedRun";
    m_varFinishedRun = new VoidType(vmMan, var4){
        public void deliver(Message m)
        {
          handleMessageAt_FinishedRun(m, this, checkType(m, this));
        }
        public void deliver()
        {
          handleMessageAt_FinishedRun();
        }
      };
    addVariable(var4, m_varFinishedRun);
    
    //set initial Variable values to be false or 0.0
    m_varStartRun.setDefault(false);
    m_varEvaluated.setDefault(0.0);
    
	//set initial state of fields
    m_initial_run = true;
    m_population_size = 0;
  }
    //----------------------------------------------------------------------//
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  //---------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the PopulationNode variable - StartRun.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public final void handleMessageAt_StartRun(Message m, Variable v, 
                                             boolean diff){
    PackageLogger.log.fine("Received message at StartRun variable!");
    
    Boolean b = m_varStartRun.extractData(m);
    
    if (b != null)
      handleMessageAt_StartRun(b.booleanValue());
    else
      PackageLogger.log.warning("Received message with no boolean value!");
  } 
  /**
   * Process the receipt of a message at the PopulationNode variable - StartRun.
   * Implemented by subclass
   *
   * @param b value received
   */
  public abstract void handleMessageAt_StartRun(boolean b);
  //----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the PopulationNode var - Evaluated.
   * Implemented by subclass.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public final void handleMessageAt_Evaluated(Message m, Variable v, 
                                              boolean diff){
    Double d = m_varEvaluated.extractData(m);

    if ( d != null )
      handleMessageAt_Evaluated(d.doubleValue());
    else
      PackageLogger.log.warning("Received message at Evaluated "
                                +"variable with no double value!");
  }
  /**
   * Process the receipt of a message at the PopulationNode var - Evaluated.
   * Implemented by subclass.
   *
   * @param d double value received
   *
   */
  public abstract void handleMessageAt_Evaluated(double d);
  //----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the PopulationNode var - StartEx.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public final void handleMessageAt_StartEx(Message m, Variable v, 
                                            boolean diff) { 
    PackageLogger.log.fine("Received message at StartEx variable!"
                           +" Used as an output variable only.");
  }
  /**
   * Process the receipt of a message at the PopulationNode variable - StartEx.
   */
  public final void handleMessageAt_StartEx() { 
    PackageLogger.log.fine("Received message at StartEx variable!"
                           + " Used as an output variable only.");
  }
  //----------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the PopulationNode var - FinishedRun.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public final void handleMessageAt_FinishedRun(Message m, Variable v, 
                                                boolean diff) { 
    PackageLogger.log.fine("Received message at FinishedRun variable!"
                           +" Used as an output variable only.");
  }
  /**
   * Process the receipt of a message at the PopulationNode var - FinishedRun.
   *
   */
  public final void handleMessageAt_FinishedRun() { 
    PackageLogger.log.fine("Received message at FinishedRun variable!"
                           +" Used as an output variable only.");
  }
  //----------------------------------------------------------------------//
  
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at PopulationNode!");
  }
  //----------------------------------------------------------------------//
  
  /** Run a generation (iteration) worth of runs. 
   * Implemented by subclass.
   */
  protected abstract void doExperiment();
  
  /** Generate initial population.
   * Implemented by subclass.
   */
  protected abstract void doInitial();
  
  /** Set population size. 
   * If <code>p</code> is not greater than 0, set <code>p</code> to 1.
   *
   * @param p an integer which represents population size.
   */
  protected final void setPopSize(int p){
    if ( p > 0 ) m_population_size = p;
    else p = 1;
  }
  
  /** Return currently set population size.
   * @return population size.
   */
  protected final int getPopSize(){
    return m_population_size;
  }
  
  protected final boolean getInitial(){
    return m_initial_run;
  }
  
  protected final void finishedInitial(){
    m_initial_run = false;
  }
  
  public final void setRNGNode(RNGNode r){
    m_rng = r;
  }
  
  public final void setCrossoverNode(CrossoverNode cn){
    m_crossoverNode = cn;
  }
  
  public final void setMutationNode(MutationNode mn){
    m_mutationNode = mn;
  }
  
  public final void setSelectionNode(SelectionNode sn){
    m_selectionNode = sn;
  }
  
  public final void setGenomeNode(GenomeNode gn){
    m_genomeNode = gn;
  }
  
  protected final GenomeNode getGenomeNode(){
    return m_genomeNode;
  }
  
  protected final RNGNode getRNGNode(){
    return m_rng;
  }
  
  protected final CrossoverNode getCrossoverNode(){
    return m_crossoverNode;
  }
  
  protected final MutationNode getMutationNode(){
    return m_mutationNode;
  }
  
  protected final SelectionNode getSelectionNode(){
    return m_selectionNode;
  }
  
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    //check variables
    Variable v;
    String vName;
    String name = this.getVNName() + ".";
    int num;
    
    // //need to check variables and links
//     Collection variables = allVariables();
//     for(Iterator i = variables.iterator(); i.hasNext(); )
//       {
//         v = (Variable) i.next();
//         vName = v.getVNName();
//         num = v.allLinks().size();
        
//         if ( vName.equals(name + "StartEx") && num != 1 ){
//           throw new DependencyException(name+"StartEx variable must"
//                                         +" have ONE link only: " 
//                                         + num);
//         }
//         else if ( vName.equals(name + "FinishedRun") && num != 1 ){
//           throw new DependencyException(name+"FinishedRun variable"
//                                         +" must have ONE link only: "
//                                         + num);
//         }
//         else if ( vName.equals(name + "StartRun") && num != 1 ){
//           throw new DependencyException(name+"StartRun variable must"
//                                         + " have ONE link only: " 
//                                         + num);
//         }
//         else if ( vName.equals(name + "Evaluated") && num != 1 ){
//           throw new DependencyException(name+"Evaluated variable must"
//                                         +" have ONE link only: " 
//                                         + num);
//         }
//       }
    
    //check nodes - SelectionNode, MutationNode, CrossoverNode, GenomeNode
    if ( m_crossoverNode != null )
      m_crossoverNode.checkDependencies();
    else
      throw new DependencyException("PopulationNode cannot find a "
                                    +" CrossoverNode.");
    if ( m_mutationNode != null )
      m_mutationNode.checkDependencies();
    else
      throw new DependencyException("PopulationNode cannot find a "
                                    +" MutationNode.");
    if ( m_selectionNode != null )
      m_selectionNode.checkDependencies();
    else
      throw new DependencyException("PopulationNode cannot find a "
                                    +" SelectionNode.");
    if ( m_genomeNode != null )
      m_genomeNode.checkDependencies();
    else
      throw new DependencyException("PopulationNode cannot find a "
                                    +" GenomeNode.");
  }
  
  //----------------------------------------------------------------------//
  //QuickSort algorithm - to sort an ArrayList of Chromosomes
  //modified from revised version of James Gosling's QSortAlgorithm demo, 1995.
  //Sort into descending order
  protected void sort(ArrayList l){
    sort(l, 0, l.size()-1);
  }
  
  protected void sort(ArrayList l, int lo0, int hi0){
    int lo = lo0;
    int hi = hi0;
    
    Chromosome c1,c2;
    double d1, d2;
    
    if (lo >= hi) {
      return;
    }
    else if( lo == hi - 1 ) {
      /*
       *  sort a two element list by swapping if necessary 
       */
      c1 = (Chromosome) l.get(lo);
      d1 = c1.getEvaluation();
      c2 = (Chromosome) l.get(hi);
      d2 = c2.getEvaluation();
      if ( d1 > d2 ) {
        Chromosome T = (Chromosome) c1.clone();;
        l.set(lo,c2);
        l.set(hi,T);
      }
      return;
    }
    
    /*
     *  Pick a pivot and move it out of the way
     */
    Chromosome pivot = (Chromosome) l.get((lo + hi) / 2);
    l.set((lo + hi) / 2, l.get(hi));
    l.set(hi,pivot);
    
    while( lo < hi ) {
      /*
       *  Search forward from l[lo] until an element is found that
       *  is greater than the pivot or lo >= hi 
       */
      c1 = (Chromosome) l.get(lo);
      while (c1.getEvaluation() <= pivot.getEvaluation() && lo < hi) {
        lo++;
        c1 = (Chromosome) l.get(lo);
      }
      
      /*
       *  Search backward from l[hi] until element is found that
       *  is less than the pivot, or lo >= hi
       */
      c2 = (Chromosome) l.get(hi);
      while (pivot.getEvaluation() <= c2.getEvaluation() && lo < hi ) {
        hi--;
        c2 = (Chromosome) l.get(hi);
      }
      
      /*
       *  Swap elements l[lo] and l[hi]
       */
      if( lo < hi ) {
        c1 = (Chromosome) l.get(lo);
        Chromosome T = (Chromosome) c1.clone();
        l.set(lo, l.get(hi));
        l.set(hi, T);
      }
    }
    
    /*
     *  Put the median in the "center" of the list
     */
    l.set(hi0, l.get(hi));
    l.set(hi,pivot);
    
    /*
     *  Recursive calls, elements l[lo0] to l[lo-1] are less than or
     *  equal to pivot, elements l[hi+1] to l[hi0] are greater than
     *  pivot.
     */
    sort(l, lo0, lo-1);
    sort(l, hi+1, hi0);
  }
  
  //----------------------------------------------------------------------//
  /** Print population. */
  public void printPopulation(ArrayList population){
    Chromosome c;
    ArrayList l;
    Gene g;
    int num_chr = population.size();
    c = (Chromosome) population.get(num_chr-1);
    int num_genes = c.getNumGenes();
    PackageLogger.log.fine("\nGA population: with " + num_chr 
                           + " Chromosomes. \nEach Chromsome with " 
                           + num_genes + " genes.\n");
    
    //sort(population);
    
    for(int i = num_chr-1; i >= 0; i--){
      c = (Chromosome) population.get(i);
      PackageLogger.log.fine("\nChromosome[" + i + "]:\n");
      l = c.getGenes();
      for(int j = 0; j < num_genes; j++){
        g = (Gene) l.get(j);
        PackageLogger.log.fine("\t Gene["+j+"]: " + g.getValue() 
                               + "\n");
      }
      PackageLogger.log.fine("Evaluation: " + c.getEvaluation()  + "\n");
    }  
  }
  
  public void printBest(ArrayList population){
    Chromosome c;
    int counter = 1; //always at least one best!
    double eval;
    
    c = (Chromosome) population.get(population.size()-1);
    eval = c.getEvaluation(); //evaluation of best chromosome
    
    //check convergence
    for(int k = population.size()-2; k > 0; k--){
      c = (Chromosome) population.get(k);
      if ( eval != c.getEvaluation() ) break;
      counter++;
    }
    
    PackageLogger.log.info("\nBest chromosome(s) ["+counter
                           +"] in population have evaluation: " + eval);
  }
  
  public void printToFile(int epochs) throws IOException{
    Chromosome c;
    ArrayList l;
    Gene g;
    int num_genes;
    double eval;
    
    File d = new File("/tmp/log");
    
    if ( !d.exists() )
      d.mkdir();
    
    long t = System.currentTimeMillis();
    File f = new File(d, "log"+ t);		     
    
    if ( !f.exists() ) f.createNewFile();
    
    FileWriter fw = new FileWriter(f);
    
    c = (Chromosome) m_population.get(0);
    num_genes = c.getNumGenes();
    
    sort(m_population);
    
    fw.write("GA population of "+m_population.size()+" with "
             +num_genes+" number of genes.\n");
    fw.write("GA number of epochs: " + epochs + "\n");
    fw.flush();
    for(int k = 0; k < m_population.size(); k++){
      c = (Chromosome) m_population.get(k);
      fw.write("Chromosome: " + (k+1) + "\n");
      fw.flush();
      l = c.getGenes();
      eval = c.getEvaluation();
      for(int j = 0; j < num_genes; j++){
        g = (Gene) l.get(j);
        fw.write("\t Gene["+(j+1)+"]: " + g.getValue() + "\n");
        fw.flush();
      }
      fw.write("Evaluation: " + eval  + "\n");
      fw.flush();
      
    }
    
    PackageLogger.log.info("Printed out to file: " + f.getAbsolutePath() );
  }
}
