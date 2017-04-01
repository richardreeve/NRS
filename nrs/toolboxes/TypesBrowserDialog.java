// $Id: TypesBrowserDialog.java,v 1.4 2005/09/16 16:09:46 s0125563 Exp $
package nrs.toolboxes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import nrs.csl.CSL_Element_Constraint;
import nrs.csl.CSL_Element_Description;
import nrs.csl.CSL_Element_FloatInfo;
import nrs.csl.CSL_Element_IntegerInfo;
import nrs.csl.CSL_Element_Message;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Element_Segment;
import nrs.csl.CSL_Element_StringInfo;
import nrs.csl.CSL_Element_Unit;
import nrs.csl.CSL_Message_Registry;
import nrs.csl.CSL_Node_Registry;
import nrs.csl.CSL_Unit_Registry;
import nrs.csl.CaseInsensitiveMessageComparator;
import nrs.csl.CaseInsensitiveNodeComparator;
import nrs.csl.CaseInsensitiveUnitComparator;

/** An extension of {@link Toolbox} which displays NRS unit, message and
 * node types */
public class TypesBrowserDialog extends Toolbox implements ActionListener,
                                                         ListSelectionListener
{

  ///// GUI components
  JButton  m_closeButton;

  JList     m_unitList;
  JTextArea m_unitText;
  JSplitPane m_unitPanel;

  JList     m_msgList;
  JTextArea m_msgText;
  JSplitPane m_msgPanel;

  JList     m_nodeList;
  JTextArea m_nodeText;
  JSplitPane m_nodePanel;

  /** Link back to the underlying message registry. */
  CSL_Message_Registry m_msgReg;

  /** Link back to the underlying node registry. */
  CSL_Node_Registry m_nodeReg;

  /** Link back to the underlying unit registry. */
  CSL_Unit_Registry m_unitReg;

  /** Title of the dialog */
  public static String TITLE = "Types Browser";

  ///// Name of entries in the properties file
  private static String NODE_DIVIDER = "TYPE_BROSWER_NODE_DIVIDER";
  private static String MESSAGE_DIVIDER = "TYPE_BROSWER_MESSAGE_DIVIDER";
  private static String UNIT_DIVIDER = "TYPE_BROSWER_UNIT_DIVIDER";

  //----------------------------------------------------------------------
  /** Constructor */
  public TypesBrowserDialog(Frame owner,
                            CSL_Unit_Registry unitReg,
                            CSL_Node_Registry nodeReg,
                            CSL_Message_Registry msgReg,
                            ToolboxParent parent)
  {
    super(owner, TITLE, parent, "TypesBrowserDialog_");

    m_unitReg = unitReg;
    m_nodeReg = nodeReg;
    m_msgReg = msgReg;

    // TODO - this is just an example of how to do icons. Really need to
    // make some nice icons though, for units, messages and nodes, and
    // then create an images directory which gets installed etc. Once
    // all that is done, this code section can be updated.
    ImageIcon icon = null;
    URL imageURL = TypesBrowserDialog.class.getResource("neuron_icon.png");

    if (imageURL != null)
      {
        icon = new ImageIcon(imageURL);
      }

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Units", icon, constructUnitPanel(),
                      "Browse unit types");
    tabbedPane.addTab("Messages", icon,  constructMessagePanel(),
                      "Browse message types");
    tabbedPane.addTab("Nodes", icon, constructNodePanel(),
                      "Browse node types");

    m_closeButton = new JButton("Close");
    m_closeButton.addActionListener(this);
    JPanel closePanel = new JPanel();
    closePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    closePanel.add(m_closeButton);

    // Configure the root panel
    JPanel root = (JPanel) this.getContentPane();
    root.setLayout(new BorderLayout());

    root.add(closePanel, BorderLayout.SOUTH);
    root.add(tabbedPane, BorderLayout.CENTER);

    setSize(300,300);

    this.pack();
  }
  //----------------------------------------------------------------------
  /** Construct and return the panel which shows details of NRS units */
  private JPanel constructUnitPanel()
  {
    m_unitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JPanel bottomPanel = new JPanel();

    // Configure the top panel
    m_unitList = new JList();
    m_unitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    m_unitList.addListSelectionListener(this);
    populateUnitList();
    m_unitText = new JTextArea();
    m_unitText.setLineWrap(true);
    m_unitText.setWrapStyleWord(true);
    m_unitText.setEditable(false);
    m_unitPanel.setTopComponent(new JScrollPane(m_unitList));
    m_unitPanel.setBottomComponent(new JScrollPane(m_unitText));

    // Configure the lowest panel
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(m_unitPanel, BorderLayout.CENTER);

    return panel;
  }
  //----------------------------------------------------------------------
  /** Construct and return the panel which shows details of NRS node types */
  private JPanel constructNodePanel()
  {
    m_nodePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JPanel bottomPanel = new JPanel();

    // Configure the top panel
    m_nodeList = new JList();
    m_nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    m_nodeList.addListSelectionListener(this);
    populateNodeList();
    m_nodeText = new JTextArea();
    m_nodeText.setLineWrap(true);
    m_nodeText.setWrapStyleWord(true);
    m_nodeText.setEditable(false);
    m_nodePanel.setTopComponent(new JScrollPane(m_nodeList));
    m_nodePanel.setBottomComponent(new JScrollPane(m_nodeText));

    // Configure the lowest panel
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(m_nodePanel, BorderLayout.CENTER);

    return panel;
  }
  //----------------------------------------------------------------------
  /** Construct and return the panel which shows details of NRS message
      types */
  private JPanel constructMessagePanel()
  {
    m_msgPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JPanel bottomPanel = new JPanel();

    // Configure the top panel
    m_msgList = new JList();
    m_msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    m_msgList.addListSelectionListener(this);
    populateMsgList();
    m_msgText = new JTextArea();
    m_msgText.setLineWrap(true);
    m_msgText.setWrapStyleWord(true);
    m_msgText.setEditable(false);
    m_msgPanel.setTopComponent(new JScrollPane(m_msgList));
    m_msgPanel.setBottomComponent(new JScrollPane(m_msgText));

    // Configure the lowest panel
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(m_msgPanel, BorderLayout.CENTER);

    return panel;
  }
  //----------------------------------------------------------------------
  /** Invoked when an action occurs on one of the GUI components. */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == m_closeButton)
      {
        this.setVisible(false);
      }
  }
  //----------------------------------------------------------------------
  public void valueChanged(ListSelectionEvent e)
  {
    if ((e.getSource() == m_unitList) &&  (e.getValueIsAdjusting() == false))
      {
        showUnitDetails((CSL_Element_Unit) m_unitList.getSelectedValue());
        return;
      }

    if ((e.getSource() == m_msgList) &&  (e.getValueIsAdjusting() == false))
      {
        showMessageDetails((CSL_Element_Message) m_msgList.getSelectedValue());
      }

    if ((e.getSource() == m_nodeList) &&  (e.getValueIsAdjusting() == false))
      {
        showNodeDetails((CSL_Element_NodeDescription)
                        m_nodeList.getSelectedValue());
      }
  }
  //----------------------------------------------------------------------
  /** Populate the GUI list component with the unit names. */
  public void populateUnitList()
  {
    Vector listData = new Vector();

    Iterator it = m_unitReg.getUnitIterator();

    // Build up a list of the entries in the unit registry
    while (it.hasNext())
      {
        CSL_Element_Unit unit = (CSL_Element_Unit) it.next();
        listData.add(unit);
      }

    // Now sort the list we have made, using the Collections sort algorithm
    Collections.sort(listData, CaseInsensitiveUnitComparator.getInstance());

    m_unitList.setListData(listData);

    // Now refresh GUI
    invalidate();
  }
  //----------------------------------------------------------------------
  /** Populate the GUI list component with the node names. */
  public void populateNodeList()
  {
    Vector listData = new Vector();

    Iterator it = m_nodeReg.getNodeIterator();

    // Build up a list of the entries in the node registry
    while (it.hasNext())
      {
        CSL_Element_NodeDescription node =
          (CSL_Element_NodeDescription) it.next();
        listData.add(node);
      }

    // Now sort the list we have made, using the Collections sort algorithm
    Collections.sort(listData, CaseInsensitiveNodeComparator.getInstance());

    m_nodeList.setListData(listData);

    // Now refresh GUI
    invalidate();
  }
  //----------------------------------------------------------------------
  /** Populate the GUI list component with the message names. */
  public void populateMsgList()
  {
    Vector listData = new Vector();

    Iterator it = m_msgReg.getMessageIterator();

    // Build up a list of the entries in the message registry
    while (it.hasNext())
      {
        CSL_Element_Message unit = (CSL_Element_Message) it.next();
        listData.add(unit);
      }

    // Now sort the list we have made, using the Collections sort algorithm
    Collections.sort(listData, CaseInsensitiveMessageComparator.getInstance());

    m_msgList.setListData(listData);

    // Now refresh GUI
    invalidate();
  }
  //----------------------------------------------------------------------
  void showMessageDetails(CSL_Element_Message msg)
  {
    String s = "";

    s += "Name : " + msg.getMessageName() + "\n";

    if (msg.getDesc() != null)
      {
        CSL_Element_Description desc = msg.getDesc();
        s += "Description : " + desc.getDescription() + "\n";
      }

    if (msg.isImplicit())
      {
        s += "Defined implicitly.\n";
      }

    // Now display the unit segments
    Iterator it = msg.getSegmentIterator();

    while ((it != null) && (it.hasNext()))
      {
        CSL_Element_Segment seg = (CSL_Element_Segment) it.next();

        s += "\n";
        s += "Segment : " + seg.getSegmentName() + "\n";
        s += "Unit : " + seg.getSegmentUnit() + "\n";

        if (seg.getDesc() != null)
          {
            s += "Description : "
              + seg.getDesc().getDescription() + "\n";
          }

        if (seg.getSegmentStringInfo() != null)
          {
            s += "TODO: show actual details of the StringInfo\n";
          }

      }

    m_msgText.setText(s);
  }
  //----------------------------------------------------------------------
  void showNodeDetails(CSL_Element_NodeDescription node)
  {
    m_nodeText.setText("Todo");
  }
  //----------------------------------------------------------------------
  void showUnitDetails(CSL_Element_Unit unit)
  {
    String s = "";

    s += "Name : " + unit.getUnitName() + "\n";
    s += "Type : " + unit.getUnitType() + "\n";

    if (unit.getFloatInfo() != null)
      {
        CSL_Element_FloatInfo ni = unit.getFloatInfo();
        s += "FloatInfo : " + ni.getSummary();
      }

    if (unit.getIntegerInfo() != null)
      {
        CSL_Element_IntegerInfo ni = unit.getIntegerInfo();
        s += "IntegerInfo : " + ni.getSummary();
      }

    if (unit.getStringInfo() != null)
      {
        CSL_Element_StringInfo si = unit.getStringInfo();
        s += "StringInfo : " + si.getSummary();
      }

    if (unit.getDesc() != null)
      {
        CSL_Element_Description desc = unit.getDesc();
        s += "Description : " + desc.getDescription() + "\n";
      }

    //    if (unit.getValueList() != null)
    //      {
    //        ArrayList values = unit.getValueList();
    //        s += "List choices : ";
    //        for (int i = 0; i < values.size(); i++)
    //          {
    //            s+= "'" + values.get(i) + "'";
    //            if (i < values.size()) s += ", ";
    //          }
    //        s += "\n";
    //      }

    //    if (unit.getUnitConstraint() != null)
    //      {
    //        CSL_Element_Constraint constraint = unit.getUnitConstraint();
    //        s += "Constraint : ";
    //        s += constraint.getLowerBoundStr();
    //        s += " to ";
    //        s += constraint.getUpperBoundStr();
    //        s += "\n";
    //     }

    if (unit.isImplicit())
      {
        s += "Defined implicitly.\n";
      }

    m_unitText.setText(s);
  }
  //----------------------------------------------------------------------
  /** Provide a {@link java.util.prefs.Preferences} object which allows self
   * to save various settings for future sessions. */
  public void saveSettings(Preferences prefs)
  {
    prefs.putInt(NODE_DIVIDER, m_nodePanel.getDividerLocation());
    prefs.putInt(MESSAGE_DIVIDER, m_msgPanel.getDividerLocation());
    prefs.putInt(UNIT_DIVIDER, m_unitPanel.getDividerLocation());

    super.saveSettings(prefs);
  }
  //----------------------------------------------------------------------
  /** Provide a {@link java.util.prefs.Preferences} object which allows self
   * to restore various settings saved from previous sessions. */
  public void restoreSettings(Preferences prefs)
  {
    m_nodePanel.setDividerLocation(prefs.getInt(NODE_DIVIDER, 50));
    m_msgPanel.setDividerLocation(prefs.getInt(MESSAGE_DIVIDER, 50));
    m_unitPanel.setDividerLocation(prefs.getInt(UNIT_DIVIDER, 50));

    super.restoreSettings(prefs);
  }
}
