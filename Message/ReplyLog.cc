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

#include "StringLiterals.hh"
#include "Callback.hh"
#include "Target.hh"
#include "ReplyLogManager.hh"
#include "CallbackDirector.hh"
#include "ReplyLog.hh"

NRS::Message::ReplyLog::ReplyLog( std::string aName ) :
  Variable( aName, NRS::Literals::Object::REPLYLOG_VARIABLE() )
{
}

NRS::Message::ReplyLog::~ReplyLog()
{
}

void NRS::Message::ReplyLog::sendMessage( Base::Target &theTarget,
					  unsigned_t aMessageID,
					  Base::Target &theLog,
					  bool isPMLNotBMF )
{
  if (!theTarget.hasTarget())
    theTarget.setTargetVNName( Literals::Object::REPLYLOG_VARIABLE(),
			       false );

  dynamic_cast< ReplyLogManager* >( iVM )->sendMessage( theTarget, 
							aMessageID, 
							theLog,
							isPMLNotBMF );
}


void NRS::Message::ReplyLog::receiveMessage( unsigned_t aMessageID,
					     Base::Target &theLog,
					     bool isPMLNotBMF )
{
  Base::CallbackDirector &theCD = Base::CallbackDirector::getDirector();

  // check if there is a callback associated with this message id
  if (aMessageID != 0)
    {
      if ( theCD.hasCallback( aMessageID ) )
	{
	  // add the Log to this variables messageID -> Log map
	  iMessageIDLogMap.
	    insert( make_pair( aMessageID, 
			       std::pair< Base::Target, bool >( theLog,
								isPMLNotBMF )
			       ) );
	  
	  // tell the variable that is expecting the just-received
	  // Log that this Log is available from the Reply variable
	  Base::Callback *theCallbackObj = theCD.getCallback( aMessageID );
	  
	  if (!theCallbackObj)
	    _WARN_( "No callback found for Log reply\n\t" );
	  else
	    theCallbackObj->callback( aMessageID );
	}
    }
}
