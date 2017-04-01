/* $Id: JointWindow.java,v 1.5 2005/05/09 22:00:07 hlrossano Exp $ */
/*
 * Created on 30-Apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nrs.tracker.jointmanager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nrs.toolboxes.LayerPanel;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import nrs.tracker.palette.ExampleFileFilter;
import nrs.tracker.sticktrack.Group;
import nrs.tracker.sticktrack.Vector2D;
import nrs.tracker.stickgui.FindJointsInterface;
 
/**
 * @author Hugo L. Rosano
 */

public class JointWindow extends Toolbox implements JointWindowInterface, ListSelectionListener{
	
	private static Logger m_log = Logger.getLogger("nrs.tracker.sticktrack");
	
	private JList jsList;
	private JointSequenceManager jointSequenceManager;
	
	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenu menuDel = new JMenu();
	
	private JLabel totalSequences;
	private JLabel timeStamp;
	private JLabel jointTime;
	private JLabel sequenceSelected;
	private JLabel currentCoord;
	private JLabel jStepPhase;
	private JLabel fromTo;
	
	private MyJLabel transFrom;
	private MyJLabel transTo;
	private MyJLabel absRotate;
	private MyJLabel transX;
	private MyJLabel transY;
	private MyJLabel scaleTo;

	private JButton bt = new JButton("TEST");
	
	private JPopupMenu m_popupMenu;
	private final DefaultListModel jListSeqManager = new DefaultListModel();
	
	public static String TITLE = "JointSequenceManager";
	private JointInterface jointDelegate;
	private FindJointsInterface findDelegate;
	private String lastFileSaved = "";
	
	private final String POPUP_CHANGE_LAB = "Change Joint Label...";
	private final String POPUP_DELETE_SEQ = "Sequence...";
	private final String POPUP_DELETE_TOEND = "From here...";
	private final String POPUP_DELETE_JOINT = "Joint...";
	private final String POPUP_DELETE_ALL = "All";
	private final String POPUP_RENAME = "Rename Sequence...";
	private final String POPUP_HIDE = "Hide Sequence";
	private final String POPUP_SHOW = "Show Sequence";
	private final String POPUP_SWAP = "Swap with...";
	private final String POPUP_SPLIT = "Split...";
	private final String POPUP_ADJUST_JOINT = "Adjust Coordinate...";
	private final String POPUP_GLUE = "Glue with...";
	
	private final String POPUP_SAVE = "Save";
	private final String POPUP_SAVEAS = "Save As...";
	private final String POPUP_LOAD = "Load...";
	
	private final String LABEL_FROM = "From: ";
	private final String LABEL_TO = "To: ";
	private final String LABEL_ROTATION = "Rotation: ";
	private final String LABEL_XOFF = "Translation: (";
	private final String LABEL_YOFF = ",";
	private final String LABEL_SCALE = "Scale:";
	
	String PREF_KEY_SCALE = "K_SCALE";
	String PREF_KEY_ROTATION = "K_ROTATION";
	String PREF_KEY_XOFF = "K_OFFX";
	String PREF_KEY_YOFF = "K_OFFY";
	String PREF_KEY_FROM = "K_FROM";
	String PREF_KEY_TO = "K_TO";
	String PREF_KEY_LASTFILESAVED = "K_LASTFILESAVED";
	
	public JointWindow(Frame owner, ToolboxParent parent){
		super(owner, "JointManager", parent, "JointManagerToolbox_");
		jointSequenceManager = new JointSequenceManager();
		guiInit();
	}
	
