/* $Id: JointWindowInterface.java,v 1.4 2005/05/09 22:00:07 hlrossano Exp $ */
/*
 * Created on 30-Apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nrs.tracker.jointmanager;

import nrs.tracker.sticktrack.Group;

/**
 * @author Hugo L. Rosano
 *
 */

public interface JointWindowInterface {
	
	public void deleteAll();
	
	public int addNewJoints(Group cg);
	
	public void setJointCallBack(JointInterface myJointInterface);
	
	public JointSequenceManager getJointManager();
	
	public JointSequence getSelectedJointSequence();
	
	public JointSequenceManager getAdjustNice();
	
	public int getTo();
	
	public int getFrom();
	
	public JointTimeInstance getPrevious();
	
	public void setTimeStamp(int time);
	
}
