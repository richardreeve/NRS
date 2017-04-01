package nrs.oscilloscope.oscilloscopeGUI;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Dimension;
import java.util.Iterator;

public class DigitalScreen extends GraphicalScreen {
	private static final long serialVersionUID = 2L;
	
    protected static final double INIT_X = 0.5;
    protected static final double INIT_Y = 4;

    private int _minY = 10;
    private int _maxY = 30;

    public DigitalScreen (Dimension d) {
	super (d);
    }

    private synchronized void drawSpike (Graphics g, int t) {
	g.drawLine (t,_minY,t,_maxY);
    }

    protected synchronized void draw (Graphics g) {
	Point p;
    	//drawAxis (g);
    	Iterator iter = _javaGraph.getPoints().iterator();
    	g.setColor (_drawingColor);
    	while (iter.hasNext ()) {
	    p = (Point) iter.next ();
	    drawSpike (g,(int)p.getX());
    	}
    }
    
    protected synchronized boolean resizeManager () {
    	if (super.resizeManager()) {
    		_maxY = (int) (this.getHeight() - 0.1*this.getHeight());
    		_minY = (int) (0.1*this.getHeight());
    		return true;
    	}
    	return false;
    }
}
