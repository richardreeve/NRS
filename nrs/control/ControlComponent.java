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
package nrs.control;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import nrs.core.base.BaseComponent;
import nrs.core.base.Link;
import nrs.core.base.Message;
import nrs.core.base.FieldNotFoundException;
import nrs.core.base.MessageTools;
import nrs.core.base.Node;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.RouteManager;
import nrs.core.message.*;

import static nrs.core.message.Constants.Fields.*;

/**
 * Root Component class for control component, which handles all message handling.
 *
 * @author Thomas French
 */
public class ControlComponent extends BaseComponent
{
  /** Name of component. */
  private final static String m_Name = "NRS.control";
  /** Component type. */
  private final static String m_CType = "control";
  /** Component version. */
  private final static String m_Version = "1.0";
  /** Whether this component has a CSL descriptor file. */
  private final static boolean m_HasCSL = true;
  /** Can the component speak BMF. */
  private final static boolean m_SpeaksBMF = false;
  /** Can the component speak PML. */
  private final static boolean m_SpeaksPML = true;
  
  /** Root node for this component. */
  private ControlNode m_cnNode;
  
  /** ButtonNode manager. */
  private ButtonNodeManager m_buttonMan;
  
  /** LEDNode manager. */
  private LEDNodeManager m_ledMan;
  
  /** CheckBoxNode manager. */
  private CheckBoxNodeManager m_checkMan;
  
  /** FloatButtonNode manager. */
  private FloatButtonNodeManager m_doubleMan;
  
  /** FloatDisplayNode manager. */
  private FloatDisplayNodeManager m_dDisplayMan;

    /** TimeButtonNode manager. */
    private TimeButtonNodeManager m_timeMan;
    
    /** StringDisplayNode manager. */
  private StringDisplayNodeManager m_sDisplayMan;

     /** StringButtonNode manager. */
  private StringButtonNodeManager m_stringMan;
  
  /** IntegerButtonNode manager. */
  private IntegerButtonNodeManager m_intMan;
  
  /** IntegerDisplayNode manager. */
  private IntegerDisplayNodeManager m_iDisplayMan;

    //----------------------------------------------------------------------
    /**Constructor
     *
     * @param vMan reference to {@link VariableManager} object.
     */
    public ControlComponent(VariableManager vMan)
    {
      super(vMan, m_Name, m_CType, m_SpeaksBMF, m_SpeaksPML, m_HasCSL, m_Version);
      m_vMan = vMan;

      m_buttonMan = ButtonNodeManager.getInstance();
      m_ledMan = LEDNodeManager.getInstance();
      m_checkMan = CheckBoxNodeManager.getInstance();
      m_doubleMan = FloatButtonNodeManager.getInstance();
      m_dDisplayMan = FloatDisplayNodeManager.getInstance();
      m_sDisplayMan = StringDisplayNodeManager.getInstance();
      m_intMan = IntegerButtonNodeManager.getInstance();
      m_stringMan = StringButtonNodeManager.getInstance();
      m_iDisplayMan = IntegerDisplayNodeManager.getInstance();
      m_timeMan = TimeButtonNodeManager.getInstance();
    }

