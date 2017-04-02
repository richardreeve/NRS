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
#include "ReplyMaxLogManager.hh"
#include "CallbackDirector.hh"
#include "ReplyMaxLog.hh"

NRS::Message::ReplyMaxLog::ReplyMaxLog( std::string aName ) :
  Variable( aName, NRS::Literals::Object::REPLYMAXLOG_VARIABLE() )
{

}

NRS::Message::ReplyMaxLog::~ReplyMaxLog()
{
}

void NRS::Message::ReplyMaxLog::sendMessage( Base::Target &theTarget,
					      unsigned_t aMessageID,
					      unsigned_t maxLogs )
{
  if (!theTarget.hasTarget())
    theTarget.setTargetVNName( NRS::Literals::Object::REPLYMAXLOG_VARIABLE(),
			       false );

  dynamic_cast< ReplyMaxLogManager* >( iVM )->sendMessage( theTarget, 
							   aMessageID, 
							   maxLogs );
}


void NRS::Message::ReplyMaxLog::receiveMessage( unsigned_t aMessageID,
						unsigned_t maxLogs )
{
  NRS::Base::CallbackDirector &theCD =
    NRS::Base::CallbackDirector::getDirector();

  //check if there is a callback associated with this message id
  if (aMessageID != 0) 
    {
      if ( theCD.hasCallback( aMessageID ) )
	{
	  //add the MaxLog to this variables messageID -> MaxLog map
	  iMessageIDMaxLogMap[ aMessageID ] = maxLogs;
	  
	  // tell the variable that is expecting the just-received
	  // MaxLog that this MaxLog is available from the Reply variable
	  NRS::Base::Callback *theCallbackObj =
	    theCD.getCallback( aMessageID );
	  if (!theCallbackObj)
	    _WARN_( "No callback found for vnid reply\n\t" );
	  else
	    theCallbackObj->callback( aMessageID );
	}
    }
}
