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

import javax.swing.JRadioButton;
import java.awt.*;
import java.awt.event.*;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.VoidType;

/** Represents LEDNodes for the label component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Darren Smith
 * @author Thomas French
*/

public class LEDNode extends Node
{
  /** GUI, <tt>MainFrame</tt>, to which Buttons are added. */
  private MainFrame m_mainFrame;
  
  /** JRadioButton which belongs to this <tt>ButtonNode</tt>. */
  private JRadioButton m_button;
  
  /** <tt>LEDNode</tt> variable Input. */
  //VoidVariable m_inputVar;
  private VoidType m_inputVar;
  
  /** Label . */
  String m_label;
  
  /** Blink time. */
  private int m_blink;

  private final static String type = "LEDNode";
  
  /** Constructor
   * @deprecated
   */
  public LEDNode(String name, int id){
    super(id, name);
  }
  
  /** Constructor. Use {@link VariableManager} to suggest vnid and register node.
   *
   * @param mainFrame user MainFrame to add button to GUI
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   */
  public LEDNode(MainFrame mainFrame, VariableManager vmMan, String vnName)
  {
    super(vmMan, vnName, type);
    m_mainFrame = mainFrame;
    
    //register input variable with VariableManager
    String varName = vnName+ ".Input";
    m_inputVar = new VoidType(vmMan, varName){
        public void deliver(Message m)
        {
          handleMessageAt_Input();
        }
        public void deliver(){
          handleMessageAt_Input();
        }
      };
    //add Output variable to this Node localVars
    addVariable(varName, m_inputVar);
  }
  
  public void handleMessageAt_Input() {
    m_button.doClick();
    try
      {
        Thread.sleep(m_blink);
      }
    catch(InterruptedException ie){ }
    m_button.doClick();
  }
  
  //----------------------------------------------------------------------//
  
  /** Remove label from GUI. */
  public void removeFromGUI(){
    m_mainFrame.removeBt(m_button);
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
    PackageLogger.log.fine("Received message " + m + " at LEDNode: " 
                           + getVNName());
   
    m_label = m.getField("label");
    if ( m_label == null | m_label.equals("") )
      m_label = "An LED";
    
    //if not created, create button, set label and add to GUI
    if ( m_button == null )
      {
        m_button = new JRadioButton(m_label);
        m_button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        m_mainFrame.addBt(m_button);
      }
    else if ( !m_button.getText().equals(m_label))
      {
        m_button.setText(m_label);
        m_button.setSize(m_button.getPreferredSize());
        m_mainFrame.packFrame();
      }
    
    m_blink = Integer.parseInt(m.getField("holdTime"));
    if (m_blink < 0 )
      m_blink = 1000;
  }
}
