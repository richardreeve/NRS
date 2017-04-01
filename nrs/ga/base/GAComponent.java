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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import nrs.core.base.BaseComponent;
import nrs.core.base.Link;
import nrs.core.base.Message;
import nrs.core.base.MessageTools;
import nrs.core.base.Node;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.RouteManager;
import nrs.core.base.FieldNotFoundException;
import nrs.core.message.*;

import static nrs.core.message.Constants.Fields.*;

import nrs.ga.population.*;
import nrs.ga.rng.*;
import nrs.ga.selection.*;
import nrs.ga.mutation.*;
import nrs.ga.crossover.*;
import nrs.ga.replacement.*;

/**
 * Root Component class for ga component, which handles all message handling.
 *
 * NOTE: CreateNode and DeleteNode could be done in a more elegant way.
 *       Using Factory pattern, where you pass in the VNType.
 *
 * @author Thomas French
 */
public class GAComponent extends BaseComponent
{
    /** Name of component. */
    private final static String m_Name = "NRS.ga";
    /** Component type. */
    private final static String m_CType = "ga";
    /** Component version. */
    private final static String m_Version = "1.0";
    /** Whether this component has a CSL descriptor file. */
    private final static boolean m_HasCSL = true;
    /** Can the component speak BMF. */
    private final static boolean m_SpeaksBMF = false;
    /** Can the component speak PML. */
    private final static boolean m_SpeaksPML = true;

    /** Root node for this component. */
    private GANode m_gaNode;
    private PopulationNode m_populationNode;
    private RNGNode m_rngNode;
    private CrossoverNode m_crossoverNode;
    private MutationNode m_mutationNode;
    private ReplacementNode m_replacementNode;
    private SelectionNode m_selectionNode;
    private GenomeNode m_genomeNode;
    private CalcNode m_calcNode;

    //----------------------------------------------------------------------
    /**Constructor
     *
     * @param vMan reference to {@link VariableManager} object.
     */
    public GAComponent(VariableManager vMan)
    {
	super(vMan, m_Name, m_CType, m_SpeaksBMF, m_SpeaksPML, 
	      m_HasCSL, m_Version);
	m_vMan = vMan;
    }
    
    //----------------------------------------------------------------------//

