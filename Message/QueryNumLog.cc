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
#include "ReplyNumLog.hh"
#include "QueryNumLogManager.hh"

#pragma implementation
#include "QueryNumLog.hh"

NRS::Message::QueryNumLog::QueryNumLog( std::string aName ) :
  Variable( aName, NRS::Literals::Object::QUERYNUMLOG_VARIABLE() ),
  iReplyNumLog( NULL )
{
}

NRS::Message::QueryNumLog::~QueryNumLog()
{
}

bool NRS::Message::QueryNumLog::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyNumLog)
    {
      if (theVND.hasVariable( Literals::Object::REPLYNUMLOG_VARIABLE() ) )
	{
	  iReplyNumLog = dynamic_cast< ReplyNumLog* >
	    ( theVND.getVariable( Literals::Object::REPLYNUMLOG_VARIABLE() ) );
	}
    }
  return (iReplyNumLog != NULL);
}

void NRS::Message::QueryNumLog::sendMessage( Base::Target &theTarget,
					     Base::Target &returnTarget,
					     unsigned_t aMessageID )
{
  returnTarget.setTargetVNID( iReplyNumLog->getVNID() );

  dynamic_cast< QueryNumLogManager* >( iVM )->sendMessage( theTarget,
							  returnTarget,
							  aMessageID );
}


void NRS::Message::QueryNumLog::receiveMessage( Base::Target &returnTarget,
						unsigned_t aMessageID )
{
  Base::ExternalInterfaceDirector &theEID = 
    Base::ExternalInterfaceDirector::getDirector();
  // send the vnid to ReplyNumLog variable
  iReplyNumLog->sendMessage( returnTarget, aMessageID,
			     theEID.getLoggingPorts().size() );
}
