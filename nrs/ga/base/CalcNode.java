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
package nrs.ga.base;

import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.core.type.FloatType;
import nrs.core.type.BooleanType;

import java.util.ArrayList;

/** Abstract class to represent CalcNode
 * Subclassed to implement specific type of calculation node, 
 * e.g. addition, mean, etc.
 *
 * This node is used to apply the fitness function for the GA on each evalution.
 *
 * @author Thomas French
*/

abstract class CalcNode extends Node
{
  //Variables
  private FloatType m_varEvaluation; //input
  private FloatType m_varEvaluated; //output
  private BooleanType m_varGo; //input & output

  private boolean intType;
  
  //vector of evaluations received
  private ArrayList<Double> m_evaluations;
  
  /** Constructor - protected.
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   * @param type VNType of node.
   */
  protected CalcNode(VariableManager vmMan, String vnName, String type, boolean intType){
    super(vmMan, vnName, type);
    this.intType = intType;
    //register variables
    //input: evaluation (e.g. NSim Evaluator)
    //output: evaluated (GenomeNode)
    
    String varName = vnName+ ".Evaluation";
    m_varEvaluation = new FloatType(vmMan, varName, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Evaluation(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Evaluation(d);
        }
      };
    addVariable(varName, m_varEvaluation);
    
    String varName2 = vnName+ ".Evaluated";
    m_varEvaluated = new FloatType(vmMan, varName2, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Evaluated(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Evaluated(d);
        }
      };
    addVariable(varName2, m_varEvaluated);
    
    String varName3 = vnName+ ".Go";
    m_varGo = new BooleanType(vmMan, varName3, false, false){
        public void deliver(Message m)
        {
          handleMessageAt_Go(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_Go(b);
        }
      };
    addVariable(varName3, m_varGo);
    
    m_evaluations = new ArrayList<Double>(1);
  }

  /** Returns the intType boolean field of the node - 
  * true for integer, false for double
  */
  public final boolean getNumType() {
      return intType;
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  
  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":CalcNode!");
  }
  //-----------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the Evaluation variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Evaluation(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Evaluation variable!");
    
    //store value
    Double d = m_varEvaluation.extractData(m);
    if ( d != null ){
      handleMessageAt_Evaluation(d.doubleValue());
    }
  }
  /**
   * Process the receipt of a message at the Evaluation variable.
   *
   * @param d the value received
   */
  public void handleMessageAt_Evaluation(double d) { 	
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Evaluation variable!");  
    setEvaluation(d);
  }
  
  /**
   * Process the receipt of a message at the Evaluated variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Evaluated(Message m, Variable v, boolean diff){ 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Evaluated variable!"+
                           " Used only as an output variable.");
  }
  /**
   * Process the receipt of a message at the Evaluated variable.
   *
   * @param d the value received
   */
  public void handleMessageAt_Evaluated(double d) { 	
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Evaluated variable!"+
                           " Used only as an output variable.");
  }
  
  /**
   * Process the receipt of a message at the Go variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Go(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Go variable!");
    
    Boolean b = m_varGo.extractData(m);
    if ( b != null )
      handleMessageAt_Go(b.booleanValue());
  }
  /**
   * Process the receipt of a message at the Go variable.
   * @param b the received value
   */
  public void handleMessageAt_Go(boolean b) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Go variable!");
    
    m_varGo.onEvent(b);
  }
  
  //----------------------------------------------------------------------//
  
  private final void setEvaluation(double f){
    m_evaluations.add(f);
    
    PackageLogger.log.fine("CalcNode received evaluation: " + f);
    
    PackageLogger.log.fine("Number of evaluations received is: " 
                           + m_evaluations.size() + " and number of "
                           + "children is: " + allNodes().size());
    
    //number of evaluations received equals number of children nodes
    if ( m_evaluations.size() == allNodes().size() ){
      double m_fitness = calcFitness(m_evaluations);

      if (intType) {
          m_fitness = Math.floor(calcFitness(m_evaluations));
          PackageLogger.log.fine("Fitness is calculated to be: " 
                                 + (int)m_fitness);
      } else {
          PackageLogger.log.fine("Fitness is calculated to be: " 
                                 + m_fitness);
      }
      
      m_evaluations.clear(); //clear all evaluations
      
      //send fitness to GenomeNode
      m_varEvaluated.onEvent(m_fitness);
    }
  }
  
  //----------------------------------------------------------------------//
  /** Calculate fitness given list of evaluations.
   * Subclassed by specific implementations of fitness functions, 
   * e.g. addition or mean, etc.
   * 
   * @param list list of evaluations from all EvaluationNodes
   */
  protected abstract double calcFitness(ArrayList<Double> list);
  
  //-----------------------------------------------------------------------//
  
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    ArrayList nodes = allNodes();
    EvaluationNode n;
    int num = nodes.size();
    if ( num == 0 ){
      throw new DependencyException("CalcNode has no children nodes." 
                                    + "It requires at least one "
                                    + " EvaluationNode.");
    }
    
    //call subnodes checkDependencies() methods
    for(int j = 0; j < nodes.size(); j++){
      n = (EvaluationNode) nodes.get(j);
      n.checkDependencies();
    }
  }
}
