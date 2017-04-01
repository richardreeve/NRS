/*
 * Copyright (C) 2004 Ben Torben-Nielsen
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.audio.simulator ;

import nrs.audio.simulator.* ;
import java.io.* ;
import java.util.* ;
import java.text.* ;
import java.awt.* ;
import java.awt.event.*;
import java.awt.geom.* ;
import java.awt.image.* ;
import javax.swing.border.* ;
import javax.swing.* ;
import javax.imageio.*;
import java.io.* ;
import org.w3c.dom.*;
import org.apache.xerces.dom.* ;
import org.apache.batik.svggen.SVGGraphics2D;

public class DSEarComponent extends JPanel implements Serializable
{
    private static final int RAD = 5 ;
    private static final int UNIT_F = 40 ; 
    private static final int C_UNIT = 2 ; // the Change unit
    private static final int DOUBLE_CLICK_INTERVAL = 350; // millis in long
    private static final int TIMEOUT_INTERVAL = 2000; // millis in long
    
    public static final int LEFT 	= 1 ;		
    public static final int RIGHT 	= 666 ;	
    public static final int BOTH 	= -3 ;		
    
    private Object parent = null;
    private static JPanel panel ;
    
    private Graphics2D 	g2 ;
    
    private Graphics2D picG2 ;
    private static BufferedImage image ;	
    
    private double 	width, 
	height ;
    private Point2D.Double 	center,	// the center of the map
	cor,	// the centre of rotation
	isS,	// store the Standard position
	csS,
	itS,
	ctS ;
    private Line2D.Double	xax,
	yax ;
    private Rectangle2D.Double white ;
    
    private double	xunit,
	yunit ;
    
    // keep track of the chances from the normal positions
    private double isx = 0, isy = 0, csx = 0, csy = 0, itx = 0, ity = 0, ctx = 0, cty = 0 ;
    
    private boolean eMode = true ;
    
    private Ellipse2D.Double 	isC,
	csC,
	itC,
	ctC;
    
    private boolean dragging ;
    //private Ellipse2D.Double draggedObject ;
    private int draggedObject ;
    private static final int 	DIS = 1,
	DIT = 2,
	DCS = 3,
	DCT = 4 ;
    private int corParent = -1 ;
    
    private boolean firstClick = true ;
    private Chrono chrono ;
    
    private JPopupMenu popup ;
    private JCheckBoxMenuItem spirItem ;
    private JCheckBoxMenuItem memItem ;
    private boolean lock = false ;
    private boolean membrane = false ;
    
    private int lockd1, lockd2 ;
    private double d1, d2 ;
    private boolean firstLock = true;
    
    private JLabel 	isitLabel,
	csitLabel,
	csctLabel,
	isctLabel ;
    
    private DSEarComponent ec ;
    
    public DSEarComponent(Object parent)
    {
	super() ;
	this.parent = parent ;
	panel = this ;
	chrono = new Chrono() ;
	ec = this ;
	
	// set default ear details, Michelsen 1994
	gain1 = 1 ;
	gain2 = 1.5 ;
	gain3 = 0.44 ;
	gain4 = 1 ;
	gain5 = 1.5 ;
	gain6 = 0.44 ;
	delay1 = 0 ;
	delay2 = 95;
	delay3 = 128;
	delay4 = 0 ;
	delay5 = 95;
	delay6 = 128;
	
	makePopup() ;
	
	MyMouseListener mlistener = new MyMouseListener() ;
	MyKeyListener klistener = new MyKeyListener() ;
	addMouseListener(mlistener) ;
	addMouseMotionListener(mlistener) ;
	addKeyListener(klistener) ;
	
	setBorder(new TitledBorder(new EtchedBorder(),"Ear Configuration")) ;
	setToolTipText("The ear configuration used in the simulation.\nChange options in the \"ears\" menu");
    }
    
    /**
     * Yak, THIS IS DIRTY PROGRAMMING (my style!)
     */
    public void setLabels(JLabel isitLabel, JLabel csitLabel, JLabel csctLabel, JLabel isctLabel)
    {
	this.isitLabel = isitLabel ;
	this.csitLabel = csitLabel ;	
	this.csctLabel = csctLabel ;
	this.isctLabel = isctLabel ;
    }
    
    private class MyKeyListener extends KeyAdapter
    {
	public void keyPressed(KeyEvent ke)
	{
	    int kc = ke.getKeyCode() ;
	    
	    if(kc == KeyEvent.VK_DOWN || kc == KeyEvent.VK_KP_DOWN)
		{
		    System.out.println("DOWN button pressed") ;	
		}
	    
	    if(kc == KeyEvent.VK_UP || kc == KeyEvent.VK_KP_UP)
		{
		    System.out.println("UP button pressed") ;	
		}
	    
	    if(kc == KeyEvent.VK_LEFT || kc == KeyEvent.VK_KP_LEFT)
		{
		    System.out.println("LEFT button pressed") ;	
		    
		    if(lock)
			{
			    if(draggedObject == DIT)
				{
				    System.out.println("links, DIT");	
				    int cx = (int)itS.getX() ;
				    int cy = (int)itS.getY() ;
				    
				    double adj = itC.getCenterX() - isC.getCenterX() ;
				    double opp = itC.getCenterY() - isC.getCenterY() ;
				    double hyp =  d1 ;//new Point2D.Double(isC.getCenterX(), isC.getCenterY()).distance(itC.getCenterX(), itC.getCenterY()) ;
				    
				    System.out.println("DEEL I  || hyp: " + hyp + ", opp: " + opp + ", adj: " + adj) ;
				    
				    double alpha = Math.atan2( adj, opp) ;
				    
				    System.out.println("alpha: " +alpha) ;
				    
				    adj = Math.cos(alpha-Math.toRadians(10)) * hyp ;
				    opp = Math.sin(alpha-Math.toRadians(10)) * hyp ;
				    
				    Point2D.Double nP = new Point2D.Double(isC.getCenterX() + (adj), isC.getCenterY() + (opp)) ;
				    
				    System.out.println("DEEL II || hyp: " + hyp + ", opp: " + opp + ", adj: " + adj) ;
				    
				    double nx = nP.getX() ;
				    double ny = nP.getY() ;
				    
				    double xa = (cx - nx) ; // X Adjustment
				    double ya = (cy - ny); // Y Adjustment
				    
				    System.out.println("xa: " +xa) ;
				    System.out.println("ya: " +ya) ;
				    
				    itx = Math.round(xa) /xunit ;
				    ity = Math.round(ya) /yunit ;
				    
				    System.out.println("itx: " +itx) ;
				    System.out.println("ity: " +ity) ;
				    
				    panel.validate() ;
				    ((JFrame)parent).validate() ;
				    ((JFrame)parent).repaint() ;
				    
				    ((DSFrame)parent).runSimulation() ;					
				}
			    if(draggedObject == DCT)
				{
				    System.out.println("links, DCT");
				    
				    int cx = (int)ctS.getX() ;
				    int cy = (int)ctS.getY() ;
				    
				    double adj = ctC.getCenterX() - csC.getCenterX() ;
				    double opp = ctC.getCenterY() - csC.getCenterY() ;
				    double hyp =  d1 ;//new Point2D.Double(isC.getCenterX(), isC.getCenterY()).distance(itC.getCenterX(), itC.getCenterY()) ;
				    
				    double alpha = Math.atan2( adj, opp) ;
				    
				    adj = Math.cos(alpha-Math.toRadians(10)) * hyp ;
				    opp = Math.sin(alpha-Math.toRadians(10)) * hyp ;
				    
				    Point2D.Double nP = new Point2D.Double(csC.getCenterX() + (adj), csC.getCenterY() + (opp)) ;
				    
				    double nx = nP.getX() ;
				    double ny = nP.getY() ;
				    
				    double xa = (cx - nx) ; // X Adjustment
				    double ya = (cy - ny); // Y Adjustment
				    
				    ctx = Math.round(xa) /xunit ;
				    cty = Math.round(ya) /yunit ;
				    
				    panel.validate() ;
				    ((JFrame)parent).validate() ;
				    ((JFrame)parent).repaint() ;
				    
				    ((DSFrame)parent).runSimulation() ;	
				    
				}	
			}
		}
	    
	    if(kc == KeyEvent.VK_RIGHT || kc == KeyEvent.VK_KP_RIGHT)
		{
		    System.out.println("RIGHT button pressed") ;
		    
		    if(lock)
			{
			    if(draggedObject == DIT)
				{
				    System.out.println("right, DIT");	
				}
			    if(draggedObject == DCT)
				{
				    System.out.println("rechts, DCT");
				    
				}	
			}	
		}
	}
    }
    
    private class MyMouseListener extends MouseAdapter implements MouseMotionListener
    {
	public void mouseClicked(MouseEvent me)
	{
	    panel.requestFocusInWindow() ;		
	    
	    if(me.getButton() == MouseEvent.BUTTON3)
		{
		    showPopup(me) ;	
		    System.out.println("right button pressed") ;
		}
	    
	    if(firstClick)
		{
		    // start timer
		    firstClick = false	;
		    chrono.start() ;
		}
	    else
		{
		    // second click in a certain interval?
		    chrono.stop() ;
		    if (chrono.getMillis() > TIMEOUT_INTERVAL)
			{
			    firstClick = true;
			    chrono.start();
			}
		    else
			{
			    if(chrono.getMillis() < DOUBLE_CLICK_INTERVAL)
				{
				    // yes: do something, firstclick = true
				    setCoR(me) ;
				}
			    firstClick = true ;	
			}
		}	
	}
	
	public void mousePressed(MouseEvent me)
	{
	    if(isC.contains(me.getX(), me.getY()) && !lock)
		{
		    dragging = true ;
		    draggedObject = DIS ;		
		}
	    else if(itC.contains(me.getX(), me.getY()))
		{
		    dragging = true ;
		    draggedObject = DIT ;		
		}
	    else if(csC.contains(me.getX(), me.getY()) && !lock)
		{
		    dragging = true ;
		    draggedObject = DCS ;		
		}
	    else if(ctC.contains(me.getX(), me.getY()))
		{
		    dragging = true ;
		    draggedObject = DCT ;	
		}
	    else
		{
		    dragging = false ;
		    System.out.println("()dragging off") ;	
		}		
	}
	
	public void mouseDragged(MouseEvent me)
	{
	    if(dragging = true)
		{
		    //System.out.println("Dragging!") ;
		    
		    int nx = me.getX() ;
		    int ny = me.getY() ;
		    
		    int cx = 0, 
			cy = 0 ;
		    switch(draggedObject)
			{
			case DIS :
			    {
				cx = (int)isS.getX() ; // USE THE STANDARD CIRCLE	
				cy = (int)isS.getY() ;		
			    } ; break ;	
			case DIT :
			    {
				cx = (int)itS.getX() ; // USE THE STANDARD CIRCLE	
				cy = (int)itS.getY() ;		
			    } ; break ;	
			case DCS :
			    {
				cx = (int)csS.getX() ; // USE THE STANDARD CIRCLE	
				cy = (int)csS.getY() ;		
			    } ; break ;	
			case DCT :
			    {
				cx = (int)ctS.getX() ; // USE THE STANDARD CIRCLE	
				cy = (int)ctS.getY() ;		
			    } ; break ;	
			}
		    
		    int xa = (cx - nx) ; // X Adjustment
		    int ya = (cy - ny); // Y Adjustment
		    
		    
		    if(!lock && Math.abs(xa/xunit) < C_UNIT && Math.abs(ya/yunit) < C_UNIT)
			{
			    // update the position
			    if(draggedObject == DIS)
				{
				    isx = xa/xunit ;
				    isy = ya/yunit ;
				}
			    else if(draggedObject == DIT)
				{	
				    itx = xa/xunit ;
				    ity = ya/yunit ;
				}
			    else if(draggedObject == DCS)
				{	
				    csx = xa/xunit ;
				    csy = ya/yunit ;
				}
			    else if(draggedObject == DCT)
				{	
				    ctx = xa/xunit ;
				    cty = ya/yunit ;
				}
			}
		    else if(lock)
			{
			    int dd1 = (int)Math.round(Math.sqrt( Math.pow((isC.getCenterX() - me.getX()),2) + Math.pow((isC.getCenterY() - me.getY()),2) ) ) ;		
			    
			    if(draggedObject == DIT && (lockd1 > dd1-4) &&(lockd1 < dd1+4))
				{	
				    itx = xa/xunit ;
				    ity = ya/yunit ;
				}
			    
			    int dd2 = (int)Math.round(Math.sqrt( Math.pow((csC.getCenterX() - me.getX()),2) + Math.pow((csC.getCenterY() - me.getY()),2) ) ) ;		
			    
			    if(draggedObject == DCT && (lockd2 > dd2-4) &&(lockd2 < dd2+4))
				{	
				    ctx = xa/xunit ;
				    cty = ya/yunit ;
				}
			    
			}
		    panel.validate() ;
		    ((JFrame)parent).validate() ;
		    ((JFrame)parent).repaint() ;
		    
		    ((DSFrame)parent).runSimulation() ;
		}				
	}
	
	public void mouseMoved(MouseEvent me)
	{
	    // do nothing
	}
	
	public void mouseReleased(MouseEvent me)
	{
	    dragging = false ;
	}	
    }
    
    public static void saveImageAsPNG()
    {
	JFileChooser chooser = new JFileChooser();
	chooser.addChoosableFileFilter(new DSEarFileFilter(DSEarFileFilter.PNG)) ;
	
	int returnVal = chooser.showSaveDialog(panel);
	
	if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
		saveImageAsPNG(chooser.getSelectedFile().getName()) ;
	    }
    }
    
    public void saveImageAsSVG()
    {
	JFileChooser chooser = new JFileChooser();
	chooser.addChoosableFileFilter(new DSEarFileFilter(DSEarFileFilter.SVG)) ;
	
	int returnVal = chooser.showSaveDialog(panel);
	
	if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
		//System.out.println("save file to: " +chooser.getSelectedFile().getName()) ;
		saveImageAsSVG(chooser.getSelectedFile().getName()) ;
	    }	
    }
    
    public static void saveImageAsPNG(String fileName)
    {
	try
	    {	
		ImageIO.write( image, "png", new File( fileName ) );
	    }
	catch(Exception e)
	    {
		//System.out.println("GEEN IMAGE") ;		
	    }		
    }
    
    public void saveImageAsSVG(String fileName)
    {
	try
	    {
		Document domFactory = new DocumentImpl() ;
		SVGGraphics2D svgGenerator = new SVGGraphics2D(domFactory) ;
		this.paintComponent(svgGenerator) ;
		svgGenerator.setSVGCanvasSize(new Dimension((int)this.getBounds().getWidth(),(int)this.getBounds().getHeight())) ;
		svgGenerator.stream(fileName) ;			
	    }
	catch(Exception e)
	    {
		
	    }	
    }
    
    public void paintComponent(Graphics g)
    {
	g2 = (Graphics2D)g ;
	AntiAlias.enableFor(g2) ;
	
	// EXPERIMENTAL
	//g2.rotate(Math.toRadians(90),width/2, height/2) ;
	
	width = getBounds().getWidth() ;
	height = getBounds().getHeight() ;
	width = height = Math.min( width, height );
	center = new Point2D.Double(width / 2, (height / 4.0)*2.5) ; // center on 75% of height
	
	xunit = width / UNIT_F ;
	yunit = height / UNIT_F ;
	
	// set the Standard ear positions
	setStandards() ;
	
	xax = new Line2D.Double(new Point2D.Double(20.0, center.getY()), new Point2D.Double(width-20, center.getY())) ;
	yax = new Line2D.Double(new Point2D.Double(center.getX(), 20.0), new Point2D.Double(center.getX(), height - 20)) ;
	
	
	white = new Rectangle2D.Double(20.0,20.0,width - 40, height - 40) ;	
	
	image = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_INT_RGB) ;
	picG2 = (Graphics2D)image.createGraphics() ;
	AntiAlias.enableFor(picG2) ;
	
	// draw the background
	g2.setColor(Color.white) ;
	g2.fill(white) ;
	picG2.setColor(Color.white) ;
	Rectangle r = new Rectangle(0,0,(int)width, (int)height) ;
	picG2.fill(r) ;
	
	// draw the axes
	g2.setColor(Color.black) ;
	g2.draw(xax) ;
	g2.draw(yax) ;
	picG2.draw(xax) ;
	picG2.draw(yax) ;
	
	// only in editing mode: show boudings areas (as X-Y axis)
	if(! lock)
	    {
		// is
		Line2D.Double isLX = new Line2D.Double(isS.getX() - (C_UNIT*xunit),isS.getY(),isS.getX() + (C_UNIT*xunit),isS.getY()) ;	
		Line2D.Double isLY = new Line2D.Double(isS.getX(),isS.getY() - (C_UNIT*yunit),isS.getX(),isS.getY() + (C_UNIT*yunit)) ;	
		
		// cs
		Line2D.Double csLX = new Line2D.Double(csS.getX() - (C_UNIT*xunit),csS.getY(),csS.getX() + (C_UNIT*xunit),csS.getY()) ;	
		Line2D.Double csLY = new Line2D.Double(csS.getX(),csS.getY() - (C_UNIT*yunit),csS.getX(),csS.getY() + (C_UNIT*yunit)) ;
		
		// is
		Line2D.Double itLX = new Line2D.Double(itS.getX() - (C_UNIT*xunit),itS.getY(),itS.getX() + (C_UNIT*xunit),itS.getY()) ;	
		Line2D.Double itLY = new Line2D.Double(itS.getX(),itS.getY() - (C_UNIT*yunit),itS.getX(),itS.getY() + (C_UNIT*yunit)) ;
		
		// is
		Line2D.Double ctLX = new Line2D.Double(ctS.getX() - (C_UNIT*xunit),ctS.getY(),ctS.getX() + (C_UNIT*xunit),ctS.getY()) ;	
		Line2D.Double ctLY = new Line2D.Double(ctS.getX(),ctS.getY() - (C_UNIT*yunit),ctS.getX(),ctS.getY() + (C_UNIT*yunit)) ;
		
		g2.setColor(Color.red) ;
		g2.draw(isLX) ;
		g2.draw(isLY) ;
		g2.draw(csLX) ;
		g2.draw(csLY) ;
		g2.draw(itLX) ;
		g2.draw(itLY) ;
		g2.draw(ctLX) ;
		g2.draw(ctLY) ;
		
		picG2.setColor(Color.red) ;
		picG2.draw(isLX) ;
		picG2.draw(isLY) ;
		picG2.draw(csLX) ;
		picG2.draw(csLY) ;
		picG2.draw(itLX) ;
		picG2.draw(itLY) ;
		picG2.draw(ctLX) ;
		picG2.draw(ctLY) ;
	    }
	else
	    {
		if(firstLock)
		    {
			d1 = Math.sqrt( Math.pow((isC.getCenterX() - itC.getCenterX()),2) + Math.pow((isC.getCenterY() - itC.getCenterY()),2) ) ;	
			lockd1 = (int)d1 ;
			d2 = Math.sqrt( Math.pow((csC.getCenterX() - ctC.getCenterX()),2) + Math.pow((csC.getCenterY() - ctC.getCenterY()),2) ) ;	
			lockd2 = (int)d2 ;
			firstLock = false ;
		    }
		
		g2.setColor(Color.RED) ;
		g2.drawArc( (int)(isC.getCenterX() - (d1)),
			    (int)(isC.getCenterY() - (d1)),
			    (int)(d1*2),(int)(d1*2),0,360 );
		
		g2.setColor(Color.RED) ;
		g2.drawArc( (int)(csC.getCenterX() - (d2)),
			    (int)(csC.getCenterY() - (d2)),
			    (int)(d2*2),(int)(d2*2),0,360 );
		
		picG2.setColor(Color.RED) ;
		picG2.drawArc( (int)(isC.getCenterX() - (d1)),
			       (int)(isC.getCenterY() - (d1)),
			       (int)(d1*2),(int)(d1*2),0,360 );
		
		picG2.setColor(Color.RED) ;
		picG2.drawArc( (int)(csC.getCenterX() - (d2)),
			       (int)(csC.getCenterY() - (d2)),
			       (int)(d2*2),(int)(d2*2),0,360 );
	    }
	
	/*
	 * Draw the ears
	 * - calculate standard positions
	 * - calculate the deviation from standard positions
	 * - draw new positions (as eclipses)
	 */
	isC = new Ellipse2D.Double(isS.getX()-RAD-(isx*xunit), isS.getY()-RAD-(isy*yunit),RAD*2,RAD*2) ;
	csC = new Ellipse2D.Double(csS.getX()-RAD-(csx*xunit), csS.getY()-RAD-(csy*yunit),RAD*2,RAD*2) ;
	itC = new Ellipse2D.Double(itS.getX()-RAD-(itx*xunit), itS.getY()-RAD-(ity*yunit),RAD*2,RAD*2) ;
	ctC = new Ellipse2D.Double(ctS.getX()-RAD-(ctx*xunit), ctS.getY()-RAD-(cty*yunit),RAD*2,RAD*2) ;
	
	if(cor != null)
	    {
		if(corParent > 0)
		    {
			switch(corParent)
			    {
			    case DIS :
				{
				    cor = new Point2D.Double(isC.getCenterX(), isC.getCenterY()) ;	
				} ; break ;	
			    case DIT :
				{	
				    cor = new Point2D.Double(itC.getCenterX(), itC.getCenterY()) ;
				} ; break ;	
			    case DCS :
				{
				    cor = new Point2D.Double(csC.getCenterX(), csC.getCenterY()) ;	
				} ; break ;	
			    case DCT :
				{	
				    cor = new Point2D.Double(ctC.getCenterX(), ctC.getCenterY()) ;
				} ; break ;	
			    }
			
			Line2D.Double cxax = new Line2D.Double(new Point2D.Double(20.0, cor.getY()), new Point2D.Double(width-20, cor.getY())) ;
			Line2D.Double cyax = new Line2D.Double(new Point2D.Double(cor.getX(), 20.0), new Point2D.Double(cor.getX(), height - 20)) ;
			
			g2.setColor(new Color((Color.yellow).getRed(),(Color.yellow).getGreen(),(Color.yellow).getBlue(),165)) ;
			g2.draw(cxax) ;
			g2.draw(cyax) ;	
			
			picG2.setColor(new Color((Color.yellow).getRed(),(Color.yellow).getGreen(),(Color.yellow).getBlue(),165)) ;
			picG2.draw(cxax) ;
			picG2.draw(cyax) ;				
		    }
		else
		    {
			Line2D.Double cxax = new Line2D.Double(new Point2D.Double(20.0, cor.getY()), new Point2D.Double(width-20, cor.getY())) ;
			Line2D.Double cyax = new Line2D.Double(new Point2D.Double(cor.getX(), 20.0), new Point2D.Double(cor.getX(), height - 20)) ;
			
			g2.setColor(new Color((Color.yellow).getRed(),(Color.yellow).getGreen(),(Color.yellow).getBlue(),165)) ;
			g2.draw(cxax) ;
			g2.draw(cyax) ;
			
			picG2.setColor(new Color((Color.yellow).getRed(),(Color.yellow).getGreen(),(Color.yellow).getBlue(),165)) ;
			picG2.draw(cxax) ;
			picG2.draw(cyax) ;
		    }
		
		// UGLY PROGRAMMING, yeah!
		((DSFrame)parent).runSimulation() ;
	    }
	
	g2.setColor(Color.blue) ;
	if(lock)
	    {
		Color blueT = new Color((Color.BLUE).getRed(),(Color.BLUE).getGreen(),(Color.BLUE).getBlue(),165) ;
		g2.setColor(blueT) ;
		g2.fill(isC) ;
		g2.fill(csC) ;
		
		picG2.setColor(blueT) ;
		picG2.fill(isC) ;
		picG2.fill(csC) ;
	    }
	else
	    {
		g2.setColor(Color.blue) ;
		g2.draw(isC) ;
		g2.draw(csC) ;	
		
		picG2.setColor(Color.blue) ;
		picG2.draw(isC) ;
		picG2.draw(csC) ;	
	    }
	g2.setColor(Color.blue) ;
	g2.draw(itC) ;
	g2.draw(ctC) ;
	
	picG2.setColor(Color.blue) ;
	picG2.draw(itC) ;
	picG2.draw(ctC) ;
	
	// draw the tracheas
	Line2D.Double iscs1 = new Line2D.Double(isC.getCenterX(),isC.getCenterY()-RAD,csC.getCenterX(),csC.getCenterY()-RAD) ;
	Line2D.Double iscs2 = new Line2D.Double(isC.getCenterX(),isC.getCenterY()+RAD,csC.getCenterX(),csC.getCenterY()+RAD) ;
	
	Line2D.Double itis1 = new Line2D.Double(isC.getCenterX()-RAD,isC.getCenterY(),itC.getCenterX()-RAD,itC.getCenterY()) ;
	Line2D.Double itis2 = new Line2D.Double(isC.getCenterX()+RAD,isC.getCenterY(),itC.getCenterX()+RAD,itC.getCenterY()) ;
	
	Line2D.Double ctcs1 = new Line2D.Double(csC.getCenterX()-RAD,csC.getCenterY(),ctC.getCenterX()-RAD,ctC.getCenterY()) ;
	Line2D.Double ctcs2 = new Line2D.Double(csC.getCenterX()+RAD,csC.getCenterY(),ctC.getCenterX()+RAD,ctC.getCenterY()) ;
	
	g2.draw(iscs1) ;
	g2.draw(iscs2) ;
	g2.draw(itis1) ;
	g2.draw(itis2) ;
	g2.draw(ctcs1) ;
	g2.draw(ctcs2) ;
	
	picG2.draw(iscs1) ;
	picG2.draw(iscs2) ;
	picG2.draw(itis1) ;
	picG2.draw(itis2) ;
	picG2.draw(ctcs1) ;
	picG2.draw(ctcs2) ;
	
	// draw the CM
	if(membrane)
	    {
		Rectangle2D.Double cm = new Rectangle2D.Double(center.getX()-RAD, center.getY()-RAD,RAD*2,RAD*2) ;
		Color redT = new Color((Color.RED).getRed(),(Color.RED).getGreen(),(Color.RED).getBlue(),165) ;
		g2.setColor(redT) ;
		g2.fill(cm) ;
	    }
	
	setDistanceLabels() ;
	
    }
    
    private void setDistanceLabels()
    {
	double isit = Math.sqrt( Math.pow( ((isC.getCenterX() - itC.getCenterX())/xunit) ,2) + Math.pow( ((isC.getCenterY() - itC.getCenterY())/yunit),2) ) ;	
	double csit = Math.sqrt( Math.pow( ((csC.getCenterX() - itC.getCenterX())/xunit),2) + Math.pow( ((csC.getCenterY() - itC.getCenterY())/yunit),2) ) ;		
	double iscs = Math.sqrt( Math.pow( ((isC.getCenterX() - csC.getCenterX())/xunit),2) + Math.pow( ((isC.getCenterY() - csC.getCenterY())/yunit),2) ) ;		
	double csct = Math.sqrt( Math.pow( ((csC.getCenterX() - ctC.getCenterX())/xunit),2) + Math.pow( ((csC.getCenterY() - ctC.getCenterY())/yunit),2) ) ;		
	double isct = Math.sqrt( Math.pow( ((isC.getCenterX() - ctC.getCenterX())/xunit),2) + Math.pow( ((isC.getCenterY() - ctC.getCenterY())/yunit),2) ) ;
	
	DecimalFormat formatter = new DecimalFormat() ;
	
	
	isitLabel.setText("" + formatter.format(isit) + " mm") ;
	csitLabel.setText("" + formatter.format(csit) + " mm") ;
	csctLabel.setText("" + formatter.format(csct) + " mm") ;
	isctLabel.setText("" + formatter.format(isct) + " mm") ;
    }
    
    private void setStandards()
    {
	isS = new Point2D.Double(center.getX()-(3*xunit),center.getY()) ;
	csS = new Point2D.Double(center.getX()+(3*xunit),center.getY()) ;
	itS = new Point2D.Double(center.getX()-(6*xunit),center.getY()-(6*yunit)) ;
	ctS = new Point2D.Double(center.getX()+(6*xunit),center.getY()-(6*yunit)) ;	
    }
    
    private void setCoR(MouseEvent me)
    {
	double mx = me.getX() ;
	double my = me.getY() ;
	
	g2.setColor(Color.magenta) ;
	
	if(isC.contains(me.getX(), me.getY()))
	    {
		cor = new Point2D.Double(isC.getCenterX(), isC.getCenterY()) ;
		corParent = DIS ;		
	    }
	else if(itC.contains(me.getX(), me.getY()))
	    {
		cor = new Point2D.Double(itC.getCenterX(), itC.getCenterY()) ;	
		corParent = DIT ;		
	    }
	else if(csC.contains(me.getX(), me.getY()))
	    {
		cor = new Point2D.Double(csC.getCenterX(), csC.getCenterY()) ;	
		corParent = DCS ;		
	    }
	else if(ctC.contains(me.getX(), me.getY()))
	    {
		cor = new Point2D.Double(ctC.getCenterX(), ctC.getCenterY()) ;	
		corParent = DCT ;
	    }
	else
	    {
		cor = new Point2D.Double(mx,my) ;	
		corParent = -1 ;
	    }		
	((JFrame)parent).repaint() ;	
    }
    
    private void showPopup(MouseEvent me)
    {
	int mx = me.getX() ;
	int my = me.getY() ;
	
	popup.show(this,mx,my) ;	
    }
    
    private void makePopup()
    {
	MyMenuListener mlistener = new MyMenuListener() ;
	
	popup = new JPopupMenu() ;
	JMenuItem stdItem = new JMenuItem("Orginal cnf") ;
	stdItem.setName("std") ;
	stdItem.addActionListener(mlistener) ;
	JMenuItem resItem = new JMenuItem("Reset CoR") ;
	resItem.setName("res") ;
	resItem.addActionListener(mlistener) ;
	spirItem = new JCheckBoxMenuItem("Lock spiracles") ;
	spirItem.setState(false) ;
	spirItem.setName("spir") ;
	spirItem.addActionListener(mlistener) ;
	memItem = new JCheckBoxMenuItem("Enable membrane") ;
	memItem.setState(false) ;
	memItem.setName("mem") ;
	memItem.addActionListener(mlistener) ;
	spirItem.addActionListener(mlistener) ;
	memItem.addActionListener(mlistener) ;
	
	JMenuItem setItem = new JMenuItem("Set details") ;
	setItem.setName("set") ;
	setItem.addActionListener(mlistener) ;
	
	JMenuItem pngItem = new JMenuItem("Save as PNG") ;
	pngItem.setName("pngItem") ;
	pngItem.addActionListener(mlistener) ;
	
	JMenuItem svgItem = new JMenuItem("Save as SVG") ;
	svgItem.setName("svgItem") ;
	svgItem.addActionListener(mlistener) ;
	
	popup.add(stdItem) ;
	popup.add(resItem) ;
	popup.addSeparator() ;
	popup.add(setItem) ;
	popup.addSeparator() ;
	popup.add(spirItem) ;
	popup.addSeparator() ;
	popup.add(pngItem)	;
	popup.add(svgItem)	;	
    }
    
    private class MyMenuListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    Object source = e.getSource();
	    String name = ((JMenuItem)source).getName() ;
	    System.out.println("name: " +name) ;
	    
	    if(name.equals("std") )
		{
		    // reset all offsets
		    isx = 0; isy = 0; csx = 0; csy = 0; itx = 0; ity = 0; ctx = 0; cty = 0 ;
		    ((JFrame)parent).repaint() ;
		}
	    
	    if(name.equals("res") )
		{
		    cor = null ;
		}
	    
	    if(name.equals("spir"))
		{
		    // lock/unluck the spiracles the spiracles
		    if(spirItem.getState() == true)
			{
			    lock = false ;
			    spirItem.setState(false) ;	
			}
		    else
			{
			    lock = true ;
			    spirItem.setState(true) ;
			    firstLock = true ;	
			}
		    
		    System.out.println("lock nu: " + lock) ;
		}
	    
	    if(name.equals("mem"))
		{
		    // enable/disable membrane
		    if(memItem.getState() == true)
			{
			    membrane = false ;
			    memItem.setState(false) ;	
			}
		    else
			{
			    membrane = true ;
			    memItem.setState(true) ;	
			}
		}
	    
	    if(name.equals("set"))
		{
		    // open settings panel
		    DSEarEdit ed = new DSEarEdit(ec, new double[]{gain1,gain2,gain3, gain4, gain5, gain6}, new double[]{delay1,delay2,delay3, delay4, delay5, delay6},DSEarComponent.LEFT) ;
		    ed.show() ;	
		    ed.setLocationRelativeTo(panel) ;
		    ed.toFront() ;
		}
	    
	    if(name.equals("pngItem"))
		{
		    saveImageAsPNG() ;	
		}
	    
	    if(name.equals("svgItem"))
		{
		    saveImageAsSVG() ;	
		}
	    
	    // alwys repaint
	    ((JFrame)parent).repaint() ;
	}
    };
    
    /**
     * Save the settings to a data file
     **/
    public void saveSettings(String fileName)
    {
	try
	    {
		FileOutputStream outstream = new FileOutputStream(fileName) ;
		
		Properties settings = new Properties() ;
		String header = "Ear configuration file" ;
		
		settings.setProperty("isx",""+isx) ;
		settings.setProperty("isy",""+isy) ;
		settings.setProperty("itx",""+itx) ;
		settings.setProperty("ity",""+ity) ;
		settings.setProperty("csx",""+csx) ;
		settings.setProperty("csy",""+csy) ;
		settings.setProperty("ctx",""+ctx) ;
		settings.setProperty("cty",""+cty) ;
		
		settings.setProperty("gain_it_it", ""+gain1) ;
		settings.setProperty("gain_is_it", ""+gain2) ;
		settings.setProperty("gain_cs_it", ""+gain3) ;
		
		settings.setProperty("gain_ct_ct",""+gain4) ;
		settings.setProperty("gain_cs_ct",""+gain5) ;
		settings.setProperty("gain_is_ct",""+gain6) ;
		
		settings.setProperty("delay_it_it", ""+delay1) ;
		settings.setProperty("delay_is_it", ""+delay2) ;
		settings.setProperty("delay_cs_it", ""+delay3) ;
		
		settings.setProperty("delay_ct_ct",""+delay4) ;
		settings.setProperty("delay_cs_ct",""+delay5) ;
		settings.setProperty("delay_is_ct",""+delay6) ;
		
		if(cor != null)
		    {
			settings.setProperty("cor_x", "" + cor.getX()) ;
			settings.setProperty("cor_y", "" + cor.getY()) ;
		    }
		
		settings.store(outstream,header) ;
		outstream.close() ;
	    }
	catch(IOException ioe)
	    {
		System.out.println("Error while loading ear settings") ;
		ioe.printStackTrace() ;	
	    }		
    }
    
    /**
     * Load the settings from a data file
     **/
    public void loadSettings(String fileName)
    {
	try
	    {
		FileInputStream inStream = new FileInputStream(fileName) ;
		
		Properties settings = new Properties() ;
		settings.load(inStream) ;
		
		isx = Double.parseDouble(settings.getProperty("isx","66.6")) ;
		isy = Double.parseDouble(settings.getProperty("isy","66.6")) ;
		csx = Double.parseDouble(settings.getProperty("csx","66.6")) ;
		csy = Double.parseDouble(settings.getProperty("csy","66.6")) ;
		itx = Double.parseDouble(settings.getProperty("itx","66.6")) ;
		ity = Double.parseDouble(settings.getProperty("ity","66.6")) ;
		ctx = Double.parseDouble(settings.getProperty("ctx","66.6")) ;
		cty = Double.parseDouble(settings.getProperty("cty","66.6")) ;
		
		gain1 = Double.parseDouble(settings.getProperty("gain_it_it","1")) ;
		gain2 = Double.parseDouble(settings.getProperty("gain_is_it","1.5")) ;
		gain3 = Double.parseDouble(settings.getProperty("gain_cs_it","0.44")) ;
		gain4 = Double.parseDouble(settings.getProperty("gain_ct_ct","1")) ;
		gain5 = Double.parseDouble(settings.getProperty("gain_cs_ct","1.5")) ;
		gain6 = Double.parseDouble(settings.getProperty("gain_is_ct","0.44")) ;
		
		delay1= Double.parseDouble(settings.getProperty("delay_it_it","0")) ;
		delay2= Double.parseDouble(settings.getProperty("delay_is_it","95")) ;
		delay3= Double.parseDouble(settings.getProperty("delay_cs_it","128")) ;
		delay4= Double.parseDouble(settings.getProperty("delay_ct_ct","0")) ;
		delay5= Double.parseDouble(settings.getProperty("delay_cs_ct","95")) ;
		delay6= Double.parseDouble(settings.getProperty("delay_is_ct","128")) ;
		
		try
		    {
			double cx = Double.parseDouble(settings.getProperty("cor_x")) ;
			double cy = Double.parseDouble(settings.getProperty("cor_y")) ;
			cor = new Point2D.Double(cx,cy) ;
			corParent = -1 ;
		    }
		catch(NullPointerException npe)
		    {
			//	no problem...
		    }
		
		inStream.close() ;
	    }
	catch(IOException ioe)
	    {
		System.out.println("Error while loading ear settings") ;
		ioe.printStackTrace() ;	
	    }
	
	((JFrame)parent).repaint() ;
    }
    
    private double gain1, gain2, gain3,gain4, gain5, gain6, delay1, delay2, delay3, delay4, delay5, delay6 ;
    
    public void setGains(double[] gains, int side)
    {
	if(side == LEFT)
	    {
		gain1 = gains[0] ;
		gain2 = gains[1] ;
		gain3 = gains[2] ;	
	    }
	else
	    {
		gain4 = gains[0] ;
		gain5 = gains[1] ;
		gain6 = gains[2] ;				
	    }
    }
    
    public void setDelays(double[] delays, int side)
    {
	if(side == LEFT)
	    {
		delay1 = delays[0] ;
		delay2 = delays[1] ;
		delay3 = delays[2] ;
	    }
	else
	    {
		delay4 = delays[0] ;
		delay5 = delays[1] ;
		delay6 = delays[2] ;			
	    }	
    }
    
    public double[] getDelays(int side)
    {
	if(side == LEFT)
	    return new double[]{delay1, delay2, delay3} ;
	else if(side == RIGHT)
	    return new double[]{delay4, delay5, delay6} ;
	else
	    return new double[]{delay1, delay2, delay3, delay4, delay5, delay6} ;	
    }
    
    public double[] getGains(int side)
    {
	if(side == LEFT)
	    return new double[]{gain1, gain2, gain3} ;	
	else if(side == RIGHT)
	    return new double[]{gain4, gain5, gain6} ;
	else
	    return new double[]{gain1, gain2, gain3, gain4, gain5, gain6} ;	
    }
    
    public void setEditable(boolean eMode)
    {
	this.eMode = eMode ;
    }
    
    public Point2D.Double getISPosition()
    {
	return new Point2D.Double(isC.getCenterX()/xunit, isC.getCenterY()/yunit) ;		
    }
    
    public Point2D.Double getITPosition()
    {
	return new Point2D.Double(itC.getCenterX()/xunit, itC.getCenterY()/yunit) ;		
    }
    
    public Point2D.Double getCSPosition()
    {
	return new Point2D.Double(csC.getCenterX()/xunit, csC.getCenterY()/yunit) ;		
    }
    
    public Point2D.Double getCTPosition()
    {
	return new Point2D.Double(ctC.getCenterX()/xunit, ctC.getCenterY()/yunit) ;		
    }
    
    public Point2D.Double getCoR()
    {
	return new Point2D.Double(cor.getX()/xunit, cor.getY()/yunit) ;	
    }
    
    // test
    public static void main(String[] cmdl)
    {
	JFrame tFrame = new JFrame() ;
	tFrame.setTitle("Test panel") ;
	tFrame.setSize(400,400) ;
	Container contentPane = tFrame.getContentPane() ;
	
	JPanel previewPanel = new JPanel(new GridLayout(1,1)) ;
	previewPanel.setBorder(new TitledBorder(new EtchedBorder(), "Preview t")) ;
	
	DSEarComponent preview = new DSEarComponent(tFrame) ;
	previewPanel.add(preview) ;
	
	
	contentPane.add(previewPanel) ;
	
	tFrame.setContentPane(contentPane) ;
	tFrame.show() ;
	tFrame.addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    System.exit(0) ;
		}
	    });	
    }	
}
