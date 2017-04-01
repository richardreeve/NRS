/* $Id: Vector2D.java,v 1.7 2005/05/09 22:07:01 hlrossano Exp $ */
package nrs.tracker.sticktrack;

import java.io.Serializable;

//import java.lang.Math.*;
/**
*	@author Hugo L. Rosano
*	@version 1.0
* Two dimensional vector in double resolution.
* Basic vectoral operations implemented.
*/
public class Vector2D implements Serializable{
	
	static final long serialVersionUID = 1L;	
	
	protected double x=-1,y=-1;								//Coordinates

	/** default constructor*/
	public Vector2D(){}

	/** double x and y constructor*/
	public Vector2D(double x, double y){				//Constructor
		this.x = x;
		this.y = y;
	}

	/** int x and y constructor*/
	public Vector2D(int x, int y){						//Constructor
		this.x = x;
		this.y = y;
	}
	
	public Vector2D copy(){
		return new Vector2D(x,y);
	}
	
	/** if it hasn't been initialize returns -1*/
	public boolean isEmpty(){
		return (this.x==-1);
	}

	/** Get x component*/
	public double getX(){								//X Coordinate
		return this.x;
	}
	/** Get y component*/
	public double getY(){								//Y Coordinate
		return this.y;
	}

	/** Return the magnitud of this Vector2D*/
	public double getMagnitud(){							//Magnitud of vector
		return Math.sqrt(this.x*this.x+this.y*this.y);
	}

	/** Dot product with the vector specified
	public double dotProduct(Vector2D v){					//Dot product with vector v
		return this.x*v.getX()+this.y*v.getY();
	}*/
	/** if(Math.abs(xv-(int)x)<=1 && Math.abs(yv-(int)y)<=1) */
	public boolean isNear(Vector2D v){
		int xv = (int)v.getX();
		int yv = (int)v.getY();
		if(Math.abs(xv-(int)x)<=1 && Math.abs(yv-(int)y)<=1)
			return true;
		return false;
	}

	/** This vector equals others when both components are equal*/
	public boolean equals(Object v){
		return ((Vector2D)v).getX()==x && ((Vector2D)v).getY()==y;
	}

	/** Get the result of substracting this with other vector*/
	public Vector2D sub(Vector2D v){						//substraction
		double coordx = this.x - v.getX();
		double coordy = this.y - v.getY();
		return new Vector2D(coordx,coordy);
	}

	/** this vector substracted by the other*/
	public void dec(Vector2D v){							//decrement both coord
		this.x -= v.getX();
		this.y -= v.getY();
	}

	/** this vector substracted by the other*/
	public Vector2D add(Vector2D v){						//addition
		double coordx = this.x + v.getX();
		double coordy = this.y + v.getY();
		return new Vector2D(coordx,coordy);
	}

	/** increment this by*/
	public void inc(Vector2D v){							//increment both coord
		this.x += v.getX();
		this.y += v.getY();
	}

	public void rotate(double angle){
		double x = this.x;
		double y = this.y;
		this.x = x*Math.cos(angle) + y*Math.sin(angle);
		this.y = y*Math.cos(angle) - x*Math.sin(angle);
	}
	
	public Vector2D getScaled(double val){
		return new Vector2D(this.x/val, this.y/val);
	}
	
	public void scale(double val){
		this.x *= val;
		this.y *= val;
	}
	
	/** String representation */
	public String toString(){						//Description of class
		return this.x+"\t"+this.y+"\t";
	}
}