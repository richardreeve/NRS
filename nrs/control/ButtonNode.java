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

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.*;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.VoidType;

/** Represents ButtonNodes for the button component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
*/

public class ButtonNode extends Node implements ActionListener{
  
  /** GUI, <tt>MainFrame</tt>, to which Buttons are added. */ 
  private MainFrame m_mainFrame;
  
  /** JButton which belongs to this <tt>ButtonNode</tt>. */
  private JButton m_button;
  
  /** <tt>ButtonNode</tt> variable Output. */
  private VoidType m_outputVar;
  
  /** Label of JButton. */
  private String m_label;
  
  private final static String type = "ButtonNode";

  /** Constructor 
   * @deprecated
   */
  public ButtonNode(String name, int id){
    super(id, name);
  }
  
  /** Constructor. Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param mainFrame user MainFrame to add button to GUI
   * @param vmMan VariableManager to register with.
   * @param vnName name of this node.
   */
  public ButtonNode(MainFrame mainFrame, VariableManager vmMan, 
                    String vnName){
    super(vmMan, vnName, type);
    m_mainFrame = mainFrame;
    
    String varName = vnName+ ".Output";
    m_outputVar = new VoidType(vmMan, varName){
        public void deliver(Message m)
        {
          handleMessageAt_Output();
        }
        public void deliver()
        {
          handleMessageAt_Output();
        }
      };
    addVariable(varName, m_outputVar);
  }
  
  //-----------------------------------------------------------------------//
  
  /** Remove button from GUI. */
  public void removeFromGUI(){
    m_mainFrame.removeBt(m_button);
  }
  
  /** Catch button events. */
  public void actionPerformed(ActionEvent e){
    PackageLogger.log.fine("Button clicked for : " + getVNName() 
                           + " sending out messages over links!");
    
    //send out void message over any links the Output variable has.	
    m_outputVar.onEvent();
  }
  
  //-----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the ButtonNode variable - Output.
   *
   */
  public void handleMessageAt_Output() { 
    PackageLogger.log.warning("Received message at Output variable."
                              + " This should not happen!");
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  
  //-----------------------------------------------------------------------//
  /** Deliver a {@link Message} to this <code>Node</code>.
   *
   * @param m Message to deliver to <code>Node</code>.
   */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at ButtonNode: " 
                           + getVNName());
    
    m_label = m.getField("label");
    if ( m_label == null | m_label.equals("") )
      m_label = "A buttton";
    
    //if not created, create button, set label and add to GUI
    if ( m_button == null ){ 
      m_button = new JButton(m_label);
      m_button.setAlignmentX(Component.CENTER_ALIGNMENT);
      m_button.addActionListener(this);
      
      m_mainFrame.addBt(m_button);
    }
    //change text on button
    else if ( !m_button.getText().equals(m_label)) 
      {
        m_button.setText(m_label);
        m_button.setSize(m_button.getPreferredSize());
        m_mainFrame.packFrame();
      }
  }
}
