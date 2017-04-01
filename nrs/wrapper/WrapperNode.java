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

import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

/** Represents top-level Node for the component.
 *
 * @author Thomas French
*/

public class WrapperNode extends Node
{
  private final static String type = "WrapperNode";

  /** Constructor. 
   *
   * @param vmMan {@link VariableManager} to register node with
   * @param vnName vnname of the node
   */
  public WrapperNode(VariableManager vmMan, String vnName){
    super(vmMan, vnName, type);
  }
  
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){ 
    PackageLogger.log.fine("Received a message at: " + getVNName());
  }
}
