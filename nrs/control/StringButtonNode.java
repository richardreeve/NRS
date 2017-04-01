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

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.StringType;

/** Represents StringButtonNodes for the button component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
*/

public class StringButtonNode extends Node implements ActionListener{

  /** GUI, <tt>MainFrame</tt>, to which Buttons are added. */
  private MainFrame m_mainFrame;
  
  /** JButton which belongs to this <tt>StringButtonNode</tt>. */
  private JButton m_button;
  /** JTextField which belongs to this <tt>StringButtonNode</tt>. */
  private JTextField m_textField;
  /** JPanel which belongs to this <tt>StringButtonNode</tt>. */
  private JPanel m_panel;
  
  /** <tt>StringButtonNode</tt> variable Output. */
  private StringType m_outputVar;
  
  /** Label of JButton. */
  private String m_label;
  
  private static final int COLUMNS = 6;

  private final static String type = "StringButtonNode";
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node.
   *
   * @param mainFrame user MainFrame to add button to GUI
   * @param vmMan VariableManager to register with.
   * @param vnName name of this node.
   */
  public StringButtonNode(MainFrame mainFrame, VariableManager vmMan, 
                         String vnName){
    super(vmMan, vnName, type);
    m_mainFrame = mainFrame;
    
    String varName = vnName+ ".Output";
    m_outputVar = new StringType(vmMan, varName, true, true){
        public void deliver(Message m)
        {
          handleMessageAt_Output(m, this, checkType(m, this));
        }
        public void deliver(String s){
          handleMessageAt_Output(s);
        }
        
      };
    addVariable(varName, m_outputVar);
  }
  
  //--------------------------------------------------------------------//
  /** Remove button from GUI. */
  public void removeFromGUI(){
    m_mainFrame.removeBt(m_panel);
  }
  
  /** Catch button events. */
  public void actionPerformed(ActionEvent e){
      String s = m_textField.getText();
    
      if ( s != null )
          m_outputVar.setValue(s);
  }
  
  //--------------------------------------------------------------------//
  /**
   * Process the receipt of a message at the StringButtonNode var - Output.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Output(Message m, Variable v, boolean diff) {
    PackageLogger.log.warning("Received message at Output variable."
                              +" This should not happen!");
  }
  /**
   * Process the receipt of a message at the StringButtonNode var - Output.
   *
   * @param s String value received
   *
   */
  public void handleMessageAt_Output(String s){
    PackageLogger.log.warning("Received message at Output variable."
                              +" This should not happen!");
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }
  
  //--------------------------------------------------------------------//
  /** Deliver a {@link Message} to this <code>Node</code>.
   *
   * @param m Message to deliver to <code>Node</code>.
   */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at StringButtonNode: " 
                           + getVNName());
    
    m_label = m.getField("label");
    if ( m_label == null | m_label.equals("") )
      m_label = "A buttton";
    
    String s = m.getField("value");
   
    if ( s.equals(""))
        s = m_outputVar.getValue();
    
    m_outputVar.setValue(s);
    
    //if not created, create button, set label and add to GUI
    if ( m_panel == null ){
      m_panel = new JPanel();
      
      m_textField = new JTextField(s, 
                                   StringButtonNode.COLUMNS);
      m_textField.addActionListener(this);
      
      m_button = new JButton(m_label);
      m_button.setAlignmentX(Component.CENTER_ALIGNMENT);
      m_button.addActionListener(this);

      m_panel.add(m_button);
      m_panel.add(m_textField);
      
      m_mainFrame.addBt(m_panel);
    }
    else if ( !m_button.getText().equals(m_label)) 
      {
        m_button.setText(m_label);
        m_button.setSize(m_button.getPreferredSize());
        m_mainFrame.packFrame();
      }
  }
}
