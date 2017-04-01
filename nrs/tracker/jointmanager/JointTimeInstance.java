/* $Id: JointTimeInstance.java,v 1.3 2005/05/09 22:00:07 hlrossano Exp $ */
/*
 * Created on 05-May-2005
 *
 */
package nrs.tracker.jointmanager;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Hugo L. Rosano
 *
 */
public class JointTimeInstance extends LinkedList {

	private int time = 0;
	
	public JointTimeInstance(){
		super();
		if (!isEmpty() && this.size()==1)
			this.time = ((Joint)this.getFirst()).getTime();
	}
	
	public int time(){
		return time;
	}
	
	public boolean add(Object o){
		boolean ret = super.add(o);
		if(ret && this.size()==1)
			this.time = ((Joint)this.getFirst()).getTime();
		return ret;
	}
	
	public Object clone(){
		JointTimeInstance njti = new JointTimeInstance(this.time);
		Iterator it = this.iterator();
		while(it.hasNext())
			njti.add( ((Joint)it.next()).clone() );
		return njti;		
	}
	
	public JointTimeInstance(int time){
		super();
		this.forceNewTime(time);
	}
	
	public void forceNewTime(int time){
		if (time<0)
			this.time = 0;
		else
			this.time = time;
	}
	
	public JointTimeInstance copy(){
		JointTimeInstance njti = new JointTimeInstance(this.time);
		Iterator it = this.iterator();
		while(it.hasNext())
			njti.add(it.next());
		return njti;
	}
	
	public void shift(Joint j){
		Iterator it = this.iterator();
		while(it.hasNext())
			((Joint)it.next()).inc(j);
	}

	public void scale(double amount){
		Iterator it = this.iterator();
		while(it.hasNext())
			((Joint)it.next()).scale(amount);
	}

	public void rotate(double angle){
		Iterator it = this.iterator();
		while(it.hasNext())
			((Joint)it.next()).rotate(angle);
	}
	
	public void rotateFromCenter(double angle){
		Joint c = this.centerOfMass();
		c.scale(-1.0);
		this.shift(c);
		this.rotate(angle);
		c.scale(-1.0);
		this.shift(c);
	}
	
	public static double calculateScale(JointTimeInstance jti1, JointTimeInstance jti2){
		double num = 0, den = 0;
		Joint j1, j2;
		Iterator it1 = jti1.iterator();
		Iterator it2 = jti2.iterator();
		while(it1.hasNext()){
			j1 = (Joint)it1.next();
			j2 = (Joint)it2.next();
			num += j1.getX()*j2.getX() + j1.getY()*j2.getY();
			den += j1.getX()*j1.getX() + j1.getY()*j1.getY();
		}
		if (den == 0){
			return 0;
		}
		return num/den;
	}
	
	public static double calculateRotation(JointTimeInstance jti1, JointTimeInstance jti2){
		double num = 0, den = 0;
		Joint j1, j2;
		Iterator it1 = jti1.iterator();
		Iterator it2 = jti2.iterator();
		while(it1.hasNext()){
			j1 = (Joint)it1.next();
			j2 = (Joint)it2.next();
			num += j1.getX()*j2.getY() - j1.getY()*j2.getX();
			den += j1.getX()*j2.getX() + j1.getY()*j2.getY();
		}
		if (den == 0){
			return Math.PI/2;
		}
		return -Math.atan(num/den);
	}
	
	public boolean similar(Object o){
		if ( o.getClass() != JointTimeInstance.class )
			return false;
		JointTimeInstance jti = (JointTimeInstance)o;
		if (this.size() != jti.size())
			return false;
		Joint j1, j2;
		Iterator it1 = this.iterator();
		Iterator it2 = jti.iterator();
		while(it1.hasNext()){
			j1=(Joint)it1.next();
			j2=(Joint)it2.next();
			if (!j1.getIdName().equals(j2.getIdName()))
				return false;
		}
		return true;		
	}
	
	public boolean equals(Object o){
		if ( o.getClass() != JointTimeInstance.class )
			return false;
		JointTimeInstance jti = (JointTimeInstance)o;		
		return similar(o) && (this.time == jti.time());
	}
	
	public Joint centerOfMass(){
		Joint res = new Joint(Joint.REFERENCE_DISP, 0,0);
		res.setTime(this.time);
		for(int i=0;i<this.size();i++)
			res.inc((Joint)this.get(i));
		res.scale(1.0/this.size());
		return res;
	}
	
}
