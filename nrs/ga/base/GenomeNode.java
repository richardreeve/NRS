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

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;
import nrs.core.type.FloatType;
import nrs.core.type.VoidType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

/** Class to represent GenomeNode.
 * This class acts as a container for {@link GeneNode}s and a {@link CalcNode}.
 *
 * @author Thomas French
*/

public class GenomeNode extends Node
{
  //Variables.
  private VoidType m_varStartEx; //input & output
  private BooleanType m_varGo; //input & output
  private FloatType m_varEvaluated; //input & output
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName Name of this node.
   */
  public GenomeNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "GenomeNode");
    
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
    
    String varName2 = vnName+ ".Go";
    m_varGo = new BooleanType(vmMan, varName2, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Go(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_Go(b);
        }
      };
    addVariable(varName2, m_varGo);
    
    String varName3 = vnName+ ".Evaluated";
    m_varEvaluated = new FloatType(vmMan, varName3, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Evaluated(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Evaluated(d);
        }
      };
    addVariable(varName3, m_varEvaluated);
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
                           + ":GenomeNode!");
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
    
    //start new experiment - values have already been sent to GeneNodes, 
    //who have then sent them to their targets.
    //setGo(true); //DON'T DO THIS ANYMORE
    
    //start new experiment 
    m_varStartEx.onEvent();
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
                           + ":Evaluated variable!");
    
    //store value
    Double d = m_varEvaluated.extractData(m);
    if ( d != null ){
      handleMessageAt_Evaluated(d.doubleValue());
    }
  }
  /**
   * Process the receipt of a message at the Evaluated variable.
   *
   * @param d the value received
   */
  public void handleMessageAt_Evaluated(double d) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Evaluated variable!");
    
    m_varEvaluated.onEvent(d);
  }
  //-----------------------------------------------------------------------//
  /** Update GeneNodes with new values. 
   *
   * @param list list of Genes.
   */
  public final void changeValues(ArrayList list){
    //iterate through list setting new values of GeneNodes.
    ArrayList genes = getGeneNodes();
    int num = genes.size(), num2 = list.size();
    
    if ( num != num2 ){
      PackageLogger.log.severe("Number of GeneNodes (" + num 
                               + ") not equal to number of Genes (" 
                               + num2 + ")" ); 
      return;
    }
    
    for(int i = 0; i < num; i++){
      NumberGeneNode gn = (NumberGeneNode) genes.get(i);
      Gene g = (Gene) list.get(i);
      
      gn.setValue(g.getValue()); //sets value and sends it to target.
    }  
  }
  
  /** Get GeneNodes contained by GenomeNode. 
   * @return collection of GeneNodes of this GenomeNode.
   */
  public final ArrayList getGeneNodes(){
    ArrayList l = allNodes(), temp = new ArrayList();
    
    Node n;
    for(int i=0;i<l.size();i++){
      n = (Node) l.get(i);
      if ( n instanceof GeneNode )
        temp.add(n);
    }
    return temp;
  }
  
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    Variable v;
    String vName;
    String name = this.getVNName() + ".";
    int num;
    
    //need to check variables and links
    Collection variables = allVariables();
    for(Iterator i = variables.iterator(); i.hasNext(); )
      {
        v = (Variable) i.next();
        vName = v.getVNName();
        num = v.allLinks().size();
        
        // Go can have 1 input and 1 output
        //  if ( vName.equals(name + "Go") && num != 1 ){
        //               throw new DependencyException(name+"Go variable must "
        //                                             + " have ONE link only. " 
        //                                             + " It has: " + num);
        //                                              	
            //}
        // Evaluated - 1 input and 1 ouput
        //             else if ( vName.equals(name + "Evaluated") && num != 2 ){
        //               throw new DependencyException(name+"Evaluated variable"
        //                                             +" must have TWO links only."
        //                                             + " It has: " + num);
//             }
//             else if ( vName.equals(name + "StartEx") && num != 2 ){
        
//               throw new DependencyException(name+"StartEx variable must"
//                                             +" have TWO links."
//                                             + " It has: " + num);
//             }
//             else if ( vName.equals(name + "Evaluation") && num != 1 ){
//               throw new DependencyException(name+"Evaluation variable must"
//                                             + " have ONE link only."
//                                             + " It has: " + num);
//             }
      }
    
    // check children
    ArrayList nodes = allNodes();
    Node n;
    num = nodes.size();
    if ( num == 0 ){
      throw new DependencyException("GenomeNode could not find any"
                                    + " children nodes.");
    }
    
    GeneNode gn;
    CalcNode cn;
    //call subnodes checkDependencies() methods
    for(int j = 0; j < nodes.size(); j++){
      n = (Node) nodes.get(j);
      if ( n instanceof GeneNode ){
        gn = (GeneNode) n;
        gn.checkDependencies();
      }
      else if ( n instanceof CalcNode ){
        cn = (CalcNode) n;
        cn.checkDependencies();
      }
    }
  }
}
