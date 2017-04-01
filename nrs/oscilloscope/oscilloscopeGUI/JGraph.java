package nrs.oscilloscope.oscilloscopeGUI;

import java.util.LinkedList;
import java.util.Iterator;
import java.awt.Point;
import java.awt.Dimension;

public class JGraph {
	private LinkedList <Point> _pointList;
	private Dimension _graphSize;
	
	public JGraph (Dimension d) {
		_pointList = new LinkedList <Point> ();
		_graphSize = d;
		
	}
	
	public synchronized void shiftX (int x) {
    	Point p;
    	for (Iterator iter = _pointList.iterator(); iter.hasNext();) {
    		p = (Point) iter.next ();
    		p.setLocation(p.getX()-x,p.getY());
    	}
	if (_pointList.size() > 0) {	
	    if (_pointList.getFirst().getX() <= 0) {
    		_pointList.removeFirst();
	    }
	}
    }
    
    
    public synchronized void empty () {
    	_pointList.clear();
    }
    
    public synchronized void setDimension (Dimension d) {
    	_graphSize = d;
    }
    
	public synchronized LinkedList getPoints () {
    	return _pointList;
    }
    
    public synchronized double getWidth() {
    	return _graphSize.getWidth();
    }
    
    public synchronized double getHeight() {
    	return _graphSize.getHeight();
    }
    
	public synchronized void add (Point p) {
    	_pointList.add(p);
    }
	
}