	public void guiInit(){
		m_log.fine("initializing JointWindow");
		jsList = new JList(jListSeqManager);
		jsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jsList.addListSelectionListener(this);
	    jsList.addMouseListener(
                new MouseAdapter()
                {
                  public void mouseClicked(MouseEvent e)
                  { if (e.isPopupTrigger()) popup(e); }

                  public void mousePressed(MouseEvent e)
                  { if (e.isPopupTrigger()) popup(e); }

                  public void mouseReleased(MouseEvent e)
                  { if (e.isPopupTrigger()) popup(e); }
                });
	    
	    menuFile.setText("File");
	    menuDel.setText("Delete...");
	    
	    menuFile.add(makeMenuItem(POPUP_SAVE));
	    menuFile.add(makeMenuItem(POPUP_SAVEAS));
	    menuFile.add(makeMenuItem(POPUP_LOAD));
	    
	    menuDel.add(makeMenuItem(POPUP_DELETE_SEQ));
	    menuDel.add(makeMenuItem(POPUP_DELETE_TOEND));
	    menuDel.add(makeMenuItem(POPUP_DELETE_JOINT));
	    menuDel.add(makeMenuItem(POPUP_DELETE_ALL));
	    
	    menuBar.add(menuFile);
	    menuBar.add(menuDel);
	    this.setJMenuBar(menuBar);
	    
	    LayerPanel main = new LayerPanel();
	    LayerPanel seqList = new LayerPanel();
	    LayerPanel briefListInfo = new LayerPanel();
	    LayerPanel info = new LayerPanel();
	    LayerPanel leftInfo = new LayerPanel();
	    LayerPanel timeInf = new LayerPanel();
	    LayerPanel control = new LayerPanel();
	    LayerPanel displayControl = new LayerPanel();
	    
	    seqList.add(new JScrollPane(jsList), BorderLayout.CENTER);
	    briefListInfo.add(new JLabel("Joints:"));
	    totalSequences = new JLabel("0");
	    briefListInfo.add(totalSequences);
	    seqList.add(briefListInfo, BorderLayout.SOUTH);
	    
	    timeInf.add(new JLabel("Current time"));
	    timeStamp = new JLabel("0");
	    timeInf.add(timeStamp);
	    timeInf.createRow();
	    timeInf.add(new JLabel("Time stamp"));
	    jointTime = new JLabel("0");
	    timeInf.add(jointTime);
	    timeInf.createRow();
	    timeInf.add(new JLabel("Period:"));
	    fromTo = new JLabel("-");
	    timeInf.add(fromTo);
	
		timeInf.add(bt);
		bt.addKeyListener(new KeyAdapter(){
							public void keyPressed(KeyEvent e){handleKeyEvent(e);}
							public void keyReleased(KeyEvent e){}
							public void keyTyped(KeyEvent e){}
						}		
					);

	    timeInf.setBorder();
	    
	    control.add(new JLabel("Joint selected:"));
	    sequenceSelected = new JLabel("");
	    control.add(sequenceSelected);
	    control.createRow();
	    control.add(new JLabel("Coordinate: "));
	    currentCoord = new JLabel("()");
	    control.add(currentCoord);
	    control.createRow();
	    control.add(new JLabel("Label: "));
	    jStepPhase = new JLabel("");
	    control.add(jStepPhase);
	    control.setBorder();
	    
	    displayControl.add(new JLabel("Display options"));
	    displayControl.createRow();
	    transFrom = new MyJLabel(LABEL_FROM);
	    displayControl.add(transFrom);
	    displayControl.createRow();
	    transTo = new MyJLabel(LABEL_TO);
	    displayControl.add(transTo);
	    displayControl.createRow();
	    absRotate = new MyJLabel(LABEL_ROTATION);
	    displayControl.add(absRotate);
	    displayControl.createRow();
	    transX = new MyJLabel(LABEL_XOFF, 100);
	    transY = new MyJLabel(LABEL_YOFF, 100);
	    displayControl.add(transX);
	    displayControl.add(transY);
	    displayControl.add(new JLabel(")"));
	    displayControl.createRow();
	    scaleTo = new MyJLabel(LABEL_SCALE, 1);
	    displayControl.add(scaleTo);
	    displayControl.setBorder();
	    
	    leftInfo.add(timeInf);
	    leftInfo.createRow();
	    leftInfo.add(control);

	    info.add(displayControl);
	    info.add(leftInfo);
	    
	    main.addHorizontalSplitPane(seqList, info, BorderLayout.CENTER);
	    
	    m_popupMenu = new JPopupMenu("JointManager");
	    buildPopup();
	    
	    Container root = getContentPane();
	    root.setLayout(new BorderLayout());
	    root.add(main);
	    
	    setSize(400,300);
	    this.pack();
	    setResizable(true);

	}

	
	private void buildPopup(){
		m_popupMenu.add(makeMenuItem(POPUP_RENAME));
		m_popupMenu.add(makeMenuItem(POPUP_CHANGE_LAB));
		m_popupMenu.addSeparator();
		m_popupMenu.add(makeMenuItem(POPUP_ADJUST_JOINT));
		m_popupMenu.addSeparator();
		m_popupMenu.add(makeMenuItem(POPUP_HIDE));
		m_popupMenu.add(makeMenuItem(POPUP_SHOW));
		m_popupMenu.addSeparator();
		m_popupMenu.add(makeMenuItem(POPUP_SWAP));
		m_popupMenu.add(makeMenuItem(POPUP_SPLIT));
		m_popupMenu.add(makeMenuItem(POPUP_GLUE));
	}
	
