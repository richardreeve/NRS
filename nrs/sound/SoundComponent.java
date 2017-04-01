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
package nrs.sound;

import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
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
 * Root Component class for sound component, which handles all
 * message handling.
 *
 * @author Thomas French
 * @author Sarah Cope
 */
public class SoundComponent extends BaseComponent
{
    /** Name of component. */
    private final static String m_Name = "NRS.sound";
    /** Component type. */
    private final static String m_CType = "sound";
    /** Component version. */
    private final static String m_Version = "1.0";
    /** Whether this component has a CSL descriptor file. */
    private final static boolean m_HasCSL = true;
    /** Can the component speak BMF. */
    private final static boolean m_SpeaksBMF = false;
    /** Can the component speak PML. */
    private final static boolean m_SpeaksPML = true;

    /** Root node for this component. */
    private SoundNode m_soundNode;

    // This allows the tone nodes to contain
    // a reference to their parent speaker
    private HashMap speakerNodes;

    //----------------------------------------------------------------------
    /**Constructor
     *
     * @param vMan reference to {@link VariableManager} object.
     */
    public SoundComponent(VariableManager vMan)
    {
	super(vMan, m_Name, m_CType, m_SpeaksBMF, m_SpeaksPML, 
	      m_HasCSL, m_Version);
	m_vMan = vMan;
        speakerNodes = new HashMap();
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

	//receive message to create root node - SoundNode
	if ( vnType.equals("SoundNode")){

	    m_soundNode = new SoundNode(m_vMan, vnName);

	    PackageLogger.log.fine("Created a SoundNode with name "
				   + vnName);

        } else if ( vnType.equals("SpeakerNode")){
            
	    SpeakerNode m_speakerNode = new SpeakerNode(m_vMan, vnName);

            speakerNodes.put(vnName, m_speakerNode);
            m_soundNode.addSpeaker(m_speakerNode);
            
	    PackageLogger.log.fine("Created a SpeakerNode with name "
				   + vnName);

        } else if ( vnType.equals("ContinuousSpeaker")){
            
	    ContinuousSpeaker m_contSpeaker = new ContinuousSpeaker(m_vMan, vnName);
            
            speakerNodes.put(vnName, m_contSpeaker);
            m_soundNode.addSpeaker(m_contSpeaker);
            
	    PackageLogger.log.fine("Created a ContinuousSpeaker with name "
				   + vnName);

        } else if ( vnType.equals("ToneNode") || vnType.equals("ContToneNode")){

            // Takes name of parent node (46 = char '.')
            String parentName = vnName.substring(0, vnName.lastIndexOf(46));
            
            Speaker parent = (Speaker)speakerNodes.get(parentName);

	    ToneNode m_toneNode = new ToneNode(m_vMan, vnName, parent);
            parent.addTone(m_toneNode);
            
	    PackageLogger.log.fine("Created a ToneNode with name "
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

	if ( m_soundNode == null ){
	    PackageLogger.log.warning("DeleteNode message received for unknown"
				      + " node with vnid " + vnid);
	    return;
	}

	//remove root node from m_vMan
	if ( vnid == m_soundNode.getVNID() ){
	    //remove all nodes, variables and links contained by root node
	    m_soundNode.removeAll();

	    //remove this node from Variable Manager
	    m_vMan.remove(m_soundNode);

	    //get garbage collection to remove object
	    m_soundNode = null;

            speakerNodes.clear();

	    PackageLogger.log.fine("Deleted root SoundNode!");
	} else {
            Node n;
          
	    //delete node
	    if ( vnid > 0 ){
		n = (Node) m_vMan.get(vnid);
                if (n instanceof Speaker) {
                    speakerNodes.remove(n.getVNName());
                    m_soundNode.removeSpeaker((Speaker)n);
                }
                if (n instanceof ToneNode) {
                    ToneNode t = (ToneNode) n;
                    t.parent.removeTone(t);
                }
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
