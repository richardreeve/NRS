package nrs.core.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import nrs.core.message.Constants;
import nrs.core.message.ReplyVNID;
import nrs.core.message.ReplyVNName;

/**
 * Manages the existence of all {@link Variable} objects within an
 * application. The variables are stored in two ways: by their numeric
 * VNID value; and by their VNName string. Typically the former will be
 * unique for each instance of a variable, and so it serves as an ideal
 * lookup key; however, this is not true for the latter, and so caution
 * should be taken when adding and removing {@link Variable} objects if
 * you intend to use class to provide a by-VNName lookup service.
 *
 * @author Darren Smith
 * @author Thomas French
 */
public class VariableManager extends MessageProcessor
{
  /** Object used for sending messages out of application */
  private OutboundPipeline m_outboundPipeline;

  /** Mapping for message numeric ID to the actual variable. Keys are of
   * type {@link Integer} and values {@link Variable}.
   */
  private Map m_mapID;

  /** Mapping for message names to the actual variable. Keys are of type
   *{@link String} and values {@link Variable}.
   */
  private Map m_mapName;

  /** Notes the next ID number which is free, according to all variables
   * added here, or to any requested ID handed out. Note : 0 cannot be
   * used for a variable ID.
   */
  private int m_nextID = 1;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  public VariableManager()
  {
    m_mapID = new HashMap();
    m_mapName = new HashMap();
  }
  //----------------------------------------------------------------------
  /**
   * Set the outbound pipeline which methods of this class can use when
   * they need to send messages to other NRS components.
   */
  public void setOutboundPipeline(OutboundPipeline outboundPipeline)
  {
    m_outboundPipeline = outboundPipeline;
  }
  //----------------------------------------------------------------------
  /**
   * Add a new {@link Variable}. Take care when using this class for
   * by-VNName lookup purposes. Calling this method with different
   * {@link Variable} objects that possess the same VNName attribute
   * will cause the first registered variable to be lost from the
   * by-VNName lookup table.
   */
  public void add(Variable v)
  {
    if (m_mapID.containsValue(v))
    {
      PackageLogger.log.warning("Variable \""
                                + v
                                + "\" already in collection. Not adding.");

      return;
    }
    m_mapID.put(new Integer(v.getVNID()), v);
    m_mapName.put(v.getVNName(), v);

    if (v.getVNID() >= m_nextID) m_nextID = v.getVNID()+1;
  }
  //----------------------------------------------------------------------
  /**
   * Remove an existing {@link Variable}.
   */
  public void remove(Variable mv)
  {
    m_mapID.remove(new Integer(mv.getVNID()));
    m_mapName.remove(mv.getVNName());
  }
  //----------------------------------------------------------------------
  /**
   * Retrieve a {@link Variable} by-VNID lookup.
   *
   * @return the {@link Variable} found, or else <tt>null</tt> if no
   * object was found with that ID
   */
  public Variable get(int id)
  {
    return (Variable) m_mapID.get(new Integer(id));
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to retrieve an object by its name. If there are multiple
   * objects corresponding to <tt>name</tt>, then it is undefined which
   * instance is returned.
   *
   * @return the {@link Variable} found, or else <tt>null</tt> if no
   * object was found for this <tt>name</tt>
   */
  public Variable get(String name)
  {
    return (Variable) m_mapName.get(name);
  }
  //----------------------------------------------------------------------
  /**
   * Suggest a numeric ID for a new variable. This is determined by
   * choosing a number which is greater than all other ID's that exist
   * within the stored variable collection, and higher than any other
   * suggested ID returned previously.
   */
  public int suggestID()
  {
    return m_nextID++;
  }
  //----------------------------------------------------------------------
  /**
   * Return the highest numeric ID assigned to variables. <b>Note</b>: do not
   * use this method for obtaining new port IDs. Use {@link
   * #suggestID()} instead.
   */
  public int getMaxID()
  {
    return m_nextID-1;
  }
  //----------------------------------------------------------------------
  /**
   * Return a {@link Collection} of all {@link Variable} objects
   * stored. The by-VNID lookup table is used for this operation, so
   * there is no danger of this collection being incompleted due to
   * possible by-VNName conflicts. In the returned collection, each
   * element can be safely cast to type {@link Variable}.
   */
  public Collection getAll()
  {
    return m_mapID.values();
  }
  //----------------------------------------------------------------------
  /** Address the received message to a VN node.
   *
   * <p>If the message <tt>m</tt> is intelligent and contains the
   * <tt>iTargetVNName</tt> field, an attempt is made to address the
   * message to a variable with that name.
   *
   * <p>Otherwise, the value of <tt>toVNID</tt> will be taken, and used
   * to identify a {@link Variable}.
   *
   * <p>If either of these attempts fail, the message is passed to the
   * next message processor.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    if (m.hasNRSField(Constants.MessageFields.intelligent)
        && m.hasNRSField(Constants.MessageFields.iTargetVNName))
    {
      String vnName = m.getNRSField(Constants.MessageFields.iTargetVNName);

      if ( vnName != null )
      {
        Variable mv = get(vnName);

        if ( mv != null )
        {
          mv.deliver(m);
          return;
        }
        else
        {
          PackageLogger.log.warning("Message " + m
                                    + " is address to an unrecognised"
                                    + " variable " + vnName);
        }
      }
    }
    else if (m.hasNRSField(Constants.MessageFields.toVNID))
    {
      int toVNID = 0;
      try
      {
        toVNID = Integer.
          parseInt(m.getNRSField(Constants.MessageFields.toVNID));
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning("Message " + m + " has a "
                                  +  Constants.MessageFields.toVNID
                                  + " field with a non-numeric value ("
                                  + m.getNRSField(Constants.MessageFields.toVNID)
                                  + "): can't route to variable");
        next(m);
      }
      
      if (toVNID > 0)
      {
        Variable mv = get(toVNID);

        if (mv != null)
        {
          mv.deliver(m);
          return;
        }
      }
    }

    // default behaviour: pass to next
    next(m);
  }
  //----------------------------------------------------------------------
  /**
   * Since a {@link VariableManager} instance should contain knowledge
   * of all NRS-variables existing within an application, it is best
   * placed to answer QueryVNID messages directed at an NRS
   * component. So this method builds an appropriate ReplyVNID message
   * and returns it.
   *
   * @param q a {@link Message} representing a QueryVNID.
   */
  public Message buildReplyToQueryVNID(Message q) throws FieldNotFoundException
  {
    ReplyVNID r = new ReplyVNID();

    // check and throw exceptions
    q.checkField(Constants.MessageFields.vnName);

    // grab the variable - T. French fixed bug here
    Variable mv = get(q.getField(Constants.MessageFields.vnName)); 

    // use the variable to set the fields
    r.setFields(q, mv);

    return r;
  }
  //----------------------------------------------------------------------
  /**
   * Since a {@link VariableManager} instance should contain knowledge
   * of all NRS-variables existing within an application, it is best
   * placed to answer QueryVNID messages directed at an NRS
   * component. So this method builds and sends an appropriate ReplyVNID
   * message to a received QueryVNID.
   *
   * @param q a {@link Message} representing a QueryVNID.
   */
  public void replyToQueryVNID(Message q)
  {
    replyToQueryVNID(q, m_outboundPipeline);
  }
  //----------------------------------------------------------------------
  /**
   * Reply to a ReplyVNID request, using the provided
   * <code>destination</code> for dispatching the reply Message.
   *
   * @param q a {@link Message} representing a QueryVNID.
   *
   * @param destination a {@link MessageProcessor} that will be sent the
   * reply message.
   */
  public void replyToQueryVNID(Message q, MessageProcessor destination)
  {
    if (m_outboundPipeline == null)
    {
      PackageLogger.log.warning("Unable to send "
                                + Constants.MessageTypes.ReplyVNID
                                + " : "
                                + "no outbound message pipeline");
      return;
    }

    try
    {
      Message r = buildReplyToQueryVNID(q);
      m_outboundPipeline.submit(r);
    }
    catch (FieldNotFoundException fe)
    {
      PackageLogger.log.warning("Unable to send "
                                + Constants.MessageTypes.ReplyVNID
                                + " : "
                                + fe.getMessage());
    }
  }
  //----------------------------------------------------------------------
  /**
   * Since a {@link VariableManager} instance should contain knowledge
   * of all NRS-variables existing within an application, it is best
   * placed to answer QueryVNName messages directed at an NRS
   * component. So this method builds and sends an appropriate
   * ReplyVNName message to a received QueryVNName.
   *
   * @param q a {@link Message} representing a QueryVNName.
   */
  public void replyToQueryVNName(Message q)
  {
    try
    {
      ReplyVNName reply = new ReplyVNName();

      q.checkField(Constants.MessageFields.vnid);

      Variable mv
        = get(Integer.parseInt(q.getField(Constants.MessageFields.vnid)));

      reply.setFields(q, mv);

      if (m_outboundPipeline == null)
      {
        PackageLogger.log.warning("No outbound message pipeline: unable"
                                  + " to send "
                                  + Constants.MessageTypes.ReplyVNName);
      }
      else
      {
        m_outboundPipeline.submit(reply);
      }
    }
    catch (FieldNotFoundException fe)
    {
      PackageLogger.log.warning("Unable to send "
                                + Constants.MessageTypes.ReplyVNName
                                + " : "
                                + fe.getMessage());
    }
    catch (NumberFormatException e)
    {
      PackageLogger.log.warning("Can't extract a VNID from "
                                + q
                                + ", value="
                                + q.getField(Constants.MessageFields.vnid)
                                + ": unable to send "
                                + Constants.MessageTypes.ReplyVNName);
    }
  }

    /** Return OutboundPipeline being used by this class
     * @return pointer to OutboundPipeline being used.
    */
    public OutboundPipeline getOutboundPipeline() {
	if (m_outboundPipeline == null)
	    {
              PackageLogger.log.warning("No outbound message pipeline: unable"
                                        + " to send "
                                        + Constants.MessageTypes.ReplyVNName);
		return null;
	    }
	else
	    {
		return m_outboundPipeline;
	    }
    }
}
