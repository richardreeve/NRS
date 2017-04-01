/*
 * Copyright (C) 2006 Edinburgh University
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
package nrs.oscilloscope.nodes;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;
import nrs.core.type.FloatType;
import nrs.core.type.StringType;
import nrs.core.type.VoidType;

import nrs.core.unit.Time;
import nrs.core.unit.Filename;

import nrs.oscilloscope.oscilloscopeGUI.AnalogueOscilloscope;

/** Represents node to log float messages for this component
 * 
 * @author Theophile Gonos
 *
*/

public class FloatNode extends Node
{
  //Variables  
    private StringType m_varDirName;
    private Time m_varTime;
    private FloatType m_varValue;
    private VoidType m_varReset;
  
    private AnalogueOscilloscope _myOscilloscope;

    private static final String type = "FloatNode";
  
  /** Constructor.
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param name vnname of the node
   */
  public FloatNode(VariableManager vmMan, String name)
  {
    super(vmMan, name, type);

    String varName = name+ ".DirName";
    m_varDirName = new StringType(vmMan, varName, true, false){
	    public void deliver(Message m)
	    {
		handleMessageAt_DirName(m, this, checkType(m, this));
	    }
	    public void deliver(String s){
		handleMessageAt_DirName(s);
	    }
	};
    addVariable(varName, m_varDirName);

     String varName2 = name+ ".Time";
     m_varTime = new Time(vmMan, varName2, true, false){
        public void deliver(Message m)
        {
          handleMessageAt_Time(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Time(d);
        }
      };
    addVariable(varName2, m_varTime);
    
    String varName3 = name+ ".Value";
    m_varValue = new FloatType(vmMan, varName3, true, false){
        public void deliver(Message m)
        {
          handleMessageAt_Value(m, this, checkType(m, this));
        }
        public void deliver(double d){
          handleMessageAt_Value(d);
        }
      };
    addVariable(varName3, m_varValue);
    
    String varName4 = name+ ".Reset";
    m_varReset = new VoidType(vmMan, varName4){
        public void deliver(Message m)
        {
          handleMessageAt_Reset(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Reset();
        }
      };
    addVariable(varName4, m_varReset);


    open_oscilloscope();
  }
  //----------------------------------------------------------------------
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m)
  {
    PackageLogger.log.fine("Received a message at: " + getVNName());
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

   /**
   * Process the receipt of a message at the DirName variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_DirName(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":DirName variable!");
    
    String s = m_varDirName.extractData(m);
    if ( s != null )
      handleMessageAt_DirName(s);
  }
  /**
   * Process the receipt of a message at the DirName variable.
   */
  public void handleMessageAt_DirName(String s) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":DirName variable!");
    
    m_varDirName.setValue(s);
    
  }

  /**
   * Process the receipt of a message at the Time variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Time(Message m, Variable v, boolean diff) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Time variable!");
    
    Double d = m_varTime.extractData(m);
    
    if ( d != null )
      handleMessageAt_Time(d.doubleValue());
  }
  /**
   * Process the receipt of a message at the Time variable.
   */
  public void handleMessageAt_Time(double d) { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Time variable!");
    m_varTime.setValue(d);
  } 
  
  /**
   * Process the receipt of a message at the Value variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Value(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Value variable!");
    
    Double d = m_varValue.extractData(m);
    handleMessageAt_Value(d.doubleValue());
  }
  /**
   * Process the receipt of a message at the Value variable.
   */
  public void handleMessageAt_Value(double d) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Value variable!");
    m_varValue.setValue(d);
    
    drawOut(); // draw out value to file.
  }
  
  /**
   * Process the receipt of a message at the Reset variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Reset(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Reset variable!");
    
    handleMessageAt_Reset();
  }
  /**
   * Process the receipt of a message at the Reset variable.
   */
  public void handleMessageAt_Reset() 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Reset variable!");

    // re-initialise the oscilloscope ? ToDo

  }
  
  /** Open oscilloscope window. */
  private void open_oscilloscope(){
      _myOscilloscope = new AnalogueOscilloscope ("Displaying : "
						  +m_varDirName.getValue());
  }

  /** Draw out value to oscilloscope. */
  private void drawOut(){
      _myOscilloscope.nextPoint((m_varValue.getValue()),
				m_varTime.getValue()*0.001);
  }
}
