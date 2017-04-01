/* $Id: JointSequenceManager.java,v 1.5 2005/05/09 22:00:07 hlrossano Exp $ */
/*
 * Created on 30-Apr-2005
 *
 */
package nrs.tracker.jointmanager;

import nrs.tracker.sticktrack.Vector2D;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * @author Hugo L. Rosano
 *
 * TODO complete this
 */

public class JointSequenceManager extends Vector implements Serializable{
	
	static final long serialVersionUID = 1L;
	
	public static final int OK = 100;
	public static final int MISSING_JOINTS = 102;
	public static final int EXTRA_JOINTS = 103;
	public static final int TIME_PROBLEM = 104;
	public static final int STOP_ALL = 105;
	
	private static final int ANYSTEP_PHASE = Joint.STANCE-1;
	
	transient private String onEdition = "";	
	transient private int question;
	
	public boolean add(String JointSequenceName){
		return setOnEdition(JointSequenceName);
	}
	
	public String onEdit(){
		return onEdition;
	}
	
	public JointSequence getJointSequence(String idName){
		Iterator it = this.iterator();
		JointSequence answer = null;
		boolean notFound = true;
		while(it.hasNext()){
			answer = (JointSequence)it.next(); 
			if (answer.getIdName().equals(idName)){
				notFound = false;
				break;
			}
		}
		if (notFound)
			return null;
		return answer;
	}
	
	public void clear(){
		Iterator it=this.iterator();
		while(it.hasNext())
			((LinkedList)it.next()).clear();
		super.clear();
	}
	
	public void changeLabel(String seq, int from, int to){
		JointSequence seq1 = getJointSequence(seq);
		if(seq1==null)
			return;
		seq1.changeLabel(Joint.stepPhaseToInt(seq),from,to);
	}
	
	public Joint getBodyMean(){
		JointTimeInstance g;
		JointTimeInstance answer = new JointTimeInstance();
		for(int i=0; !((g = getJointsAt(i, Joint.BODY)).isEmpty()) ; i++){
			answer.add(g.centerOfMass());
		}
		return new Joint(Joint.REFERENCE, answer.centerOfMass());
	}
	
	public JointSequenceManager getAdjustNice(int from,  int to, double angle, Joint shift, double scale){
		JointSequenceManager answer = new JointSequenceManager();
		if (this.isEmpty())
			return answer;
		JointTimeInstance target = (JointTimeInstance)this.getJointsAt(from, Joint.BODY).clone();
		if (target.isEmpty())
			return answer;
		JointTimeInstance timeIt;
		JointTimeInstance jti;
		question = 1;
		for(int i = from; i<=to ; i++){
			if ( (timeIt = getJointsAt(i, Joint.BODY)).isEmpty() )
				break;
			if (!timeIt.similar(target))
				break;
			
			JointTimeInstance njti = (JointTimeInstance)timeIt.clone();
			Joint cm = njti.centerOfMass();
			cm.scale(-1);
			njti.shift(cm);
			Joint m2 = target.centerOfMass();
			m2.scale(-1.0);
			target.shift(m2);
			double mainRot = JointTimeInstance.calculateRotation(njti, target);
			njti.rotate(mainRot);
			double mainScale = JointTimeInstance.calculateScale(njti, target);

			jti = (JointTimeInstance)getJointsAt(i).clone();
			jti.shift(cm);
			jti.rotate(mainRot+angle);
			jti.scale(mainScale*scale);
			jti.shift(shift);
			answer.addLinkedListForced(jti, i);
		}
		Iterator it = answer.iterator();
		JointSequence js;
		while(it.hasNext()){
			 js = (JointSequence)it.next();
			 js.setVisible(getJointSequence(js.getIdName()).getVisible());
		}
		return answer;
	}

	
	public boolean swapJoints(String swap1, String swap2, int time){
		JointSequence seq1 = getJointSequence(swap1);
		JointSequence seq2 = getJointSequence(swap2);
		if ((seq1==null)||(seq2==null)||(time<0))
			return false;
		JointSequence tail1 = seq1.divide(time, seq2.getIdName());
		JointSequence tail2 = seq2.divide(time, seq1.getIdName());
		seq1.addAll(tail2);
		seq2.addAll(tail1);
		return true;
	}
	
	public boolean splitJoint(String js, int time){
		JointSequence seq1 = getJointSequence(js);
		if (seq1==null)
			return false;
		JointSequence tail1 = seq1.split(time, "default_" + (this.size()+1));
		if (tail1!=null)
			this.add(tail1);
		return true;
	}
	
	public boolean removeToEnd(String js, int time){
		JointSequence seq = getJointSequence(js);
		if(seq==null)
			return false;
		return seq.removeToEnd(time);
	}
	
	public boolean setOnEdition(String onEdition){
		onEdition = onEdition.trim().toLowerCase();
		if(!onEdition.equals("")){
			this.onEdition = onEdition;
		}
		return false;
	}
	
	private JointTimeInstance getJointsAt(int time, int stepPhase){
		JointTimeInstance answer = new JointTimeInstance(time);
		Joint j;
		Iterator it = this.iterator();
		while(it.hasNext()){
			j = ((JointSequence)it.next()).getJoint(time);
			if ((j!=null) && ((stepPhase == ANYSTEP_PHASE) || (j.getStepPhase() == stepPhase )) ){
				answer.add(j);
			}
		}
		return answer;
	}
	
