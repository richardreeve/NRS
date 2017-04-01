// $Id: CompoundColour.java,v 1.2 2005/05/09 22:03:55 hlrossano Exp $
package nrs.tracker.palette;

import java.util.LinkedList;
import java.util.Iterator;
import nrs.tracker.sticktrack.Matrix;

/**
*	@author Hugo L. Rosano
*	@version 1.0
*	This class is for representing many sample colours contained in it
*/
public class CompoundColour extends LinkedList{

private String id;
private double []invCovariance;
private double []mean = new double[]{255, 255, 255};
private int undoCounter = 0;

  /**
   * This variable controls whether the display colour is linked to the
   * mean colour.
   */
  private boolean linkDisplayColour = true;
  private double [] displayColour = new double[]{255, 255, 255};
  

	/** Default constructor, id default is "default"*/
	public CompoundColour(){	
		this("default");
	}

	public boolean add(Object o){
		undoCounter++;
		return super.add(o);
	}
	
	/** Constructor specifing id name*/
	CompoundColour(String id){
		if (!id.equals(""))
			this.id = id;
	}

	public void undoFromHere(){
		undoCounter = 0;
	}

	public void undo(){
		if(!this.isEmpty()){
			for (int i=0;i<undoCounter;i++)
				this.removeLast();
		}
		undoCounter = 0;
	}
  
  /** Set whether the display colour for this {@link CompoundColour}
   * instance should be linked to the actual colour mean, or instead is
   * set to a client-specificied representative colour.
   *
   * @param b if true, the display colour is always linked to the actual
   * mean. If false, then the client can specifiy it separately.
   */
  void setDisplayColourLinked(boolean b)
  {
    linkDisplayColour = b;
  }

  /**
   * Explicitly set the display colour. The display colour for an {@link
   * CompoundColour} instance is a representative colour that can be
   * used in GUIs for debugging purposes. Usually this display colour is
   * linked to the actual mean colour represented by an {@link
   * CompoundColour} instance. 
   *
   * Note, calling this method does not unlink the display colour from
   * the mean. To perform that unlink call {@link
   * #setDisplayColourLinked}.
   */
  void setDisplayColour(int R, int G, int B)
  {
    displayColour[0] = R;
    displayColour[1] = G;
    displayColour[2] = B;
  }

  /**
   * Return the display colour to use for this class. See {@link
   * #setDisplayColour} for more information regarding setting and
   * unlinking the display colour from the actual mean colour.
   *
   * @return an array of size three, in the format of Red, Green, Blue.
   */
  public double[] getDisplayColour()
  {
    if (linkDisplayColour)
    {
      return displayColour;
    }
    return getMean();
  }
  

	/** name of this class
	*@return id name of the class*/
	public String id(){
		return id;
	}

        /** Rename the textual label of this colour */
        public void rename(String new_id)
        {
          // defensive
          if (new_id == null || new_id.trim().length() == 0) return;
 
          id = new_id;
        }

	/** This class is recognized as equal if the id name is the same
	*	@return true if equal
	*	@param other is the object to be tested
	*/
	public boolean equals(Object other){
		return ((CompoundColour)other).id().equals(this.id);
	}

	/** mean of the colours of this compound colour
	*@return the mean as a Colour class*/
	public Colour getColourMean(){
		double [] c = getMean();
		return new Colour((int)c[0], (int)c[1], (int)c[2]);
	}

	/** mean of the colours of this compound colour
	*@return mean of this compound colour */
	public double[] getMean(){
		if(!this.isEmpty()){
			double red =0, green =0, blue =0, c[];
			Iterator it = this.iterator();
			while(it.hasNext()){
				c = ((Colour)it.next()).toArrayDouble();
				red += c[0];
				green += c[1];
				blue += c[2];
			}
			mean = new double[]{red/size(), green/size(), blue/size()};
		}
		return mean;
	}

	/** inverse covariance of the colours of this compound colour
	*@return the covariance as a 1x9 array*/
	public double[] getInvCovariance(){
		double [] covariance = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		if(size()>0){
			int k, c[], m[] = this.getColourMean().toArray();
			Iterator it = this.iterator();
			while(it.hasNext()){
				c = ((Colour)it.next()).toArray();
				k=0;
				for(int i=0;i<3;i++){
					for(int j=0;j<3;j++)
						covariance[k++] += (c[j]-m[j])*(c[i]-m[i]);
				}
			}
			for(int i=0;i<9;i++)
				covariance[i] /= size();
			//this.invCovariance = nrs.tracker.sticktrack.Matrix.inverse(covariance);
			Matrix test = new Matrix(covariance, 3, 3);
			this.invCovariance = test.inverse().reshape().getValues()[0];
		}
		return invCovariance;
	}

	/** Returns a 12 size array. 
	*	@return array.	9 first elements are the inv covariance, 
	*					the next 3 are the mean of the colours contained herein, 
	*					last is the number of elements*/
	public double[] getAllInfo(){
		double []res = new double[13];
		for(int i=0;i<9;i++)
			res[i]	=	invCovariance[i];
		for(int i=0;i<3;i++)
			res[9+i]	=	mean[i];
		res[12] = this.size();
		return res;
	}

	/** String representation of this group*/
	public String toString(){
		return id;
	}

}
