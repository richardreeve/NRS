/* $Id: MyCanvas.java,v 1.7 2005/05/09 22:05:12 hlrossano Exp $ */
package nrs.tracker.stickgui;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import nrs.tracker.jointmanager.Joint;
import nrs.tracker.jointmanager.JointInterface;
import nrs.tracker.jointmanager.JointSequence;
import nrs.tracker.jointmanager.JointSequenceManager;
import nrs.tracker.jointmanager.JointTimeInstance;
import nrs.tracker.jointmanager.JointWindow;
import nrs.tracker.palette.ClassifierInterface;
import nrs.tracker.palette.Colour;
import nrs.tracker.palette.CompoundColour;
import nrs.tracker.palette.PaletteWindow;
import nrs.tracker.palette.ScanInterface;
import nrs.tracker.palette.ScanWindow;
import nrs.tracker.sticktrack.CompoundGroup;
import nrs.tracker.sticktrack.Grid;
import nrs.tracker.sticktrack.Group;
import nrs.tracker.sticktrack.Matrix;
import nrs.tracker.sticktrack.Vector2D;

/**
*	@author Hugo L. Rosano
*	@version 1.0
*	This class handle mouse events and contains the ImageData
*/
public class MyCanvas extends JComponent implements KeyListener, MouseMotionListener, MouseListener, ScanInterface, FindInterface, JointInterface{

public static final int MOUSE_SCANING = 0;
public static final int MOUSE_FINDING = 1;
public static final int MOUSE_PAINTING = 2;

private boolean graphEnable = true;
private ImageData image;						//IMAGE

private CompoundGroup cgroup = new CompoundGroup();		//each group is a possible joint
private CompoundGroup groups 	= new CompoundGroup();	//groups found by growing region or candidates

private CompoundGroup figures	= new CompoundGroup();	//objects drawn so far

private Group tempGroup = new Group();			//tempGroup use while drawing
private Group findCursor = new Group();			// marks on canvas for FindWindow, use for many functions

private Vector2D growingDot = new Vector2D();		//growing region cursor, for use with scan mode, PLUS SIGN
private Vector2D helperDot = new Vector2D();		//find helper cursor, only with pressed mouse and FindMode SMALL CIRCLE

//this is use for detecting mouse dragging
private boolean readMouse 	= false;			//once the mouse is been pressed and move one more pixel
private int mouseBehaviour	= 0;			//type of region scan over the image

private Square square  	= new Square();			//square region scan
private Circle circle	= new Circle();			//circle region scan

private Grid m_grid = new Grid();

private Preferences myPrefs;

private static PaletteWindow myPalWin;
private static ScanWindow myScanWin;
private static FindWindow myFindWin;
private static DisplayWindow myDisplayWin;
private static JointWindow myJointWin;

private String imageName = "";
private int imageNum = 0;
private Pattern pattern = Pattern.compile("\\d++((.((jpg)|(JPG)))|\\z)");

	/** Constructs Canvas*/
	public MyCanvas(STMainFrame parent){
		setBackground(Color.white);
		addMouseMotionListener(this);
		addMouseListener(this);

		myPrefs = Preferences.userNodeForPackage(getClass());

		myPalWin = parent.getPalWin();

		myScanWin = parent.getScanWin();
		myScanWin.setScanCallBack(this);
		myScanWin.setCandidatesCallBack(this);
		
		myFindWin = parent.getFindWin();
		myFindWin.setFindCallBack(this);	

		myJointWin = parent.getJointWin();
		myJointWin.setJointCallBack(this);
		myJointWin.setFindJointCallBack(myFindWin);
		
		myDisplayWin = parent.getDisplayWin();
		myDisplayWin.setJointWindowInterface(myJointWin);
		
	}

	public void exit(){
		myScanWin.saveSettings(myPrefs);
		myPalWin.saveSettings(myPrefs);
		myFindWin.saveSettings(myPrefs);
		myJointWin.saveSettings(myPrefs);
	}

	/** If an image has been loaded
	* @return true if it has image. 
	*/
	public boolean hasImage(){
		return !(image==null);
	}

	/** Set a new image on canvas*/
	public void setImage(ImageData image){
		this.image = image;
		repaint();
	}