	private void popup(MouseEvent e){
		m_popupMenu.show(jsList, e.getX(), e.getY());
	}
	
	public void valueChanged(ListSelectionEvent e){
		if (e.getValueIsAdjusting()) return;
		//Update the underlying palette with the next colour selection
		JointSequence jointSequenceSelected = getSelectedJointSequence();
		if (jointSequenceSelected != null){
			jointSequenceManager.setOnEdition(jointSequenceSelected.toString());
			updateInfoPane(jointSequenceSelected);
		}
	}
	
	private int stampTime(){
		return Integer.parseInt(timeStamp.getText());
	}
	
	/* *****************************************
	 * ***********JOINTWINDOWS INTERFACE METHODS
	 * *****************************************
	 */
	
	public JointSequenceManager getAdjustNice(){
		int from = (int)transFrom.getDouble();
		int to = (int)transTo.getDouble();
		double angle = absRotate.getDouble()*Math.PI/180.0; 
		Joint j = new Joint(Joint.REFERENCE, new Vector2D(transX.getDouble(),transY.getDouble()));
		double scale = scaleTo.getDouble();
		return jointSequenceManager.getAdjustNice(from, to, angle, j, scale);
	}
	
	public void setJointCallBack(JointInterface myJointInterface){
		jointDelegate = myJointInterface;
	}
	
	public void setFindJointCallBack(FindJointsInterface myFindJointsInterface){
		findDelegate = myFindJointsInterface;
	}
	
	public JointTimeInstance getPrevious(){
		return jointSequenceManager.getPrevious(stampTime()-1);
	}
	
	public void deleteAll(){
		m_log.fine("All sequences deleted");
		lastFileSaved = "";
		jointSequenceManager.clear();
		rebuildList();
		updateInfoPane();
	}
	
	public JointSequence getSelectedJointSequence(){
		if (jsList.isSelectionEmpty() || jointSequenceManager.isEmpty()) return null;
	    return (JointSequence) jsList.getSelectedValue();
	}
	
	public int addNewJoints(Group cg){
		int status = jointSequenceManager.addLinkedList(cg, stampTime() );
		int current = Integer.parseInt(totalSequences.getText());
		if (jointSequenceManager.size() != current){
			rebuildList();
		}
		updateInfoPane();
		transTo.setInt((int)transTo.getDouble()+1);
		this.invalidate();
		return status;
	}
	
	public JointSequenceManager getJointManager(){
		return jointSequenceManager;
	}
	
	public void setTimeStamp(int time){
		if (time == stampTime())
			return;
		if (time<0)
			time = 0;
		this.timeStamp.setText(String.valueOf(time));
		updateInfoPane();
	}
	
	/* *****************************************/
	
	private void rebuildList(){
		jListSeqManager.clear();
		Iterator it = jointSequenceManager.iterator();
		while(it.hasNext()){
			jListSeqManager.addElement(it.next());
		}
		totalSequences.setText(String.valueOf(jListSeqManager.size()));
	}
	
	private void updateInfoPane(){
		updateInfoPane(this.getSelectedJointSequence());
	}
	
	private void updateInfoPane(JointSequence js){
		if ((js!=null) && (!js.isEmpty())){
			sequenceSelected.setText(js.toString());
			Joint temp = js.getJoint(stampTime());
			fromTo.setText(String.valueOf(((Joint)js.getFirst()).getTime())+"->"+
						String.valueOf(((Joint)js.getLast()).getTime()));
			if (temp!=null){
				currentCoord.setText("("+(int)temp.getX()+","+(int)temp.getY()+")");
				jointTime.setText(" = this");
				jStepPhase.setText(Joint.stepPhaseToString(temp.getStepPhase()));
			}
			else{
				temp = (Joint)js.getLast();
				if (temp == null)
					return;
				currentCoord.setText("("+(int)temp.getX()+","+(int)temp.getY()+")");
				jointTime.setText(" last " + String.valueOf(temp.getTime()));	
				jStepPhase.setText("");
			}
		}
		else{
			currentCoord.setText("(-,-)");
			sequenceSelected.setText("-");
			jointTime.setText("-");
		}
		this.invalidate();
		repaint();
		if(jointDelegate!=null){
			jointDelegate.repaint();
			jointDelegate.repaint();
		}
	}
	
