package nrs.oscilloscope.oscilloscopeGUI;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Dimension;
import java.util.Iterator;

public class AnalogueScreen extends GraphicalScreen {


    public AnalogueScreen (Dimension d) {
	super (d);
    }

    protected synchronized void draw (Graphics g) {
	Point p1, p2;
    	//drawAxis (g);
    	Iterator iter = _javaGraph.getPoints().iterator();
    	if (iter.hasNext ()) {
	    p1 = (Point) iter.next ();
    	} else return;
    	g.setColor (_drawingColor);
    	while (iter.hasNext ()) {
	    p2 = (Point) iter.next ();
	    g.drawLine ((int)p1.getX(),(int)p1.getY(),
			(int)p2.getX(),(int)p2.getY());
	    p1 = p2;
    	}	
    }
}