    /**
     * Process the receipt of a message at the CreateNode variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */
    public void handleMessageAt_CreateNode(Message m, Variable v, 
                                           boolean diff) {

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

	//receive message to create root node - ControlNode
	if ( vnType.equals("ControlNode")){

	    m_cnNode = new ControlNode(m_vMan, vnName);

            addRootNode(m_cnNode);

	    PackageLogger.log.fine("Created a ControlNode with name "
				   + vnName);

	}

	//create Button Node
	else if ( vnType.equals("ButtonNode") ){

	    //if not in ButtonNodeManager
	    if ( m_buttonMan.getVNID(vnName) == null ){

		ButtonNode buttonNode =
		    new ButtonNode(AppManager.getInstance().
				   getMainFrame(), m_vMan, vnName);

		//add button to ButtonNodeManager
		m_buttonMan.add(buttonNode);

		//add to ControlNode - root node
		m_cnNode.addNode(buttonNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

	//create Button Node
	else if ( vnType.equals("LEDNode") ){

	    //if not in LEDNodeManager
	    if ( m_ledMan.getVNID(vnName) == null ){

		LEDNode ledNode =
		    new LEDNode(AppManager.getInstance()
				.getMainFrame(), m_vMan, vnName);

		//add label to LEDNodeManager
		m_ledMan.add(ledNode);

		//add to ControlNode - root node
		m_cnNode.addNode(ledNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

	//create CheckBox Node
	else if ( vnType.equals("CheckBoxNode") ){

	    //if not in CheckBoxNodeManager
	    if ( m_checkMan.getVNID(vnName) == null ){

		CheckBoxNode checkNode =
		    new CheckBoxNode(AppManager.getInstance()
				     .getMainFrame(), m_vMan, vnName);

		//add button to CheckBoxNodeManager
		m_checkMan.add(checkNode);

		//add to ControlNode - root node
		m_cnNode.addNode(checkNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}
	//create FloatButton Node
	else if ( vnType.equals("FloatButtonNode") ){

	    //if not in FloatButtonNodeManager
	    if ( m_doubleMan.getVNID(vnName) == null ){

		FloatButtonNode doubleNode =
		    new FloatButtonNode(AppManager.getInstance()
					 .getMainFrame(), m_vMan, vnName);

		//add double to FloatButtonNodeManager
		m_doubleMan.add(doubleNode);

		//add to ControlNode - root node
		m_cnNode.addNode(doubleNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

	//create TimeButton Node
	else if ( vnType.equals("TimeButtonNode") ){

	    //if not in TimeButtonNodeManager
	    if ( m_timeMan.getVNID(vnName) == null ){

		TimeButtonNode timeNode =
		    new TimeButtonNode(AppManager.getInstance()
					 .getMainFrame(), m_vMan, vnName);

		//add double to TimeButtonNodeManager
		m_timeMan.add(timeNode);

		//add to ControlNode - root node
		m_cnNode.addNode(timeNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

        //create StringButton Node
	else if ( vnType.equals("StringButtonNode") ){

	    //if not in StringButtonNodeManager
	    if ( m_stringMan.getVNID(vnName) == null ){

		StringButtonNode stringNode =
		    new StringButtonNode(AppManager.getInstance()
					 .getMainFrame(), m_vMan, vnName);

		//add string to StringButtonNodeManager
		m_stringMan.add(stringNode);

		//add to ControlNode - root node
		m_cnNode.addNode(stringNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

	//create FloatDisplay Node
	else if ( vnType.equals("FloatDisplayNode") ){

	    //if not in FloatDisplayNodeManager
	    if ( m_dDisplayMan.getVNID(vnName) == null ){

		FloatDisplayNode doubleNode =
		    new FloatDisplayNode(AppManager.getInstance()
					  .getMainFrame(), m_vMan, vnName);

		//add double to FloatDisplayNodeManager
		m_dDisplayMan.add(doubleNode);

		//add to ControlNode - root node
		m_cnNode.addNode(doubleNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }


	}

        //create StringDisplay Node
	else if ( vnType.equals("StringDisplayNode") ){

	    //if not in StringDisplayNodeManager
	    if ( m_sDisplayMan.getVNID(vnName) == null ){

		StringDisplayNode stringNode =
		    new StringDisplayNode(AppManager.getInstance()
					  .getMainFrame(), m_vMan, vnName);

		//add string to StringDisplayNodeManager
		m_sDisplayMan.add(stringNode);

		//add to ControlNode - root node
		m_cnNode.addNode(stringNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }


	}

        //create IntegerButton Node
	else if ( vnType.equals("IntegerButtonNode") ){

	    //if not in IntegerButtonNodeManager
	    if ( m_doubleMan.getVNID(vnName) == null ){

		IntegerButtonNode intNode =
		    new IntegerButtonNode(AppManager.getInstance()
					 .getMainFrame(), m_vMan, vnName);

		//add double to IntegerButtonNodeManager
		m_intMan.add(intNode);

		//add to ControlNode - root node
		m_cnNode.addNode(intNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

	//create IntegerDisplay Node
	else if ( vnType.equals("IntegerDisplayNode") ){

	    //if not in IntegerDisplayNodeManager
	    if ( m_iDisplayMan.getVNID(vnName) == null ){

		IntegerDisplayNode intDNode =
		    new IntegerDisplayNode(AppManager.getInstance()
					  .getMainFrame(), m_vMan, vnName);

		//add double to IntegerDisplayNodeManager
		m_iDisplayMan.add(intDNode);

		//add to ControlNode - root node
		m_cnNode.addNode(intDNode);
	    }else{
		PackageLogger.log.fine("Got CreateNode message for node that already exists!");
	    }

	}

	else{
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

	PackageLogger.log.fine("Received a DeleteNode message for vnID: "
			       + vnid);

	//remove root node from m_vMan
	if ( vnid == m_cnNode.getVNID() ){

	    m_cnNode.removeAll();

	    m_buttonMan.removeAll();
	    m_ledMan.removeAll();
	    m_checkMan.removeAll();
	    m_doubleMan.removeAll();
	    m_dDisplayMan.removeAll();
            m_intMan.removeAll();
            m_sDisplayMan.removeAll();
            m_stringMan.removeAll();
            m_iDisplayMan.removeAll();
	    m_timeMan.removeAll();

	    m_vMan.remove(m_cnNode);  //remove this node from Variable Manager

            removeRootNode(m_cnNode);

	    m_cnNode = null;          //get garbage collection to remove object

	    AppManager.getInstance().getMainFrame().setTitle("");

	    PackageLogger.log.fine("Deleted root Control Node!");
	}
	else{
	    Node n;
	    //delete node
	    if ( vnid > 0 ){
		n = (Node) m_vMan.get(vnid);
	    }else{
		PackageLogger.log.warning("Error deleting Node - no VNID"
					  +" specified!");
		n = null;
	    }

	    //need to remove all nodes and variables node contains
	    //and remove it from Variable Manager
	    if ( n != null){
		n.removeAll();

		if ( n instanceof ButtonNode )
		    m_buttonMan.remove(new Integer(vnid));
		else if ( n instanceof LEDNode )
		    m_ledMan.remove(new Integer(vnid));
		else if ( n instanceof CheckBoxNode )
		    m_checkMan.remove(new Integer(vnid));
		else if ( n instanceof FloatButtonNode )
		    m_doubleMan.remove(new Integer(vnid));
		else if ( n instanceof FloatDisplayNode )
		    m_dDisplayMan.remove(new Integer(vnid));
                else if ( n instanceof StringDisplayNode )
		    m_sDisplayMan.remove(new Integer(vnid));
                else if ( n instanceof StringButtonNode )
		    m_stringMan.remove(new Integer(vnid));
		else if ( n instanceof IntegerButtonNode )
		    m_intMan.remove(new Integer(vnid));
		else if ( n instanceof IntegerDisplayNode )
		    m_iDisplayMan.remove(new Integer(vnid));
		else if ( n instanceof TimeButtonNode )
		    m_timeMan.remove(new Integer(vnid));

		// remove from root node
		m_cnNode.removeNode(n);

		PackageLogger.log.fine("Deleted Node!");
	    }
	    else{
		PackageLogger.log.warning("DeleteNode message received for"
					  +" unknown node with vnID: " + vnid);
	    }
	}
    }
  
  //----------------------------------------------------------------------
}