    /**
     * Process the receipt of a message at the CreateNode variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */
    public void handleMessageAt_CreateNode(Message m, Variable v, boolean diff) 
    {

	// Extract message fields
	int vnid;
	String vnName, vnType;
        boolean intType = false;

	try{
	    vnName = m.checkField(F_vnName);
	    vnType = m.checkField(F_vnType);
	    vnid = Integer.parseInt(m.checkField(F_vnid));
	}
	catch (FieldNotFoundException e){
	    PackageLogger.log.warning(m.getType() + " message missing "
				      + e.fieldName() + " field; ignoring");
	    return;
	}

	PackageLogger.log.fine("Received a CreateNode message, for type: " +
			       vnType + " with name: " + vnName );

	//receive message to create root node - GANode
	if ( vnType.equals("GANode") ){

	    m_gaNode = new GANode(m_vMan, vnName);
            
            addRootNode(m_gaNode);

	    PackageLogger.log.fine("Created GANode (root node) with name: "
				   + vnName);
	}

	//PopulationNode - currently only GenerationalPopNode
	else if ( vnType.equals("GenerationalPopNode") || vnType.equals("SteadyStatePopNode") ){
	    if ( vnType.equals("GenerationalPopNode") )
		m_populationNode = new GenerationalPopNode(m_vMan, vnName);
	    else if ( vnType.equals("SteadyStatePopNode") )
		m_populationNode = new SteadyStatePopNode(m_vMan, vnName);

	    //add to root node
	    m_gaNode.addNode(m_populationNode);

	    //this node requires reference to RNGNode
	    if ( m_rngNode != null )
		m_populationNode.setRNGNode(m_rngNode);

	    PackageLogger.log.fine("Created PopulationNode with name: " + vnName);
	}

	//RNGNode - currently only UniformRNGNode
	else if ( vnType.equals("UniformRNGNode")){
            if (m_gaNode != null) {
                intType = m_gaNode.getNumType();
            }
	    m_rngNode = new UniformRNGNode(m_vMan, vnName, intType);

	    //add to root node
	    m_gaNode.addNode(m_rngNode);

	    //Certain nodes require a reference to RNGNode
	    if ( m_populationNode != null )
		m_populationNode.setRNGNode(m_rngNode);

	    PackageLogger.log.fine("Created RNGNode with name " + vnName);
	}

	//SelectionNode
	else if ( vnType.equals("RouletteWheelSelectionNode")
		  || vnType.equals("TournamentSelectionNode") ){

	    if ( vnType.equals("RouletteWheelSelectionNode") )
		m_selectionNode = new RouletteWheelSelectionNode(m_vMan, vnName);
	    else if ( vnType.equals("TournamentSelectionNode") )
		m_selectionNode = new TournamentSelectionNode(m_vMan, vnName);

	    m_populationNode.addNode(m_selectionNode);

	    m_populationNode.setSelectionNode(m_selectionNode);

	    PackageLogger.log.fine("Created SelectionNode with name: " + vnName);
	}

	//CrossoverNode
	else if ( vnType.equals("OnePointCrossoverNode")
		  || vnType.equals("TwoPointCrossoverNode")
		  || vnType.equals("NPointCrossoverNode")
		  || vnType.equals("UniformCrossoverNode")
		  || vnType.equals("RandomMultiPointCrossoverNode") ){

	    if ( vnType.equals("OnePointCrossoverNode") )
		m_crossoverNode = new OnePointCrossoverNode(m_vMan, vnName);
	    if ( vnType.equals("TwoPointCrossoverNode") )
		m_crossoverNode = new TwoPointCrossoverNode(m_vMan, vnName);
	    if ( vnType.equals("NPointCrossoverNode") )
		m_crossoverNode = new NPointCrossoverNode(m_vMan, vnName);
	    if ( vnType.equals("RandomMultiPointCrossoverNode") )
		m_crossoverNode = new RandomMultiPointCrossoverNode(m_vMan,
								    vnName);
	    if ( vnType.equals("UniformCrossoverNode") )
		m_crossoverNode = new UniformCrossoverNode(m_vMan, vnName);

	    m_populationNode.addNode(m_crossoverNode);

	    m_populationNode.setCrossoverNode(m_crossoverNode);

	    PackageLogger.log.fine("Created CrossoverNode with name: " + vnName);
	}

	//MutationNode
	else if ( vnType.equals("SimpleMutationNode")
		  || vnType.equals("GaussianMutationNode") ){

	    if ( vnType.equals("SimpleMutationNode") )
		m_mutationNode = new SimpleMutationNode(m_vMan, vnName);
	    else if ( vnType.equals("GaussianMutationNode") )
		m_mutationNode = new GaussianMutationNode(m_vMan, vnName);

	    m_populationNode.addNode(m_mutationNode);

	    m_populationNode.setMutationNode(m_mutationNode);

	    PackageLogger.log.fine("Created MutationNode with name: " + vnName);
	}

	//ReplacementNode - only with SteadyStatePopNode
	else if ( vnType.equals("WorstReplacementNode") ||
		  vnType.equals("RandomReplacementNode") ){

	    if ( vnType.equals("WorstReplacementNode") )
		m_replacementNode = new WorstReplacementNode(m_vMan, vnName);
	    else if ( vnType.equals("RandomReplacementNode") )
		m_replacementNode = new RandomReplacementNode(m_vMan, vnName);

	    if ( m_populationNode instanceof SteadyStatePopNode ){
		//add to SteadyStatePopNode.
		m_populationNode.addNode(m_replacementNode);

		SteadyStatePopNode pop = (SteadyStatePopNode) 
                  m_populationNode;
		pop.setReplacementNode(m_replacementNode);
	    }

	    PackageLogger.log.fine("Created ReplacementNode with name: "
				   + vnName);
	}

	//GenomeNode
	else if ( vnType.equals("GenomeNode") ){
	    m_genomeNode = new GenomeNode(m_vMan, vnName);

	    m_populationNode.addNode(m_genomeNode);

	    m_populationNode.setGenomeNode(m_genomeNode);

	    PackageLogger.log.fine("Created a GenomeNode with name: " 
                                   + vnName);
	}

	//GeneNodes
	else if ( vnType.equals("FloatGeneNode") ){
            if (m_gaNode != null) {
                intType = m_gaNode.getNumType();
            }
            if (intType) {
                PackageLogger.log.warning("\n\nUsing Float Gene Node with integer GA\n\n");
            } 
            FloatGeneNode gn = new FloatGeneNode(m_vMan, vnName, "FloatGeneNode");
            m_genomeNode.addNode(gn);
            
            PackageLogger.log.fine("Created a FloatGeneNode with name: " + vnName);
        }

        else if ( vnType.equals("IntGeneNode") ){
            if (m_gaNode != null) {
                intType = m_gaNode.getNumType();
            }
            if (!intType) {
                PackageLogger.log.warning("\n\nUsing Int Gene Node with float GANode\n\n");
            }
            IntGeneNode gn = new IntGeneNode(m_vMan, vnName, "IntGeneNode");
            m_genomeNode.addNode(gn);
            
            PackageLogger.log.fine("Created an IntGeneNode with name: " + vnName);
        }

	else if ( vnType.equals("EvaluationNode") ){
	    EvaluationNode en = new EvaluationNode(m_vMan, vnName);
	    m_calcNode.addNode(en);
	}

	//CalcNode types - add new ones here!
	else if ( vnType.equals("AddCalcNode") ){
            if (m_gaNode != null) {
                intType = m_gaNode.getNumType();
            }
	    if ( m_calcNode == null ){

		if ( vnType.equals("AddCalcNode") )
		    m_calcNode = new AddCalcNode(m_vMan, vnName, intType);

		m_genomeNode.addNode(m_calcNode);
	    }
	}

         // FunctionCalcNode
        else if ( vnType.equals("FunctionCalcNode") ){
            if (m_gaNode != null) {
                intType = m_gaNode.getNumType();
            }
	    if ( m_calcNode == null ){
                
		if ( vnType.equals("FunctionCalcNode") )
		    m_calcNode = new FunctionCalcNode(m_vMan, vnName, intType);

		m_genomeNode.addNode(m_calcNode);
	    }
	}


	else if ( vnType.equals("BooleanValueNode") ||
		  vnType.equals("FloatValueNode") ||
                  vnType.equals("IntegerValueNode") ||
                  vnType.equals("DummyValueNode") ){

	    ValueNode vn = null;

	    if ( vnType.equals("BooleanValueNode") )
		vn = new BooleanValueNode(m_vMan, vnName);
	    else if ( vnType.equals("FloatValueNode") )
		vn = new FloatValueNode(m_vMan, vnName);
            else if ( vnType.equals("IntegerValueNode") )
		vn = new IntegerValueNode(m_vMan, vnName);
            else if ( vnType.equals("DummyValueNode") )
		vn = new DummyValueNode(m_vMan, vnName);

	    //get right EvaluationNode and add this node to it
	    int end = vnName.lastIndexOf((int)'.');
	    String evalName = vnName.substring(0,end);

	    EvaluationNode n = null;
	    try{
		n = (EvaluationNode) m_vMan.get(evalName);
	    }
	    catch(ClassCastException cce){
		PackageLogger.log.warning(cce.getMessage());
	    }

	    if ( n != null ){
		n.addNode(vn);
		PackageLogger.log.fine("Added ValueNode: "+ vnName
				       + " to EvaluationNode: " + evalName);
	    }
	    else
		PackageLogger.log.warning("Not able to CreateNode with name:"
					  + vnName + " and node type: "
					  + vnType);
	}

	else{
	    PackageLogger.log.warning("CreateNode message received for unknown"
				      + " node type: " + vnType);
	}

    }
    //----------------------------------------------------------------------
    /**
     * Override in inherited classes to process the receipt of a message
     * at the DeleteNode variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */
    public void handleMessageAt_DeleteNode(Message m,
					   Variable v,
					   boolean diff){

	//extract vnid from message
	int vnid;
	try{
	    vnid = Integer.parseInt(m.checkField(F_vnid));
	}
	catch (FieldNotFoundException e){
	    PackageLogger.log.warning(m.getType() + " message missing "
				      + e.fieldName() + " field; ignoring");
	    return;
	}

	if ( vnid <= 0 ){
	    PackageLogger.log.warning("DeleteNode message received with vnid "
				      + vnid + ", this is an error.");
	    return;
	}

	PackageLogger.log.fine("Received a DeleteNode message for vnID: "+vnid);

        Node n; // use later, if required
        try
        {
          n = (Node) m_vMan.get(vnid);
        }
        catch(ClassCastException cce){
          PackageLogger.log.warning("DeleteNode message received not targeted at " 
                                    + "a node. VNID: " + vnid);
          return;
        }

	if ( n == null ){
	    PackageLogger.log.warning("DeleteNode message received for unknown"
				      + " node with vnid " + vnid);
	    return;
	}

	//remove root node from m_vMan
	if ( m_gaNode != null && vnid == m_gaNode.getVNID() ){
	    //remove all nodes, variables and links contained by root node
	    m_gaNode.removeAll();

	    //remove this node from Variable Manager
	    m_vMan.remove(m_gaNode);

            removeRootNode(m_gaNode);
            
	    //get garbage collection to remove object
	    m_gaNode = null;

	    //need to set all these to null as well.
	    m_populationNode = null;
	    m_rngNode = null;
	    m_crossoverNode = null;
	    m_mutationNode = null;
	    m_selectionNode = null;
	    m_genomeNode = null;

	    PackageLogger.log.fine("Deleted root GANode!");
	}
	//RNGNode
	else if ( m_rngNode != null && vnid == m_rngNode.getVNID() ){
	    m_rngNode.removeAll();
	    m_vMan.remove(m_rngNode);
	    m_rngNode = null;

	    //need to reset these
            if ( m_populationNode != null ) 
	    m_populationNode.setRNGNode(null);
            if ( m_selectionNode != null )
	    m_selectionNode.setRNGNode(null);
            if ( m_crossoverNode != null )
            m_crossoverNode.setRNGNode(null);
            if ( m_mutationNode != null )
	    m_mutationNode.setRNGNode(null);

	    PackageLogger.log.fine("Deleted RNGNode!");
	}
	//PopulationNode
	else if ( m_populationNode != null && vnid == m_populationNode.getVNID() ){
	    m_populationNode.removeAll();
	    m_vMan.remove(m_populationNode);
	    m_populationNode = null;

	    PackageLogger.log.fine("Deleted PopulationNode!");
	}
	//SelectionNode
	else if (m_selectionNode != null && vnid == m_selectionNode.getVNID() ){
	    m_selectionNode.removeAll();
	    m_vMan.remove(m_selectionNode);
	    m_selectionNode = null;

	    if ( m_populationNode != null )
		m_populationNode.setSelectionNode(null);

	    PackageLogger.log.fine("Deleted SelectionNode!");
	}
	//CrossoverNode
	else if ( m_crossoverNode != null && vnid == m_crossoverNode.getVNID() ){
	    m_crossoverNode.removeAll();
	    m_vMan.remove(m_crossoverNode);
	    m_crossoverNode = null;

	    if ( m_populationNode != null )
		m_populationNode.setCrossoverNode(null);

	    PackageLogger.log.fine("Deleted CrossoverNode!");
	}
	//MutationNode
	else if ( m_mutationNode != null && vnid == m_mutationNode.getVNID() ){
	    m_mutationNode.removeAll();
	    m_vMan.remove(m_mutationNode);
	    m_mutationNode = null;

	    if ( m_populationNode != null )
		m_populationNode.setMutationNode(null);

	    PackageLogger.log.fine("Deleted MutationNode!");
	}
	//ReplacementNode
	else if ( m_replacementNode != null && vnid == m_replacementNode.getVNID() ){
	    m_replacementNode.removeAll();
	    m_vMan.remove(m_replacementNode);
	    m_replacementNode = null;

	     if ( m_populationNode != null && m_populationNode instanceof
		  SteadyStatePopNode ){
		 SteadyStatePopNode pop = (SteadyStatePopNode) 
                   m_populationNode;
		 pop.setReplacementNode(null);
	     }

	    PackageLogger.log.fine("Deleted ReplacementNode!");
	}
	//GenomeNode
	else if ( m_genomeNode != null && vnid == m_genomeNode.getVNID() ){
	    m_genomeNode.removeAll();
	    m_vMan.remove(m_genomeNode);
	    m_genomeNode = null;

	    if ( m_populationNode != null )
		m_populationNode.setGenomeNode(null);

	    PackageLogger.log.fine("Deleted GenomeNode!");
	}
	//CalcNode ( + all derived classes )
	else if ( m_calcNode != null && vnid == m_calcNode.getVNID() ){
	    m_calcNode.removeAll();
	    m_vMan.remove(m_calcNode);

            if ( m_genomeNode != null )
	    m_genomeNode.removeNode(m_calcNode);

	    m_calcNode = null;
	    PackageLogger.log.fine("Deleted CalcNode!");
	}

	//could be GeneNode, Evaluation Node, or ValueNode,
	//use vnid get variable and remove from
	else{
          //need to remove all nodes and variables node contains and
          //remove it from Variable Manager
          if ( n != null){
            String s = n.getVNName();
            n.removeAll();
            m_vMan.remove(n);
            
            //remove this GeneNode from GenomeNode
            if ( n instanceof GeneNode ){
              if ( m_genomeNode != null )
              m_genomeNode.removeNode(n);
              PackageLogger.log.fine("Deleted GeneNode!");
            }
            // EvaluationNode
            else if ( n instanceof EvaluationNode ){
              if ( m_calcNode != null )
              m_calcNode.removeNode(n);
              PackageLogger.log.fine("Deleted an EvaluationNode!");
            }
            //any ValueNode
            else if ( n instanceof ValueNode ){
              //get right EvaluationNode and remove this node from it
              if ( s == null ) return; // already deleted

              int end = s.lastIndexOf((int)'.');
              
              EvaluationNode en = null;
              try{
                en = (EvaluationNode) m_vMan.get(s.substring(0,end));
              }
              catch(ClassCastException cce){
                PackageLogger.log.warning(cce.getMessage());
                return;
              }
              if ( en != null )
              en.removeNode(n);
              PackageLogger.log.fine("Deleted a ValueNode!");
            }
            else
            PackageLogger.log.warning("DeleteNode message received for"
                                      + " unknown node with vnid: "
                                      + vnid);
          }
	}
    }
}