	private JointTimeInstance getJointsAt(int time){
		return getJointsAt(time, ANYSTEP_PHASE);
	}
	
	public JointTimeInstance getFirstJoints(){
		return getJointsAt(0, ANYSTEP_PHASE);
	}
	
	public JointTimeInstance getLastJoints(){
		return getLastJoints(ANYSTEP_PHASE);
	}
	
	public JointTimeInstance getLastJoints(int stepPhase){
		JointTimeInstance answer = new JointTimeInstance();
		Joint j;
		Iterator it = this.iterator();
		while(it.hasNext()){
			j = (Joint)((JointSequence)it.next()).getLast();
			if ((j!=null) && ((stepPhase == ANYSTEP_PHASE) || (j.getStepPhase() == stepPhase )) ){
				answer.add(j);
			}
		}
		return answer;	
	}
	
	private JointTimeInstance getPrevious(int time, int stepPhase){
		JointTimeInstance answer = new JointTimeInstance(time);
		Joint j;
		Iterator it = this.iterator();
		while(it.hasNext()){
			j = (Joint)((JointSequence)it.next()).getLast();
			if ((j!=null) && (j.getTime() == time ) && 
					((stepPhase == ANYSTEP_PHASE) || (j.getStepPhase() == stepPhase )) ){
				answer.add(j);
			}
		}
		return answer;			
	}
	
	public JointTimeInstance getPrevious(int time){
		return getPrevious(time, ANYSTEP_PHASE);		
	}
	
	public int addLinkedList(LinkedList g, int time){
		question = 0;
		return addLinkedListInternal(g, time);
	}
	
	private int addLinkedListForced(LinkedList g, int time){
		int tempQuestion = question;
		question = 1;
		int answer = addLinkedListInternal(g, time);
		question =tempQuestion;
		return answer;
	}
	
	private int addLinkedListInternal(LinkedList g, int time){
		Iterator it = g.iterator();
		int lastSize = getPrevious(time-1).size();
		int toAdd = (lastSize==0) ? g.size() : lastSize;
		int added = 0;
		while(it.hasNext()){
			Object o = it.next();
			if ( (o.getClass() == Vector2D.class) && (addVector2D((Vector2D)o, time) ) ) 
				added++;
			else if ( (o.getClass() == Joint.class) && (addJoint((Joint)o) ) ) 
				added++;
		}
		if (added == toAdd)
			return OK;
		if (added > toAdd)
			return EXTRA_JOINTS;
		return MISSING_JOINTS; 			//if (added < toAdd)
	}
	
	private boolean addVector2D(Vector2D v, int time){
		Joint j = new Joint(time, v);
		j.setStepPhase(Joint.REFERENCE);
		return addJoint(j);
	}
	
	private boolean addJoint(Joint j){
		JointSequence js;
		JointSequence jsClosest = null;
		Joint jt;
		Iterator it = this.iterator();
		double minDist = Double.MAX_VALUE, dist;
		
		while(it.hasNext()){
			js = (JointSequence)it.next();
			jt = ((Joint)js.getLast());
			if (jt.getTime() == j.getTime()-1){
				dist = jt.sub(j).getMagnitud();
				if (dist < minDist){
					minDist = dist;
					jsClosest = js;
				}
			}
		}
		if (minDist != Double.MAX_VALUE){
			if(j.getStepPhase() == Joint.REFERENCE)
				j.setStepPhase(((Joint)jsClosest.getLast()).getStepPhase());
			return jsClosest.add(j);
		}
		
		if(this.isEmpty()){
				question = 1;
		}
		if ((question == 0)||(question==2)){
			Object[] options = {"Yes", "Yes All", "No", "No All"};
			question = JOptionPane.showOptionDialog(null, "Could not allocate joint to any sequence\n" +
					"Create new sequence for it?", "Joint Problem", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		}
		if (question < 2 ){
			String seqName = (j.getIdName() == "default") ? "default_" + (this.size()+1) : j.getIdName() ;
			return this.add(new JointSequence(seqName, j));
		}
		return false;
		
	}
	
	public boolean glueJoints(String glue1, String glue2){
		JointSequence seq1 = getJointSequence(glue1);
		JointSequence seq2 = getJointSequence(glue2);
		if ((seq1==null)||(seq2==null))
			return false;
		if ( ((Joint)seq2.getFirst()).getTime() == ((Joint)seq1.getLast()).getTime()+1 ){
			seq1.addAll(seq2);
			this.remove(seq2);
			return true;
		}
		if ( ((Joint)seq1.getFirst()).getTime() == ((Joint)seq2.getLast()).getTime()+1 ){
			seq2.addAll(seq1);
			this.remove(seq1);
			return true;
		}
		if ( ((Joint)seq2.getFirst()).getTime() == ((Joint)seq1.getLast()).getTime() ){
			seq2.removeLast();
			seq1.addAll(seq2);
			this.remove(seq2);
			return true;
		}
		if ( ((Joint)seq1.getFirst()).getTime() == ((Joint)seq2.getLast()).getTime() ){
			seq1.removeLast();
			seq2.addAll(seq1);
			this.remove(seq1);
			return true;
		}
		return false;
	}	
	
}
