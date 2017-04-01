/* $Id: Group.java,v 1.4 2005/05/09 22:07:01 hlrossano Exp $ */
package nrs.tracker.sticktrack;

import java.util.LinkedList;
import java.util.Iterator;
import java.io.BufferedWriter;
//import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

/**
*	@author Hugo L. Rosano
*	@version 1.0
*	Group of Vectors
*/
public class Group extends LinkedList{

private double compactness = 0.25;

	/** Constructor with an initial vector
	*@param vector initial vector to initialize the Group*/
	public Group(Vector2D vector){
		this.add(vector);
	}

	/** Contructor empty*/ 
	public Group(){
		super();
	}

	/** Group of points that are near "Vector2D.isNear()" the vectors contains in this group
	* @return Group
	* @param b is the group to which new vectors are tested to be closed
	* @param vec reference
	*/
	public static Group findNear(Vector2D vec, Group b){
		return findNear(new Group(vec), b);
	}

	private static Group findNear(Group a, Group b){
		Iterator ga = a.iterator();
		Iterator gb;
		Group res = new Group();
		Vector2D vA, vB;
		while(ga.hasNext()){
			vA = (Vector2D)ga.next();
			gb = b.iterator();
			while(gb.hasNext()){
				vB = (Vector2D)gb.next();
				if(!a.contains(vB) && !res.contains(vB) && vA.isNear(vB) )
						res.add(vB);
			}
		}
		if(res.isEmpty())
			return a;
		a.addAll(res);
		return findNear(a,b);
	}

	/** matrix representation of all the points contained herein 
	* @return a this.size() by two double matrix */
	public double[][] getCoords(){
		Iterator it = this.iterator();
		Vector2D myV;
		int sizeRoute = this.size();
		double res[][] = new double[2][sizeRoute];
		for(int pointer=0;pointer<sizeRoute;pointer++){
			myV = (Vector2D)it.next();
			res[0][pointer] = myV.getX();
			res[1][pointer] = myV.getY();
		}
		return res;
	}

	/** Rough test to verify if this group is compact according to the imaginary circle form by the number of elements*/
	public boolean isCompact(){
		double radius = this.iRad();
		Vector2D mean = this.centerOfMass();
		int out = 0;
		Iterator it = this.iterator();
		while(it.hasNext()){
			if (  ((Vector2D)it.next()).sub(mean).getMagnitud() > radius  )
				out++;
		}
		return (double)out/this.size() < compactness;
	}

	public Matrix toMatrix(){
		return new Matrix(this.getCoords());
	}

	public boolean isValid(int minSize){
		//return isCompact() && this.size()>minSize;
		return this.size()>minSize;
	}

	public double iRad(){
		return Math.sqrt(this.size()/Math.PI);		
	}

	public boolean contains(Object o){
		Iterator it = this.iterator();
		boolean res = false;
		if(!this.isEmpty()){
			while(it.hasNext()){
				if (  ((Vector2D)it.next()).equals( o )  )
					res = true;
			}
		}
		return res;
	}
	
	/** Calculates the moments of Hu according to parameters	*/
	private double momentUV(double u,double v){
		double res = 0.0;
		Vector2D mean = this.centerOfMass();
		Vector2D rel;
		for(int i=0;i<size();i++){
			rel = ((Vector2D)this.get(i)).sub(mean);
			res += Math.pow(rel.getX(),u) * Math.pow(rel.getY(),v);
		}
		return res;
	}

	/** Moment of Hu invariants, the first four
	*	@return double array with the four rotation invariants of Hu
	*/
	public double[] rotationInvatiants(){
		double res[] = new double[4];
		double u20 = momentUV(2,0);
		double u02 = momentUV(0,2);
		double u11 = momentUV(1,1);
		double u30 = momentUV(3,0);
		double u03 = momentUV(0,3);
		double u21 = momentUV(2,1);
		double u12 = momentUV(1,2);
		res[0] = 4*Math.log(10*(u20*u20 + u02*u02));
		res[1] = 1.5*Math.log( (( (u20-u02)*(u20-u02) + 4*u11 )+0.0001)/0.0101 );
		res[2] = 1.5*Math.log( (( (u30-3*u12)*(u30-3*u12) + (u03-3*u21)*(u03-3*u21) )+0.0005)/0.0025 ) + 3;
		res[3] = Math.log( (( (u30-u12)*(u30-u12) + (u03-u21)*(u03-u21) )+0.0000001)/0.0000101 );
		return res;
	}

	/** calculates the scale invariant of this group
	*	@param	u index for Hu moments
	*	@param 	v index for Hu moments
	*	@return 	mUV/this.size()^((u+v)/2+1)
	*/
	public double scaleInvariant(double u, double v){
		return momentUV(u,v)/Math.pow(this.size(),(u+v)/2+1);
	}

	/**
	*	calculates the centre of mass of this group
	*	@return Vector2D
	*/
	public Vector2D centerOfMass(){
		Vector2D res = new Vector2D();
		for(int i=0;i<size();i++)
			res.inc((Vector2D)this.get(i));
		return new Vector2D(res.getX()/size(), res.getY()/size());
	}

	/** String representation of this class*/
	public void toFile(String fileName){							//Return description of instance
		if(!this.isEmpty()){
			try{
				BufferedWriter pal = new BufferedWriter(new FileWriter(fileName));
				Iterator vec = this.iterator();		
				pal.write("group =[\n");
				while(vec.hasNext()){
					pal.write(  ((Vector2D)vec.next()).toString() + "\t;\n");
				}
				pal.write("];\n");
				pal.close();
			}catch(java.io.IOException e){System.out.println("Problems while saving Group!!!");}
		}
	}

  public static Group getGroupFromFile(String _fileName){
		Group res = new Group();
		if(!_fileName.equals("")){
			try{
				BufferedReader readin = new BufferedReader(new FileReader(_fileName));
				readin.readLine();			//ignore first line
				String temp="", coords[];
				temp = readin.readLine();
				do{
					coords = temp.split("\t");	
					res.add(new Vector2D( Double.parseDouble(coords[0]), Double.parseDouble(coords[1]) ) );
					temp = readin.readLine();
				}while ( !temp.equals("];") && !temp.equals("eof") );	//indicates end of file
				readin.close();
			}catch(Exception ex){
				System.out.println("Error!\n"+ex);}
		}
		return res;
  }


}