	public void setImage(String imageName){
		if(!imageName.endsWith(".jpg") && !imageName.endsWith(".JPG"))
			imageName += ".jpg";
		this.setImage(imageName, -1);
	}

	void setImage(String imageName, int num){
		this.imageName = imageName;
		try{
			java.util.regex.Matcher matcher = pattern.matcher(this.imageName);
			try{
				if(matcher.find() && num>=0){
					int numberLength = matcher.group().length()-4;
					String zeros = "";
					for(int i=0;i<numberLength-String.valueOf(num).length();i++)
						zeros += "0";
					this.imageName = matcher.replaceAll(zeros + num + ".jpg");
				}
			}catch(Exception e){
				System.out.println("Error "+ e);
				}
			this.image = new ImageData( ImageIO.read(new File(this.imageName)));
		}catch(IOException e){
			System.out.println("File not found" + this.imageName);
		}
		if (myFindWin.getImproveMode())
			image.adjustColours();
		if (myFindWin.getDoubleScale())
			image = image.goDouble();
		imageNum = num;		
		clearFindCursors();
		cgroup.clear();
	}

	private void clearFindCursors(){
		helperDot = new Vector2D();
		findCursor = new Group();
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	/**		THIS ARE METHODS IMPLEMENTED BY THE SCAN INTERFACE*/

	/** The candidates according to statistics of the image and classes are drawn on the canvas */
	public void showCandidates(double thresProb){
		if(hasImage() && !myPalWin.isSelectionEmpty()){
			Group temp = getDots(thresProb);
			if (!temp.isEmpty())
				groups.add(temp);
			repaint();
		}
	}

	/** Performe the region scan of the given square section */
	public void regionScan(){
		myPalWin.undoFromHere();
		if (hasImage()){
			if( square.isValid() && !myPalWin.isSelectionEmpty() )
				regionScan(square);
			else if( circle.isValid() && !myPalWin.isSelectionEmpty() )
				regionScan(circle);
			else if ( !growingDot.isEmpty() && !myPalWin.isSelectionEmpty() ){
				groups.add( growRegion(growingDot) );
				growingDot = new Vector2D(-1,-1);
			}
		}
		repaint();
	}

	/** Erare figures, groups, current drawing and square region*/
	public void eraseTrack(){
		figures.clear();
		groups.clear();
		myJointWin.deleteAll();
		tempGroup.clear();
		findCursor.clear();
		cgroup.clear();
		square = new Square();
		circle = new Circle();
		clearFindCursors();
		repaint();
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------


	public void addManual(){
		if( !findCursor.isEmpty() ){
			myJointWin.addNewJoints(findCursor);
			clearFindCursors();
		}
		repaint();
	}

	/**		THIS ARE METHODS IMPLEMENTED BY THE JOINT INTERFACE*/
	public boolean adjustJoint(Joint joint){
		if (!findCursor.isEmpty()) {
			Vector2D last = (Vector2D)findCursor.getLast();
			if (last!=null){
				joint.setCoords(last);
				clearFindCursors();
				return true;
			}
		}
		return false;
	}
	
	/**		THIS ARE METHODS IMPLEMENTED BY THE FIND INTERFACE*/

	/** all points belonging to the class on edition are gathered in a group. Then this are grouped according
	* to their relative position close to eachother.
	*/
	public int getRegionGroups(boolean onlyWherePrevious){
		int question = JointSequenceManager.OK;
		int status = JointSequenceManager.STOP_ALL;
		if(hasImage()){
			Group temp = new Group();
			JointTimeInstance previous = myJointWin.getPrevious();

			// If scan is performed only within certain region, cgroup or startingCMasses should not be empty
			if(onlyWherePrevious && (!previous.isEmpty())){
				Vector2D vec;
				int scanSize = myFindWin.getScanSize();
				Iterator it = previous.iterator();
				while(it.hasNext()){
					vec = (Vector2D)it.next();
					temp.addAll(getDots((int)vec.getX()-scanSize, (int)vec.getX()+scanSize, 
																(int)vec.getY()-scanSize, (int)vec.getY()+scanSize , myScanWin.getProbThreshold() ));
				}
			}else{
				// if there is no previous information, scan is done on hte whole image
				question = JOptionPane.showConfirmDialog(this, "Scan the entire image is a slow process\n Do you still want to proceed", "Warning", JOptionPane.YES_NO_OPTION);
				if (question == JOptionPane.YES_OPTION ){
					temp = getDots(myScanWin.getProbThreshold());
				}else{
					return question;
				}
			}
			
			// new pixels found in image are stored in newFindings
			cgroup = new CompoundGroup(temp, myFindWin.getMinSizeGroup());
			status = myJointWin.addNewJoints(cgroup.getCenterOfMasses());
			clearFindCursors();
			repaint();
		}
		return status;
	}

	public void visualEnable(boolean showGraphics){
		graphEnable = showGraphics;
	}
	
	public void setImage(int counter){
		if(!imageName.equals(""))
			setImage(imageName, counter);
	}

	/** remove of image*/
	public void removeImg(){
		image = null;
		repaint();
	}

	//-------------------------------------------------------
	//		GRID FUNCTIONS
	//-------------------------------------------------------

	public void setGrid(){
		if(!findCursor.isEmpty()){
			m_grid.setGridMarks( (Group) findCursor.clone());
			findCursor.clear();
			helperDot = new Vector2D();
		}
		repaint();
	}

	public void loadWeights(Matrix weights){
		m_grid = new Grid(weights);}

	public void exportGrid(String fileName){
		m_grid.export(fileName);}

	public void loadGrid(Group grid){
		m_grid.setGridMarks(grid);
		repaint();
	}

	public void saveCalibration(String fileName){
		m_grid.saveWeights(fileName);}

	public void calibrate(Group refs){
		Matrix ws = m_grid.calibrate(refs);
		if(ws.isEmpty())
			JOptionPane.showMessageDialog(this, "Number of points does not match", "Calibration Problem", JOptionPane.OK_OPTION);
		repaint();
	}

	public Vector2D transform(Vector2D vec){
		return m_grid.transform(vec);
	}

	public void displayTransform(int scale, int offset){
		myDisplayWin.setImage(m_grid.transformImage(image, scale, offset));
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	/** Scan the region inside the specified square. The colour found are located on the current colour group*/
	private void regionScan(Square sq){
		CompoundColour compoundC = myPalWin.getSelectedColour();
		if(compoundC != null){
			for (int i=sq.getX();i<sq.getX2();i++){
				for(int j=sq.getY();j<sq.getY2();j++)
					compoundC.add(image.getColour(i,j));
			}
		}
		myPalWin.updateStats();
	}

	private void regionScan(Circle c){
		CompoundColour compoundC = myPalWin.getSelectedColour();
		if(compoundC != null){
			for (int i=c.getX();i<c.getX2();i++){
				for(int j=c.getY();j<c.getY2();j++){
					if(c.isIn(new Vector2D(i,j)))
						compoundC.add(image.getColour(i,j));
				}
			}
		}
		myPalWin.updateStats();
	}

	/** Scan the region starting at the given position. 
	* Colours are added if close enough in distance and colour (thresDist)
	*/
	private Group growRegion(Vector2D vector){
		Group result = new Group();
		if(!myPalWin.isSelectionEmpty()){
			boolean something = false;
			int width = image.getWidth(), height = image.getHeight();
			int [][] tv = new int[width+2][height+2];
			CompoundColour compoundC = myPalWin.getSelectedColour();
			if(compoundC != null){
				Colour colour = image.getColour((int)vector.getX(),(int)vector.getY());
				tv[(int)vector.getX()+1][(int)vector.getY()+1] = 1;
				do{
					something = false; 
					for(int i=1;i<width+1;i++){
						for(int j=1;j<height+1;j++){	
							if (tv[i][j] == 1 ){
								if(colour.distanceTo(image.getColour(i-1,j-1)) < myScanWin.getGrowThreshold()  ){
									tv[i][j] = 2;
									vector = new Vector2D(i-1, j-1);
									result.add(vector);
									compoundC.add(image.getColour((int)vector.getX(),(int)vector.getY()));
									for(int m=-1;m<=1;m++){	
										for(int n=-1;n<=1;n++){
											if(tv[i+m][j+n] == 0){
												tv[i+m][j+n] = 1;
												something = true;
											}
										}
									}
								}else
									tv[i][j] = 3;
							}
						}
					}
				}while(something);
			}
		}
		myPalWin.updateStats();
		return result;
	}

	/** Returns the coordinates of the pixels that contain a colour with in the probabilistic limit of the 
	* specified color group. This according to the current covariances, means and number of elements in each class.
	* On name error returns an empty group.
	* @return {@link Group} containing coordinates of the pixels found
	*/
	private Group getDots(double thresProb){
		return getDots( 0, image.getWidth(), 0, image.getHeight(), thresProb);
	}

	/** Returns the coordinates of the pixels that contain a colour with in the probabilistic limit of the 
	* specified color group. This according to the current covariances, means and number of elements in each class.
	* On index error returns an empty group. Scan is performed only in the specified zone
	*/
	private Group getDots(int minX, int maxX, int minY, int maxY, double thresProb){
		if(!myPalWin.isSelectionEmpty()){
			Group group = new Group();
			for(int i=minX;i<maxX;i++){
				for(int j=minY;j<maxY;j++){
					if(image.insideBounds(i,j)){

						if(myScanWin.getFindMode() == ScanWindow.COLOUR_ONEDITION_HIGHLY_PROB){
							if( myPalWin.getPalette().probabilisticOnEdition(image.getColour(i,j)) > thresProb)
								group.add(new Vector2D(i,j));

						}else if(myScanWin.getFindMode() == ScanWindow.MOST_PROBABLE_COLOUR){
							if( myPalWin.getPalette().moreProbable(image.getColour(i,j)).equals(myPalWin.getPalette().getOnEdition()) )
								group.add(new Vector2D(i,j));
						}

					}
				}
			}
			return group;
		}
		return new Group();
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	//------------------------------------------MOUSE EVENTS-------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	private void setFindWindowCoords(int x, int y){
		Vector2D temp = transform(new Vector2D(x,y));
		double x2 = ((double)( (int)(temp.getX()*100) ))/100;
		double y2 = ((double)( (int)(temp.getY()*100) ))/100;
		if(x2 == -1 && y2 == -1){
			x2 = Double.NaN;
			y2= x2;
		}
		myFindWin.setCoords(x, y, x2, y2);
	}

	/** Function called when the mouse button is pressed down*/
	public void mousePressed(MouseEvent e){
		setFindWindowCoords(e.getX(), e.getY());
		Vector2D vector = new Vector2D(e.getX(),e.getY());
		this.readMouse = true;
		circle = new Circle();
		square = new Square();
		growingDot = new Vector2D();
		helperDot = new Vector2D();
	
		if (myFindWin.getFindMode()){
			this.mouseBehaviour = MOUSE_FINDING;
			helperDot = vector;
		}
		else if(false){
			this.mouseBehaviour = MOUSE_SCANING;
			this.tempGroup = new Group(vector);
		}
		else {
			this.mouseBehaviour = MOUSE_SCANING;
			if (myScanWin.getScanMode() == ScanWindow.GROW_REGION_MODE)
				growingDot = vector;
			if (myScanWin.getScanMode() == ScanWindow.CIRCLE_REGION_MODE)
				circle.setCentre(vector);
			if (myScanWin.getScanMode() == ScanWindow.SQUARE_REGION_MODE)
				square.setCorn1(vector);
		}
		repaint();
	}
	/** function called while dragging the mouse*/
	public void mouseDragged(MouseEvent e){
		setFindWindowCoords(e.getX(), e.getY());
		if(this.readMouse){
			Graphics g = getGraphics();
			int x = e.getX(), y =e.getY();
			Vector2D vector = new Vector2D(x,y);

			switch (mouseBehaviour) {
				case MOUSE_SCANING:
					if (myScanWin.getScanMode() == ScanWindow.GROW_REGION_MODE)
							growingDot = vector ;
					if (myScanWin.getScanMode() == ScanWindow.CIRCLE_REGION_MODE)
							circle.setRad(vector);
					if (myScanWin.getScanMode() == ScanWindow.SQUARE_REGION_MODE)
							square.setCorn2(vector);
					break;
				case MOUSE_FINDING:
					if( myFindWin.getFindMode()  )
						helperDot = vector;
					break;
				case MOUSE_PAINTING:
					g.fillOval(x,y,1,1);
					tempGroup.add(vector);
					break;
			}
			repaint();
		}
	}
	/** function called on the release of the mouse button*/
	public void mouseReleased(MouseEvent e){
		setFindWindowCoords(e.getX(), e.getY());
		switch (mouseBehaviour){
		case MOUSE_SCANING:
			if (myScanWin.getScanMode() == ScanWindow.CIRCLE_REGION_MODE)
				circle.setRad(new Vector2D(e.getX(), e.getY()));
			if (myScanWin.getScanMode() == ScanWindow.SQUARE_REGION_MODE)
				square.setCorn2(new Vector2D(e.getX(), e.getY()));
			break;
		case MOUSE_FINDING:
			if( myFindWin.getFindMode() )
				findCursor.add( helperDot.copy() );
				helperDot = new Vector2D();
			break;
		case MOUSE_PAINTING:
			if(this.tempGroup.size()>1){
				Vector2D vector = new Vector2D(e.getX(),e.getY());
				tempGroup.add(vector);
				figures.add(this.tempGroup);
			}
			break;
		}
		repaint();
		this.readMouse = false;
	}
	
	/** Function called on mouse clicked*/
	public void mouseClicked(MouseEvent e){
		setFindWindowCoords(e.getX(), e.getY());
		if (mouseBehaviour == MOUSE_SCANING && myScanWin.getScanMode() == ClassifierInterface.GROW_REGION_MODE && hasImage())
			growingDot = new Vector2D(e.getX(),e.getY());
		else if (mouseBehaviour == MOUSE_FINDING && hasImage())
			helperDot = new Vector2D(e.getX(),e.getY());
		else{
			Graphics g = getGraphics();
			g.setColor(Color.red);
			g.fillOval(e.getX()-2,e.getY()-2,5,5);
		}
		repaint();
	}

	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	//------------------------------------------PAINT EVENTS-------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------

	public void paintComponent(Graphics g){
		myDisplayWin.repaint();
		myJointWin.setTimeStamp(myFindWin.getCounter());
		if (graphEnable){
			if(hasImage()){
				if (myFindWin.getCounter() != imageNum)
					setImage(myFindWin.getCounter());
				image.refresh();
				g.drawImage(image, 0, 0, this);
			}
	
			g.setColor(Color.orange);
			switch (mouseBehaviour){
			case MOUSE_SCANING:
				switch (myScanWin.getScanMode()){
					case ClassifierInterface.GROW_REGION_MODE:
						if(!growingDot.isEmpty())
							PaintStuff.paintCursor(g, PaintStuff.C_PLUS, growingDot);
					break;
					case ClassifierInterface.CIRCLE_REGION_MODE:
						if (circle.isValid())
							circle.paintCircle(g);
					break;
					case ClassifierInterface.SQUARE_REGION_MODE:
						if(square.isValid())
							square.paintSquare(g);
					break;
				}
			break;
			case MOUSE_FINDING:
				if(!helperDot.isEmpty()){
					g.setColor(Color.MAGENTA);
					PaintStuff.paintCursor(g, PaintStuff.C_CIRCLE_INNERDOT, helperDot);
				}
			break;
			case MOUSE_PAINTING:
				PaintStuff.paintList(tempGroup, g, Color.BLACK, PaintStuff.C_NORMAL);
			break;
			}
			
			if(!cgroup.isEmpty()){
				double[][] coords;
				Group pGroup;
				int rad;
				Iterator it = this.cgroup.iterator();
				while(it.hasNext()){
					pGroup = (Group)it.next();
					if(pGroup.isValid(myFindWin.getMinSizeGroup())){
						double col[] = myPalWin.getSelectedColour().getMean();				//colour found displayed as its negative  mean
						g.setColor(new Color(255 - (int)col[0], 255 - (int)col[1], 255 - (int)col[2]));
						coords = pGroup.getCoords();
						for(int i=0;i<pGroup.size();i++)
							g.fillOval((int)coords[0][i],(int)coords[1][i],1,1);
						rad = (int)pGroup.iRad();									//imaginary circle
						g.setColor(Color.white);
						g.drawOval((int)(pGroup.centerOfMass().getX())-rad,(int)(pGroup.centerOfMass().getY())-rad,2*rad,2*rad);
					}else{
						g.setColor(Color.yellow);										//groups not considered as circles
						g.fillOval((int)(pGroup.centerOfMass().getX()),(int)(pGroup.centerOfMass().getY()),1,1);
					}
				}
			}
	
			PaintStuff.paintList(findCursor, g, Color.orange, PaintStuff.C_DOUBLE_CIRCLE_INNERDOT);

			if(m_grid.hasMarks())
				PaintStuff.paintList(m_grid.getMarks(), g, Color.red, PaintStuff.C_PLUS);
	
			PaintStuff.paintCGroup(groups,  g, Color.red);
			PaintStuff.paintCGroup(figures,  g, Color.green);
			PaintStuff.paintJointManager(g, myJointWin.getJointManager(), myJointWin.getSelectedJointSequence(), 
					myJointWin.getFrom(), myFindWin.getCounter(), myJointWin.getTo());
		}
	}
	
	
	public void keyTyped(KeyEvent e){}
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){}	
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseMoved(MouseEvent e){
		setFindWindowCoords(e.getX(), e.getY());
	}
	
	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	//------------------------------------------SUPPPORT INNER CLASSES-------------------------------------
	//----------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------


	//START OF SQUARE CLASS
	class Square {
		Vector2D corner1;
		Vector2D corner2;

		/** Contructor of Sqaure, size zero*/
		Square(){
			corner1 = new Vector2D();
			corner2 = new Vector2D();
		}
		/** Specify first corner*/
		 void setCorn1(Vector2D cor1){
			corner1 = cor1.copy();
		}
		/** Specify second corner*/
		 void setCorn2(Vector2D cor2){
			corner2 = cor2.copy();
		}

		boolean isValid(){
			return (getX()>0 && getY()>0 && getWidth()>0 &&getHeight()>0 && image.insideBounds(getX2(),getY2()));
		}

		void paintSquare(Graphics g){
			g.drawRect(getX(), getY(), getWidth(), getHeight());
		}
	
		/** Return X component of the most left and upper corner*/
		 int getX(){
			if (corner1.getX()<=corner2.getX())
				return (int)corner1.getX();
			return (int)corner2.getX();
		}
		/** Return Y component of the most left and upper corner*/
		 int getY(){
			if (corner1.getY()<=corner2.getY())
				return (int)corner1.getY();
			return (int)corner2.getY();
		}
		/** Return X component of the most right and lower corner*/
		 int getX2(){
			if (corner1.getX()>=corner2.getX())
				return (int)corner1.getX();
			return (int)corner2.getX();
		}
		/** Return Y component of the most right and lower corner*/
		 int getY2(){
			if (corner1.getY()>=corner2.getY())
				return (int)corner1.getY();
			return (int)corner2.getY();
		}
		/** Width of this*/
		 int getWidth(){
			return Math.abs((int)corner2.getX() - (int)corner1.getX());
		}
		/** Height of this*/
		 int getHeight(){
			return Math.abs((int)corner2.getY() - (int)corner1.getY());
		}
	}//END OF SQUARE CLASS

	//START OF CIRCLE CLASS
	class Circle{
		Vector2D centre;
		double radius;

		/** Contructor of Sqaure, size zero*/
		Circle(){
			centre = new Vector2D();
			radius = 0.0;
		}

		/** Specify first corner*/
		 void setCentre(Vector2D cent){
			centre = cent.copy();
		}
		/** Specify second corner*/
		 void setRad(Vector2D vec2){
			radius = vec2.sub(centre).getMagnitud();
		}
		void setRad(double rad){
			radius = rad;
		}
		boolean isValid(){
			return (getX()>0 && getY()>0 && radius>0 && image.insideBounds(getX2(),getY2()));
		}
		boolean isIn(Vector2D vec2){
			return ( radius > vec2.sub(centre).getMagnitud() );
		}
		/** Return X component of the most left and upper corner*/
		 int getX(){
			return (int)(centre.getX()-radius);
		}

		void paintCircle(Graphics g){
			g.drawOval(getX(), getY(), getWidth(), getHeight());
		}

		/** Return Y component of the most left and upper corner*/
		 int getY(){
			return (int)(centre.getY()-radius);
		}
		/** Return X component of the most right and lower corner*/
		 int getX2(){
			return (int)(centre.getX()+radius);
		}
		/** Return Y component of the most right and lower corner*/
		 int getY2(){
			return (int)(centre.getY()+radius);
		}
		/** Width of this*/
		 int getWidth(){
			return (int)(2*radius);
		}
		/** Height of this*/
		 int getHeight(){
			return getWidth();
		}
	}//END OF CIRCLE CLASS
}
