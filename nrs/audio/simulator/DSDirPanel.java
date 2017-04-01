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
import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.*;
import java.awt.geom.* ;
import javax.swing.border.* ;
import javax.swing.* ;
import javax.imageio.*;
import java.io.* ;
import org.w3c.dom.*;
import org.apache.xerces.dom.* ;
import org.apache.batik.svggen.SVGGraphics2D;

public class DSDirPanel extends JPanel
{
    public static final int L = 1 ;
    public static final int R = 2 ;
    private static final int UNIT_F = 500 ; 
    
    private static final int RIGHT = 0 ; 
    private static final int LEFT = 1 ; 
    
    private static final double RELATIVE_FIRST_VALUE = 75 ; // relative to yunit
    
    private static final Color magenta_t = new Color(Color.magenta.getRed(),Color.magenta.getGreen(),Color.magenta.getBlue(),128) ;
    private static final Color cyan_t = new Color(Color.cyan.getRed(),Color.cyan.getGreen(),Color.cyan.getBlue(),128) ;
    private static final Color red_t = new Color(Color.red.getRed(),Color.red.getGreen(),Color.red.getBlue(),128) ;
    private static final Color blue_t = new Color(Color.blue.getRed(),Color.blue.getGreen(),Color.blue.getBlue(),128) ;
    private static final Color black_t = new Color(Color.black.getRed(),Color.black.getGreen(),Color.black.getBlue(),255) ;
    
    private double 	firstRightValue,
	rightDivider,
	firstLeftValue,
	leftDivider ; 
    
    private Graphics2D g2 ;
    
    private Graphics2D picG2 ;
    private static BufferedImage image ;
    
    private Rectangle2D.Double white ;
    
    private double width, height ;
    
    private Point2D.Double center ;
    private Point2D.Double cor ; 
    
    private Line2D.Double xax, yax ;
    
    private double[] leftData, rightData ;
    private Point2D.Double[] leftPoints, rightPoints ;
    
    private double	xunit,
	yunit ;
    
    private boolean set = false ;
    private boolean drawLeftMinMax = false,
	drawRightMinMax = false,
	drawReferenceCircleLeft = false,
	drawReferenceCircleRight = false,
	drawRightData = true,
	drawLeftData = true,
	drawWinnerCircle = false;
    
    private JPanel panel ;
    
    private Image loudspeaker ;
    
    public DSDirPanel()
    {
	super() ;
	
	rightPoints = new Point2D.Double[360] ;
	leftPoints = new Point2D.Double[360] ;
	
	ImageIcon ii = new ImageIcon("loudspeaker2.gif") ;
	loudspeaker = ii.getImage() ;
	
	panel = this ;
    }
    
    public void paintComponent(Graphics g)
    {
	super.paintComponent(g) ;
	g2 = (Graphics2D)g ;
	
	AntiAlias.enableFor(g2) ;
	
	width = getBounds().getWidth() ;
	height = getBounds().getHeight() ;
	
	width = height = Math.min( width, height );
	xunit = width / UNIT_F ;
	yunit = height / UNIT_F ;

	image = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_INT_RGB) ;	
	
	center = new Point2D.Double(width / 2, height / 2.0) ;
	
	xax = new Line2D.Double(new Point2D.Double(20.0, height / 2.0), new Point2D.Double(width-20, height / 2.0)) ;
	yax = new Line2D.Double(new Point2D.Double(width / 2, 20.0), new Point2D.Double(width/2, height - 20)) ;
	
	white = new Rectangle2D.Double(20.0,20.0,width - 40, height - 40) ;
	r = new Rectangle(0,0,(int)width, (int)height) ;
	
	// draw the background
	//g2.setColor(SystemColor.control) ;
	g2.setColor(Color.white) ;
	g2.fill(white) ;
	
	picG2 = (Graphics2D)image.createGraphics() ;
	AntiAlias.enableFor(picG2) ;
	picG2.setColor(Color.white) ;
	picG2.fill(r) ;
	
