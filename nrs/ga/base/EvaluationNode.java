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

import nrs.core.type.BooleanType;
import nrs.core.type.FloatType;
import nrs.core.type.VoidType;

import java.util.ArrayList;
import java.util.Collection;

/** Class to represent EvaluationNode.
 * 
 * This node is used to represent an evaluation. When this node is triggered 
 * it triggers its subnodes ({@link ValueNode}s) to send their values out. 

 * The node then receives the evaluation score from the target and relays 
 * this value to its parent {@link CalcNode}.
 *
 * @author Thomas French
*/

class EvaluationNode extends Node
{
  //Variables.
  private VoidType m_varStartEx; //input
  private VoidType m_varStartNextEx; //output
  private FloatType m_varEvaluation; //input & output
  private VoidType m_varTrigger; //output
  private BooleanType m_varGo; //output
  private BooleanType m_varConsistent; //input
  
  /* Whether this node is expecting evalution value. */
  private boolean m_expectingEval;
  private int m_numConsistents;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   */
  public EvaluationNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "EvaluationNode");
    
    //register variables
    //input: startEx, evaluation
    //output: trigger (children (>0)), evaluation (CalcNode), startNextEx
    
    String varName = vnName+ ".StartEx";
    m_varStartEx = new VoidType(vmMan, varName){
        public void deliver(Message m)
        {
          handleMessageAt_StartEx(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_StartEx();
        }
      };
    addVariable(varName, m_varStartEx);
    
    String varName2 = vnName+ ".Evaluation";
    m_varEvaluation = new FloatType(vmMan, varName2, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Evaluation(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Evaluation(d);
        }
      };
    addVariable(varName2, m_varEvaluation);
    
    String varName3 = vnName+ ".Trigger";
    m_varTrigger = new VoidType(vmMan, varName3){
        public void deliver(Message m)
        {
          handleMessageAt_Trigger(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Trigger();
        }
      };
    addVariable(varName3, m_varTrigger);
    
    String varName4 = vnName+ ".StartNextEx";
    m_varStartNextEx = new VoidType(vmMan, varName4){
        public void deliver(Message m)
        {
          handleMessageAt_StartNextEx(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_StartNextEx();
        }
      };
    addVariable(varName4, m_varStartNextEx);
    
    String varName5 = vnName+ ".Go";
    m_varGo = new BooleanType(vmMan, varName5, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Go(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_Go(b);
        }
      };
    addVariable(varName5, m_varGo);
    
    String varName6= vnName+ ".Consistent";
    m_varConsistent = new BooleanType(vmMan, varName6, false, false){
        public void deliver(Message m)
        {
          handleMessageAt_Consistent(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_Consistent(b);
        }
      };
    addVariable(varName6, m_varConsistent);
    
    m_expectingEval = false;
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
                           + ":EvaluationNode!");
  }
  //-----------------------------------------------------------------------//
  
  /**
   * Process the receipt of a message at the StartEx variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_StartEx(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":StartEx variable!");
    handleMessageAt_StartEx();
  }
  /**
   * Process the receipt of a message at the StartEx variable.
   */
  public void handleMessageAt_StartEx() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":StartEx variable!");
    m_numConsistents = 0;
    
    //set bool flag to receive evaluation true
    m_expectingEval = true;
    
    //trigger child nodes (ValueNodes) to send their values
    m_varTrigger.onEvent();
  }

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
    
    if ( !m_expectingEval || m_numConsistents < allNodes().size() ) return;
    
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
    
    if ( !m_expectingEval || m_numConsistents < allNodes().size() ) return;
    
    PackageLogger.log.fine("Evaluation for: " + getVNName() 
                           + " is: " + d);
    
    m_expectingEval = false;
    
    //send value to CalcNode
    m_varEvaluation.onEvent(d);
    
    //send StartNextEx to another EvaluationNode (if one exists)
    m_varStartNextEx.onEvent();
  }
  /**
   * Process the receipt of a message at the StartNextEx variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_StartNextEx(Message m, Variable v, 
                                          boolean diff){ 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":StartNextEx variable!"+
                           " Used only as an output variable.");
  }
  /**
   * Process the receipt of a message at the StartNextEx variable.
   */
  public void handleMessageAt_StartNextEx() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":StartNextEx variable!"+
                           " Used only as an output variable.");
  }
  
  /**
   * Process the receipt of a message at the Trigger variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Trigger(Message m, Variable v, boolean diff){ 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Trigger variable!"+
                           " Used only as an output variable.");
  }

  /**
   * Process the receipt of a message at the Trigger variable.
   */
  public void handleMessageAt_Trigger() { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Trigger variable!"+
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
                           + ":Go variable!"
                           + " Used only as an output variable.");
  }
  /**
   * Process the receipt of a message at the Go variable.
   * @param b the received value
   */
  public void handleMessageAt_Go(boolean b) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Go variable!"
                           + " Used only as an output variable.");
  }
  
  /**
   * Process the receipt of a message at the Consistent variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Consistent(Message m, Variable v, 
                                         boolean diff){ 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Consistent variable!");
    
    Boolean b = m_varConsistent.extractData(m);
    if ( b != null )
      handleMessageAt_Consistent(b);
  }
  /**
   * Process the receipt of a message at the Consistent variable.
   * @param b the received value
   */
  public void handleMessageAt_Consistent(boolean b) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Consistent variable!");
    
    if ( b == true )
      m_numConsistents++;
    
    //start new experiment
    if ( m_numConsistents == allNodes().size() && m_expectingEval ){
      PackageLogger.log.finer("All ValueNodes are consistent. "
                              + " Ready to go!");
      m_varGo.onEvent(true);
    }
  }
  
  //-----------------------------------------------------------------------//
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    String vName;
    String name = this.getVNName() + ".";
    int num;
    
    //need to check variables and links
    Collection<Variable> variables = allVariables();
    for(Variable v : variables)
      {
        vName = v.getVNName();
        num = v.allLinks().size();
        
        //  //check variables and their links
        //                 if (vName.equals(name + "StartEx") && num != 1 ){
//                   throw new DependencyException(name+"StartEx variable"
//                                                 + " must have ONE link "
//                                                 + " only: " + num);
// 		}
//                 else if ( vName.equals(name+"Evaluation") && (num != 2) ){
//                   throw new DependencyException(name+"Evaluation variable"
//                                                 + " must have only TWO links."
//                                                 + "One input and one output. "
//                                                 + "It currently has: " + num);
//                 }
//                 else if ( vName.equals(name+"Go") && num != 1 ){
//                   throw new DependencyException(name+"Go variable"
//                                                 + " must have ONE link "
//                                                 + " only: " + num);
//                 }
	    }

    //check for ValueNodes - although not needed
    ArrayList nodes = allNodes();
    num = nodes.size();
    if ( num == 0 ){
      PackageLogger.log.warning(getVNName() 
                                + ":EvaluationNode has no children "
                                + " (ValueNodes).");
    }	
    
    ValueNode vn;
    //call subnodes checkDependencies() methods
    for(int i=0;i<nodes.size();i++){
      vn = (ValueNode) nodes.get(i);
      vn.checkDependencies();
    }
  }
}
