package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JOptionPane;
import nrs.core.base.MessageException;
import nrs.csl.CSL_Element_Interface;
import nrs.csl.CSL_Element_Message;
import nrs.util.job.JobManager;

/**
 * Manage the construction of inter-node / variable links.
 *
 * @author  Darren Smith
 */
public class LinkManager
{
  /** Collection of {@link Link} objects - is this really being used?
   * Why would I need a central list of all links? This makes it
   * trouble-some to have separate networks, etc. */
  private final Collection m_links = new ArrayList();

  // The current source & target variables selected
  private NodeVariable m_variable;

  /** Collection of observers, each of type {@link LinkManagerListener} */
  private final Collection m_observers = new ArrayList();

  private JobManager m_jm;
  //----------------------------------------------------------------------
  /**
   * Constructor.
   */
  public LinkManager(JobManager jm)
  {
    m_jm = jm;
  }
  //----------------------------------------------------------------------
  /**
   * Add a state change observer
   */
  public void addObserver(LinkManagerListener observer)
  {
    if (observer != null)
    {
      m_observers.add(observer);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Remove a state change observer
   */
  public void removeObserver(LinkManagerListener observer)
  {
    if (observer == null) return;

    while (m_observers.contains(observer)) m_observers.remove(observer);
  }
  //----------------------------------------------------------------------
  /**
   * Set the source variable for a potential link. Can be set to
   * null.
   */
  public void setVariable(NodeVariable newSource)
  {
    if ( newSource == m_variable ) return;

    m_variable = newSource;
    notifyVariableChanged();
  }
  //----------------------------------------------------------------------  
  /**
   * Get the current variable. Can return null.
   */
  public NodeVariable getVariable()
  {
    return m_variable;
  }
  //----------------------------------------------------------------------
  /**
   * Utility method for creating single links, which are then added to
   * the list.
   */
  private void makeSingleLink(NodeVariable source, NodeVariable target)
  {
    if ((source != null) && (target != null))
    {
      Link newLink = new Link(source, target, true);
      PackageLogger.log.fine("Link created from \""
                             + source + "\" to \""
                             + target + "\"");
      m_links.add(newLink);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Should be called in response to the user selecting a single node
   * variable
   *
   */
  public void setSelected(NodeVariable _nodeV)
  {
    setVariable(_nodeV);
  }
  //----------------------------------------------------------------------
  /** Check if possible to create link between two variables. */
  public boolean checkPossibleLink(NodeVariable source, NodeVariable target){
    if ( areCompatible(source, target) && okayOnDirection(source, target) )
      return true;
    
    return false;
  }
  //----------------------------------------------------------------------
  /** Check if these variables can connect according to their interfaces.*/
  private boolean okayOnDirection(NodeVariable source, NodeVariable target){
    Iterator intfIter;  // interface iterator
    boolean reject;
    
    Link link = new Link(source, target, false);
    
    // Check that source has at least one output interface
    reject = true;
    intfIter = link.getSource().getType().getInterfaceIterator();
    while (intfIter.hasNext() && reject == true)
      {
        CSL_Element_Interface intf = (CSL_Element_Interface) intfIter.next();
        if (intf.canOut()) reject = false;
      }
    
    if (reject == false)
      {
        reject = true;
        intfIter = link.getTarget().getType().getInterfaceIterator();
        while (intfIter.hasNext() && reject == true)
          {
            CSL_Element_Interface intf = 
              (CSL_Element_Interface) intfIter.next();
            if (intf.canIn()) reject = false;
          }
      }
    
    if (reject == true)
      {
        return false;
      }
    
    return true;
  }
  //----------------------------------------------------------------------
  /**
   * Search if a {@link Link} exists between the specified source and
   * target. If found, return it, else, return <tt>null</tt>.
   */
  public Link findLink(NodeVariable source, NodeVariable target)
  {
    // iterator through the links of the source
    for (Iterator i = source.linkIterator(); i.hasNext(); )
    {
      Link link = (Link) i.next();

      if ((link.getSource() == source)  && (link.getTarget() == target))
      {
        return link;
      }
    }

    return null;
  }
  //----------------------------------------------------------------------
  /**
   * Filter on message type compatibility
   *
   * @param s the {@link NodeVariable} providing a message
   *
   * @param t the {@link NodeVariable} receiving a message
   */
  public boolean areCompatible(NodeVariable s, NodeVariable t)
  {
    CSL_Element_Message sm = s.getType().getType();
    CSL_Element_Message st = t.getType().getType();

    if ((sm == null) || (st == null))
    {
  PackageLogger.log.severe("s.getType().getType()=" + s.getType().getType()
                           + " s=" + s);;
  PackageLogger.log.severe("t.getType().getType()=" + t.getType().getType()
                           + " t=" + t);

      return false;
    }
 
    return sm.compatibleWith(st);
  }
  //----------------------------------------------------------------------
  private void notifyVariableChanged()
  {
    for (Iterator i = m_observers.iterator(); i.hasNext(); )
    {
      LinkManagerListener lml = (LinkManagerListener) i.next();
      lml.variableChanged();
    }
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  public void makeLink(NodeVariable source, NodeVariable target)
  {
    makeLink(new Link(source, target, true));
  }

  //----------------------------------------------------------------------
  /**
   * Establishes the specified link by updating both the source and
   * target of the link (as determined by the fields of
   * <tt>link</tt>). An attempt will also be made to create the link on
   * the remote component when in online mode.
   */
  public void makeLink(Link link)
  {
    // _TODO_(deprecate m_links - instead search each node variable);
    if (m_links.contains(link)) return;

    link.getSource().addLink(link);
    link.getTarget().addLink(link);
    m_links.add(link);
    link.setExists(true);

    CreateLinkJob job =
      new CreateLinkJob(AppManager.getInstance().getProgrammer(), link);

    m_jm.add(job);

//     try
//     {
//       AppManager.getInstance().getProgrammer().onlineCreate(link);
//     }
//     catch (MessageException e)
//     {
//       final String msg = e.toString();
//       // Place feedback box in a separate thread
//       Thread userFeedback = new Thread()
//         {
//           public void run()
//           {
//             JOptionPane.showMessageDialog(AppManager.getInstance()
//                                           .getMainFrame(),
//                                           msg.toString(),
//                                           "Warning",
//                                           JOptionPane.ERROR_MESSAGE);
//           }
//         };
//       userFeedback.start();
//       PackageLogger.log.warning(e.toString());
//     }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to remove the specified link
   */
  void removeLink(Link link)
  {
    if (!m_links.contains(link))
    {
      PackageLogger.log.warning("Attempting to remove link="
                                + link + " which doesn't already exist");
      return;
    }

//     try
//     {
      // Do remote delete
      DeleteLinkJob job =
        new DeleteLinkJob(AppManager.getInstance().getProgrammer(), link);
      m_jm.add(job);

    //      AppManager.getInstance().getProgrammer().onlineDelete(link);

      link.getSource().removeLink(link);
      link.getTarget().removeLink(link);
      m_links.remove(link);
      link.setExists(false);
//     }
//     catch (MessageException e)
//     {
//       final String msg = e.toString();
//       // Place feedback box in a separate thread
//       Thread userFeedback = new Thread()
//         {
//           public void run()
//           {
//             JOptionPane.showMessageDialog(AppManager.getInstance()
//                                           .getMainFrame(),
//                                           msg.toString(),
//                                           "Warning",
//                                           JOptionPane.ERROR_MESSAGE);
//           }
//         };
//       userFeedback.start();
//       PackageLogger.log.warning(e.toString());
//     }
  }

} // class
