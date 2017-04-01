/*
 * Copyright (C) 2004 Edinburgh University
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
package nrs.control;

import java.util.HashMap;

import javax.swing.JCheckBox;
import java.awt.*;
import java.awt.event.*;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;

/** Represents ButtonNodes for the button component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
*/

public class CheckBoxNode extends Node implements ActionListener{
    
  /** GUI, <tt>MainFrame</tt>, to which Buttons are added. */ 
  private MainFrame m_mainFrame;
  
  /** JButton which belongs to this <tt>ButtonNode</tt>. */
  private JCheckBox m_checkBox;
  
  /** <tt>ButtonNode</tt> variable Output. */
  private BooleanType m_outputVar;
  
  /** Label of JCheckBox. */
  private String m_label;

  private final static String type = "CheckBoxNode";
  
  /** Constructor. Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param mainFrame user MainFrame to add button to GUI
   * @param vmMan VariableManager to register with.
   * @param vnName name of this node.
   */
  public CheckBoxNode(MainFrame mainFrame, VariableManager vmMan, 
                      String vnName){
    super(vmMan, vnName, type);
    m_mainFrame = mainFrame;
    
    String varName = vnName+ ".Output";
    m_outputVar = new BooleanType(vmMan, varName, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Output(m, this, checkType(m, this));
        }
        public void deliver(boolean b)
        {
          handleMessageAt_Output(b);
        }
      };
    addVariable(varName, m_outputVar);
  }
  
  //-----------------------------------------------------------------------//
  
  /** Remove button from GUI. */
  public void removeFromGUI(){
    m_mainFrame.removeBt(m_checkBox);
  }
  
  /** Catch button events. */
  public void actionPerformed(ActionEvent e){
    PackageLogger.log.fine("CheckBox clicked for : " + getVNName() 
                           + " sending out messages over links!");
    
    boolean b = m_checkBox.isSelected();
    m_outputVar.onEvent(b);
  }
  
  //-----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the ButtonNode variable - Output.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Output(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at Output variable!");
    //Boolean b = Boolean.valueOf(m.getField("boolean"));

    Boolean b = m_outputVar.extractData(m);
    
    if ( b != null )
      handleMessageAt_Output(b.booleanValue());
  }

  /**
   * Process the receipt of a message at the ButtonNode variable - Output.
   *
   * @param b boolean value received
   *
     */
  public void handleMessageAt_Output(boolean b){
    PackageLogger.log.fine("Received message at Output variable!");
    m_checkBox.setSelected(b);
    
    m_outputVar.onEvent(b);
  }    
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  
  //----------------------------------------------------------------------//
  /** Deliver a {@link Message} to this <code>Node</code>.
   *
   * @param m Message to deliver to <code>Node</code>.
   */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at CheckBoxNode: " 
                           + getVNName());
    
    m_label = m.getField("label");
    if ( m_label == null || m_label.equals("") )
      m_label = "A check box";
    
    //if not created, create check box, set label and add to GUI
    if ( m_checkBox == null ){ 
      m_checkBox = new JCheckBox(m_label, 
                                 Boolean.valueOf(m.getField("on"))
                                 .booleanValue());
      m_checkBox.setAlignmentX(Component.CENTER_ALIGNMENT);
      m_checkBox.addActionListener(this);
      
      m_mainFrame.addBt(m_checkBox);
    }

    else if ( !m_checkBox.getText().equals(m_label)) 
      {
        m_checkBox.setText(m_label);
        m_checkBox.setSize(m_checkBox.getPreferredSize());
        m_mainFrame.packFrame();
      }
  }
}
