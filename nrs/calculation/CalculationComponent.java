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
package nrs.calculation;

import java.util.Collection;
import java.util.Iterator;
import nrs.core.base.BaseComponent;
import nrs.core.base.Message;
import nrs.core.base.FieldNotFoundException;
import nrs.core.base.MessageTools;
import nrs.core.base.Node;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.message.*;
import static nrs.core.message.Constants.Fields.*;

/**
 * Root Component class for calculation component, which handles all
 * message handling.
 *
 * @author Thomas French
 * @author Sarah Cope
 */
public class CalculationComponent extends BaseComponent
{
    /** Name of component. */
    private final static String m_Name = "NRS.calculation";
    /** Component type. */
    private final static String m_CType = "calculation";
    /** Component version. */
    private final static String m_Version = "1.0";
    /** Whether this component has a CSL descriptor file. */
    private final static boolean m_HasCSL = true;
    /** Can the component speak BMF. */
    private final static boolean m_SpeaksBMF = false;
    /** Can the component speak PML. */
    private final static boolean m_SpeaksPML = true;

    public static final int ADD = 0, SUBTRACT = 1, MULTIPLY = 2, DIVIDE = 3, AND = 4, OR = 5, XOR = 6, 
        LESSTHAN = 7, EQUALTO = 8, INT = 9, STRING = 10, FLOAT = 11, BOOL = 12, VOID = 13, 
        TIME = 14, CAPACITANCE = 15, VOLTAGE = 16, CURRENT = 17, FREQUENCY = 18, CONDUCTANCE = 19, 
        FILENAME = 20;

    /** Root node for this component. */
    private CalculationNode m_calcNode;


    //----------------------------------------------------------------------
    /**Constructor
     *
     * @param vMan reference to {@link VariableManager} object.
     */
    public CalculationComponent(VariableManager vMan)
    {
	super(vMan, m_Name, m_CType, m_SpeaksBMF, m_SpeaksPML, 
	      m_HasCSL, m_Version);
	m_vMan = vMan;
    }
   
