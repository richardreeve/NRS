/* $Id: JointSequence.java,v 1.3 2005/05/09 22:00:07 hlrossano Exp $ */
/*
 * Created on 30-Apr-2005
 */
package nrs.tracker.jointmanager;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Hugo L. Rosano
 *
 * TODO complete this comment
 */
public class JointSequence extends LinkedList implements Serializable{

	static final long serialVersionUID = 1L;
	
	private boolean visible = true;
	private String idName = "_default";
	
	JointSequence(){
		super();
	}
	
	JointSequence(Object o){
		super();
		this.add(o);
	}
	
	JointSequence(String idName, Object o){
		super();
		this.idName = idName;
		this.add(o);
	}
	
	public void setVisible(boolean visible){
		this.visible = visible;
	}
	
	public boolean getVisible(){
		return visible;
	}
	
	public Joint getMean(){
		Joint j = new Joint(idName, 0, 0);
		Iterator it = this.iterator();
		while(it.hasNext()){
			j.inc((Joint)it.next());
		}
		j.scale(1.0/this.size());
		return j;
	}
	
	public void changeLabel(int newStep, int from, int to){
		if (newStep==0)
			return;
		for(int i=from;i<=to;i++){
			Joint j = getJoint(i);
			if (j!=null){
				j.setStepPhase(newStep);
			}
		}
	}
	
	public boolean add(Object o){
		Joint jointTemp = (Joint)o;
		if (idName == "_default"){
			idName = jointTemp.getIdName();
		}
		else{
			jointTemp.setIdName(idName);
		}
		super.add(jointTemp);
		return true;
	}
	
	public int getLastTime(){
		return ((Joint)this.getLast()).getTime();
	}
	
	public Joint getJoint(int time){
		Iterator it = this.iterator();
		while(it.hasNext()){
			Joint tempJ = (Joint)it.next();
			if ( tempJ.getTime() == time ){
				return tempJ;
			}
		}
		return null;
	}
	
	public String getIdName(){
		return idName;
	}
	
	public boolean removeToEnd(int from){
		if(this.isEmpty())
			return false;
		Joint j = (Joint)this.getLast();
		while(j.getTime() >= from){
			this.removeLast();
			j = (Joint)this.getLast();
		}
		return true;
	}
	
	public JointSequence split(int from, String newIdName){
		JointSequence js = divide(from, newIdName);
		if (js.isEmpty())
			return js;
		this.add(js.getFirst());
		return js;
	}
	
	public JointSequence divide(int from, String newIdName){
		JointSequence newSeq = new JointSequence();
		newSeq.setIdName(newIdName);
		if(this.isEmpty())
			return null;
		Joint j = (Joint)this.getLast();
		while ( (j!=null) && (j.getTime()>=from) ){
			j = (Joint)this.removeLast();
			j.setIdName(newIdName);
			newSeq.addFirst(j);
			if (this.isEmpty())
				break;
			j = (Joint)this.getLast();
		}
		return newSeq;
	}
	
	public JointSequence divide(int from){
		return divide(from, "_default");
	}	
	
	public void setIdName(String idName){
		Iterator it = this.iterator();
		while(it.hasNext())
			((Joint)it.next()).setIdName(idName);
		this.idName = idName;
	}
	
	public String toString(){
		return idName;
	}
	
	
}
