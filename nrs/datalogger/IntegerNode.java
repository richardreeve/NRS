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
package nrs.datalogger;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;
import nrs.core.type.IntegerType;
import nrs.core.type.StringType;
import nrs.core.type.VoidType;

import nrs.core.unit.Filename;
import nrs.core.unit.Time;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Represents node to log int messages for this component
 * 
 * @author Thomas French
*/

public class IntegerNode extends Node
{
  //Variables
  private StringType m_varDirName;
  private Time m_varTime;
  private IntegerType m_varValue;
  private VoidType m_varReset;

  private Filename m_filename;
  
  private File m_file;
  private FileWriter m_out;
  
  private BooleanType m_appendToFile;

  private final static String type = "IntegerNode";
  
  /** Constructor.
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param name vnname of the node
   */
  public IntegerNode(VariableManager vmMan, String name)
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
    m_varValue = new IntegerType(vmMan, varName3, true, false){
        public void deliver(Message m)
        {
          handleMessageAt_Value(m, this, checkType(m, this));
        }
        public void deliver(int i){
          handleMessageAt_Value(i);
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

    String varName5 = name+ ".Filename";
    m_filename = new Filename(vmMan, varName5, true, false){
        public void deliver(Message m)
        {
          handleMessageAt_Filename(m, this, checkType(m, this));
        }
        public void deliver(String s){
          handleMessageAt_Filename(s);
        }
      };
    addVariable(varName5, m_filename);

    String varName6 = name+ ".AppendToFile";
    m_appendToFile = new BooleanType(vmMan, varName6, true, false){
        public void deliver(Message m)
         {
           handleMessageAt_AppendToFile(m, this, checkType(m, this));
         }
        public void deliver(boolean b){
          handleMessageAt_AppendToFile(b);
        }
      };
    addVariable(varName6, m_appendToFile);
  }
  //----------------------------------------------------------------------
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m)
  {
    PackageLogger.log.fine("Received a message at: " + getVNName());
    
    //extract file name
    String s = m.getField("filename");
    if ( s == null )
      throw new NullPointerException("filename can't be null");
    
    if ( m_filename.getRestriction().valid(s) )
      m_filename.setValue(s);
    else
      PackageLogger.log.warning("Invalid filename: " + s);
        
    Boolean b = Boolean.valueOf(m.getField("append"));
    if ( b == null ) 
      m_appendToFile.setDefault(false);
    else
      m_appendToFile.setDefault(b.booleanValue());
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
    
    open_file();
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
    
    Integer i = m_varValue.extractData(m);
    handleMessageAt_Value(i.intValue());
  }
  /**
   * Process the receipt of a message at the Value variable.
   */
  public void handleMessageAt_Value(int i) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Value variable!");
    m_varValue.setValue(i);
    
    writeOut(); // write out value to file.
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
    
    m_appendToFile.reset();
    
    //open stream again.
    try{
      if ( m_out != null )
        m_out.close();
      
      m_out = new FileWriter(m_file, m_appendToFile.getValue());
      PackageLogger.log.info(getVNName() + " is writing to: " 
                             + m_file.getAbsoluteFile());
    }
    catch(IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
      ioe.printStackTrace();
    }
  }

  /**
   * Process the receipt of a message at the Filename variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Filename(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Filename variable!");

    String f = m_filename.extractData(m);
    if ( f != null )
      handleMessageAt_Filename(f);
  }
  /**
   * Process the receipt of a message at the Filename variable.
   */
  public void handleMessageAt_Filename(String s) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Filename variable!");
    m_filename.setValue(s);
    open_file();
  }

  /**
   * Process the receipt of a message at the AppendToFile variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_AppendToFile(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":AppendToFile variable!");

    Boolean b = m_appendToFile.extractData(m);
    if ( b != null )
      m_appendToFile.setValue(b);
  }
  /**
   * Process the receipt of a message at the AppendToFile variable.
   */
  public void handleMessageAt_AppendToFile(Boolean b) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":AppendToFile variable!");
    m_appendToFile.setValue(b);
  }  

  /** Open stream to file with current value of DirName and Filename. */
  private void open_file(){
    try{
      if ( m_out != null ) m_out.close();

      m_file = new File(m_varDirName.getValue()+ "/" + m_filename.getValue());
      PackageLogger.log.info(getVNName() + " is writing to: " 
                             + m_file.getAbsoluteFile());
      
      m_out = new FileWriter(m_file, m_appendToFile.getValue());
    }
    catch(IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
      ioe.printStackTrace();
    }
  }

  /** Write out value to file. */
  private void writeOut(){
    try{
      // write out as chars
      StringBuffer sb = new StringBuffer();
      
      sb.append(m_varTime.getValue().toString() + "\t");
      sb.append(m_varValue.getValue().toString() + "\n");
      
      m_out.write(sb.toString());
      m_out.flush();
    }
    catch(IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
      ioe.printStackTrace();
    }
  }
}
