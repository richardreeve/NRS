package nrs.oscilloscope.oscilloscopeGUI;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class GraphicalScreen extends JPanel {
    private static final long serialVersionUID = 8L;
    protected static final double INIT_X = 0.5;
    protected static final double INIT_Y = 50;
    
    protected NGraph _memoryGraph;
    protected JGraph _javaGraph;
    
    private double [] _zoom;
    
    private Color _bgColor;
    protected Color _drawingColor;
    private Dimension _dimension;
    
    public GraphicalScreen (Dimension d) {
	super(true);
	
	_dimension = d;
	_memoryGraph = new NGraph (new Point2D.Double (0,0),
				   INIT_X,INIT_Y);
	_javaGraph = new JGraph (_dimension);
	_zoom = new double [2];
	computeZoom();
	
	_bgColor = Color.BLACK;
	_drawingColor = Color.CYAN;	
	
	
	this.setVisible(true);
    }
    
    private synchronized void computeZoom () {
    	_zoom[0] = _javaGraph.getWidth() / _memoryGraph.getXAxisSize();
    	_zoom[1] = _javaGraph.getHeight() / _memoryGraph.getYAxisSize();
    }
    private synchronized double translateX (double x) {
    	return (x - _memoryGraph.getMinX());
    }
    
    private synchronized double translateY (double y) {
    	return (y - _memoryGraph.getMinY());
    }
    
    private synchronized int convertX (double x) {
    	int pX = (int) (x * _zoom[0]);
    	return pX;
    }
    
    private synchronized int convertY (double y) {
    	int pY = (int) (_javaGraph.getHeight() - (y * _zoom[1]));
    	return pY;
    }
    
    private synchronized Point convert (double x, double y) {
    	return new Point (convertX (x),convertY (y));
    }

    private synchronized void computeAll () {
    	computeZoom();
    	_javaGraph.empty();
    	Point2D.Double p;
    	for (Iterator iter = _memoryGraph.getPoints().iterator(); 
	     iter.hasNext();) {
	    p = (Point2D.Double) iter.next ();
	    _javaGraph.add(convert(translateX(p.getX()),translateY(p.getY())));
    	}	
    }


    //-------------------------------------------------------------------
    
    public synchronized double getMaxX () {
    	return _memoryGraph.getMaxX();
    }
    
    public synchronized double getMinX () {
	return _memoryGraph.getMinX();
    }
    
    public synchronized double getMaxY () {
    	return _memoryGraph.getMaxY();
    }
    
    public synchronized double getMinY () {
	return _memoryGraph.getMinY();
    }
    
    public synchronized double getXAxisSize() {
    	return _memoryGraph.getXAxisSize();
    }
    
    public synchronized void setMaxX (double maxX) {
    	_memoryGraph.setMaxX(maxX);
    }
    
    public synchronized void setMinX (double minX) {
    	_memoryGraph.setMinX(minX);
    }
    
    public synchronized void setMaxY (double maxY) {
    	_memoryGraph.setMaxY(maxY);
    }
    
    public synchronized void setMinY (double minY) {
    	_memoryGraph.setMinY(minY);
    }
    
    //--------------------------------------------------------------------

    public synchronized void shiftX (double x) {
    	_memoryGraph.shiftX (x);
    	int tmp = convertX (x);
    	_javaGraph.shiftX (tmp);
    }

    public synchronized void autoResizeGraph () {
	//double maxX = _memoryGraph.getMaxPointX();
	//double minX = _memoryGraph.getMinPointX();
	double maxY = _memoryGraph.getMaxPointY();
	double minY = _memoryGraph.getMinPointY();

	//double xLength = maxX - minX;
	double yLength = maxY - minY;

	//_memoryGraph.setMaxX(maxX);
	//_memoryGraph.setMinX(minX);
	_memoryGraph.setMaxY(maxY+0.1*yLength);
	_memoryGraph.setMinY(minY-0.1*yLength);
	computeAll();
    }
    
    public synchronized void addPoint (double x, double y) {
    	_memoryGraph.add(new Point2D.Double (x,y));
    	_javaGraph.add(convert (translateX(x),translateY(y)));
    }


    /*
     * painting tools
     */
     
    protected synchronized boolean resizeManager () {
	Dimension d = this.getSize ();
	if ((_dimension.getWidth() != d.getWidth()) 
	    || (_dimension.getHeight() != d.getHeight())) {
	    _dimension = d;
	    _javaGraph.setDimension (_dimension);
	    computeAll ();
	    return true;
		}
		return false;
    }
    
    public synchronized void paint (Graphics g) {
	resizeManager();
	g.setColor(_bgColor);
	g.fillRect (0,0,getWidth(),getHeight());
	draw(g);
    }
    
    protected abstract void draw (Graphics g); 
}
