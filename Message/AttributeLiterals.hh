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

#ifndef _ATTRIBUTE_LITERALS_HH
#define _ATTRIBUTE_LITERALS_HH

#include "StringLiterals.hh"

namespace NRS
{
  namespace Literals
  {
    /// Attribute related literals
    class Attribute
    {
    public:
      /// name of variable or node
      LITERAL( VNNAME, "vnName" );
      /// variable or node id
      LITERAL( VNID, "vnid" );
      /// Type of variable or node
      LITERAL( VNTYPE, "vnType" );
      /// Capabilities of component
      LITERAL( CAPABILITIES, "Capabilities" );
      /// Component id
      LITERAL( CID, "cid" );
      /// Type of component
      LITERAL( CTYPE, "cType" );
      /// Version of component
      LITERAL( CVERSION, "cVersion" );
      /// Message id
      LITERAL( MSGID, "msgID" );
      /// Reply id
      LITERAL( REPLYMSGID, "replyMsgID" );
      /// vnid to reply to
      LITERAL( RETURNTOVNID, "returnToVNID" );
      /// return route for message
      LITERAL( RETURNROUTE, "returnRoute" );
      /// forward route inforation carried by ReplyRoute messages
      LITERAL( FORWARDROUTE, "forwardRoute" );
      /// reverse route inforation carried by ReplyRoute messages
      LITERAL( REVERSEROUTE, "reverseRoute" );
      /// port message was passed out through
      LITERAL( PORT, "port" );
      /// Does the component speak PML?
      LITERAL( SPEAKSPML, "speaksPML" );
      /// Does the component speak BMF?
      LITERAL( SPEAKSBMF, "speaksBMF" );
      /// Was the message successful?
      LITERAL( SUCCESS, "success" );
      /// Max number of bits in integer
      LITERAL( MAXBITS, "maxBits" );
      /// Does the component handle floating point
      LITERAL( FLOATINGPOINT, "floatingPoint" );
      /// Translation count
      LITERAL( TRANSLATIONCOUNT, "translationCount" );
      /// Message is for source or target
      LITERAL( SOURCENOTTARGET, "sourceNotTarget" );
      /// target CID
      LITERAL( TARGETCID, "targetCID" );
      /// target VNID
      LITERAL( TARGETVNID, "targetVNID" );
      /// target VNName
      LITERAL( TARGETVNNAME, "targetVNName" );
      /// pass on request?
      LITERAL( PASSONREQUEST, "passOnRequest" );
      /// temporary connection?
      LITERAL( TEMPORARY, "temporary" );
      /// priority of error message
      LITERAL( PRIORITY, "priority" );
      /// error id
      LITERAL( ERRID, "errID" );
      /// textual error string
      LITERAL( ERRSTRING, "errString" );
      /// log port
      LITERAL( LOGPORT, "logPort" );
      /// log route
      LITERAL( LOGROUTE, "logRoute" );
      /// is the port PML or BMF
      LITERAL( ISPMLNOTBMF, "isPMLNotBMF" );
      /// Number of logs
      LITERAL( LOG, "log" );
      /// Route associated with port
      LITERAL( PORTROUTE, "portRoute" );

    private:
      /// private constructor forbids contruction of class
      Attribute();
    };
  }
}
#endif //ndef _ATTRIBUTE_LITERALS_HH
