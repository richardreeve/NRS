/* $Id: CompoundGroup.java,v 1.4 2005/05/09 22:07:01 hlrossano Exp $ */
package nrs.tracker.sticktrack;

import java.util.LinkedList;
import java.util.Iterator;

/**
 *	CompoundGroup contains Groups of Vector2D 
 * @author s0231560
 *
 */
public class CompoundGroup extends LinkedList{

	/**
	 * This is just a big number
	 */
public static final double BIG = 999999;

/**
 * center of masses of each Group contain herein
 */
private Group cmasses = new Group();

	/**
	 * Empty Constructor
	 *
	 */
	public CompoundGroup(){super();}

	/**
	 * Constructor given a Group of vectors and a minimum size
	 * @param group to be added
	 * @param minSize minimum elements in a group to be valid
	 */
	public CompoundGroup(Group group, int minSize){
		if( !group.isEmpty() ){
			Iterator it = group.iterator();
			Vector2D myV;
			while(it.hasNext()){
				myV = (Vector2D)it.next();
				if (!this.contains(myV))
					this.add( Group.findNear(myV, group) );
			}
			this.representatives(minSize);
		}
	}

	public CompoundGroup(Group group){
		this(group, 15);
	}

	public boolean contains(Object o){
		Iterator it = this.iterator();
		boolean res = false;
		if(!this.isEmpty()){
			while(it.hasNext()){
				if (  ((Group)it.next()).contains( o )  )
					res = true;
			}
		}
		return res;
	}

	public void addCenterOfMass(Vector2D cmass){
		cmasses.add(cmass);
	}

	public void clear(){
		Iterator it=this.iterator();
		while(it.hasNext())
			((LinkedList)it.next()).clear();
		super.clear();
	}
	
	public Group getCenterOfMasses(){
		return cmasses;
	}

	/** This function calcs the center of mass of the groups contained in this class
	*/
	public void representatives(int minSize){
		Iterator it = this.iterator();
		Group temp;
		cmasses.clear();
		while(it.hasNext()){
			temp = (Group)it.next();
			if( temp.isValid(minSize) )
				cmasses.add(temp.centerOfMass());
		}
	}

}