	public int getFrom(){
		return (int)transFrom.getDouble();
	}
	
	public int getTo(){
		return (int)transTo.getDouble();
	}
	
	public void restoreSettings(Preferences props){
		super.restoreSettings(props);
		transFrom.setText(props.get(PREF_KEY_FROM, LABEL_FROM+"0"));
		transTo.setText(props.get(PREF_KEY_TO, LABEL_TO+"100"));
		absRotate.setText(props.get(PREF_KEY_ROTATION, LABEL_ROTATION+"0"));
		transX.setText(props.get(PREF_KEY_XOFF, LABEL_XOFF+"100"));
		transY.setText(props.get(PREF_KEY_YOFF, LABEL_YOFF+"100"));
		scaleTo.setText(props.get(PREF_KEY_SCALE, LABEL_SCALE+"1"));
		lastFileSaved = props.get(PREF_KEY_LASTFILESAVED, "");
		loadJointManager();
	}
	
	public void saveSettings(Preferences props){
	    super.saveSettings(props);
	    props.put(PREF_KEY_FROM, transFrom.getText());
	    props.put(PREF_KEY_TO, transTo.getText());
	    props.put(PREF_KEY_ROTATION, absRotate.getText());
	    props.put(PREF_KEY_XOFF, transX.getText());
	    props.put(PREF_KEY_YOFF, transY.getText());
	    props.put(PREF_KEY_SCALE, scaleTo.getText());
	    props.put(PREF_KEY_LASTFILESAVED, lastFileSaved);
	}
	
	private void handleEvent(ActionEvent e){
		if ((e.getActionCommand() == POPUP_LOAD)){
			String fileName = getFileName();
			if (fileName != "")
				loadJointManager(fileName);
			rebuildList();
			updateInfoPane();
		}
		if(jsList.isSelectionEmpty()){
			return;
		}
		boolean noProblems = true;
		if ((e.getActionCommand() == POPUP_DELETE_ALL) ){
			deleteAll();
	    }
		if ((e.getActionCommand() == POPUP_DELETE_TOEND) ){
			noProblems = deleteToEnd();
	    }		
		if ((e.getActionCommand() == POPUP_DELETE_SEQ) ){
			deleteSequence();
	    }
		if ((e.getActionCommand() == POPUP_DELETE_JOINT) ){
			deleteJoint();
	    }
		if ((e.getActionCommand() == POPUP_RENAME) ){
			rename();
	    }
		if ((e.getActionCommand() == POPUP_SWAP) ){
			noProblems = swap();
	    }
		if ((e.getActionCommand() == POPUP_SPLIT) ){
			noProblems = split();
	    }
		if ((e.getActionCommand() == POPUP_ADJUST_JOINT) ){
			noProblems = adjustJoint();
	    }
		if ((e.getActionCommand() == POPUP_CHANGE_LAB) ){
			changeLabels();
		}
		if ((e.getActionCommand() == POPUP_GLUE)){
			noProblems = glueSequences();
		}
		if ((e.getActionCommand() == POPUP_SHOW)){
			setSeqVisible();
		}
		if ((e.getActionCommand() == POPUP_HIDE)){
			setSeqInvisible();
		}
		if ((e.getActionCommand() == POPUP_SAVEAS)){
			String fileName = getFileName();
			if (fileName != "")
				saveAsJointManager(fileName);
		}
		if ((e.getActionCommand() == POPUP_SAVE)){
			saveJointManager();
		}
		if (!noProblems){
			m_log.fine("Some problems found");
			JOptionPane.showMessageDialog(this,"Some problems found","Problem",JOptionPane.ERROR_MESSAGE);
		}
		rebuildList();
		updateInfoPane();
	}
	
	private void setSeqVisible(){
		getSelectedJointSequence().setVisible(true);
	}
	
	private void setSeqInvisible(){
		getSelectedJointSequence().setVisible(false);
	}
	
