package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import nrs.core.base.Message;
import nrs.core.base.MessageProcessor;
import nrs.core.message.Constants;

/**
 * A simple collection of all the nodes and variables the user has
 * constructed using the GUI. Any {@link UserVN} created as the result
 * of a user request should also be added here.
 *
 * <p>This is class also provides a useful node-name lookup operation.
 *
 * @author Darren Smith
 */
class NodeManager extends MessageProcessor
{
  /** Reference to the list of nodes created in the GUI.  Each value in
    * in the list is of type {@link UserVN}, and the key is the qualified
    * name of the element (which should all be unique). */
  private final Map m_items = new HashMap();

  //----------------------------------------------------------------------
  /**
   * Return a count of the number of individual nodes in existence.
   */
  int size()
  {
    return m_items.size();
  }
  //----------------------------------------------------------------------
  /**
   * Test whether a specified node name exists.
   */
  boolean exists(String qName)
  {
    return m_items.containsKey(qName);
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link UserVN} found for the specified qualified name
   * (VNName), or <tt>null</tt> if no such node found.
   */
  UserVN getUserVN(String vnName)
  {
    return (UserVN) m_items.get(vnName);
  }
  //----------------------------------------------------------------------
  /**
   * Add a new {@link UserVN}. The node is stored by its qualified name
   * (VNName), so a check is first made to ensure this name does not
   * already exist. If it already exists a warning is issued.
   *
   * @see #exists(String)
   */
  void add(UserVN n)
  {
    if (!exists(n.VNName()))
    {
      m_items.put(n.VNName(), n);
    }
    else
    {
      PackageLogger.log.warning("Can't add node " + n.toString()
                                + "; qualified VNName already exists: "
                                + n.VNName());
    }
  }
  //----------------------------------------------------------------------
  /**
   * Remove an existing {@link UserVN}
   */
  void remove(UserVN n)
  {
    if (n != null)
    {
      m_items.remove(n.VNName());
    }
  }
  //----------------------------------------------------------------------
  /**
   * Receive ReplyVNID messages and attempt to update remote object info
   * for the appropriate {@link UserVN} indicated. After this they are
   * passed to the destination {@link MessageProcessor}. Messages which
   * are not of type ReplyVNID are ignored, and a warning is logged. In
   * order for this class to know which {@link UserVN} to update, the
   * received message <tt>m</tt> should also possess, in its auxillary
   * fields, and original QeuryVNID message request. The latter allows a
   * VNName to be extracted, which can be used to lookup a {@link
   * UserVN} instance.
   *
   * @param m the {@link Message} being transferred
   * @param sender not used; set it to <tt>null</tt>
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    if (m.getType() == Constants.MessageTypes.ReplyVNID)
    {
      // we need the original message, in order to find the VNName
      Message request = m.aux().getOrigMessageRequest() ;
      if (request != null)
      {
        if (request.hasField(Constants.MessageFields.vnName))
        {
          String vnName = request.getField(Constants.MessageFields.vnName);
          String vnid = m.getField(Constants.MessageFields.vnid);

          // some debugging
          try
          {
            int i_vnid = Integer.parseInt(vnid);

            if (i_vnid == 0)
            {
              PackageLogger.log.warning("Received a "
                                        + Constants.MessageTypes.ReplyVNID
                                        + " message which contain a VNID of 0."
                                        + " Message: " + m.diagString());
            }
          }
          catch (NumberFormatException exception)
          {
            PackageLogger.log.warning("Expected integer value for VNID"
                                      + " but received '" + vnid + "':"
                                      + m.diagString());
          }


          UserVN vnNode = getUserVN(vnName);
          if (vnNode != null)
          {
            vnNode.handleReplyVNID(m);
          }
          else
          {
            PackageLogger.log.warning("NodeManager received "
                                      + m
                                      + " but no related variable/node"
                                      + " exists with VNName : "
                                      + vnName);
          }
        }
        else
        {
          PackageLogger.log.warning("NodeManager can't find VNName from"
                                    + " original " + m + " message");
        }
      }
      else
      {
        PackageLogger.log.warning("NodeManager ignoring " + m + " message : "
                                  + "Original message request not available");
      }
    }
    else
    {
      PackageLogger.log.warning("NodeManager ignoring message type : "
                                + m);
    }

    next(m);
  }
}