    public void handleMessageAt_CreateNode(Message m, Variable v, boolean diff) {

	// Extract message fields
	int vnid;
	String vnName, vnType;

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

	PackageLogger.log.fine("Received a CreateNode message, for type: "
			       + vnType + " with name: " + vnName);

	//receive message to create root node - CalculationNode
	if ( vnType.equals("CalculationNode")){

	    m_calcNode = new CalculationNode(m_vMan, vnName);

	    PackageLogger.log.fine("Created a CalculationNode with name "
				   + vnName);

	//receive message to FloatAddNode
        } else if ( vnType.equals("FloatAddNode")){

            FloatNode floatAddNode = new FloatNode(m_vMan, vnName, ADD, "FloatAddNode");
            
            m_calcNode.addNode(floatAddNode);

            PackageLogger.log.fine("Created an FloatAddNode with name "
				   + vnName);

            //receive message to IntAddNode
        }  else if ( vnType.equals("IntAddNode")){

	    IntNode intAddNode = new IntNode(m_vMan, vnName, ADD, "IntAddNode");

            m_calcNode.addNode(intAddNode);

	    PackageLogger.log.fine("Created an IntAddNode with name "
				   + vnName);

            	//receive message to FloatSubtractNode
        } else if ( vnType.equals("FloatSubtractNode")){

	    FloatNode floatSubNode = new FloatNode(m_vMan, vnName, SUBTRACT, "FloatSubtractNode");

            m_calcNode.addNode(floatSubNode);

	    PackageLogger.log.fine("Created a FloatSubtractNode with name "
				   + vnName);

            //receive message to IntSubtractNode
        }  else if ( vnType.equals("IntSubtractNode")){

	    IntNode intSubNode = new IntNode(m_vMan, vnName, SUBTRACT, "IntSubtractNode");

            m_calcNode.addNode(intSubNode);

	    PackageLogger.log.fine("Created an IntSubtractNode with name "
				   + vnName);


            //receive message to FloatMulitplyNode
        } else if ( vnType.equals("FloatMultiplyNode")){

	    FloatNode floatMultNode = new FloatNode(m_vMan, vnName, MULTIPLY, "FloatMultiplyNode");

            m_calcNode.addNode(floatMultNode);

	    PackageLogger.log.fine("Created a FloatMultiplyNode with name "
				   + vnName);

            //receive message to IntMultiplyNode
        } else if ( vnType.equals("IntMultiplyNode")){

	    IntNode intMultNode = new IntNode(m_vMan, vnName, MULTIPLY, "IntMultiplyNode");

            m_calcNode.addNode(intMultNode);

	    PackageLogger.log.fine("Created an IntMultiplyNode with name "
				   + vnName);

                //receive message to FloatDivideNode
        } else if ( vnType.equals("FloatDivideNode")){

	    FloatNode floatDivNode = new FloatNode(m_vMan, vnName, DIVIDE, "FloatDivideNode");

            m_calcNode.addNode(floatDivNode);

	    PackageLogger.log.fine("Created a FloatDivideNode with name "
				   + vnName);

            //receive message to IntDivideNode
        } else if ( vnType.equals("IntDivideNode")){

	    IntNode intDivNode = new IntNode(m_vMan, vnName, DIVIDE, "IntDivideNode");

            m_calcNode.addNode(intDivNode);

	    PackageLogger.log.fine("Created an IntDivideNode with name "
				   + vnName);

            // receive message to ANDBooleanNode
        } else if ( vnType.equals("ANDBooleanNode")){

	    BooleanNode boolANDNode = new BooleanNode(m_vMan, vnName, AND, "ANDBooleanNode");

            m_calcNode.addNode(boolANDNode);

	    PackageLogger.log.fine("Created an ANDBooleanNode with name "
				   + vnName);

            // receive message to ORBooleanbode
        } else if ( vnType.equals("ORBooleanNode")){

	    BooleanNode boolORNode = new BooleanNode(m_vMan, vnName, OR, "ORBooleanNode");

            m_calcNode.addNode(boolORNode);

	    PackageLogger.log.fine("Created an ORBooleanNode with name "
				   + vnName);

        } else if ( vnType.equals("XORBooleanNode")){

	    BooleanNode boolXORNode = new BooleanNode(m_vMan, vnName, XOR, "XORBooleanNode");

            m_calcNode.addNode(boolXORNode);
            
	    PackageLogger.log.fine("Created an XORBooleanNode with name "
				   + vnName);

        } else if ( vnType.equals("NOTNode")){

	    NOTNode notNode = new NOTNode(m_vMan, vnName, "NOTNode");

            m_calcNode.addNode(notNode);

	    PackageLogger.log.fine("Created a NOTBooleanNode with name "
				   + vnName);
            
        } else if ( vnType.equals("DeltaNode")){
            
	    DeltaNode deltaNode = new DeltaNode(m_vMan, vnName, "DeltaNode");
            
            m_calcNode.addNode(deltaNode);
            
	    PackageLogger.log.fine("Created a DeltaNode with name "
				   + vnName);
        } else if ( vnType.equals("IntForLoopNode")){
                 
            ForLoopNode forLoop = new ForLoopNode(m_vMan, vnName, INT, "IntForLoopNode");
            
            m_calcNode.addNode(forLoop);

	    PackageLogger.log.fine("Created a IntForLoopNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatForLoopNode")){
            
            ForLoopNode forLoop = new ForLoopNode(m_vMan, vnName, FLOAT, "FloatForLoopNode");
            
            m_calcNode.addNode(forLoop);
            
	    PackageLogger.log.fine("Created a FloatForLoopNode with name "
				   + vnName);

        } else if ( vnType.equals("LessThanIntCondNode")){
            
	    IntCondNode lessThan = new IntCondNode(m_vMan, vnName, LESSTHAN,"LessThanIntCondNode");
            
            m_calcNode.addNode(lessThan);

	    PackageLogger.log.fine("Created a LessThanIntCondNode with name "
				   + vnName);

        } else if ( vnType.equals("EqualsIntCondNode")){
            
	    IntCondNode equalTo = new IntCondNode(m_vMan, vnName, EQUALTO, "EqualsIntCondNode");
            
            m_calcNode.addNode(equalTo);
            
	    PackageLogger.log.fine("Created a EqualsIntCondNode with name "
				   + vnName);

        } else if ( vnType.equals("LessThanFloatCondNode")){
            
	    FloatCondNode lessThan = new FloatCondNode(m_vMan, vnName, LESSTHAN, "LessThanFloatCondNode");
            
            m_calcNode.addNode(lessThan);

	    PackageLogger.log.fine("Created a LessThanFloatCondNode with name "
				   + vnName);

        } else if ( vnType.equals("EqualsFloatCondNode")){
            
	    FloatCondNode equalTo = new FloatCondNode(m_vMan, vnName, EQUALTO, "EqualsFloatCondNode");
            
            m_calcNode.addNode(equalTo);
            
	    PackageLogger.log.fine("Created a EqualsFloatCondNode with name "
				   + vnName);

        } else if ( vnType.equals("VoidLatchNode")){
            
	    LatchNode voidLatch = new LatchNode(m_vMan, vnName, VOID, "VoidLatchNode");
            
            m_calcNode.addNode(voidLatch);

	    PackageLogger.log.fine("Created a VoidLatchNode with name "
				   + vnName);

        } else if ( vnType.equals("IntLatchNode")){
            
	    LatchNode intLatch = new LatchNode(m_vMan, vnName, INT, "IntLatchNode");
            
            m_calcNode.addNode(intLatch);
            
	    PackageLogger.log.fine("Created a IntLatchNode with name "
				   + vnName);


        } else if ( vnType.equals("FloatLatchNode")){
            
	    LatchNode floatLatch = new LatchNode(m_vMan, vnName, FLOAT, "FloatLatchNode");
            
            m_calcNode.addNode(floatLatch);

	    PackageLogger.log.fine("Created a FloatLatchNode with name "
				   + vnName);

        } else if ( vnType.equals("BooleanLatchNode")){
            
	    LatchNode booleanLatch = new LatchNode(m_vMan, vnName, BOOL, "BooleanLatchNode");
            
            m_calcNode.addNode(booleanLatch);

	    PackageLogger.log.fine("Created a BooleanLatchNode with name "
				   + vnName);

        } else if ( vnType.equals("VoidDelayNode")){
                
	    DelayNode voidDelay = new DelayNode(m_vMan, vnName, VOID, "VoidDelayNode");
            
            m_calcNode.addNode(voidDelay);

	    PackageLogger.log.fine("Created a VoidDelayNode with name "
				   + vnName);

        } else if ( vnType.equals("IntDelayNode")){
            
	    DelayNode intDelay = new DelayNode(m_vMan, vnName, INT, "IntDelayNode");
            
            m_calcNode.addNode(intDelay);
            
	    PackageLogger.log.fine("Created a IntDelayNode with name "
				   + vnName);


        } else if ( vnType.equals("FloatDelayNode")){
            
	    DelayNode floatDelay = new DelayNode(m_vMan, vnName, FLOAT, "FloatDelayNode");
            
            m_calcNode.addNode(floatDelay);

	    PackageLogger.log.fine("Created a FloatDelayNode with name "
				   + vnName);

        } else if ( vnType.equals("BooleanDelayNode")){
            
	    DelayNode booleanDelay = new DelayNode(m_vMan, vnName, BOOL, "BooleanDelayNode");
            
            m_calcNode.addNode(booleanDelay);

	    PackageLogger.log.fine("Created a BooleanDelayNode with name "
				   + vnName);


        } else if ( vnType.equals("FloatToIntConversionNode")){
            
	    FloatConversionNode int_convert = new FloatConversionNode(m_vMan, vnName, INT, "FloatToIntConversionNode");
            
            m_calcNode.addNode(int_convert);
            
	    PackageLogger.log.fine("Created a FloatToIntConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatToStringConversionNode")){
            
	    FloatConversionNode string_convert = new FloatConversionNode(m_vMan, vnName, STRING, "FloatToStringConversionNode");
            
            m_calcNode.addNode(string_convert);
            
	    PackageLogger.log.fine("Created a FloatToStringConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatToBooleanConversionNode")){
            
	    FloatConversionNode boolean_convert = new FloatConversionNode(m_vMan, vnName, BOOL, "FloatToBooleanConversionNode");
            
            m_calcNode.addNode(boolean_convert);
            
	    PackageLogger.log.fine("Created a FloatToBooleanConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatToTimeConversionNode")){
            
	    FloatConversionNode time_convert = new FloatConversionNode(m_vMan, vnName, TIME, "FloatToTimeConversionNode");
            
            m_calcNode.addNode(time_convert);
            
	    PackageLogger.log.fine("Created a FloatToTimeConversionNode with name "
				   + vnName);
            
        } else if ( vnType.equals("FloatToConductConversionNode")){
            
	    FloatConversionNode conduct_convert = new FloatConversionNode(m_vMan, vnName, CONDUCTANCE, "FloatToConductConversionNode");
            
            m_calcNode.addNode(conduct_convert);
            
	    PackageLogger.log.fine("Created a FloatToConductConversionNode with name "
				   + vnName);


        } else if ( vnType.equals("FloatToCapacitConversionNode")){
            
	    FloatConversionNode capacit_convert = new FloatConversionNode(m_vMan, vnName, CAPACITANCE, "FloatToCapacitConversionNode");
            
            m_calcNode.addNode(capacit_convert);
            
	    PackageLogger.log.fine("Created a FloatToCapacitConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatToVoltageConversionNode")){
            
	    FloatConversionNode voltage_convert = new FloatConversionNode(m_vMan, vnName, VOLTAGE, "FloatToVoltageConversionNode");
            
            m_calcNode.addNode(voltage_convert);
            
	    PackageLogger.log.fine("Created a FloatToVoltageConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatToCurrentConversionNode")){
            
	    FloatConversionNode current_convert = new FloatConversionNode(m_vMan, vnName, CURRENT, "FloatToCurrentConversionNode");
            
            m_calcNode.addNode(current_convert);
            
	    PackageLogger.log.fine("Created a FloatToCurrentConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("FloatToFreqConversionNode")){
            
	    FloatConversionNode freq_convert = new FloatConversionNode(m_vMan, vnName, FREQUENCY, "FloatToFreqConversionNode");
            
            m_calcNode.addNode(freq_convert);
            
	    PackageLogger.log.fine("Created a FloatToFreqConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("StringConcatNode")){
            
	    StringConcatNode string_concat = new StringConcatNode(m_vMan, vnName, "StringConcatNode");
            
            m_calcNode.addNode(string_concat);
            
	    PackageLogger.log.fine("Created a StringConcatNode with name "
				   + vnName);

        } else if ( vnType.equals("IntToStringConversionNode")){
            
	    IntConversionNode string_convert2 = new IntConversionNode(m_vMan, vnName, STRING, "IntToStringConversionNode");
            
            m_calcNode.addNode(string_convert2);
            
	    PackageLogger.log.fine("Created a IntToStringConversionNode with name "
				   + vnName);
            
        } else if ( vnType.equals("IntToFloatConversionNode")){
            
	    IntConversionNode float_convert = new IntConversionNode(m_vMan, vnName, FLOAT, "IntToFloatConversionNode");
            
            m_calcNode.addNode(float_convert);
            
	    PackageLogger.log.fine("Created a IntToFloatConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("IntToBooleanConversionNode")){
            
	    IntConversionNode boolean_convert = new IntConversionNode(m_vMan, vnName, BOOL, "IntToBooleanConversionNode");
            
            m_calcNode.addNode(boolean_convert);
            
	    PackageLogger.log.fine("Created a IntToBooleanConversionNode with name "
				   + vnName);


        } else if ( vnType.equals("VoidToBooleanNode")){
            
	    VoidToBooleanNode void_convert = new VoidToBooleanNode(m_vMan, vnName, "VoidToBooleanNode");
            
            m_calcNode.addNode(void_convert);
            
	    PackageLogger.log.fine("Created a VoidToBooleanNode with name "
				   + vnName);

        } else if ( vnType.equals("VoidToBooleanDelayNode")){
            
	    VoidToBooleanDelayNode void_convert2 = new VoidToBooleanDelayNode(m_vMan, vnName, "VoidToBooleanDelayNode");
            
            m_calcNode.addNode(void_convert2);
            
	    PackageLogger.log.fine("Created a VoidToBooleanDelayNode with name "
				   + vnName);


        } else if ( vnType.equals("BooleanToVoidConversionNode")){
            
	    BooleanConversionNode bool_convert = new BooleanConversionNode(m_vMan, vnName, VOID, "BooleanToVoidConversionNode");
            
            m_calcNode.addNode(bool_convert);
            
	    PackageLogger.log.fine("Created a BooleanToVoidConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("BooleanToIntConversionNode")){
                 
	    BooleanConversionNode int_convert = new BooleanConversionNode(m_vMan, vnName, INT, "BooleanToIntConversionNode");
            
            m_calcNode.addNode(int_convert);
            
	    PackageLogger.log.fine("Created a BooleanToIntConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("BooleanToFloatConversionNode")){
                 
	    BooleanConversionNode float_convert = new BooleanConversionNode(m_vMan, vnName, FLOAT, "BooleanToFloatConversionNode");
            
            m_calcNode.addNode(float_convert);
            
	    PackageLogger.log.fine("Created a BooleanToFloatConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("BooleanToStringConversionNode")){
                 
	    BooleanConversionNode string_convert = new BooleanConversionNode(m_vMan, vnName, STRING, "BooleanToStringConversionNode");
            
            m_calcNode.addNode(string_convert);
            
	    PackageLogger.log.fine("Created a BooleanToStringConversionNode with name "
				   + vnName);

        } else if ( vnType.equals("StringToFilenameConversionNode")){
            
	    StringConversionNode filename_convert = new StringConversionNode(m_vMan, vnName, FILENAME, "StringToFilenameConversionNode");
            
            m_calcNode.addNode(filename_convert);
            
	    PackageLogger.log.fine("Created a StringToFilenameConversionNode with name "
				   + vnName);


	} else { 
	    PackageLogger.log.warning("CreateNode message received for unknown node type: " + vnType);
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

	if ( m_calcNode == null ){
	    PackageLogger.log.warning("DeleteNode message received for unknown"
				      + " node with vnid " + vnid);
	    return;
	}

	//remove root node from m_vMan
	if ( vnid == m_calcNode.getVNID() ){
	    //remove all nodes, variables and links contained by root node
	    m_calcNode.removeAll();

	    //remove this node from Variable Manager
	    m_vMan.remove(m_calcNode);

	    //get garbage collection to remove object
	    m_calcNode = null;

	    PackageLogger.log.fine("Deleted root CalculationNode!");
	} else {
            Node n;
	    //delete node
	    if ( vnid > 0 ){
		n = (Node) m_vMan.get(vnid);
                n.removeAll();
                m_vMan.remove(n);
	    }else{
		PackageLogger.log.warning("Error deleting Node - no VNID"
					  +" specified!");
		n = null;
	    }

        }
	
         
    }
}
