package nrs.core.base;

import nrs.core.message.Constants;

/**
 * The class provides methods that are commonly used on {@link Message}
 * objects.
 *
 * @author Darren Smith
 */
public class MessageTools
{
  static public final String TRUE  = "true";
  static public final String FALSE = "false";
  static public final int HOPCOUNT  = 10;

  //----------------------------------------------------------------------
  /**
   * Tells whether this is an intelligent message. An intelligent
   * message is one that contains the intelligent message field {@link
   * nrs.core.message.Constants.MessageFields#intelligent}.
   *
   * @return <tt>true</tt> if the message is intelligent, <tt>false</tt>
   * otherwise
   */
  public static boolean isIntelligent(Message m)
  {
    return (m.hasNRSField(Constants.MessageFields.intelligent));
  }
  //----------------------------------------------------------------------
  /**
   * Set the message to be intelligent. An intelligent message is one
   * that contains the intelligent message field {@link
   * nrs.core.message.Constants.MessageFields#intelligent}.
   */
  public static void setIntelligent(Message m)
  {
    m.setNRSField(Constants.MessageFields.intelligent, "true");
  }
  //----------------------------------------------------------------------
  /**
   * Tells whether the message is a broadcast.
   *
   * @return <tt>true</tt> if the message is broadcast, <tt>false</tt>
   * otherwise
   */
  public static boolean isBroadcast(Message m)
  {
    return ((isIntelligent(m)) &&
            (m.hasNRSField(Constants.MessageFields.iIsBroadcast)));
  }
  //----------------------------------------------------------------------
  /**
   * Decrements the value of the intelligent field iHopCount if present,
   * and resets the value to zero if it becomes negative.
   *
   * @return true if the hop count has reached zero, false otherwise
   */
  public static boolean decrementHop(Message m)
  {
    // short-cut
    String iHopCount = Constants.MessageFields.iHopCount;

    if (!m.hasNRSField(iHopCount)) return false;

    int hopValue;
    try
    {
      hopValue = Integer.parseInt(m.getNRSField(iHopCount));

      hopValue--;
      if (hopValue < 0) hopValue = 0;
    }
    catch (NumberFormatException nfe)
    {
      PackageLogger.log.warning("iHopCount value could not be parsed: "
                                + m.getNRSField(iHopCount));
      hopValue = 0;
    }

    m.setNRSField(Constants.MessageFields.iHopCount,
                  Integer.toString(hopValue));

    return (hopValue == 0);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to locate return-route information. If the intelligent
   * field <tt>iReturnRoute</tt> is present, and the message is
   * intelligent, then that value is returned. Otherwise if the field
   * <tt>returnRoute</tt> is present (some message types contain it),
   * its value is returned. If neither of those fields are present,
   * <tt>null</tt> is returned.
   */
  public static String findReturnRoute(Message m)
  {
    if ((isIntelligent(m)) &&
        (m.hasNRSField(Constants.MessageFields.iReturnRoute)))
    {
      return m.getNRSField(Constants.MessageFields.iReturnRoute);
    }
    else return m.getField(Constants.MessageFields.returnRoute);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to locate forward-route information. If the intelligent
   * field iForwardRoute is present, and the message is intelligent,
   * then that value is returned. Otherwise if the field forwardRoute is
   * present, its value is returned. If neither of those fields are
   * present, null is returned.
   */
  public static String findForwardRoute(Message m)
  {
    if ((isIntelligent(m)) &&
        (m.hasNRSField(Constants.MessageFields.iForwardRoute)))
    {
      return m.getNRSField(Constants.MessageFields.iForwardRoute);
    }
    else return m.getField(Constants.MessageFields.forwardRoute);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to extract the routing information needed to reply to the
   * specified {@link Message}. If the iReturnRoute fields is present,
   * its value is returned; else null is returned.
   *
   * @deprecated - use {@link #findReturnRoute(Message m)}
   */
  public static String findRoute(Message m)
  {
    String route = m.getNRSField(Constants.MessageFields.iReturnRoute);
    if ((isIntelligent(m)) &&
        (route != null))
    {
      return route;
    }
    else return null;
  }
  //----------------------------------------------------------------------
  /**
   * Return an integer value suitable for the toVNID field in a reply to
   * the specified {@link Message}. This is achieved by looking for, and
   * parsing, the returnToVNID field. If it is not found, or the field's
   * value cannot be parsed, then 0 is returned.
   */
  public static int findToVNID(Message m)
  {
    int toVNID = 0;

    if (m.hasField(Constants.MessageFields.returnToVNID))
    {
      try
      {
        toVNID =
          Integer.parseInt(m.getField(Constants.MessageFields.returnToVNID));
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning(Constants.MessageFields.toVNID
                                  + " field in"
                                  + " " + m + " is not numeric: can't parse"
                                  + "; unable to reply");
      }
    }

    return toVNID;
  }
  //----------------------------------------------------------------------
  /**
   * Return a value for the field replyMsgID based on the specified
   * {@link Message}. If that message contains the field msgID, then its
   * value is returned. Otherwise the <tt>default</tt> is returned.
   *
   * @param defaultValue the default value to return is no msgID found
   * in the specified message. This should typically be set to the empty
   * string ("").
   */
  static public String findReplyMsgID(Message m, String defaultValue)
  {
    String replyMsgID = m.getField(Constants.MessageFields.msgID);

    if ( replyMsgID == null )
    {
      replyMsgID = defaultValue;
    }

    return replyMsgID;
  }
  //----------------------------------------------------------------------
  /**
   * Return a value suitable for use in the translationCount field of a
   * reply to the specified {@link Message}. This value is taken from
   * the iTranslationCount field, if available and the message is
   * intelligent. Otherwise "0" is returned.
   */
  public static String findTranslationCount(Message m)
  {
    int retVal = 0;

    if ((isIntelligent(m)) &&
        (m.hasNRSField(Constants.MessageFields.iTranslationCount)))
    {
      return m.getNRSField(Constants.MessageFields.iTranslationCount);
    }
    else
    {
      return "0";
    }
  }
  //----------------------------------------------------------------------
  /**
   * Fix an incomplete toVNID field. If the toVNID field is missing, or
   * is zero, then intelligent addressing fields will be added to the
   * message.
   *
   * @param m the {@link Message} to be fixed. Its fields may be altered.
   * @param toVNName the VNName of the variable the message should be
   * sent to.
   *
   * @return <tt>true</tt> if a fix was required, or <tt>false</tt> if
   * there was no need to fix the message
   */
  static public boolean fixToVNID(Message m, String toVNName)
  {
    int toVNID = 0;

    String toVNIDStr = m.getNRSField(Constants.MessageFields.toVNID);
    if (toVNIDStr != null)
    {
      try
      {
        toVNID =
          Integer.parseInt(toVNIDStr);
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning(Constants.MessageFields.toVNID
                                  + " field in"
                                  + " " + m + " is not numeric: fixing");
      }
    }

    // If a toVNID was found and parsed, then no fix needed
    if (toVNID != 0) return false;

    // ...othwerwise, add intelligent fields, and toVNID to ensure it's there
    m.setNRSField(Constants.MessageFields.toVNID, "0");
    m.setNRSField(Constants.MessageFields.iTargetVNName, toVNName);
    m.setNRSField(Constants.MessageFields.intelligent, TRUE);

    return true;
  }
  //----------------------------------------------------------------------
  /**
   * Add broadcast fields to the the supplied message, using a default
   * value for the broadcast hopcount.
   *
   * @see #addBroadcastFields(Message, String, int)
   * @see #HOPCOUNT
   */
  static public void addBroadcastFields(Message m, String targetCID)
  {
    addBroadcastFields(m, targetCID, HOPCOUNT);
  }
  //----------------------------------------------------------------------
  /**
   * Add broadcast fields to the supplied message. The field / values
   * added are:
   *
   * <p><tt>iHopCount</tt> = <tt>hop</tt>
   * <p><tt>iTargetCID</tt> =  <tt>targetCID</tt>
   * <p><tt>iIsBroadcast</tt> = <tt>true</tt>
   * <p><tt>intelligent</tt> = <tt>true</tt>
   */
  static public void addBroadcastFields(Message m,
                                        String targetCID,
                                        int hop)
  {
    m.setNRSField(Constants.MessageFields.iHopCount, Integer.toString(hop));
    m.setNRSField(Constants.MessageFields.iTargetCID, targetCID);
    m.setNRSField(Constants.MessageFields.iIsBroadcast, TRUE);
    m.setNRSField(Constants.MessageFields.intelligent, TRUE);
  }
  //----------------------------------------------------------------------
  /**
   * Add fields for intelligent by-name addressing. A pair of fields are
   * added: <tt>iTargetVNName</tt>, with the specified
   * <code>vnName</code>, and the "is intelligent" flag-field.
   *
   * <p>Before the message is sent to another component, the caller
   * should ensure that any <tt>toVNID</tt> field that belongs to the
   * message should be set to 0.
   */
  static public void addIntelligentAddressing(Message m, String vnName)
  {
    m.setNRSField(Constants.MessageFields.iTargetVNName, vnName);
    setIntelligent(m);
  }
  //----------------------------------------------------------------------
  /**
   * Add fields for intelligent by-name addressing. A pair of fields are
   * added: <tt>iTargetVNName</tt>, with the value taken from the type
   * of the message, and the "is intelligent" flag-field.
   *
   * <p>Before the message is sent to another component, the caller
   * should ensure that any <tt>toVNID</tt> field that belongs to the
   * message should be set to 0.
   */
  static public void addIntelligentAddressing(Message m)
  {
    m.setNRSField(Constants.MessageFields.iTargetVNName, m.getType());
    setIntelligent(m);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to fix a missing or empty <tt>route</tt> field in the
   * provided message by adding broadcast fields to the specified
   * CID. If the <tt>route</tt> field is missing, is <tt>null</tt> or is
   * empty, then broadcast fields are added that use the specified CID
   * and hop values, and also an empty <tt>route</tt> field is added.
   *
   * @param m the {@link Message} to fix
   * @param CID the CID of component being targetted
   * @param hopCount the hop-number to use in the broadcast fields
   *
   * @return <tt>true</tt> if the message was fixed, or <tt>false</tt>
   * if there was no need to fix the message
   */
  static public boolean fixRoute(Message m, String CID, int hopCount)
  {
    String route = m.getNRSField(Constants.MessageFields.route);

    if ((route == null) || ( route.trim().equals("")))
    {
      m.setNRSField(Constants.MessageFields.route, "");
      addBroadcastFields(m, CID, hopCount);
      return true;
    }
    else return false;
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to fix a missing or empty <tt>route</tt> field in the
   * provided message by adding broadcast fields to the specified
   * CID. If the <tt>route</tt> field is missing, is <tt>null</tt> or is
   * empty, then broadcast fields are added that use the specified CID
   * and a default hop count, and also an empty <tt>route</tt> field is
   * added.
   *
   * @param m the {@link Message} to fix
   * @param CID the CID of component being targetted
   *
   * @return <tt>true</tt> if the message was fixed, or <tt>false</tt>
   * if there was no need to fix the message
   */
  static public boolean fixRoute(Message m, String CID)
  {
    return fixRoute(m, CID, HOPCOUNT);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to fix a missing or empty <tt>route</tt> field in the
   * provided reply-message. The fix works by looking for the
   * intelligent field <tt>iSourceCID</tt> in the request-message. If
   * found appropriate broadcast fields are added, and also an empty
   * <tt>route</tt> field is added.
   *
   * @param replyMsg the {@link Message} to be fixed. Its fields may be
   * altered.
   * @param hopCount the value to use for the hope count field
   * @param requestMsg the {@link Message} which is the request. This
   * will be examined for the presence of the <tt>iSourceCID</tt> field.
   *
   * @return <tt>true</tt> if a fix was required, or <tt>false</tt> if
   * no fix was required.
   */
  static public boolean fixRoute(Message replyMsg,
                                 int hopCount,
                                 Message requestMsg)
  {
    //_TODO_(fix only if there is no route, and no broadcast route)
    String route = replyMsg.getNRSField(Constants.MessageFields.route);

    String srcCID = requestMsg.getNRSField(Constants.MessageFields.iSourceCID);

    if ((route == null) &&
        (isIntelligent(requestMsg)) &&
        (srcCID != null))
    {
      replyMsg.setNRSField(Constants.MessageFields.route, "");
      addBroadcastFields(replyMsg, srcCID, hopCount);
      return true;
    }
    else
    {
      return false;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Fix a missing route in a reply-message by looking at the contents
   * of the request-message (and using a default hop count number)
   *
   * @see #fixRoute(Message, int, Message)
   * @see #HOPCOUNT
   */
  static public boolean fixRoute(Message replyMsg, Message requestMsg)
  {
    return fixRoute(replyMsg, HOPCOUNT, requestMsg);
  }
  //----------------------------------------------------------------------
  /**
   * Verify there is routing information in the provided message. If
   * there is, returns <tt>false</tt>. Otherwise logs a warning and
   * returns <tt>true</tt>. This method checks for the existence of
   * either a route field, or broadcast fields.
   */
  static public boolean verifyRoute(Message m)
  {
    String route = m.getNRSField(Constants.MessageFields.route);

    if ((route != null)
        &&
        (route.length() != 0)) return false;

    if (route == null)
    {
      // fix
      PackageLogger.log.warning("Message " + m + " had null route field:"
                                + " setting it to the empty string");
      m.setNRSField(Constants.MessageFields.route, "");
    }

    // okay, is this a valid broadcast message?

    String targetCID = m.getNRSField(Constants.MessageFields.iTargetCID);

    int hopCount = -1;
    String hopCountString = m.getNRSField(Constants.MessageFields.iHopCount);
    if (hopCountString != null)
    try
    {
      hopCount =
        Integer.parseInt(hopCountString);
    }
    catch (NumberFormatException e)
    {
      PackageLogger.log.warning(Constants.MessageFields.iHopCount
                                + " field in"
                                + " " + m + " is not numeric");
    }

    return !( (isBroadcast(m)) && (hopCount > -1) && (targetCID != null) );
  }
  //----------------------------------------------------------------------
  /**
   * Verify the provided field value, associated with the provided
   * fieldname, to check that it is non-null. If the fieldvalue is
   * <tt>null</tt> then an error is logged which says the provided
   * message cannot be replied to.
   *
   * @param fieldname the field name being checked.
   * @param fieldvalue the value that has been calculated for the
   * field. A test will be made to see if it is null.
   * @param query the {@link Message} which is the query to which the
   * provided field name and value form part of the potential reply.
   *
   * @return <tt>true</tt> if the fieldvalue was <tt>null</tt>
   * (indicating a problem), or <tt>false</tt> otherwise.
   */
  static public boolean verify(String fieldname,
                               String fieldvalue,
                               Message query)
  {
    if (fieldvalue == null)
    {
      PackageLogger.log.warning("Unable to determine value for field"
                                + " " + fieldname + ": unable to reply"
                                + " to " + query);
      return true;
    }

    return false;

  }
  //----------------------------------------------------------------------
  /**
   * Adds an empty <tt>iReturnRoute</tt> field, and the intelligent
   * message flag.
   */
  static public void addIntelligentReturnRoute(Message m)
  {
    m.setNRSField(Constants.MessageFields.iReturnRoute, "");
    setIntelligent(m);
  }
  //----------------------------------------------------------------------
  /**
   * Adds an empty <tt>iReturnRoute</tt> field, an <tt>iSourceCID</tt>
   * with the specified <tt>cid</tt> value, and the intelligent message
   * flag.
   */
  static public void addIntelligentReturnRoute(Message m, String cid)
  {
    m.setNRSField(Constants.MessageFields.iReturnRoute, "");
    m.setNRSField(Constants.MessageFields.iSourceCID, cid);
    setIntelligent(m);
  }
}