	// draw the axes
	g2.setColor(Color.black) ;
	g2.draw(xax) ;
	g2.draw(yax) ;
	
	picG2.setColor(Color.black) ;
	picG2.draw(xax) ;
	picG2.draw(yax) ;
	
	
	if(set)
	    {
		firstRightValue = rightData[90] ;
		firstLeftValue = leftData[90] ;
		rightDivider = RELATIVE_FIRST_VALUE / firstRightValue ;	
		leftDivider = rightDivider ; //RELATIVE_FIRST_VALUE / firstLeftValue ;				
	    }
	
	// draw the reference cirkel (at r = first value) (both left and right)	
	if(drawReferenceCircleRight && set)
	    {
		for(int i = 0 ; i < 360;i++)
		    {					
			g2.setColor(magenta_t) ;
			g2.draw(new Line2D.Double(valueToPoint(rightData[90]*rightDivider,i), valueToPoint(rightData[90]*rightDivider,i+1))) ;					
			picG2.setColor(magenta_t) ;
			picG2.draw(new Line2D.Double(valueToPoint(rightData[90]*rightDivider,i), valueToPoint(rightData[90]*rightDivider,i+1))) ;					
		    }
	    }
	
	if(drawReferenceCircleLeft && set)
	    {
		for(int i = 0 ; i < 360;i++)
		    {					
			g2.setColor(cyan_t) ;
			g2.draw(new Line2D.Double(valueToPoint(leftData[90]*leftDivider,i), valueToPoint(leftData[90]*leftDivider,i+1))) ;					
			picG2.setColor(cyan_t) ;
			picG2.draw(new Line2D.Double(valueToPoint(leftData[90]*leftDivider,i), valueToPoint(leftData[90]*leftDivider,i+1))) ;					
		    }	
	    }
	