	private boolean deleteToEnd(){
		if(JOptionPane.showConfirmDialog(this, "Joints from here to end of sequence will be deleted\nDo you want to proceed?", "Warning", JOptionPane.YES_NO_OPTION)
				== JOptionPane.OK_OPTION ){
				return jointSequenceManager.removeToEnd(getSelectedJointSequence().getIdName(), stampTime());
		}
		return true;
	}
	
	private boolean glueSequences(){
		Object o = JOptionPane.showInputDialog(this,"Choose a sequence to glue","Glue",JOptionPane.QUESTION_MESSAGE,null,jointSequenceManager.toArray(),jointSequenceManager.get(0));
		String swapWith = o.toString();
		String swap = getSelectedJointSequence().getIdName();
		if(swapWith.equals(swap))
			JOptionPane.showMessageDialog(this, "Cannot glue with itself");
		return jointSequenceManager.glueJoints(getSelectedJointSequence().getIdName(), swapWith);		
	}
	
	private void deleteSequence(){
		if(JOptionPane.showConfirmDialog(this, "All sequence will be deleted\nDo you want to proceed?", "Warning", JOptionPane.YES_NO_OPTION)
				== JOptionPane.OK_OPTION){
			jointSequenceManager.remove(getSelectedJointSequence());
		}		
	}
	
	private void deleteJoint(){
		if(JOptionPane.showConfirmDialog(this, "Last entry will be deleted\nDo you want to proceed?", "Warning", JOptionPane.YES_NO_OPTION)
				== JOptionPane.OK_OPTION){
			getSelectedJointSequence().removeLast();				
		}		
	}
	
	private boolean split(){
		if(JOptionPane.showConfirmDialog(this, "Joint sequence is going to be divided\nDo you want to proceed?", "Warning", JOptionPane.YES_NO_OPTION)
				== JOptionPane.OK_OPTION){
			return jointSequenceManager.splitJoint(getSelectedJointSequence().getIdName(),stampTime());
		}		
		return false;
	}
	
	private boolean adjustJoint(){
		Joint joint = getSelectedJointSequence().getJoint(stampTime());
		if (!jointDelegate.adjustJoint(joint))
			return false;
		jointDelegate.repaint();
		return true;
	}
	
