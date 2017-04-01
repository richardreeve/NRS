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
package nrs.wrapper;

import nrs.core.base.Message;
import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;

import nrs.core.type.BooleanType;
import nrs.core.type.VoidType;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.StringTokenizer;

/** Node that is used to execute a command with given arguments.
 * Through variables it is then possible to start and force stop the 
 * executing process.
 *
 * @author Thomas French
*/

public class ExecuteNode extends Node
{
  // Variables
  private VoidType m_varStart; // input
  private VoidType m_varStop; // input and output
  private BooleanType m_varRunning;// input and output

  private String[] m_cmd;
  
  private Process m_proc;

  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public ExecuteNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, "ExecuteNode");

    String varName = vnName+ ".Start";
    m_varStart = new VoidType(vmMan, varName){
        public void deliver(Message m)
        {
          handleMessageAt_Start(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Start();
        }
      };
    addVariable(varName, m_varStart);

    String varName2 = vnName+ ".Stop";
    m_varStop = new VoidType(vmMan, varName2){
        public void deliver(Message m)
        {
          handleMessageAt_Stop(m, this, checkType(m, this));
        }
        public void deliver(){
          handleMessageAt_Stop();
        }
      };
    addVariable(varName2, m_varStop);

    String varName3 = vnName + ".Running";
    m_varRunning = new BooleanType(vmMan, varName3, false, true){
        public void deliver(Message m)
        {
          handleMessageAt_Running(m, this, checkType(m, this));
        }
        public void deliver(boolean b){
          handleMessageAt_Running(b);
        }
      };
    addVariable(varName3, m_varRunning);
  }
  
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){ 
    PackageLogger.log.fine("Received a message at: " + getVNName());
    
    // Extract command and args - tokenise command attribute
    String s = m.getField("command");
    
    if ( s == null || s.trim().equals("") ){
      PackageLogger.log.warning("Must enter a command to execute with any"
                                + " necessary arguments");
      return;
    }
    
    ArrayList<String> l = new ArrayList<String>();
    
    StringTokenizer st = new StringTokenizer(s);
    while (st.hasMoreTokens()) {
      l.add(st.nextToken());
    }
    
    m_cmd = (String[]) l.toArray(new String[0]);
    
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < l.size();i++)
      sb.append(m_cmd[i] + " ");

    PackageLogger.log.info("Command is: " + sb.toString());
  }
  
  /**
   * Compares the {@link Message} type and the {@link Variable}
   * type. Returns true if they appear different (based on a
   * case-sensitive string comparison); returns false otherwise.
   */
  protected boolean checkType(Message m, Variable v){
    return !(m.getType().equals(v.getVNName()));
  }

  /**
   * Process the receipt of a message at the Start variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Start(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Start variable!");
    
    handleMessageAt_Start();
  }
  /**
   * Process the receipt of a message at the Start variable.
   */
  public void handleMessageAt_Start() 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Start variable!");
    if ( m_proc == null )
      startProcess();
  }

  /**
   * Process the receipt of a message at the Stop variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Stop(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Stop variable!");
    
    handleMessageAt_Stop();
  }
  /**
   * Process the receipt of a message at the Stop variable.
   */
  public void handleMessageAt_Stop() 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Stop variable!");
  
    // stop process if it is running
    if ( m_proc != null ){
      // this should cause waitFor() all to unblock and finish, 
      // causing Stop variable onEvent to occur
      m_proc.destroy(); 
    }
  }
  
   /**
   * Process the receipt of a message at the Running variable.
   *
   * @param m the {@link Message} received
   * @param v the {@link Variable} receiving the message
   * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
   * types
   *
   */
  public void handleMessageAt_Running(Message m, Variable v, boolean diff)
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Stop variable!");
    // extract boolean
    Boolean b = m_varRunning.extractData(m);

    if ( b != null )
      handleMessageAt_Running(b.booleanValue());
  }
  /**
   * Process the receipt of a message at the Stop variable.
   */
  public void handleMessageAt_Running(boolean b) 
  { 
    PackageLogger.log.fine("Received message at " + getVNName() 
                           + ":Stop variable!");
  
    // start process if one is not already running
    if ( b && m_proc == null ){
      startProcess();
    }
    // stop process if it is running
    else if ( !b && m_proc != null ){
      // this should cause waitFor() all to unblock and finish, 
      // causing Stop variable onEvent to occur
      m_proc.destroy();
    }
    // receive to start process already running
    else if ( b && m_proc != null ){
      PackageLogger.log.warning("Process is already started.");
      // use alternative behaviour: kill process and restart?
    }
    // stop non-existent process
    else{
      PackageLogger.log.warning("Received message to kill non-existent "
                                + " process.");
    }
  }

  /** Start process.*/
  private void startProcess(){
    
    try{
      m_proc = AppManager.getInstance().getRuntime().exec(m_cmd);
    }
    catch(IOException ioe){
      PackageLogger.log.warning("Error with execution of " + m_cmd[0] + ": " 
                                + ioe.getMessage());
      ioe.printStackTrace();
    }
    
    m_varRunning.onEvent(true);
    
    // dispatch new thread watching whether process completes
    Thread t = new Thread(new Runnable(){
        int exit_code;
        public void run(){
          try{
            exit_code = m_proc.waitFor();
          }
          catch(InterruptedException ie){
            PackageLogger.log.warning("Thread waiting for completion of " 
                                      + m_cmd[0] + " has been interrupted");
            return;
          }

          m_proc = null;

          PackageLogger.log.fine("Process finished executing command: " 
                                 + m_cmd[0] + " with exit value: " 
                                 + exit_code);
          m_varStop.onEvent();
          m_varRunning.onEvent(false);
        }
      });

    // dispatch new thread watching the output stream of process
    Thread t2 = new Thread(new Runnable(){
        public void run(){
          if ( m_proc == null )
            return;
          
          BufferedInputStream is = null;
          is = new BufferedInputStream(m_proc.getInputStream());
                              
          while( m_proc != null ){
            try{
              while( is != null && is.available() > 0 ){
                System.out.print((char) is.read());
              }
            }
            catch(IOException ie){
              // Most likely that process has finished executing.
              //PackageLogger.log.fine(ie.getMessage());
              //ie.printStackTrace();
            }
          }
          // try and close stream
          try{
            is.close();
          }
          catch(IOException i){}
          
          System.out.println();
        }
      });
    
    t2.start();
    t.start();
  }
}
