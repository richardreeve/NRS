package nrs.core.message;

/** Place holder for various named constants */
public abstract class Constants
{
  public abstract class MessageTypes
  {
    public final static String AcknowledgeMessage  = "AcknowledgeMessage";
    public final static String CreateLink          = "CreateLink";
    public final static String CreateNode          = "CreateNode";
    public final static String CreateLogLink       = "CreateLogLink";
    public final static String DeleteLink          = "DeleteLink";
    public final static String DeleteLogLink       = "DeleteLogLink";
    public final static String DeleteNode          = "DeleteNode";
    public final static String Error               = "Error";
    public final static String FailedRoute         = "FailedRoute";
    public final static String QueryCID            = "QueryCID";
    public final static String QueryCSL            = "QueryCSL";
    public final static String QueryCType          = "QueryCType";
    public final static String QueryConnectedCID   = "QueryConnectedCID";
    public final static String QueryConnectedCIDs  = "QueryConnectedCIDs";
    public final static String QueryLanguage       = "QueryLanguage";
    public final static String QueryLink           = "QueryLink";
    public final static String QueryLog            = "QueryLog";
    public final static String QueryLogLink        = "QueryLogLink";
    public final static String QueryMaxConnection  = "QueryMaxConnection";
    public final static String QueryMaxPort        = "QueryMaxPort";
    public final static String QueryMaxLink        = "QueryMaxLink";
    public final static String QueryMaxLog         = "QueryMaxLog";
    public final static String QueryMaxVNID        = "QueryMaxVNID";
    public final static String QueryNumberType     = "QueryNumberType";
    public final static String QueryPort           = "QueryPort";
    public final static String QueryRoute          = "QueryRoute";
    public final static String QueryVNID           = "QueryVNID";
    public final static String QueryVNName         = "QueryVNName";
    public final static String QueryVNType         = "QueryVNType";
    public final static String ReplyCID            = "ReplyCID";
    public final static String ReplyCSL            = "ReplyCSL";
    public final static String ReplyCType          = "ReplyCType";
    public final static String ReplyConnectedCIDs  = "ReplyConnectedCIDs";
    public final static String ReplyLanguage       = "ReplyLanguage";
    public final static String ReplyLink           = "ReplyLink";
    public final static String ReplyLogLink        = "ReplyLogLink";
    public final static String ReplyMaxConnection  = "ReplyMaxConnection";
    public final static String ReplyMaxPort        = "ReplyMaxPort";
    public final static String ReplyMaxLink        = "ReplyMaxLink";
    public final static String ReplyMaxVNID        = "ReplyMaxVNID";
    public final static String ReplyNumberType     = "ReplyNumberType";
    public final static String ReplyPort           = "ReplyPort";
    public final static String ReplyRoute          = "ReplyRoute";
    public final static String ReplyVNID           = "ReplyVNID";
    public final static String ReplyVNName         = "ReplyVNName";
    public final static String ReplyVNType         = "ReplyVNType";
    public final static String Reset               = "Reset";
    public final static String SetErrorRoute       = "SetErrorRoute";
  }

  public abstract class Fields
  {
    // Fields that belong to the NRS-Attribute namespace
    public static final String F_iForwardRoute     = "iForwardRoute";
    public static final String F_iHopCount         = "iHopCount";
    public static final String F_iIsBroadcast      = "iIsBroadcast";
    public static final String F_iReturnRoute      = "iReturnRoute";
    public static final String F_iSourceCID        = "iSourceCID";
    public static final String F_iTargetCID        = "iTargetCID";
    public static final String F_iTargetVNName     = "iTargetVNName";
    public static final String F_iTranslationCount = "iTranslationCount";
    public static final String F_intelligent       = "intelligent";
    public static final String F_route             = "route";
    public static final String F_toVNID            = "toVNID";

