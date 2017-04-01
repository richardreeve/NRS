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
import nrs.core.type.VoidType;

import nrs.ga.population.PopulationNode;
import nrs.ga.rng.RNGNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.io.IOException;

/** Represent top-level Node for the GA component.
 *
 * @author Thomas French
*/

class GANode extends Node{

  //Variables
  private BooleanType m_varStartGA; //input
  private BooleanType m_varStartRun; //output
  private VoidType m_varFinishedRun; //input

  /** Number of epochs (generations) to do. */
  private int m_epochs;
  /** Counter for number of epochs (generations) done. */
  private int m_icounter; 

  private boolean intType;
  
  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public GANode(VariableManager vmMan, String vnName){
    super(vmMan, vnName);
    // default uses double values
    intType = false;
    
    m_varStartGA = new BooleanType(vmMan, vnName + ".StartGA", 
                                       true, true){
        public void deliver(Message m)
        {
          handleMessageAt_StartGA(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_StartGA(b);
        }
      };
    addVariable(vnName + ".StartGA", m_varStartGA);
    
    m_varStartRun = new BooleanType(vmMan, vnName + ".StartRun", 
                                        false, true){
        public void deliver(Message m)
        {
          handleMessageAt_StartRun(m, this, checkType(m, this));
        }
        public void deliver(boolean b)
        {
          handleMessageAt_StartRun(b);
        }		
      };
    addVariable(vnName + ".StartRun", m_varStartRun);
    
    m_varFinishedRun = new VoidType(vmMan, vnName + ".FinishedRun"){
        public void deliver(Message m)
        {
          handleMessageAt_FinishedRun(m, this, checkType(m, this));
        }
        public void deliver()
        {
          handleMessageAt_FinishedRun();
        }
      };
    addVariable(vnName + ".FinishedRun", m_varFinishedRun);
    
    //initialise fields
    m_epochs = 0;
    m_icounter = 0;
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
	PackageLogger.log.fine("Received message at GANode!");

        if (m.hasField("IntegerType")) {
            Boolean b  = new Boolean(m.getField("IntegerType"));
            if (b) {
                intType = true;
            } 
        }
	
	//need to extract attributes - number of epochs (generations)
	if ( m.hasField("epochs")){
	    int iter;
	    try{
		iter = Integer.parseInt(m.getField("epochs"));
	    }catch(NumberFormatException nfe){
		PackageLogger.log.warning("Number format exception with "
					  +"message "+ m 
					  + ", can't get epochs.");
		return;
	    }

	    if ( m_epochs == 0 ){
		m_epochs = iter;
		PackageLogger.log.config("Number of epochs: " + m_epochs);
	    }else{
		if ( !m_varStartGA.getValue().booleanValue() ){
		    if ( iter == m_epochs ) return;
		    m_epochs = iter; //change if GA hasn't started.
		    PackageLogger.log.config("Changed number of epochs to: "
					     + m_epochs);
		}else
		    PackageLogger.log.warning("GA has already started and so"+
					      " it is not possble to change"+
					      " number of epochs to: " + iter);
	    }
	}
    }
    //----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the GANode variable - StartGA.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */
    public void handleMessageAt_StartGA(Message m, Variable v, boolean diff) { 
	PackageLogger.log.fine("Received message at StartGA variable!");

	Boolean b = m_varStartGA.extractData(m);
	
	if (b != null)
	    handleMessageAt_StartGA(b.booleanValue());
	else
	    PackageLogger.log.warning("Received message with no"
				      + " boolean value!");
    }
    /**
     * Process the receipt of a message at the GANode variable - StartGA.
     *
     * @param b the boolean value received
     */
    public void handleMessageAt_StartGA(boolean b){
	if  ( b && !m_varStartGA.getValue().booleanValue() ){
	    //start GA - 
	    PackageLogger.log.info("GA is starting...");

	    //need to check that all dependencies are satisfied
	    try
		{
		    checkDependencies();
		}
	    catch(DependencyException de){
		PackageLogger.log.severe("Dependencies have not been"
					 +" satisfied, cannot start GA!");
		de.printStackTrace();
		return;
	    }

	    m_varStartGA.setValue(true);
	    m_icounter++; //first run
	    m_varStartRun.onEvent(true);
	}
	else if ( b && m_varStartGA.getValue().booleanValue() ){
	    //do nothing, already started GA
	    PackageLogger.log.fine("GA already started, doing nothing.");
	}
	else if ( !b && m_varStartGA.getValue().booleanValue() ){
	    //stop GA (which is running)
	    PackageLogger.log.info("GA stopping with " 
				   + (m_epochs - m_icounter) + " epochs left!");
	    //stop at end of epoch?
	    m_varStartGA.setValue(false);
	    m_varStartRun.onEvent(false);//start end of run behaviour
	}
	else if ( !b && !m_varStartGA.getValue().booleanValue() ){
	    PackageLogger.log.fine("Received message with " + b + 
				   ", but GA has not started yet!");
	}
    }
    //----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the GANode variable - StartRun.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */
    public void handleMessageAt_StartRun(Message m, Variable v, boolean diff) { 
	PackageLogger.log.warning("Received message at StartRun variable!"+
				  " Used as an output variable only.");
    }
    /**
     * Process the receipt of a message at the GANode variable - StartRun.
     */
    public void handleMessageAt_StartRun(boolean b){
	PackageLogger.log.warning("Received message at StartRun variable!"+
				  " Used as an output variable only.");
    }
    //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the GANode variable - FinishedRun.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */
    public void handleMessageAt_FinishedRun(Message m, Variable v, 
                                            boolean diff) { 
	handleMessageAt_FinishedRun();
    }
    /**
     * Process the receipt of a message at the GANode variable - FinishedRun.
     */
    public void handleMessageAt_FinishedRun() { 
	PackageLogger.log.fine("Received message at FinishedRun variable!");

	if ( m_varStartGA.getValue().booleanValue() ) {
	    
	    //next epoch
	    if ( m_icounter < m_epochs ){
		//send message out through StartRun variable
		PackageLogger.log.info("Epoch "+m_icounter+" over, with " 
				       + (m_epochs-m_icounter) + " left.");
		m_icounter++;
		m_varStartRun.onEvent(true);
	    }
	    //run over
	    else{
		//GA run has ended peacefully...
		PackageLogger.log.info("Run has ended after "+ m_epochs 
				       + " epochs.");

		m_icounter = 0; //reset
		
		m_varStartGA.setValue(false);
		m_varStartRun.onEvent(false);//do end of run behaviour

	    }
	}
	else if ( !m_varStartGA.getValue().booleanValue() ){
	    PackageLogger.log.info("GA epoch over and run has ended "
				   + "forcefully with: " 
				   + (m_epochs-m_icounter) + " epochs left.");
	}
    }
    //-----------------------------------------------------------------------
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
	// 	if (vName.equals(name + "StartRun") && ( num < 1 || num > 1)){
// 		    throw new DependencyException(name+"StartRun variable"
//                                                   + " must have ONE link "
//                                                   + " only: " + num);
// 		}
		
//                 else if ( vName.equals(name + "StartGA") && num < 1 ){
//                   throw new DependencyException(name+"StartGA variable must"
//                                                 + " have at least ONE link.");
// 		}
// 		else if ( vName.equals(name + "FinishedRun") && 
//                           ( num < 1 || num > 1) ){
//                   throw new DependencyException(name+"FinishedRun variable"
//                                                 +" must have only ONE link.");
// 		}
	    }

	//no subnodes
	ArrayList nodes = allNodes();
	Node n;
	num = nodes.size();
	if ( num == 0 ){
	    throw new DependencyException("GANode could not find an "
                                          + " RNGNode or a PopulationNode.");
	}
	else if ( num == 1 ){
	    n = (Node) nodes.get(0);
	    if ( n instanceof RNGNode )
		throw new DependencyException("GANode cannot find a "
                                              + " PopulationNode.");
	    else if  ( n instanceof PopulationNode )
		throw new DependencyException("GANode cannot find an "
                                              +" RNGNode.");
	}
	
	//call subnodes checkDependencies() methods
	for(int j = 0; j < nodes.size(); j++){
          n = (Node) nodes.get(j);
          if ( n instanceof RNGNode ){
            RNGNode rng = (RNGNode) n;
            rng.checkDependencies();
          }
          else if  ( n instanceof PopulationNode ){
            PopulationNode p = (PopulationNode) n;
            p.checkDependencies();
          }
	}
    }
}

