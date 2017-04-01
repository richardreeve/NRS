// $Id: ScanInterface.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.tracker.palette;

/**
*	@author Hugo L. Rosano
*/

public interface ScanInterface{

	/** Call this function for beginning scanning the region previously specified
	*/
	public void regionScan();

	/** Function called for displaying regions found in the image according to the colour classification specified
	*/
	public void showCandidates(double thresProb);

	/**erase regions display in the canvas */
	public void eraseTrack();

}