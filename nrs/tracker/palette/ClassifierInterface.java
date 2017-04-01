// $Id: ClassifierInterface.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.tracker.palette;


/**
	@author		Hugo L. Rosano
*/


public interface ClassifierInterface{

public static int GROW_REGION_MODE = 10;
public static int CIRCLE_REGION_MODE = 11;
public static int SQUARE_REGION_MODE = 12;

public static int MOST_PROBABLE_COLOUR = 20;
public static int COLOUR_ONEDITION_HIGHLY_PROB = 21;


	/**
	* @return GROW_REGION_MODE when selection in done with a growing region method
	*			CIRCLE_REGION_MODE when selectrion in done with a circle area
	*			SQUARE_REGION_MODE when selectrion in done with a square area
	*/
	public int getScanMode();

	/** @return double	Representing threshold distance between colours used by the growing algorithm
	*/
	public double getGrowThreshold();

	public double getProbThreshold();

	/** @return MOST_PROBABLE_COLOUR	This mode indicates that regions are found based on the class that is more probable for the colour sample given
	*			COLOUR_ONEDITION_HIGHLY_PROB	This mode returns the probability of the sample colour to belong to the Colour under edition
	*/
	public int getFindMode();

	public void setScanCallBack(ScanInterface myScanInterface);

	public void setCandidatesCallBack(ScanInterface myScanInterface);

}