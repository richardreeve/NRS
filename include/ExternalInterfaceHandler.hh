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

#ifndef _EXTERNAL_INTERRFACE_HANDLER_HH
#define _EXTERNAL_INTERRFACE_HANDLER_HH

#include <list>
#include <string>

#include "Exception.hh"
#include "ExternalInterfaceDirector.hh"
#include "Types.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    /// This is the base class for ExternalInterface handlers.
    class ExternalInterfaceHandler
    {
    public:
      /// Constructor
      /**
       *
       * This constructor is the base constructor for all
       * ExternalInterfaceHandlers. ExternalInterfaceHandlers are only
       * constructed statically at initialisation time, at which point
       * they register themselves with the ExternalInterfaceDirector.
       *
       * \param aConnectionType Name of the connection type of the
       * external interface (socket, serial, etc.)
       *
       * \param anEncodingType Name of the encoding type used by the
       * interface (PML or BMF)
       *
       **/
      ExternalInterfaceHandler( Enum::ConnectionType aConnectionType,
				Enum::EncodingType anEncodingType );

      /// Destructor
      /**
       *
       * The destructor deregisters the handler from the
       * ExternalInterfaceDirector.
       *
       **/
      virtual ~ExternalInterfaceHandler()
      {
	unRegister();
      }

      /// Unregister the ExternalInterfaceHandler from the
      /// ExternalInterfaceDirector.

      void unRegister()
      {
	if (iRegistered)
	  {
	    _TRY_( ExternalInterfaceDirector::getDirector().
		   removeExternalInterfaceHandler( this ), _RETHROW_ );
	    iRegistered = false;
	  }
      }

      /// Get the connection type of interface.
      /**
       *
       * \return the connection type of the interface.
       *
       **/
      Enum::ConnectionType getConnectionType() const
      {
        return iConnectionType;
      }
      
      /// Get the encoding type of interface.
      /**
       *
       * \return the encoding type of the interface.
       *
       **/
      Enum::EncodingType getEncodingType() const
      {
        return iEncodingType;
      }

      /// Create a new ExternalInterface
      /**
       *
       * \param stringParamList list of string parameters for interface
       * \param intParamList list of integer parameters for interface
       * \param doRead Is the interface an input interface?
       * \param doWrite Is the interface an output interface?
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       *
       * \return ExternalInterface pointer which has been constructed
       *
       **/
      virtual ExternalInterface*
      createExternalInterface( std::list< std::string >
			       &stringParamList,
			       std::list< int > &intParamList,
			       bool doRead = true,
			       bool doWrite = true,
			       bool instantTransmit = true,
			       bool receiveAll = true,
			       int maxReceive = 0 ) const = 0;

      /// Destroy a interface
      /**
       *
       * \param port the port id of the interface to be destroyed.
       *
       **/
      void destroyExternalInterface( unsigned_t port ) const;

    protected:

      /// the connection type of the interface.
      Enum::ConnectionType iConnectionType;
      
      /// the encoding type of the interface.
      Enum::EncodingType iEncodingType;

      /// bool representing whether the handler is registered with the
      /// ExternalInterfaceDirector.
      bool iRegistered;
    };
  }
}
      
#endif //ndef _EXTERNAL_INTERFACE_HANDLER_HH
