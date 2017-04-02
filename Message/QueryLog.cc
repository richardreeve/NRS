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

#include <string>

#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"
#include "ExternalInterface.hh"
#include "StringLiterals.hh"
#include "Target.hh"
#include "ReplyLog.hh"
#include "QueryLogManager.hh"
#include "QueryLog.hh"

NRS::Message::QueryLog::QueryLog( std::string aName ) :
  Variable( aName, NRS::Literals::Object::QUERYLOG_VARIABLE() ),
  iReplyLog( NULL )
{
}

NRS::Message::QueryLog::~QueryLog()
{
}

bool NRS::Message::QueryLog::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyLog)
    {
      if (theVND.hasVariable( Literals::Object::REPLYLOG_VARIABLE() ) )
	{
	  iReplyLog = dynamic_cast< ReplyLog* >
	    ( theVND.getVariable( Literals::Object::REPLYLOG_VARIABLE() ) );
	}
    }
  return (iReplyLog != NULL);
}

void NRS::Message::QueryLog::sendMessage( Base::Target &theTarget,
					  Base::Target &returnTarget,
					  unsigned_t aMessageID,
					  unsigned_t aLog )
{
  returnTarget.setTargetVNID( iReplyLog->getVNID() );

  dynamic_cast< QueryLogManager* >( iVM )->sendMessage( theTarget,
							returnTarget,
							aMessageID,
							aLog );
}


void NRS::Message::QueryLog::receiveMessage( Base::Target &returnTarget,
					     unsigned_t aMessageID,
					     unsigned_t aLog )
{
  Base::ExternalInterfaceDirector &theEID = 
    Base::ExternalInterfaceDirector::getDirector();

  const std::list< unsigned_t > &ports = theEID.getLoggingPorts();
  bool hasLog = false;
  unsigned_t logNum = 0;

  for ( std::list< unsigned_t >::const_iterator anIter = ports.begin();
	anIter != ports.end(); anIter++, logNum++ )
    {
      if ((logNum == aLog) && (theEID.hasExternalInterface( *anIter )))
      {
	Base::ExternalInterface *anEI =
	  theEID.getExternalInterface( *anIter );
	Base::Target theLog( true, NULL, true, true, *anIter );
	// send the vnid to ReplyLog variable
	iReplyLog->sendMessage( returnTarget, aMessageID,
				theLog,  anEI->isPMLNotBMF() );
	hasLog = true;
      }
    }

  if (!hasLog)
    { // log does not exist...
      Base::Target nonLog( true );
      iReplyLog->sendMessage( returnTarget, aMessageID,
			      nonLog,  false );
    }
}
