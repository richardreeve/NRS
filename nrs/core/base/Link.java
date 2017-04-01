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
package nrs.core.base;

import nrs.core.base.Message;
import nrs.core.base.Variable;
import nrs.core.message.DeleteLink;
import nrs.core.message.Constants;

/** Class for representing Links between NRS variables.
 *
 * @author Thomas French
 */

public class Link {

    private String m_sourceCID;
    private Integer m_sourceVNID;

    private String m_targetCID;
    private Integer m_targetVNID;
    private String m_targetVNName;

    //private String m_logPort;
    private boolean m_temporary;
    private String m_route;

    private Variable m_sourceRef = null;
    private Variable m_targetRef = null;

    private RouteManager m_routeMan;

    private boolean m_isSource = false;

    /**
     * Constructor for capturing all the CreateLink message information.
     * Using VNIDs for source and target.
     *
     * @param sourceCID
     * @param sourceVNID
     * @param targetCID
     * @param targetVNID
     * @param temporary
     * @param route
     * @param rMan 
     * @param myCID
     *
    */
    public Link(String sourceCID, Integer sourceVNID, String targetCID,
		Integer targetVNID, boolean temporary, String route, 
		RouteManager rMan, String myCID){

	m_sourceCID = sourceCID;
	m_sourceVNID = sourceVNID;
	m_targetCID = targetCID;
	m_targetVNID = targetVNID;
	//m_logPort = logPort;
	m_temporary = temporary;
	m_route = route;
	m_routeMan = rMan;

	if ( sourceCID.equals(myCID) )
	    m_isSource = true; 
    }

    /**
     * Constructor for capturing all the CreateLink message information.
     * Using VNID for source and VNName for target.
     *
     * @param sourceCID
     * @param sourceVNID
     * @param targetCID
     * @param targetVNName
     * @param temporary
     * @param route
     * @param rMan 
     *
     * @deprecated outdated, do NOT use.
    */
    public Link(String sourceCID, Integer sourceVNID, String targetCID,
		String targetVNName, boolean temporary, String route, 
		RouteManager rMan){
	m_sourceCID = sourceCID;
	m_sourceVNID = sourceVNID;
	m_targetCID = targetCID;
	m_targetVNName = targetVNName;
	//m_logPort = logPort;
	m_temporary = temporary;
	m_route = route;
	m_routeMan = rMan;

	//must be 0 if not supplied
	m_targetVNID = new Integer(0);
    }

    /** Constructor, case where link is onboard component. */
    public Link(Variable s, Variable t){
	m_sourceRef = s;
	m_targetRef = t;
    }

    //Accessor methods
    // getters

    public String getSourceCID(){
	return m_sourceCID;
    }
    public String getTargetCID(){
	return m_targetCID;
    }

    public Integer getSourceVNID(){
	if ( m_sourceVNID != null ) return m_sourceVNID;
	return null;
    }

    public Integer getTargetVNID(){
	if ( m_targetVNID != null ) return m_targetVNID;
	return null;
    }

    public boolean isSource(int vnid){
	return ( m_isSource || 
		 ( m_sourceRef != null && m_sourceRef.getVNID() == vnid ) );
    }

    /** 
     * @deprecated 
     */
    public String getTargetVNName(){
	if ( m_targetVNName != null ) return m_targetVNName;
	return null;
    }

    public boolean getTemporary(){
	return m_temporary;
    }

    /** For on-board links. 
     * @return Reference to source of link.
     */
    public Variable getSourceRef(){
	return m_sourceRef;
    }

    /** For on-board links. 
     * @return Reference to target of link.
     */
    public Variable getTargetRef(){
	return m_targetRef;
    }

    //-----------------------------------------------------------------

    /** Fill information in to Message object from Link information.
     *
     * @param msg {@link Message} object to add information to.
    */
    public void fillMsg(Message msg) {

	// Add route
	m_route = m_routeMan.resolveRoute(m_targetCID, true);
	msg.setNRSField(Constants.MessageFields.route, m_route);
	
	// Add target
	if (m_targetVNID != null)
	    {
		msg.setNRSField(Constants.MessageFields.toVNID,
				m_targetVNID.toString());
	    }
	
	// Fix route and target
	MessageTools.fixRoute(msg, m_targetCID);
	MessageTools.fixToVNID(msg, m_targetVNName);
    }

    /** Fill in information for a DeleteLink message from Link information.
     *
     * @param msg Message to fill in.
     * @param source is caller source of link
     */
    public void fillDeleteLinkMsg(DeleteLink msg, boolean source){
	//fill in basic information required
	if ( source ) 
	    m_route = m_routeMan.resolveRoute(m_targetCID, true);
	else
	    m_route = m_routeMan.resolveRoute(m_sourceCID, true);

	msg.setNRSField(Constants.MessageFields.route, m_route);
	
	// Fix route of message and set sourceNotTarget flag
	if ( source ){
	    MessageTools.fixRoute(msg, m_targetCID);
	    msg.setField(Constants.Fields.F_sourceNotTarget, "false");
	}
	else{
	    MessageTools.fixRoute(msg, m_sourceCID);
	    msg.setField(Constants.Fields.F_sourceNotTarget, "true");
	}

	MessageTools.fixToVNID(msg, Constants.MessageTypes.DeleteLink);
	
	//fill in rest
	msg.setField(Constants.MessageFields.cid, m_sourceCID);
	msg.setField(Constants.MessageFields.vnid, m_sourceVNID.toString());	

	if ( m_targetVNID != null )
	    msg.setField(Constants.MessageFields.targetVNID, 
			 m_targetVNID.toString());

	msg.setField(Constants.MessageFields.targetCID, m_targetCID);
    }
}
