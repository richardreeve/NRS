//$Id: FindInterface.java,v 1.4 2005/05/09 22:05:12 hlrossano Exp $
package nrs.tracker.stickgui;

import nrs.tracker.sticktrack.Group;
import nrs.tracker.sticktrack.Matrix;

/**
*	@author Hugo L. Rosano
*/

public interface FindInterface{

	public void addManual();

	public int getRegionGroups(boolean onlyWherePrevious);

	/***/

	public void setGrid();

	public void setImage(int counter);

	public void repaint();
	
	public void removeImg();

	public void displayTransform(int scale, int offset);

	public void loadWeights(Matrix weights);

	public void exportGrid(String fileName);

	public void loadGrid(Group grid);

	public void saveCalibration(String fileName);

	public void calibrate(Group refs);
	
	public void visualEnable(boolean showGraph);

}