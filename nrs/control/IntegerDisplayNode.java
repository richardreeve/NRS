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

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.IntegerType;

/** Represents IntegerDisplayNodes for the button component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
*/

public class IntegerDisplayNode extends Node {
    
  /** GUI, <tt>MainFrame</tt>, to which Panels are added. */ 
  private MainFrame m_mainFrame;
  
  /** JLabel which belongs to this <tt>IntegerDisplayNode</tt>. */
  private JLabel m_label;
  /** JTextField which belongs to this <tt>IntegerDisplayNode</tt>. */
  private JTextField m_textField;
  /** JPanel which belongs to this <tt>IntegerDisplayNode</tt>. */
  private JPanel m_panel;
  
  /** <tt>IntegerDisplayNode</tt> variable Input. */
  private IntegerType m_varInput;
  
  /** Label of JLabel. */
  private String m_labelText;
  
  private static final int COLUMNS = 6;

  private final static String type = "IntegerDisplayNode";
  
  /** Constructor. Use {@link VariableManager} to suggest vnid and 
   * register node. 
   *
   * @param mainFrame user MainFrame to add label to GUI
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   */
  public IntegerDisplayNode(MainFrame mainFrame, VariableManager vmMan, 
                            String vnName){
    super(vmMan, vnName, type);
    m_mainFrame = mainFrame;
    
    //register Input variable with VariableManager
    String varName = vnName+ ".Input";
    m_varInput = new IntegerType(vmMan, varName, true, false){
        public void deliver(Message m)
        {
          handleMessageAt_Input(m, this, checkType(m, this));
        }
        public void deliver(int i){
          handleMessageAt_Input(i);
        }
	
      };
    //add Input variable to this Node localVars
    addVariable(varName, m_varInput);
  }
  
  //-----------------------------------------------------------------------//
  
  /** Remove label from GUI. */
  public void removeFromGUI(){
    m_mainFrame.removeBt(m_panel);
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
    PackageLogger.log.fine("Received message at IntegerDisplayNode: " 
                           + getVNName());
    
    m_labelText = m.getField("label");
    if ( m_labelText == null | m_labelText.equals("") )
      m_labelText = "A label";
    
    //if not created, create label, set label and add to GUI
    if ( m_panel == null ){ 
      m_panel = new JPanel();
      
      // set default value
      m_textField = new JTextField(m_varInput.getValue().toString(), 
                                   IntegerDisplayNode.COLUMNS);
      m_textField.setEditable(false);
      
      m_label = new JLabel(m_labelText);
      
      m_panel.add(m_label);
      m_panel.add(m_textField);
      
      m_mainFrame.addBt(m_panel);
    }else if ( !m_label.getText().equals(m_labelText))
      {
        m_label.setText(m_labelText);
        m_label.setSize(m_label.getPreferredSize());
        m_mainFrame.packFrame();
      }
  }
  
  //-----------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the IntegerDisplayNode variable 
   * - Input.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 
    PackageLogger.log.fine("Received message at Input variable!");
    
    Integer i = m_varInput.extractData(m);
    
    handleMessageAt_Input(i);	
  }
  /**
   * Process the receipt of a message at the IntegerDisplayNode var - Input.
   *
   * @param i int value received
   *
   */
  public void handleMessageAt_Input(int i){ 
    PackageLogger.log.fine("Received message at Input variable!");
    
    m_textField.setText(Integer.toString(i));
    m_textField.setPreferredSize(m_textField.getPreferredSize());
    
    m_varInput.setValue(i);
  }
}
