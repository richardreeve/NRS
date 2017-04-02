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
#include "StringLiterals.hh"
#include "Target.hh"
#include "ReplyMaxLog.hh"
#include "QueryMaxLogManager.hh"
#include "QueryMaxLog.hh"

NRS::Message::QueryMaxLog::QueryMaxLog( std::string aName ) :
  Variable( aName, NRS::Literals::Object::QUERYMAXLOG_VARIABLE() ),
  iReplyMaxLog( NULL )
{
}

NRS::Message::QueryMaxLog::~QueryMaxLog()
{
}

bool NRS::Message::QueryMaxLog::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyMaxLog)
    {
      if (theVND.hasVariable( Literals::Object::REPLYMAXLOG_VARIABLE() ) )
	{
	  iReplyMaxLog = dynamic_cast< ReplyMaxLog* >
	    ( theVND.getVariable( Literals::Object::REPLYMAXLOG_VARIABLE() ) );
	}
    }
  return (iReplyMaxLog != NULL);
}

void NRS::Message::QueryMaxLog::sendMessage( Base::Target &theTarget,
					     Base::Target &returnTarget,
					     unsigned_t aMessageID )
{
  returnTarget.setTargetVNID( iReplyMaxLog->getVNID() );

  dynamic_cast< QueryMaxLogManager* >( iVM )->sendMessage( theTarget,
							  returnTarget,
							  aMessageID );
}


void NRS::Message::QueryMaxLog::receiveMessage( Base::Target &returnTarget,
						unsigned_t aMessageID )
{
  Base::ExternalInterfaceDirector &theEID = 
    Base::ExternalInterfaceDirector::getDirector();
  // send the vnid to ReplyMaxLog variable
  iReplyMaxLog->sendMessage( returnTarget, aMessageID,
			     theEID.getLoggingPorts().size() );
}