    // Fields that belong to the PML namespace
    public static final String F_cType             = "cType";
    public static final String F_cVersion          = "cVersion";
    public static final String F_cid               = "cid";
    public static final String F_connection        = "connection";
    public static final String F_failure           = "failure";
    public static final String F_forwardRoute      = "forwardRoute";
    public static final String F_link              = "link";
    public static final String F_logPort           = "logPort";
    public static final String F_maxConnections    = "maxConnections";
    public static final String F_msgID             = "msgID";
    public static final String F_passOnRequest     = "passOnRequest";
    public static final String F_port              = "port";
    public static final String F_portRoute         = "portRoute";
    public static final String F_replyMsgID        = "replyMsgID";
    public static final String F_resolved          = "resolved";
    public static final String F_returnRoute       = "returnRoute";
    public static final String F_returnToVNID      = "returnToVNID";
    public static final String F_sourceNotTarget   = "sourceNotTarget";
    public static final String F_speaksBMF         = "speaksBMF";
    public static final String F_speaksPML         = "speaksPML";
    public static final String F_success           = "success";
    public static final String F_targetCID         = "targetCID";
    public static final String F_targetVNID        = "targetVNID";
    public static final String F_targetVNName      = "targetVNName";
    public static final String F_temporary         = "temporary";
    public static final String F_translationCount  = "translationCount";
    public static final String F_vnName            = "vnName";
    public static final String F_vnType            = "vnType";
    public static final String F_vnid              = "vnid";
  }

  /**
   * @deprecated - add new fields to the Fields class (above)
   */
  public abstract class MessageFields
  {
    // Fields that belong to the NRS-Attribute namespace
    public static final String iForwardRoute     = "iForwardRoute";
    public static final String iHopCount         = "iHopCount";
    public static final String iIsBroadcast      = "iIsBroadcast";
    public static final String iReturnRoute      = "iReturnRoute";
    public static final String iSourceCID        = "iSourceCID";
    public static final String iTargetCID        = "iTargetCID";
    public static final String iTargetVNName     = "iTargetVNName";
    public static final String iTranslationCount = "iTranslationCount";
    public static final String intelligent       = "intelligent";
    public static final String route             = "route";
    public static final String toVNID            = "toVNID";

    // Fields that belong to the PML namespace
    public static final String cType             = "cType";
    public static final String cVersion          = "cVersion";
    public static final String cid               = "cid";
    public static final String connection        = "connection";
    public static final String forwardRoute      = "forwardRoute";
    public static final String logPort           = "logPort";
    public static final String link              = "link";
    public static final String maxConnections    = "maxConnections";
    public static final String msgID             = "msgID";
    public static final String passOnRequest     = "passOnRequest";
    public static final String port              = "port";
    public static final String portRoute         = "portRoute";
    public static final String replyMsgID        = "replyMsgID";
    public static final String resolved          = "resolved";
    public static final String returnRoute       = "returnRoute";
    public static final String returnToVNID      = "returnToVNID";
    public static final String sourceNotTarget   = "sourceNotTarget";
    public static final String speaksBMF         = "speaksBMF";
    public static final String speaksPML         = "speaksPML";
    public static final String targetCID         = "targetCID";
    public static final String targetVNID        = "targetVNID";
    public static final String targetVNName      = "targetVNName";
    public static final String temporary         = "temporary";
    public static final String translationCount  = "translationCount";
    public static final String vnName            = "vnName";
    public static final String vnType            = "vnType";
    public static final String vnid              = "vnid";
  }

  public abstract class DNL
  {
    public static final String Author           = "Author";
    public static final String Component        = "Component";
    public static final String Description      = "Description";
    public static final String Link             = "Link";
    public static final String Links            = "Links";
    public static final String Name             = "Name";
    public static final String Network          = "Network";
    public static final String Nodes            = "Nodes";
    public static final String cType            = "cType";
    public static final String cVersion         = "cVersion";
    public static final String name             = "name";
    public static final String source           = "source";
    public static final String target           = "target";
    public static final String x                = "_x";
    public static final String y                = "_y";
  }

  public class Namespace
  {
    // PML namespace
    public final static String NRS_PML
      = "http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/pml/1.0";

    // CSL namespace
    public final static String NRS_CSL
      = "http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/csl/1.0";

    // DNL namespace
    public final static String NRS_DNL
      = "http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/dnl/1.0";

    // NRS-Attribute namespace
    public final static String NRSA
      ="http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/attributes/1.0";

    public final static String NRSA_qualifier = "nrsa";
  }
}
