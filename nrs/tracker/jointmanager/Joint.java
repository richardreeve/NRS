/* $Id: Joint.java,v 1.5 2005/05/09 22:00:07 hlrossano Exp $ */
/*
 * Created on 30-Apr-2005
 *
 */
package nrs.tracker.jointmanager;

import java.io.Serializable;

import nrs.tracker.sticktrack.Vector2D;

/**
 * @author Hugo Rosano
 * This represent an instance joint time in the scene
 */
public class Joint extends Vector2D implements Serializable{
	
	static final long serialVersionUID = 1L;
	
	public static final int STANCE = 10;
	public static final int SWING = 11;
	public static final int BODY = 12;
	public static final int REFERENCE = 13;
	
	public static final String STANCE_DISP = "Stance";
	public static final String SWING_DISP = "Swing";
	public static final String BODY_DISP = "Body";
	public static final String REFERENCE_DISP = "Reference";
	
	private int time = 0;
	private int stepPhase = REFERENCE;
	private String idName = "default";
	
	public Object clone(){
		Joint j = new Joint(this.idName, this);
		j.setStepPhase(this.stepPhase);
		j.setTime(this.time);
		return j;
	}
	
	public Joint(String idName, double x, double y){
		super(x, y);
		this.idName = idName;
	}
	
	public Joint(String idName){
		this.idName = idName;
	}
	
	public Joint(String idName, Vector2D v){
		super(v.getX(), v.getY());
		this.idName = idName;
	}
	
	public Joint(int time, Vector2D v){
		super(v.getX(), v.getY());
		this.setTime(time);
	}
	
	public Joint(int time, String idName){
		this.setTime(time);
		this.idName = idName;
	}
	
	public void setCoords(Vector2D v){
		this.x = v.getX();
		this.y = v.getY();
	}
	
	public Joint(int time, int stepPhase, String idName){
		this(time, idName);
		this.stepPhase = stepPhase;
	}
	
	public void setTime(int time){
		if (time<0)
			time = 0;
		this.time = time;
	}
	
	public int getTime(){
		return time;
	}
	
	public void setStepPhase(int stepPhase){
		this.stepPhase = stepPhase;
	}
	
	public int getStepPhase(){
		return stepPhase;
	}
	
	public static int stepPhaseToInt(String stepPhase){
		if (stepPhase.equalsIgnoreCase(STANCE_DISP))
			return STANCE;
		if (stepPhase.equalsIgnoreCase(SWING_DISP))
			return SWING;
		if (stepPhase.equalsIgnoreCase(BODY_DISP))
			return BODY;
		if (stepPhase.equalsIgnoreCase(REFERENCE_DISP))
			return REFERENCE;
		return 0;
	}
	
	public static String stepPhaseToString(int stepPhase){
		switch(stepPhase){
		case STANCE:
			return STANCE_DISP;
		case SWING:
			return SWING_DISP;
		case BODY:
			return BODY_DISP;
		case REFERENCE:
			return REFERENCE_DISP;
		}
		return "Not Defined";
	}
	
	public void setIdName(String idName){
		this.idName = idName;
	}
	
	public String getIdName(){
		return idName;
	}
	
	public Joint copyJoint(){
		Joint ans = new Joint(time, this);
		ans.setStepPhase(stepPhase);
		ans.setIdName(idName);
		return ans;
	}
	
	/** This joint equals others when their ids are the same*/
	public boolean equals(Object j){
		boolean first = ((Joint)j).getIdName()== this.idName; 
		boolean second = ((Joint)j).getTime()== this.time;
		return first && second ;
	}
}
