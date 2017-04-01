//$Id: FindJointsInterface.java,v 1.3 2005/05/09 22:05:12 hlrossano Exp $
package nrs.tracker.stickgui;


/**
	@author	Hugo L. Rosano
*/
public interface FindJointsInterface{

	public boolean getFindMode();

	public boolean getImproveMode();

	public boolean getDoubleScale();

	public int getScanSize();

	public void setCoords(int x, int y, double x2, double y2);

	public int getMinSizeGroup();
	
	public boolean getAdjustMode();
	
	public int getCounter();

	public boolean editMassCentres();

	public void increaseCount();

	public void decreaseCount();	
	
	public void setFindCallBack(FindInterface myFindInterface);


}