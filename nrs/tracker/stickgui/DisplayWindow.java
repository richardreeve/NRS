//$Id: DisplayWindow.java,v 1.6 2005/05/09 22:05:12 hlrossano Exp $

/**
*	@author Hugo L. Rosano
*	@version 1.0
*/

package nrs.tracker.stickgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JPanel;

import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import nrs.tracker.jointmanager.JointSequenceManager;
import nrs.tracker.jointmanager.JointWindowInterface;
import nrs.tracker.stickgui.PaintStuff;

public class DisplayWindow extends Toolbox {

  public static String WIN_X = "DISPLAYFRAME_BROSWER_DIALOG_WIN_X";
  public static String WIN_Y = "DISPLAYFRAME_BROSWER_DIALOG_WIN_Y";
  public static String WIN_WIDTH = "DISPLAYFRAME_BROSWER_DIALOG_WIN_WIDTH";
  public static String WIN_HEIGHT = "DISPLAYFRAME_BROSWER_DIALOG_WIN_HEIGHT";
  public static String WIN_VISIBLE = "DISPLAYFRAME_BROSWER_DIALOG_WIN_VISIBLE";

	private JointWindowInterface jointWindows;
	private MCanvas canvas = new MCanvas();
	public static String TITLE = "Auxiliary Display";

	public DisplayWindow(Frame owner, ToolboxParent parent){
		super(owner, TITLE, parent, "DisplayWindow_");
		
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(canvas, BorderLayout.CENTER);
	}
	
	/** Set a new image on canvas*/
	public void setImage(ImageData image){
		canvas.image = image;
		repaint();
	}
	
	public void setJointWindowInterface(JointWindowInterface jointWindowInterface){
		this.jointWindows = jointWindowInterface;
		repaint();
	}
	
	public void saveSettings(Preferences props){
	    super.saveSettings(props);
	    Point p = getLocation();
	    props.putInt(WIN_X, p.x);
	    props.putInt(WIN_Y, p.y);
	    Dimension d = getSize();
	    props.putInt(WIN_WIDTH, d.width);
	    props.putInt(WIN_HEIGHT, d.height);
	}
	
	public void restoreSettings(Preferences props)
	  {
	    restoreLocation(props);
	    restoreVisibility(props); 
		Point p = new Point();
		p.x = props.getInt(WIN_X, 0);
		p.y = props.getInt(WIN_Y, 0);
		setLocation(p);

		Dimension d = new Dimension();
		d.width = props.getInt(WIN_WIDTH, 300);
		d.height = props.getInt(WIN_HEIGHT, 200);
		setSize(d);
		repaint();
	  }

	
	class MCanvas extends JComponent {
		
		private ImageData image;

		MCanvas(){
			super();
			this.setBackground(Color.DARK_GRAY);
			image = new ImageData(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
			this.repaint();
		}

		public void paintComponent(Graphics g){
		    Dimension d = getSize();
		    g.setColor(Color.DARK_GRAY);
			g.fillRect(0,0,d.width, d.height);
			
			if (image != null){
				image.refresh();
				g.drawImage(image, 0, 0, this);
			}
			JointSequenceManager jm = jointWindows.getJointManager(); 
			if(jm != null){
				//PaintStuff.paintList(jm.getLastJoints(Joint.BODY),g,Color.RED, PaintStuff.C_SQUARE_INNERDOT);
				//PaintStuff.paintCursor(g, PaintStuff.C_PLUS, jm.getBodyMean());				
				PaintStuff.paintJointManager(g, jointWindows.getAdjustNice());
			}
		}
	}



}