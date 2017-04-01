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
package nrs.core.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/** Class for representing NRS nodes (which extend type {@link Variable}).
 * The main difference between a Node and a Variable, is that
 * Nodes can contains other Nodes.
 *
 * @author Thomas French
 */

public class Node extends Variable
{
  /** Store all Nodes this node contains. 
   * Each object is of type {@link Node}.
   */
  private ArrayList<Node> m_nodes;
  
  /**Contains the set of default variables belonging to this node. 
   * Each object in the collection is of type {@link Variable}.
   */
  protected HashMap m_localVars;
  
  /** Variable Manager to use. */
  private VariableManager m_vMan;
  
  //----------------------------------------------------------------------
  
  /** Constructor for <code>Node</code>
   *
   * @param ID the unique variable ID to use for this node (variable)
   * @param name the string name for this node (variable)
   *
   * @deprecated Don NOT use this anymore!
   */
  public Node(int ID, String name){
    super(ID, name);
    
    m_nodes = new ArrayList<Node>();
    m_localVars = new HashMap();
  }
  
  /** Constructor. Register with {@link VariableManager} with name.
   *
   * @param vMan VariableManager to register with.
   * @param name vnName of node.
   *
   * @deprecated Use constructor to register VNType instead.
   */
  public Node(VariableManager vMan, String name){
    super(vMan, name);
    
    m_vMan = vMan;
    m_nodes = new ArrayList<Node>();
    m_localVars = new HashMap();
  }
  
  /** Constructor. Register with {@link VariableManager} with name.
   *
   * @param vMan VariableManager to register with
   * @param name vnName of node
   * @param type vnType of node
   */
  public Node(VariableManager vMan, String name, String type){
    super(vMan, name, type);
    
    m_vMan = vMan;
    m_nodes = new ArrayList<Node>();
    m_localVars = new HashMap();
  }
  
  //----------------------------------------------------------------------
  
  /** Add <tt>Node</tt>,nd, contained by this Node to m_nodes.
   * Assumes that Node, nd, will add itself to VariableManager.
   * @param nd {@link nrs.core.base.Node} to add, which is contained by
   * this Node.
   */
  public void addNode(Node nd) {
    if ( !m_nodes.contains(nd) ) m_nodes.add(nd);
    else 
      PackageLogger.log.warning("Node \"" + nd + "\" already contained"
                                + "  by this node. Not adding.");
  } 
  
  /** Remove <tt>Node</tt> contained by this Node.
   * Will also remove the node from VariableManager.
   * @param nd {@link nrs.core.base.Node} to remove, which is contained 
   * by this Node.
   */
  public void removeNode(Node nd){
    if ( m_nodes.contains(nd) ){
      // remove links
      if ( nd.allLinks().size() > 0 )
        nd.removeAllLinks();
      
      //remove all children, etc.
      nd.removeAll();
      
      //remove from local data structure
      m_nodes.remove(m_nodes.indexOf(nd));
      
      //remove from Variable Manager
      m_vMan.remove(nd);
    }
    else 
      PackageLogger.log.warning("Node \"" + nd + "\" not contained by"
                                + " this node. Not deleting.");
  }
  
  /** Return list of all nodes contained by this node. 
   * @return <code>ArrayList<code> of nodes.
   */
  protected ArrayList<Node> allNodes(){
    return m_nodes;
  }
  
  //----------------------------------------------------------------------
  /** Add {@link nrs.core.base.Variable} to this <tt>Node</tt>. 
   * Assumes that Variable will add itself to VariableManager.
   * @param key key to store Variable with.
   * @param value Variable.
   */
  public void addVariable(Object key, Object value){
    if ( !m_localVars.containsKey(key) ) m_localVars.put(key, value);
    else
      PackageLogger.log.warning("Variable with value \"" + value 
                                + "\" already in this nodes collection."
                                + " Not adding.");
  }

  /** Remove {@link nrs.core.base.Variable} from this <tt>Node</tt>. 
   * Assumes that Variable will remove itself from VariableManager.
   * @param key key to find Variable with.
   */
  public void removeVariable(Object key){
    if ( m_localVars.containsKey(key) ){
      Variable v = (Variable) m_localVars.get(key);
      
      if ( v.allLinks().size() > 0 )
        v.removeAllLinks();
      
      m_localVars.remove(key);
      
      m_vMan.remove(v);
    }
    else
      PackageLogger.log.warning("Variable with key \""
                                + key
                                + "\" not contained by this nodes. " 
                                +" Not removing.");
  }
  
  /** Return list of all variables contained by this node. 
   * @return <code>Collection<code> of variables.
   */
  protected Collection allVariables(){
    return m_localVars.values();
  }
  
  //----------------------------------------------------------------------
  
  /** Remove all nodes, variables and links contained by this Node.  */
  public void removeAll(){
    Variable v;
    Node n;
    
    // remove all my Links first
    if ( allLinks().size() > 0 )
      removeAllLinks();
    
    for(Iterator i = m_localVars.values().iterator(); i.hasNext(); ){
      v = (Variable) i.next();
      
      if ( v.allLinks().size() > 0 )
        v.removeAllLinks();
      
      m_vMan.remove(v);
    }
    
    m_localVars.clear();
    
    // recurse down node tree and remove nodes from VariableManager. 
    for (int i = 0; i < m_nodes.size(); i++ ){
      n = (Node) m_nodes.get(i);
      
      //remove from my data structures
      removeNode(n);
    }
  }
  
  //----------------------------------------------------------------------
  
  /** Deliver a {@link Message} to this <code>Node</code>.
   * Should override in subclass.
   *
   * @param m Message to deliver to <code>Node</code>.
   */
  public void deliver(Message m){ }
  
  /** Need to reset state of {@link Variable}s.*/
  public void reset(){
    // recurse down tree of sub-nodes
    Node n;
    for (int i = 0; i < m_nodes.size(); i++ ){
      n = (Node) m_nodes.get(i);
      
      n.reset();
    }
    
    Variable v;
    for(Iterator i = m_localVars.values().iterator(); i.hasNext(); ){
      v = (Variable) i.next();
      v.reset();
    }
  }
}