	// draw the values, if set
	if(rightData != null && drawRightData)
	    {	
		
		//			// draw a 0 dB circle (=right reference circle)
		//			for(int i = 0 ; i < 360;i++)
		//			{					
		////				Stroke normal = g2.getStroke() ;
		////				BasicStroke dashed = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{100000001},0) ;
		////				g2.setStroke(dashed) ;
		//				g2.setColor(black_t) ;
		//				g2.draw(new Line2D.Double(valueToPoint(leftData[90]*leftDivider,i), valueToPoint(leftData[90]*leftDivider,i+1))) ;					
		//				picG2.setColor(black_t) ;
		//				picG2.draw(new Line2D.Double(valueToPoint(leftData[90]*leftDivider,i), valueToPoint(leftData[90]*leftDivider,i+1))) ;					
		//				
		////				g2.setStroke(normal) ;
		//			}
		
		dataToPoints(R) ;
		for(int i = 0 ; i < 360;i++)
		    {	
			g2.setColor(Color.red) ;
			picG2.setColor(Color.red) ;
			if (i < 359)
			    {
				g2.draw(new Line2D.Double(rightPoints[i], rightPoints[i+1])) ;
				picG2.draw(new Line2D.Double(rightPoints[i], rightPoints[i+1])) ;
			    }
			else
			    {
				g2.draw(new Line2D.Double(rightPoints[i], rightPoints[0])) ;	
				picG2.draw(new Line2D.Double(rightPoints[i], rightPoints[0])) ;		
			    }
			
			// T =================
			
			// draw the dB scale on the directivity pattern
			if(i == 90)
			    {
				Point2D.Double p = rightPoints[i] ;
				
				double adj = Math.cos(Math.toRadians(-i)) * firstRightValue * rightDivider ;
				double opp = Math.sin(Math.toRadians(-i)) * firstRightValue * rightDivider ; 
				Point2D.Double d = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;					
				
				adj = Math.cos(Math.toRadians(-i)) * (firstRightValue +5)* rightDivider ;
				opp = Math.sin(Math.toRadians(-i)) * (firstRightValue +5)* rightDivider; 
				Point2D.Double d_dif = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;					
				
				adj = Math.cos(Math.toRadians(-i)) * (firstRightValue -15)* rightDivider ;
				opp = Math.sin(Math.toRadians(-i)) * (firstRightValue -15)* rightDivider; 
				Point2D.Double d_dif2 = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;
				
				// set scale lines 0,2,10
				BasicStroke stroke = new BasicStroke(2) ;
				g2.setStroke(stroke) ;
				g2.setColor(Color.black) ;
				g2.draw(new Line2D.Double(d_dif,d_dif2)) ;
				picG2.setColor(Color.black) ;
				picG2.setStroke(stroke) ;
				picG2.draw(new Line2D.Double(d,d_dif)) ;
				
				Point2D.Double tp = new Point2D.Double(d.getX() - (2*xunit),d.getY()) ;
				Point2D.Double tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY()) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (5*yunit)* rightDivider) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (5*yunit)* rightDivider) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (-5*yunit)* rightDivider) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (-5*yunit)* rightDivider) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (-10*yunit)* rightDivider) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (-10*yunit)* rightDivider) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (-15*yunit)* rightDivider) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (-15*yunit)* rightDivider) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				// dB scale labels
				g2.setColor(black_t) ;
				picG2.setColor(black_t) ;
				g2.drawString("0 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)) ) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.drawString("0 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)) ) ;
				
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				g2.drawString("5 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (5*yunit)* rightDivider) ) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.drawString("5 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (5*yunit)* rightDivider) ) ;
				
				g2.drawString("-10 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (-10*yunit)* rightDivider) ) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.drawString("-10 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (-10*yunit)* rightDivider) ) ;
				
				// return to normal...
				g2.setStroke(new BasicStroke(1)) ;
				picG2.setStroke(new BasicStroke(1)) ;
				g2.setColor(Color.black) ;
				picG2.setColor(Color.black) ;
			    }				
			
			// END T =============				
		    }			
	    }
	
	if(leftData != null && drawLeftData)
	    {
		dataToPoints(L) ;
		for(int i = 0 ; i < 360;i++)
		    {	
			g2.setColor(Color.blue) ;
			picG2.setColor(Color.blue) ;
			if (i < 359)
			    {
				g2.draw(new Line2D.Double(leftPoints[i], leftPoints[i+1])) ;
				picG2.draw(new Line2D.Double(leftPoints[i], leftPoints[i+1])) ;
			    }
			else
			    {
				g2.draw(new Line2D.Double(leftPoints[i], leftPoints[0])) ;	
				picG2.draw(new Line2D.Double(leftPoints[i], leftPoints[0])) ;		
			    }
		    }			
	    }
	
	// draw the labels
	if(rightData != null || leftData != null)
	    {
		// mathematical labelling
		for(int i = 0 ; i < 360 ; i++)
		    {
			// draw a label every 30 degrees
			if(i % 30 == 0)
			    {
				
				double adj = Math.cos(Math.toRadians(-i)) * firstRightValue * rightDivider *2 ;
				double opp = Math.sin(Math.toRadians(-i)) * firstRightValue * rightDivider *2; 
				Point2D.Double d = null ;
				if(i == 90)
				    {
					
					d = new Point2D.Double(center.getX() - loudspeaker.getWidth(null)/2, (20*yunit)) ;	
					
					boolean o = g2.drawImage(loudspeaker,(int)d.getX(), (int)d.getY(), Color.white, null) ;
					picG2.drawImage(loudspeaker,(int)d.getX(), (int)d.getY(), Color.white, null) ;
// 					// Don't know what this was for!
// 					if(o)
// 					    System.out.println("i:90") ;
// 					else
// 					    System.out.println("i:90 ERROR") ;
				    }
				else
				    {
					//						// UNCOMMENT WHEN LABELS NEEDED
					//						System.out.println("i: niet 90") ;
					//						d = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;
					//				
					//						g2.setColor(Color.black) ;
					//						g2.drawString(""+i,(int)d.getX(), (int)d.getY()) ;
					//						picG2.setColor(Color.black) ;
					//						picG2.drawString(""+i,(int)d.getX(), (int)d.getY()) ;
				    }						
			    }
			//System.out.println(""+ Math.abs(i) + " has value: " + rightData[ Math.abs(i) ] ) ;								
		    }	
	    }
	
	// draw the min and max value
	if(drawLeftMinMax && leftData != null)
	    {
		int m = getLeftMaxIndex() ;
		g2.setColor(Color.blue);
		picG2.setColor(Color.blue) ;
		g2.draw(new Line2D.Double(center,leftPoints[m])) ;	
		m = getLeftMinIndex() ;
		g2.draw(new Line2D.Double(center,leftPoints[m])) ;
	    }
	
	if(drawRightMinMax && set)
	    {
		int m = getRightMaxIndex() ;
		g2.setColor(Color.red);
		picG2.setColor(Color.red) ;
		g2.draw(new Line2D.Double(center,rightPoints[m])) ;	
		m = getRightMinIndex() ;
		g2.draw(new Line2D.Double(center,rightPoints[m])) ;			
	    }
	
	// draw the "winner circle"
	if(drawWinnerCircle && set)
	    {
		
		for(int i = 0 ; i < 360 ; i++)
		    {
			
			double adj = Math.cos(Math.toRadians(-i)) * firstRightValue * rightDivider *1.5 ;
			double opp = Math.sin(Math.toRadians(-i)) * firstRightValue * rightDivider *1.5; 
			Point2D.Double d = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;
			
			double adj2 = Math.cos(Math.toRadians(-i+1)) * firstRightValue * rightDivider *1.5 ;
			double opp2 = Math.sin(Math.toRadians(-i+1)) * firstRightValue * rightDivider *1.5; 
			Point2D.Double d2 = new Point2D.Double(center.getX() + (adj2*xunit), center.getY() + (opp2*yunit)) ;				
			
			if(leftData[i] > rightData[i])
			    {
				g2.setColor(blue_t) ;
				picG2.setColor(blue_t) ;
			    }
			else
			    {
				g2.setColor(red_t) ;
				picG2.setColor(red_t) ;						
			    }
			
			adj = Math.cos(Math.toRadians(-i)) * (firstRightValue + (Math.abs(rightData[i]-leftData[i])) )* rightDivider *1.5 ;
			opp = Math.sin(Math.toRadians(-i)) * (firstRightValue + (Math.abs(rightData[i]-leftData[i])) )* rightDivider *1.5; 
			Point2D.Double d_dif = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;
			
			double dif = Math.sqrt( Math.pow(leftData[i]-rightData[i],2) ) ;
			if(dif <= 2)
			    {
				g2.setColor(Color.green) ;
				picG2.setColor(Color.green) ;
				if(i >=60 && i <= 120)
				    {
					perc++ ;		
				    }
			    }				
			
			g2.draw(new Line2D.Double(d, d2)) ;
			g2.draw(new Line2D.Double(d,d_dif)) ;
			picG2.draw(new Line2D.Double(d, d2)) ;
			picG2.draw(new Line2D.Double(d,d_dif)) ;
			if(i == 359)
			    {
				g2.draw(new Line2D.Double(d, d2)) ;	
				picG2.draw(new Line2D.Double(d, d2)) ;		
			    }
			
			// draw the dB scale on the winning circle
			if(i == 90)
			    {
				adj = Math.cos(Math.toRadians(-i)) * (firstRightValue +  15 )* rightDivider *1.5 ;
				opp = Math.sin(Math.toRadians(-i)) * (firstRightValue +  15 )* rightDivider *1.5; 
				d_dif = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;					
				
				// set scale lines 0,2,10
				BasicStroke stroke = new BasicStroke(2) ;
				g2.setStroke(stroke) ;
				g2.setColor(Color.black) ;
				g2.draw(new Line2D.Double(d,d_dif)) ;
				picG2.setColor(Color.black) ;
				picG2.setStroke(stroke) ;
				picG2.draw(new Line2D.Double(d,d_dif)) ;
				
				Point2D.Double tp = new Point2D.Double(d.getX() - (2*xunit),d.getY()) ;
				Point2D.Double tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY()) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (2*yunit)* rightDivider *1.5) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (2*yunit)* rightDivider *1.5) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (5*yunit)* rightDivider *1.5) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (5*yunit)* rightDivider *1.5) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (10*yunit)* rightDivider *1.5) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (10*yunit)* rightDivider *1.5) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				tp = new Point2D.Double(d.getX() - (2*xunit),d.getY() - (15*yunit)* rightDivider *1.5) ;
				tp2 = new Point2D.Double(d.getX() + (2*xunit),d.getY() - (15*yunit)* rightDivider *1.5) ;
				g2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				
				// dB scale labels
				g2.setColor(black_t) ;
				picG2.setColor(black_t) ;
				g2.drawString("0 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)) ) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.drawString("0 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)) ) ;
				
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				
				g2.drawString("5 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (5*yunit)* rightDivider *1.5 ) ) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.drawString("5 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (5*yunit)* rightDivider *1.5 ) ) ;
				
				g2.drawString("15 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (15*yunit)* rightDivider *1.5 ) ) ;
				picG2.draw(new Line2D.Double(tp, tp2)) ;
				picG2.drawString("15 dB", (int)(d.getX() +(5*xunit)), (int)(d.getY() + (3*yunit)  - (15*yunit)* rightDivider *1.5 ) ) ;
				
				// return to normal...
				g2.setStroke(new BasicStroke(1)) ;
				picG2.setStroke(new BasicStroke(1)) ;
				g2.setColor(Color.black) ;
				picG2.setColor(Color.black) ;
			    }				
		    }	
	    }
    }
    
    double perc = 0 ;
    
    public static BufferedImage bi ;
    public static Rectangle r ;
    
    public static void saveImageAsPNG()
    {
	saveImageAsPNG("temp_image.png") ;	
    }
    
    public void saveImageAsSVG()
    {
	saveImageAsSVG("temp.image.svg") ;	
    }
    
    public static void saveImageAsPNG(String fileName)
    {
	try
	    {	
		ImageIO.write(image, "png", new File( fileName ));
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
    
    public void saveReport(String fileName)
    {
	FileWriter writer = null ;
	BufferedWriter out = null ;
	
	try
	    {
		writer = new FileWriter(fileName);
		out = new BufferedWriter(writer) ;
		
		// write all the values
		for(int i = 0 ; i < 360 ; i++)
		    {
			out.write("" + Math.toRadians(i) + "\t" + rightData[i] + "\t" + leftData[i] +"\n") ;								
		    }
		
		// write the percentage of directional hearing in forward directions
		double p = 0 ;
		for(int i = 59 ; i<120 ;i++)
		    {
			double dif = Math.sqrt( Math.pow(leftData[i]-rightData[i],2) ) ;
			if(dif <= 1.3)
			    p++ ;	
		    }
		
		double perc = (p / 60) * 100 ;
		out.write("\nperc: " + perc) ;
		
		out.close() ;
		writer.close() ;						
	    }
	catch(IOException ioe)
	    {	
	    }			
	
    }
    
    public void setLeftData(double[] leftData)
    {
	this.leftData = leftData ;
	dataToPoints(L) ;
	set = true ;
    }
    
    public void setRightData(double[] rightData)
    {
	this.rightData = rightData ;
	dataToPoints(R) ;
	set = true ;
    }
    
    public void setDrawLeftRefCircle(boolean b)
    {
    	drawReferenceCircleLeft = b ;	
    } 
    
    public void setDrawRightRefCircle(boolean b)
    {
    	drawReferenceCircleRight = b ;	
    } 
    
    public void setDrawLeftData(boolean b)
    {
    	drawLeftData = b ;	
    }
    
    public void setDrawRightData(boolean b)
    {
    	drawRightData = b ;	
    }
    
    public void setDrawLeftMinMax(boolean b)
    {
    	drawLeftMinMax = b ;	
    }
    
    public void setDrawRightMinMax(boolean b)
    {
    	drawRightMinMax = b ;	
    }
    
    public void setDrawWinnerCircle(boolean b)
    {
    	drawWinnerCircle = b ;	
    }
    
    private Point2D.Double valueToPoint(double value, int angle)
    {
	double adj = Math.cos(Math.toRadians(angle)) * value ;
	double opp = Math.sin(Math.toRadians(angle)) * value ; 
	return new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;		
    }
    
    private void dataToPoints(int side)
    {
	if(side ==  R)
	    {
		for(int i = 0 ; i < 360 ; i++)
		    {
			double x, y ;
			double adj = Math.cos(Math.toRadians(-i)) * rightData[i] * rightDivider ;	
			double opp = Math.sin(Math.toRadians(-i)) * rightData[i] * rightDivider; 	
			Point2D.Double tP = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;
			rightPoints[i] = tP ;  
		    }
	    }
	else 
	    {
		for(int i = 0 ; i < 360 ; i++)
		    {
			double x, y ;
			double adj = Math.cos(Math.toRadians(-i)) * leftData[i] * leftDivider ;
			double opp = Math.sin(Math.toRadians(-i)) * leftData[i] * leftDivider; 
			Point2D.Double tP = new Point2D.Double(center.getX() + (adj*xunit), center.getY() + (opp*yunit)) ;
			leftPoints[i] = tP ;  
		    }		    
	    }
    }
    
    private int getMaxValue()
    {
	double mValue = -1 ;
	for(int i = 0 ; i < leftData.length ; i++)
	    {
		if(leftData[i] > mValue)
		    mValue = leftData[i] ;
	    }
	
	for(int i = 0 ; i < rightData.length ; i++)
	    {
		if(rightData[i] > mValue)
		    mValue = rightData[i] ;
	    }
	
	return (int)mValue ;
    }
    
    private int getLeftMaxIndex()
    {
	int index = 0 ;
	double mValue = -1 ;
	for(int i = 0 ; i < leftData.length ; i++)
	    {
		if(leftData[i] > mValue)
		    {
			mValue = leftData[i] ;
			index = i ;
		    }
	    }  
	return index ; 	
    }
    
    private int getRightMaxIndex()
    {
	int index = 0 ;
	double mValue = -1 ;
	for(int i = 0 ; i < rightData.length ; i++)
	    {
		if(rightData[i] > mValue)
		    {
			mValue = rightData[i] ;
			index = i ;
		    }
	    }  
	return index ; 	
    }
    
    private int getLeftMinIndex()
    {
	int index = 0 ;
	double mValue = 1000 ;
	for(int i = 0 ; i < leftData.length ; i++)
	    {
		if(leftData[i] < mValue)
		    {
			mValue = leftData[i] ;
			index = i ;
		    }
	    }  
	return index ; 	
    }
    
    private int getRightMinIndex()
    {
	int index = 0 ;
	double mValue = 1000 ;
	for(int i = 0 ; i < rightData.length ; i++)
	    {
		if(rightData[i] < mValue)
		    {
			mValue = rightData[i] ;
			index = i ;
		    }
	    }  
	return index ; 	
    }    
    
    
    public static void main(String[] cmdl)
    {
	JFrame tFrame = new JFrame() ;
	tFrame.setTitle("Test panel") ;
	tFrame.setSize(400,400) ;
	Container contentPane = tFrame.getContentPane() ;
	
	JPanel previewPanel = new JPanel(new GridLayout(1,1)) ;
	previewPanel.setBorder( new TitledBorder( new EtchedBorder(),
						  "Preview t") );
	
	DSDirPanel preview = new DSDirPanel() ;
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
	    }
				 );
	
    }
}
