package nrs.oscilloscope.oscilloscopeGUI;

import java.util.LinkedList;
import java.util.Iterator;
import java.awt.geom.Point2D;

public class NGraph {
    private LinkedList <Point2D.Double> _pointList;
    
    private double [] _graphSize;
    private Point2D.Double _origin;
    private double _maxX, _maxY;   

    public NGraph (Point2D.Double origin, double maxX, double maxY) {
	_pointList = new LinkedList <Point2D.Double> ();
	_origin = origin;
	_maxX = maxX;
	_maxY = maxY;
	_graphSize = new double [2];
	_graphSize[0] = _maxX - _origin.getX();
	_graphSize[1] = _maxY - _origin.getY();
	
    }

    //-------------------------------------------------------------
  
    private double findMinXGraph () {
	switch (_pointList.size()) {
	case 0: return 0.0;
	case 1: 
	    return _pointList.getFirst().getX();
	default:
	    double tmp = _pointList.getFirst().getX();
	    double newTmpX;
	    for (Iterator iter = _pointList.iterator();iter.hasNext();) {
		newTmpX = ((Point2D.Double)iter.next()).getX();
		if (newTmpX < tmp) tmp = newTmpX;
	    }
	    return tmp;
	}
    }    

    private double findMaxXGraph() {
	switch (_pointList.size()) {
	case 0: return 0.0;
	case 1: 
	    return _pointList.getFirst().getX();
	default:
	    double tmp = _pointList.getFirst().getX();
	    double newTmpX;
	    for (Iterator iter = _pointList.iterator();iter.hasNext();) {
		newTmpX = ((Point2D.Double)iter.next()).getX();
		if (newTmpX > tmp) tmp = newTmpX;
	    }
	    return tmp;
	}
    }    

    private double findMinYGraph () {
	switch (_pointList.size()) {
	case 0: return 0.0;
	case 1: 
	    return _pointList.getFirst().getY();
	default:
	    double tmp = _pointList.getFirst().getY();
	    double newTmpY;
	    for (Iterator iter = _pointList.iterator();iter.hasNext();) {
		newTmpY = ((Point2D.Double)iter.next()).getY();
		if (newTmpY < tmp) tmp = newTmpY;
	    }
	    return tmp;
	}
    }    

    private double findMaxYGraph() {
	switch (_pointList.size()) {
	case 0: return 0.0;
	case 1: 
	    return _pointList.getFirst().getY();
	default:
	    double tmp = _pointList.getFirst().getY();
	    double newTmpY;
	    for (Iterator iter = _pointList.iterator();iter.hasNext();) {
		newTmpY = ((Point2D.Double)iter.next()).getY();
		if (newTmpY > tmp) tmp = newTmpY;
	    }
	    return tmp;
	}
    }    

    //---------------------------------------------------
    
    public synchronized void shiftX (double x) {
	_maxX += x;
    	_origin.setLocation(_origin.getX()+x,_origin.getY());
	if (_pointList.size() > 0) {
	    if (_pointList.getFirst().getX() <= _origin.getX()) {
		_pointList.removeFirst();
	    }
	}
    }
    //------------------------------------------------------
  
    public synchronized double getMaxX () {
    	return _maxX;
    }
     public synchronized double getMinX () {
    	return _origin.getX();
    }

    public synchronized double getMaxY () {
    	return _maxY;
    }
     public synchronized double getMinY () {
	 return _origin.getY();
    }

    public synchronized LinkedList getPoints () {
    	return _pointList;
    }
    
    public synchronized double getXAxisSize() {
    	return _graphSize[0];
    }
    
    public synchronized double getYAxisSize() {
    	return _graphSize[1];
    }

    public double getMaxPointX() {
	return findMaxXGraph();
    }

    public double getMinPointX() {
	return findMinXGraph ();
    }    

    public double getMaxPointY() {
	return findMaxYGraph();
    }

    public double getMinPointY() {
	return findMinYGraph ();
    }    

    //----------------------------------------------------

    public void setMaxX (double x) {
	_maxX = x;
	_graphSize[0] = _maxX - _origin.getX();
    }

    public void setMinX (double x) {
	_origin.setLocation(x,_origin.getY());
	_graphSize[0] = _maxX - _origin.getX();
    }

    public void setMaxY (double y) {
	_maxY = y;
	_graphSize[1] = _maxY - _origin.getY();
    }

    public void setMinY (double y) {
	_origin.setLocation(_origin.getX(),y);
	_graphSize[1] = _maxY - _origin.getY();
    }

    //----------------------------------------------------
    public synchronized void add (Point2D.Double p) {
	_pointList.add(p);
    }
}
