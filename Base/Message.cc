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

#include "DeleteLink.hh"
#include "Encoding.hh"
#include "Executive.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntelligentStore.hh"
#include "Message.hh"
#include "StringLiterals.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

NRS::Base::Message::Message( const Target &aTarget ) :
  iIsLocal( aTarget.isLocal() ), iEncodings( 0 ),
  iEncodingPtr( NULL ), iOwnEncoding( false ), iTarget( aTarget )
{
  if (!iIsLocal)
    {
      const ExternalInterfaceDirector &theEID =
	ExternalInterfaceDirector::getDirector();
      if (iTarget.getRoute() != getEmptyRoute())
	{ // a straight route
	  ExternalInterface *anEIPtr =
	    theEID.getExternalInterface( iTarget.getPort() );
	  iPortPtr.push_back( anEIPtr );
	  iPortEncoding.push_back( anEIPtr->getEncoding() );
	  iEncodings[ (unsigned_t) anEIPtr->getEncoding() ] = 1;
	}
      else
	{ // broadcast => all open ports
	  for ( unsigned_t port = 0; port < theEID.getMaxPort(); port++ )
	    if (theEID.hasExternalInterface( port ))
	      {
		ExternalInterface *anEIPtr =
		  theEID.getExternalInterface( port );
		if (!anEIPtr->isLoggingPort())
		  {
		    iPortPtr.push_back( anEIPtr );
		    iPortEncoding.push_back( anEIPtr->getEncoding() );
		    encoding_t enc = 0;
		    enc[ (unsigned_t) anEIPtr->getEncoding() ] = true;
		    iEncodings |= enc;
		  }
	      }
	}
    }
}

NRS::Base::Message::~Message()
{
  if (iEncodingPtr != NULL)
    iEncodingPtr->removeEncodings( iEncodings );
  if (iOwnEncoding)
    delete iEncodingPtr;
}

void NRS::Base::Message::setEncodingStore( Encoding* theEncodingPtr )
{
  _DEBUG_( "" );
  if (iEncodingPtr != NULL)
    iEncodingPtr->removeEncodings( iEncodings );
  if (iOwnEncoding)
    delete iEncodingPtr;
  iOwnEncoding = false;
  iEncodingPtr = theEncodingPtr;
  _DEBUG_( std::boolalpha << (iEncodingPtr == NULL) );
  if (iEncodingPtr != NULL)
    iEncodingPtr->addEncodings( iEncodings );
}

void NRS::Base::Message::createEncodingStore()
{
  if (iEncodingPtr != NULL)
    iEncodingPtr->removeEncodings( iEncodings );
  if (iOwnEncoding)
    delete iEncodingPtr;
  iEncodingPtr = NULL;
  iOwnEncoding = true;
  if (!iTarget.getMessageDescriptionPtr())
    _ABORT_( "No message description for target in message" );
  iEncodingPtr = new Encoding( *iTarget.getMessageDescriptionPtr() );
  iEncodingPtr->addEncodings( iEncodings );
}

void NRS::Base::Message::sendMessages()
{
  if (!iIsLocal)
    {
      _ASSERT_( iEncodingPtr != NULL, "Encoding pointer has not been set!" );
      
      for ( unsigned_t port = 0; port < iPortPtr.size(); port++ )
	iPortPtr[port]->
	  sendMessage( iTarget,
		       iEncodingPtr->getMessageStore( iPortEncoding[port] ),
		       iEncodingPtr->getIntelligentStore( iPortEncoding[port] )
		       );
    }
}
