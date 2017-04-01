// $Id: Colour.java,v 1.3 2005/05/09 22:03:55 hlrossano Exp $
package nrs.tracker.palette;

//import java.lang.Math.*;

/**
*	@author Hugo L. Rosano
*	@version 1.0
*	This class represent an individual colour
*/
public class Colour {

private int red;
private int green;
private int blue;

	/**
		Default contructor creates white colour
	*/
	public Colour(){
		this(255,255,255);
	}

	/** Contructor specifing initial colour 
	* @param red	colour value for this pixel
	* @param green	colour value for this pixel
	* @param	blue	colour value for this pixel
	*/
	public Colour(int red, int green, int blue){
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}

	/** Contructor specifing initial colour */
	public Colour(int [] col){
		setRed(col[0]);
		setGreen(col[1]);
		setBlue(col[2]);
	}

	/** red component
	@return the red component of the colour*/
	public int getRed(){return red;}

	/** return green component
	*@return the green component of the colour*/
	public int getGreen(){return green;}

	/** return blue component
	*@return the blue component of the colour*/
	public int getBlue(){return blue;}
	
	/** integer array representation of this class 
	@return three element integer array*/
	public int[] toArray(){
		return new int[]{red,green,blue};
	}
	/** double array representation of this class 
	@return three element double array*/
	public double[] toArrayDouble(){
		return new double[]{red, green, blue};
	}

	/** converts this colour to its negative value */
	public void toNegative(){
		red =~red;
		green =~green;
		blue = ~blue;
	}

	/** regative of this colour
	*@return Colour with the negative of this*/
	public Colour getNegative(){
		return new Colour(255-red, 255-green, 255-blue);
	}

	/** set red component bounded between 0 and 255
	@param red value for that primary colour*/
	public void setRed(int red){
		if (red<=255&red>=0)
			this.red = red;
		else if (red<0)
			this.red = 0;
		else
			this.red = 255;
	}
	/** set green component bounded between 0 and 255
	@param green value for that primary colour*/
	public void setGreen(int green){
		if (green<=255&green>=0)
			this.green = green;
		else if (green<0)
			this.green = 0;
		else
			this.green = 255;
	}

	/** set blue component bounded between 0 and 255
	@param blue value for that primary colour*/
	public void setBlue(int blue){
		if (blue<=255&blue>=0)
			this.blue = blue;
		else if (blue<0)
			this.blue = 0;
		else
			this.blue = 255;
	}

	/** ecludidian distance to the black
	*@return a double with that value*/
	public double getMagnitud(){
		return Math.sqrt(red*red+green*green+blue*blue);
	}

	/** Probability of the class to the many density probabilistic region given by the covariance,
	* mean and number of elements in that group
	* @param invCovs inverse covariance of all compound colour
	* @param mean all means of all different compound colours
	* @param ns number of elements of all different compound colours
	*@return the probabilistic this colour has to belong to the many compound colours
	*/
	public double [] probToBelongHere(double [][] invCovs, double [][] mean, int []ns){
		int size = mean.length;
		double tot = 0.0, res[] = new double[size];
		for(int i=0;i<size;i++){
			res[i] = probObjectGivenColour(invCovs[i], mean[i])*ns[i];
		}
		for(int i=0;i<size;i++)
			tot += res[i];
		for(int i=0;i<size;i++)
			res[i] /= tot;
		return res;
	}

	private double probObjectGivenColour(double []ic, double []mean){
		double [] r = new double[]{red-mean[0], green-mean[1], blue-mean[2]};
		double res = 0;
		int indx = 0;
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++)
				res += ic[indx++]*r[i]*r[j];
		}
		res = Math.exp(-0.5*res);
		return res;
	}

	/** Euclidian ditance to the colour specified 
	*@return distance as double
	*@param col is the other colour to be tested*/
	public double distanceTo(Colour col){
		double dred = red-col.getRed();
		double dgreen = green-col.getGreen();
		double dblue = blue-col.getBlue();
		return Math.sqrt(dred*dred+dgreen*dgreen+dblue*dblue);
	}

	/** String representation of this colour, RGB format*/
	public String toFile(){
		return red+"\t" + green+"\t"+blue;
	}

}
