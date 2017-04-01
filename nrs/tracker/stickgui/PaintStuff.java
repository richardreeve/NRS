//$Id: PaintStuff.java,v 1.4 2005/05/09 22:05:12 hlrossano Exp $
/*
 * Created on 03-May-2005
 */
package nrs.tracker.stickgui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

import nrs.tracker.jointmanager.Joint;
import nrs.tracker.jointmanager.JointSequence;
import nrs.tracker.jointmanager.JointSequenceManager;
import nrs.tracker.sticktrack.CompoundGroup;
import nrs.tracker.sticktrack.Group;
import nrs.tracker.sticktrack.Vector2D;

/**
 * @author Hugo L. Rosano
 * @version 1.0
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PaintStuff {
	
	public static final int C_NORMAL = 111;
	public static final int C_PLUS = 222;
	public static final int C_CIRCLE_INNERDOT = 333;
	public static final int C_DOUBLE_CIRCLE_INNERDOT = 444;
	public static final int C_CIRCLE_INNERCROSS = 555;
	public static final int C_SQUARE_INNERDOT = 666;
	
	public static void paintCursor(Graphics g, int type, int x, int y){
		int rad;
		switch (type)
		{
		case C_NORMAL:
			g.fillOval(x,y,1,1);
		break;
		case C_PLUS:
			int minim = 2;
			int maxim = 6;
			g.drawLine(x - maxim, y, x - minim, y);
			g.drawLine(x + minim, y, x + maxim, y);
			g.drawLine(x, y - maxim, x, y - minim);
			g.drawLine(x, y + minim, x, y + maxim);
		break;
		case C_CIRCLE_INNERDOT:
			int radi = 6;
			g.drawOval(x - radi/2, y - radi/2, radi, radi);
			g.fillOval(x, y, 1, 1);
		break;
		case C_DOUBLE_CIRCLE_INNERDOT:
			rad = 3;
			g.fillOval(x, y, 1, 1);
			g.drawOval(x - rad, y - rad, 2*rad, 2*rad);
			g.drawOval(x - 2*rad, y - 2*rad, 4*rad, 4*rad);
		break;
		case C_CIRCLE_INNERCROSS:
			int csize = 5;
			rad = (int) (Math.sqrt(2)*csize);
			g.drawLine(x - csize, y - csize, x + csize, y + csize);
			g.drawLine(x - csize, y + csize, x + csize, y - csize);
			g.drawOval(x - rad, y - rad, 2*rad , 2*rad);
		break;
		case C_SQUARE_INNERDOT:
			int size = 2;
			g.drawRect(x - size, y - size, 2*size, 2*size);
			g.fillOval(x,y,1,1);
		break;
		}
	}

	public static void paintList(List ll, Graphics g, Color c, int cursor){
		if((ll!=null) && (!ll.isEmpty())){
			Iterator it = ll.iterator();
			Vector2D v;
			g.setColor(c);
			while(it.hasNext()){
				v = (Vector2D)it.next();
				paintCursor(g, cursor, v.getX(), v.getY());
			}
		}
	}		
	
	/**
	 *  @deprecated This class is now deprecated, use paintList 
	 * */
	public static void paintGroup(Group gr, Graphics g, Color c, int cursor){
		double[][] coords;
		if(!gr.isEmpty()){
			coords = gr.getCoords();
			g.setColor(c);
			for(int i=0;i<gr.size();i++)
				paintCursor(g, cursor, coords[0][i], coords[1][i]);
		}
	}	
	
	public static void paintCursor(Graphics g, int type, Joint join){
		paintCursor(g, type, (int)join.getX(), (int)join.getY());}
	
	public static void paintCursor(Graphics g, int type, Vector2D vec){
		paintCursor(g, type, (int)vec.getX(), (int)vec.getY());}

	public static void paintCursor(Graphics g, int type, double x, double y){
		paintCursor(g, type, (int)x, (int)y);}
	
	public static void paintJointManager(Graphics g, JointSequenceManager jointM){
		PaintStuff.paintJointManager(g, jointM, null, 0, -1, Integer.MAX_VALUE);
	}
	
	public static void paintJointManager(Graphics g, JointSequenceManager jointM, JointSequence jSeqSel, int from, int time, int to){
		if(jointM != null){
			JointSequence jSeq;
			Iterator jmIt = jointM.iterator();
			while(jmIt.hasNext()){
				jSeq = (JointSequence)jmIt.next();
				if((!jSeq.isEmpty()) && jSeq.getVisible()){
					Iterator jsIt = jSeq.iterator();
					Joint j = null, jlast = null;
					while(jsIt.hasNext()){
						if (j!=null)
							jlast = j.copyJoint();
						j = (Joint)jsIt.next();
						if ( (jlast != null) && (jlast.getTime()>=from) && (jlast.getTime()<=to) ){
							switch(jlast.getStepPhase()){
							case Joint.STANCE:
								g.setColor(Color.GREEN);
							break;
							case Joint.SWING:
								g.setColor(Color.BLUE);
							break;
							case Joint.BODY:
								g.setColor(Color.YELLOW);
							break;
							default:
								g.setColor(Color.PINK);
							}
							g.drawLine((int)jlast.getX(), (int)jlast.getY(), (int)j.getX(), (int)j.getY());
							g.setColor(Color.RED);
							paintCursor(g, C_NORMAL, jlast);
						}
					}
					j = (Joint)jSeq.getLast();
					if (j!=null){
						g.setColor(Color.ORANGE);
						paintCursor(g, C_CIRCLE_INNERDOT,j);
					}
				}
			}
			if (jSeqSel!=null){
				Joint jTemp = jSeqSel.getJoint(time);
				if (jTemp != null){
					g.setColor(Color.ORANGE);
					paintCursor(g, C_CIRCLE_INNERCROSS, jTemp);
				}
				if (jSeqSel.getVisible()){
					jTemp = (Joint)jSeqSel.getLast();
					if(jTemp != null){
						g.setColor(Color.GREEN);
						paintCursor(g, C_CIRCLE_INNERDOT,jTemp);
					}
				}
			}
		}
	}		
	
	public static void paintCGroup(CompoundGroup li, Graphics g, Color c){
		if(!li.isEmpty()){
			Group paintGroup;
			double[][] coords;
			int sizeGroup;
			Iterator grps = li.iterator();
			while(grps.hasNext()){
				paintGroup = (Group)grps.next();
				sizeGroup = paintGroup.size();
				if(sizeGroup>0){
					coords = paintGroup.getCoords();
					g.setColor(c);
					for(int i=0;i<sizeGroup;i++)
						paintCursor(g, C_NORMAL, coords[0][i], coords[1][i] );
				}
			}
		}	
	}
	
}
