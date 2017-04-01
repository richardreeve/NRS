/*
 * Copyright (C) 2004 Richard Reeve, Matthew Szenher and Edinburgh University
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

#ifndef _STRING_LITERALS_HH
#define _STRING_LITERALS_HH

#include <string>
#include "Types.hh"
#include "Exception.hh"

#pragma interface

#define LITERAL( x, y ) static const char *x() \
{ \
  static const char *p = y; \
  return p; \
}

namespace NRS
{
  /// Namespace for all literals that do not belong to any particular class
  namespace Literals
  {
    /// File and directory related literals
    class File
    {
    public:
      /// Default component name
      LITERAL( COMPONENT, "component" );
      /// autoconf package name
      LITERAL( NRS, PACKAGE );
      /// environment variable for overriding plugin directory
      LITERAL( PLUGIN_DIR_ENV, "NRS_PLUGIN_DIR" );
      /// default share directory
      LITERAL( DEFAULT_SHARE_DIR, PREFIX "/share/" PACKAGE "-"
	       VERSION "/" );
      /// default plugin directory
      LITERAL( DEFAULT_PLUGIN_DIR, PREFIX "/share/" PACKAGE "-"
	       VERSION "/plugins/" );
    private:
      /// private constructor forbids contruction of class
      File();
    };

    /// XML related literals
    class XML
    {
    public:
      /// CSL namespaces
      LITERAL( CSL_NAMESPACE,
	       "http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/csl/1.0" );
      /// PML namespace
      LITERAL( PML_NAMESPACE,
	       "http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/pml/1.0" );
      /// Reserved PML attribute namespace
      LITERAL( ATT_NAMESPACE,
	       "http://www.ipab.inf.ed.ac.uk/cricketlab/nrs/attributes/1.0" );

      /// Reserved PML attribute namespace prefix
      LITERAL( ATT_PREFIX, "nrsa" );

      // Intelligent attributes in reserved namespace

      /// Attribute denoting an intelligent message
      LITERAL( INTELLIGENT, "intelligent" );
      /// Attribute for target component id for intelligent messages
      LITERAL( ITARGETCID, "iTargetCID" );
      /// Attribute for source component id for intelligent messages
      LITERAL( ISOURCECID, "iSourceCID" );
      /// Attribute for forward route (to be built up) for intelligent messages
      LITERAL( IFORWARDROUTE, "iForwardRoute" );
      /// Attribute for return route (to be built up) for intelligent messages
      LITERAL( IRETURNROUTE, "iReturnRoute" );
      /// Attribute for hop count (to be decremented) for intelligent messages
      LITERAL( IHOPCOUNT, "iHopCount" );
      /// Attribute denoting whether intelligent message is a broadcast
      LITERAL( IISBROADCAST, "iIsBroadcast" );
      /// Attribute stating whether intelligent message needs acknowledgement
      LITERAL( IACKMSG, "iAckMsg" );
      /// Attribute containing VNID for acknowledgement of intelligent message
      LITERAL( IACKVNID, "iAckVNID" );
      /// Attribute stating whether intelligent message needs failed route info
      LITERAL( IFAILEDROUTEMSG, "iFailedRouteMsg" );
      /// Attribute containing VNID for failed route of intelligent message
      LITERAL( IFAILEDROUTEVNID, "iFailedRouteVNID" );
      /// Attribute for message id of intelligent message
      LITERAL( IMSGID, "iMsgID" );
      /// Attribute for targeting a name rather than a vnid in a message
      LITERAL( ITARGETVNNAME, "iTargetVNName" );
      /// Attribute counting number of translations carried out during
      /// message routing
      LITERAL( ITRANSLATIONCOUNT, "iTranslationCount" );

      // Normal attributes in reserved namespace

      /// forward route for message
      LITERAL( ROUTE, "route" );
      /// VNID of target of message
      LITERAL( TOVNID, "toVNID" );

      // Element names
      /// The XML element for a group of messages
      LITERAL( MESSAGES, "Messages" );
      /// root element for CSL
      LITERAL( CAPABILITIES, "Capabilities" );

      /// boolean true in XML
      LITERAL( TRUE, "true" );
      /// boolean false in XML
      LITERAL( FALSE, "false" );

      /// Which intelligent attribute is this?
      /**
       *
       * \param array containing attribute name
       *
       * \return IntelligentType corresponding to attribute
       *
       **/
      static Enum::IntelligentType getIntelligentType( const char *attr )
      {
	if (!strcmp( INTELLIGENT(), attr ))
	  return Enum::IsIntelligent;

	if (!strcmp( IFORWARDROUTE(), attr ))
	  return Enum::ForwardRoute;

	if (!strcmp( IRETURNROUTE(), attr ))
	  return Enum::ReturnRoute;

	if (!strcmp( IISBROADCAST(), attr ))
	  return Enum::IsBroadcast;

	if (!strcmp( IHOPCOUNT(), attr ))
	  return Enum::HopCount;

	if (!strcmp( ITARGETCID(), attr ))
	  return Enum::TargetCID;	

	if (!strcmp( IACKMSG(), attr ))
	  return Enum::AckMsg;

	if (!strcmp( IACKVNID(), attr ))
	  return Enum::AckVNID;

	if (!strcmp( IFAILEDROUTEMSG(), attr ))
	  return Enum::FailedRouteMsg;

	if (!strcmp( IFAILEDROUTEVNID(), attr ))
	  return Enum::FailedRouteVNID;

	if (!strcmp( IMSGID(), attr ))
	  return Enum::MsgID;

	if (!strcmp( ITRANSLATIONCOUNT(), attr ))
	  return Enum::TranslationCount;

	if (!strcmp( ISOURCECID(), attr ))
	  return Enum::SourceCID;

	if (!strcmp( ITARGETVNNAME(), attr ))
	  return Enum::TargetVNName;

	return Enum::InvalidIType;
      }

      /// get intelligent attribute string from IntelligentType
      /**
       *
       * \param intelligent the type of attribute
       *
       * \return the character array corresponding to the attribute
       *
       **/
      static const char *
      getIntelligentName( Enum::IntelligentType intelligent )
      {
	switch (intelligent)
	  {
	  case Enum::IsIntelligent:
	    return INTELLIGENT();
	    break;
	  case Enum::ForwardRoute:
	    return IFORWARDROUTE();
	    break;
	  case Enum::ReturnRoute:
	    return IRETURNROUTE();
	    break;
	  case Enum::IsBroadcast:
	    return IISBROADCAST();
	    break;
	  case Enum::HopCount:
	    return IHOPCOUNT();
	    break;
	  case Enum::TargetCID:
	    return ITARGETCID();
	    break;
	  case Enum::AckMsg:
	    return IACKMSG();
	    break;
	  case Enum::AckVNID:
	    return IACKVNID();
	    break;
	  case Enum::FailedRouteMsg:
	    return IFAILEDROUTEMSG();
	    break;
	  case Enum::FailedRouteVNID:
	    return IFAILEDROUTEVNID();
	    break;
	  case Enum::MsgID:
	    return IMSGID();
	    break;
	  case Enum::TranslationCount:
	    return ITRANSLATIONCOUNT();
	    break;
	  case Enum::SourceCID:
	    return ISOURCECID();
	    break;
	  case Enum::TargetVNName:
	    return ITARGETVNNAME();
	    break;
	  default:
	    _ABORT_( "type does not exist" ); 
	    break;
	  }
	return NULL;
      }
    private:
      /// private constructor forbids contruction of class
      XML();
    };

    /// Message type related literals
    class Message
    {
    public:
      /// PML message type
      LITERAL( PML, "PML" );
      /// BMF message type
      LITERAL( BMF, "BMF" );

       /// get encoding type string
      /**
       *
       * \param encoding the type of encoding
       *
       * \return the character array corresponding to the encoding
       *
       **/
      static const char *
      getEncodingName( Enum::EncodingType encoding )
      {
	switch (encoding)
	  {
	  case Enum::BMF:
	    return BMF();
	    break;
	  case Enum::PML:
	    return PML();
	    break;
	  default:
	    _ABORT_( "type does not exist" ); 
	    break;
	  }
	return NULL;
      }
   private:
      /// private constructor forbids contruction of class
      Message();
    };

    /// Type name literals
    class Type
    {
    public:
      /// boolean type
      LITERAL( BOOLEAN, "boolean" );
      /// integer type
      LITERAL( INTEGER, "integer" );
      /// floating point type
      LITERAL( FLOAT, "float" );
      /// unsigned integer type - deprecated
      LITERAL( UNSIGNED, "unsigned" );
      /// number type - deprecated
      LITERAL( NUMBER, "number" );
      /// string type
      LITERAL( STRING, "string" );
      /// route tyoe
      LITERAL( ROUTE, "route" );
      /// vector type
      LITERAL( VECTOR, "vector" );

      /// get type string from Type
      /**
       *
       * \param type the type to return
       *
       * \return the character array corresponding to the type
       *
       **/
      static const char *getTypeName( Enum::Type type )
      {
	switch (type)
	  {
	  case Enum::Boolean:
	    return BOOLEAN();
	    break;
	  case Enum::Integer:
	    return INTEGER();
	    break;
	  case Enum::Float:
	    return FLOAT();
	    break;
	  case Enum::String:
	    return STRING();
	    break;
	  case Enum::Route:
	    return ROUTE();
	    break;
	  case Enum::Vector:
	    return VECTOR();
	    break;
	  default:
	    _ABORT_( "type does not exist" ); 
	    break;
	  }
	return NULL;
      }

    private:
      /// private constructor forbids contruction of class
      Type();
    };

    /// Connection type related literals
    class Connection
    {
    public:
      /// a file (can be read from)
      LITERAL( FILE, "file" );
      /// a log (can be written to)
      LITERAL( LOG, "log" );
      /// a socket (bidirectional)
      LITERAL( CLIENTSOCKET, "clientsocket" );
      /// a socket (bidirectional)
      LITERAL( SERVERSOCKET, "serversocket" );
      /// a fifo (unidirectional)
      LITERAL( FIFO, "fifo" );
      /// a device (birectional)
      LITERAL( DEVICE, "device" );

      /// get connection string from Connection
      /**
       *
       * \param connection the connection to return
       *
       * \return the character array corresponding to the connection
       *
       **/
      static const char *
      getConnectionName( Enum::ConnectionType connection )
      {
	switch (connection)
	  {
	  case Enum::File:
	    return FILE();
	    break;
	  case Enum::Log:
	    return LOG();
	    break;
	  case Enum::TCPIPClient:
	    return CLIENTSOCKET();
	    break;
	  case Enum::TCPIPServer:
	    return SERVERSOCKET();
	    break;
	  case Enum::Fifo:
	    return FIFO();
	    break;
	  case Enum::Device:
	    return DEVICE();
	    break;
	  default:
	    _ABORT_( "type does not exist" ); 
	    break;
	  }
	return NULL;
      }
    private:
      /// private constructor forbids contruction of class
      Connection();
    };

    /// Object related literals - should be removed once objects are created
    class Object
    {
    public:
      /// The main component for things which are not part of a plugin
      LITERAL( MAIN_COMPONENT, "main" );

      /// The name of the argument director
      LITERAL( ARGUMENTDIRECTOR, "ArgumentDirector" );
#ifdef DEBUG
      /// The name of the debug director
      LITERAL( DEBUGDIRECTOR, "DebugDirector" );
#endif //def DEBUG
      /// The name of the executive
      LITERAL( EXECUTIVE, "Executive" );
      /// The name of the memory repository
      LITERAL( MEMORYREPOSITORY, "MemoryRepository" );
      /// The name of the plugin director
      LITERAL( PLUGINDIRECTOR, "PluginDirector" );
      /// The name of the variable/node director
      LITERAL( VARIABLENODEDIRECTOR, "VariableNodeDirector" );
      /// The name of the external interface director
      LITERAL( EXTERNALINTERFACEDIRECTOR, "ExternalInterfaceDirector" );

      // The obligatory variables
      /// DeleteNode variables delete nodes
      LITERAL( DELETENODE_VARIABLE, "DeleteNode" );
      /// CreateNode variables create nodes
      LITERAL( CREATENODE_VARIABLE, "CreateNode" );
      /// DeleteLink variables delete links
      LITERAL( DELETELINK_VARIABLE, "DeleteLink" );
      /// CreateLink variables create links
      LITERAL( CREATELINK_VARIABLE, "CreateLink" );
      /// MainLoop variables control the activation of components
      LITERAL( MAINLOOP_VARIABLE, "MainLoop" );
      /// Updater variables tell their nodes to update state of all variables
      LITERAL( UPDATER_VARIABLE, "Updater" );
      /// MessageSender variables tell their nodes to send all output messages
      LITERAL( MESSAGESENDER_VARIABLE, "MessageSender" );
      /// QueryVNID variables ask and receive variable and node id queries
      LITERAL( QUERYVNID_VARIABLE, "QueryVNID" );
      /// ReplyVNID variables reply to and get responses to vnid queries
      LITERAL( REPLYVNID_VARIABLE, "ReplyVNID" );
      /// QueryVNName variables ask and receive variable and node name queries
      LITERAL( QUERYVNNAME_VARIABLE, "QueryVNName" );
      /// ReplyVNName variables reply to and get responses to vnName queries
      LITERAL( REPLYVNNAME_VARIABLE, "ReplyVNName" );
      /// QueryVNType variables ask and receive variable and node type queries
      LITERAL( QUERYVNTYPE_VARIABLE, "QueryVNType" );
      /// ReplyVNType variables reply to and get responses to vnType queries
      LITERAL( REPLYVNTYPE_VARIABLE, "ReplyVNType" );
      /// QueryMAXVNID variables ask and receive maximum vnid queries
      LITERAL( QUERYMAXVNID_VARIABLE, "QueryMaxVNID" );
      /// ReplyMAXVNID variables reply to and get responses to MaxVNID queries
      LITERAL( REPLYMAXVNID_VARIABLE, "ReplyMaxVNID" );
      /// QueryLanguage variables ask and receive language capabilities queries
      LITERAL( QUERYLANGUAGE_VARIABLE, "QueryLanguage" );
      /// ReplyCID variables reply to and get responses to language queries
      LITERAL( REPLYLANGUAGE_VARIABLE, "ReplyLanguage" );
      /// QueryCID variables ask and receive component id queries
      LITERAL( QUERYCID_VARIABLE, "QueryCID" );
      /// ReplyCID variables reply to and get responses to cid queries
      LITERAL( REPLYCID_VARIABLE, "ReplyCID" );
      /// QueryMaxPort variables ask and receive maxPort queries
      LITERAL( QUERYMAXPORT_VARIABLE, "QueryMaxPort" );
      /// ReplyMaxPort variables ask and receive maxPort queries
      LITERAL( REPLYMAXPORT_VARIABLE, "ReplyMaxPort" );
      /// QueryPort variables ask and receive queries about port connections
      LITERAL( QUERYPORT_VARIABLE, "QueryPort" );
      /// ReplyPort variables reply to and get responses to port queries
      LITERAL( REPLYPORT_VARIABLE, "ReplyPort" );
      /// QueryCType variables ask and receive component type queries
      LITERAL( QUERYCTYPE_VARIABLE, "QueryCType" );
      /// ReplyCType variables reply to and get responses to cType queries
      LITERAL( REPLYCTYPE_VARIABLE, "ReplyCType" );
      /// QueryCSL variables ask and receive CSL queries
      LITERAL( QUERYCSL_VARIABLE, "QueryCSL" );
      /// ReplyCSL variables reply to and get responses to CSL queries
      LITERAL( REPLYCSL_VARIABLE, "ReplyCSL" );
      /// QueryNumberType variables ask and receive NumberType queries
      LITERAL( QUERYNUMBERTYPE_VARIABLE, "QueryNumberType" );
      /// ReplyNumberType variables reply to and get responses to
      /// NumberType queries
      LITERAL( REPLYNUMBERTYPE_VARIABLE, "ReplyNumberType" );
      /// QueryRoute variables ask and receive route queries
      LITERAL( QUERYROUTE_VARIABLE, "QueryRoute" );
      /// ReplyRoute variables reply to and get responses to route queries
      LITERAL( REPLYROUTE_VARIABLE, "ReplyRoute" );
      /// FailedRoute tell about and finds out about failed route messages
      LITERAL( FAILEDROUTE_VARIABLE, "FailedRoute" );
      /// AcknowledgeMessage acknowledges and finds out messages arrived
      LITERAL( ACKNOWLEDGEMESSAGE_VARIABLE, "AcknowledgeMessage" );
      /// Reset messages reset a component
      LITERAL( RESET_VARIABLE, "Reset" );
      /// Error messages pass error messages around the system
      LITERAL( ERROR_VARIABLE, "Error" );
      /// SetErrorRoute messages set the route for error messages to take
      LITERAL( SET_ERROR_ROUTE_VARIABLE, "SetErrorRoute" );
      /// QueryNumLink variables ask and receive queries about links
      LITERAL( QUERYMAXLINK_VARIABLE, "QueryMaxLink" );
      /// ReplyMaxLink variables reply to and get responses to link queries
      LITERAL( REPLYMAXLINK_VARIABLE, "ReplyMaxLink" );
      /// QueryLink variables ask and receive queries about links
      LITERAL( QUERYLINK_VARIABLE, "QueryLink" );
      /// ReplyLink variables reply to and get responses to link queries
      LITERAL( REPLYLINK_VARIABLE, "ReplyLink" );
      /// QueryMaxLog variables ask and receive queries about system logs
      LITERAL( QUERYMAXLOG_VARIABLE, "QueryMaxLog" );
      /// ReplyMaxLog variables reply to and get responses to log queries
      LITERAL( REPLYMAXLOG_VARIABLE, "ReplyMaxLog" );
      /// QueryLog variables ask and receive queries about system logs
      LITERAL( QUERYLOG_VARIABLE, "QueryLog" );
      /// ReplyLog variables reply to and get responses to log queries
      LITERAL( REPLYLOG_VARIABLE, "ReplyLog" );

    private:
      /// private constructor forbids contruction of class
      Object();
    };

    /// String related literals, should be removed once managers are defined
    class String
    {
    public:
      /// this string is one of a list
      LITERAL( LIST, "list" );
      /// this string is a token
      LITERAL( TOKEN, "token" );
      /// this string is a filename
      LITERAL( FILENAME, "filename" );
      /// this string is a vnname
      LITERAL( VNNAME, "vnname" );

      /// get string restriction string
      /**
       *
       * \param the string restriction to return
       *
       * \return the character array corresponding to the restriction
       *
       **/
      static const char *
      getRestrictionName( Enum::StringRestrictionType restriction )
      {
	switch (restriction)
	  {
	  case Enum::List:
	    return LIST();
	    break;
	  case Enum::Token:
	    return TOKEN();
	    break;
	  case Enum::Filename:
	    return FILENAME();
	    break;
	  case Enum::VNName:
	    return VNNAME();
	    break;
	  default:
	    _ABORT_( "type does not exist" ); 
	    break;
	  }
	return NULL;
      }
    private:
      /// private constructor forbids contruction of class
      String();
    };
  }
}
#endif //ndef _STRING_LITERALS_HH