	private boolean rename(){
		String newName = JOptionPane.showInputDialog(this, "Type new name for Joint", "Rename", JOptionPane.QUESTION_MESSAGE);
		if (newName!=""){
			getSelectedJointSequence().setIdName(newName.trim().toLowerCase());
			return true;
		}
		JOptionPane.showMessageDialog(this, "Invalid name", "Rename error", JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	private boolean swap(){
		Object o = JOptionPane.showInputDialog(this,"Choose a sequence to swap","Swap",JOptionPane.QUESTION_MESSAGE,null,jointSequenceManager.toArray(),jointSequenceManager.get(0));
		String swapWith = o.toString();
		String swap = getSelectedJointSequence().getIdName();
		if(swapWith.equals(swap))
			JOptionPane.showMessageDialog(this, "Cannot swap with itself");
		return jointSequenceManager.swapJoints(getSelectedJointSequence().getIdName(), swapWith, stampTime());		
	}
	
	private void changeLabels(){
		String [] labels = {"Stance", "Swing", "Body"};
		Object o = JOptionPane.showInputDialog(this,"Choose new label","New Label",JOptionPane.QUESTION_MESSAGE,null,labels,labels[0]);
		if(o==null)
			return;
		int newStep = Joint.stepPhaseToInt(o.toString());
		if (newStep!=0){
			Object[] options = {"Just one", "Time period"};
			int question = JOptionPane.showOptionDialog(this, "How many joints in the sequence?", "Joint Problem",
				    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (question == JOptionPane.YES_OPTION)
				getSelectedJointSequence().getJoint(stampTime()).setStepPhase(newStep);
			else{
				String finalT = JOptionPane.showInputDialog(this,"Type final type for relabel","Final Time",JOptionPane.QUESTION_MESSAGE);
				getSelectedJointSequence().changeLabel(newStep,stampTime(),Integer.parseInt(finalT));
			}
			
		}		
	}
	
	private boolean saveJointManager(){
		if (lastFileSaved == "")
			return false;
		String fileName = lastFileSaved;
		return saveAsJointManager(fileName);
	}
	
	private void loadJointManager(){
		if (lastFileSaved == "")
			return;
		String fileName = lastFileSaved;
		loadJointManager(fileName);
		rebuildList();
		updateInfoPane();
	}
	
	private boolean saveAsJointManager(String fileName){
		try{
			FileOutputStream fos = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(jointSequenceManager);
			out.close();
			lastFileSaved = fileName;
			return true;
		}catch(IOException ioe){
			return false;
		}
	}
	
	private void increaseCopy(){
		JointSequence js = getSelectedJointSequence();
		if (js==null)	return;
		Joint jp = js.getJoint(stampTime());
		if(jp==null)	return;
		if (js.getJoint(stampTime()+1)!=null){
			findDelegate.increaseCount();
			return;
		}
		Joint newJ = (Joint)jp.clone();
		newJ.setTime(stampTime()+1);
		js.add(newJ);
		findDelegate.increaseCount();
	}
	
	private boolean loadJointManager(String fileName){
		try{
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			jointSequenceManager.clear();
			jointSequenceManager = (JointSequenceManager)ois.readObject();
			lastFileSaved = fileName;
			ois.close();
			return true;
		}catch(IOException ioe){
		}catch(ClassNotFoundException ioe){
		}
		return false;
	}
	
	private String getFileName(){
		JFileChooser chooser = new JFileChooser();
		ExampleFileFilter mfilter = new ExampleFileFilter();
		mfilter.addExtension("jsm");
		mfilter.setDescription("JointSequenceManager");
		chooser.setFileFilter(mfilter);
		String extension = ".jsm";
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)	
			return "";
		String _filename = chooser.getSelectedFile().getAbsolutePath();
		if (_filename.length() == 0) 
			return "";
		if (!_filename.endsWith(extension.toUpperCase()) && !_filename.endsWith(extension.toLowerCase()) )
			_filename += extension;
		return _filename;
	}	
	
	private JMenuItem makeMenuItem(String menuLabel){
		JMenuItem newItem = new JMenuItem(menuLabel);
	    newItem.addActionListener(
	                              new ActionListener()
	                              {
	                                public void actionPerformed(ActionEvent e)
	                                { handleEvent(e); }
	                              });
	    return newItem;
	}

	private void handleKeyEvent(KeyEvent e){
		JointSequence js = getSelectedJointSequence();
		if(js==null)
			return;
		Joint j = js.getJoint(stampTime());
		if(j==null)
			return;
		switch(e.getKeyCode()){
		case KeyEvent.VK_LEFT:
			j.inc(new Vector2D(-1,0));
		break;
		case KeyEvent.VK_RIGHT:
			j.inc(new Vector2D(1,0));
		break;
		case KeyEvent.VK_UP:
			j.inc(new Vector2D(0,-1));
		break;
		case KeyEvent.VK_DOWN:
			j.inc(new Vector2D(0,1));
		break;
		case KeyEvent.VK_SPACE:
			findDelegate.increaseCount();
		break;
		case KeyEvent.VK_BACK_SPACE:
			findDelegate.decreaseCount();
		break;
		case KeyEvent.VK_ENTER:
			increaseCopy();
		break;
		case KeyEvent.VK_C:
			System.out.println("-c-");
		break;
		}
		jointDelegate.repaint();
	}	
	
	class MyJLabel extends JLabel implements MouseListener{

		private String label;
		
		MyJLabel(String label, int value){
			super(label+value);
			this.label=label;
			this.addMouseListener(this);
		}
		
		MyJLabel(String label){
			this(label, 0);
		}
		
		public double getDouble(){
			return Double.parseDouble(getText().substring(label.length()) );
		}
		
		public void setDouble(double value){
			this.setText(label + value);
		}
		
		public void setInt(int value){
			this.setText(label + value);
		}
		
		public void mouseClicked(MouseEvent e) {
			String newVal = JOptionPane.showInputDialog(this, "Type new Value", "Value", JOptionPane.QUESTION_MESSAGE);
			if (newVal==null)
				return;
			newVal.trim();
			if (newVal == "")
				return;
			try{
				Double.parseDouble(newVal);
			}catch(Exception ex){
				return;
			}
			this.setText(label+newVal);
			jointDelegate.repaint();
		}

		public void mouseEntered(MouseEvent e) {
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}		
		
		public void mousePressed(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}
	
}