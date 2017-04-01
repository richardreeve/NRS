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
import java.util.Collection;
import java.util.Iterator;

/** Manage IntegerButtonNodes.
 *
 *
 * @author Thomas French
*/

class IntegerButtonNodeManager {

  /** Store vnIDs of JButton nodes created using VNName as key. */
  private HashMap m_id_by_name;
  /** Store vnNames of JButton nodes created using vnID as key. */
  private HashMap m_name_by_id;
  
  private HashMap m_button_by_id;
  
  /** Singleton instance of <code>IntegerButtonNodeManager</code> class. */
  private static IntegerButtonNodeManager m_singletonInstance = null;
  
  /** 
   * Return the singleton instance of class IntegerButtonNodeManager.
   *
   * @return singleton instance of class IntegerButtonNodeManager.
   */
  public static IntegerButtonNodeManager getInstance(){
    if (m_singletonInstance == null) new IntegerButtonNodeManager();
    
    return m_singletonInstance;
  }
  
  /** Private constructor, uses singleton patter. */
  private IntegerButtonNodeManager(){
    if (m_singletonInstance == null) m_singletonInstance = this;
    
    m_id_by_name = new HashMap();
    m_name_by_id = new HashMap();
    m_button_by_id = new HashMap();
  }
  
  /** Add new IntegerButtonNode by vnName (IntegerButtonLabel variable) 
   * and vnID.
   *
   * @param bn JButton node to add 
   */
  public void add(IntegerButtonNode bn){
    Integer vnid = new Integer(bn.getVNID());
    String vnName = bn.getVNName();
    
    if ( m_id_by_name.containsKey(vnName) || m_name_by_id.containsKey(vnid)){ 
      PackageLogger.log.warning("This vnName or vnID already exists, "
                                + " cannot add."); 
      return; 
    }
    
    m_id_by_name.put(vnName, vnid);
    m_name_by_id.put(vnid, vnName);
    m_button_by_id.put(vnid, bn);
  }    
  
  /** Delete IntegerButton node by vnid.
   *
   * @param vnid id of IntegerButton node to be removed.
   */
  public void remove(Integer vnid){
    String vnName = (String) m_name_by_id.remove(vnid);
    m_id_by_name.remove(vnName);
    
    IntegerButtonNode bn = (IntegerButtonNode) m_button_by_id.get(vnid);
    bn.removeFromGUI();
  }
  
  /** Delete IntegerButton node by name.
   *
   * @param vnName name of IntegerButton node to be removed.
   */
  public void remove(String vnName){
    Integer id = (Integer) m_id_by_name.remove(vnName);
    m_name_by_id.remove(id);
    
    Integer vnid = (Integer) m_id_by_name.get(vnName);
    IntegerButtonNode bn = (IntegerButtonNode) m_button_by_id.get(id);
    bn.removeFromGUI();
  }
  
  /** Remove all IntegerButtonNodes from IntegerButtonNodeManager. */
  public void removeAll(){
    m_id_by_name.clear();
    m_name_by_id.clear();
    
    IntegerButtonNode bn;
    //iterate through and remove all buttons from GUI
    for (Iterator i = m_button_by_id.values().iterator(); i.hasNext(); ){
      bn = (IntegerButtonNode) i.next();
      bn.removeAll();
      bn.removeFromGUI();
    }
  }
  
  /** Get the vnID for variable or node with <code>vnName</code>.
   *
   * @param vnName name of variable or node to lookup.
   * @return vnID of node specified by vnName.
   */
  public Integer getVNID(String vnName){
    if ( m_id_by_name.containsKey(vnName) ) 
      return (Integer) m_id_by_name.get(vnName);
    else return null;
  }
  
  /** Get the vnName for variable or node with <code>vnid</code>
   *
   * @param vnid the VNID of variable or node to lookup.
   * @return vnName of node with vnID.
   */
  public String getVNName(Integer vnid){
    if ( m_name_by_id.containsKey(vnid) ) 
      return (String) m_name_by_id.get(vnid);
    else return "";
  }
  
  /** Check that the vnID specified exists as a IntegerButton node.
   *
   * @param vnid the VNID of variable or node to lookup.
   * @return determines whether vnID exists as a IntegerButton node. 
   *         Empty string if not.
   */
  public String checkIntegerButtonType(Integer vnid){
    if ( m_button_by_id.containsKey(vnid) ) return "IntegerButtonNode";
    else return "";
  }  
}
