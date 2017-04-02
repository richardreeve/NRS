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

#ifndef _EXTERNAL_INTERFACE_DIRECTOR_HH
#define _EXTERNAL_INTERFACE_DIRECTOR_HH

#include <list>
#include <map>
#include <vector>

#include "Exception.hh"
#include "Types.hh"


namespace NRS
{
  namespace Base
  {
    class ExternalInterfaceHandler;
    class ExternalInterface;

    /// This class holds the framework within which NRS sits.
    /**
     * 
     * A single ExternalInterfaceDirector, created at initialisation, holds all
     * of the information about the external interfaces
     * 
     **/
    class ExternalInterfaceDirector
    {
    public:
      /// static accessor for the ExternalInterfaceDirector.
      /**
       *
       * \return A reference to the ExternalInterfaceDirector.
       *
       **/
      static ExternalInterfaceDirector &getDirector()
      {
	static ExternalInterfaceDirector sEID;
	return sEID;
      }
      
      /// Do one step of the main loop of the interfaces.
      /**
       *
       * \returns whether there is any reason to continue to loop
       *
       **/
      bool mainLoop();

      /// Check for a specific ExternalInterfaceHandler.
      /**
       *
       * This method checks whether an ExternalInterfaceHandler of a given
       * name exists.
       *
       * \param aConnectionType The connection type of the
       * ExternalInterfaceHandler to check for.
       *
       * \param anEncodingType The message type of the
       * ExternalInterface handler to look for (or NoEncoding for don't
       * mind).
       *
       * \return bool signifying whether the ExternalInterfaceHandler exists.
       *
       **/

      bool hasExternalInterfaceHandler( Enum::ConnectionType aConnectionType,
					Enum::EncodingType anEncodingType =
					Enum::NoEncoding ) const;

      /// Adds a ExternalInterfaceHandler to the records.
      /**
       *
       * This method adds an ExternalInterfaceHandler to the GeneralManager's
       * records.
       *
       * \param anEIH The pointer to the ExternalInterfaceHandler to be added.
       *
       * \exception Exception thrown if an ExternalInterfaceHandler of this
       * name has already been registered.
       *
       **/
      void addExternalInterfaceHandler( ExternalInterfaceHandler *anEIH );

      /// Gets the ExternalInterfaceHandler of a specific type.
      /**
       *
       * This method returns the ExternalInterfaceHandler with a
       * particular name.
       *
       * \param aConnectionType The connection type of the
       * ExternalInterfaceHandler to return.
       *
       * \param aMessageType The message type of the ExternalInterface
       * handler to return (or NoEncoding for don't mind).
       *
       * \return a pointer to the ExternalInterfaceHandler.
       *
       * \exception Exception thrown if an ExternalInterfaceHandler of
       * that type does not exist.
       *
       **/
      ExternalInterfaceHandler*
      getExternalInterfaceHandler( Enum::ConnectionType aConnectionType,
				   Enum::EncodingType anEncodingType
				   = Enum::NoEncoding );

      /// Remove an ExternalInterfaceHandler from the GeneralManager's records
      /**
       *
       * This method removes an ExternalInterfaceHandler from the
       * GeneralManager's records.
       *
       * \param anEIH The pointer to the ExternalInterfaceHandler to
       * be removed.
       *
       * \exception Exception thrown if the ExternalInterfaceHandler is not
       * registered with the GeneralManager.
       *
       **/
      void removeExternalInterfaceHandler( ExternalInterfaceHandler *anEIH );

      /// Check for a specific ExternalInterface.
      /**
       *
       * This method checks whether an ExternalInterface with a given
       * target exists.
       *
       * \param port The port ID of the ExternalInterface to check for.
       *
       * \return bool signifying whether the ExternalInterface exists.
       *
       **/
      bool hasExternalInterface( const unsigned_t port ) const
      {
	if (port >= iPortEIVector.size())
	  return false;
	return ( iPortEIVector[ port ] != NULL );
      }

      /// Adds a ExternalInterface to the records.
      /**
       *
       * This method adds an ExternalInterface to the GeneralManager's
       * records.
       *
       * \param anEI The pointer to the ExternalInterface to be added.
       *
       * \return the port id of the interface
       *
       **/
      unsigned_t addExternalInterface( ExternalInterface *anEI );


     /// Gets the ExternalInterface with a specific target.
      /**
       *
       * This method returns the ExternalInterface with a particular
       * target.
       *
       * \param port The port of the ExternalInterface being sought.
       *
       * \return a pointer to the ExternalInterface.
       *
       * \exception Exception thrown if an ExternalInterface with that
       * target does not exist.
       *
       **/
      ExternalInterface *getExternalInterface( const unsigned_t port ) const
      {
	if ( !hasExternalInterface( port ) )
	  {
	    Exception e( _FL_ );
	    throw e << "No such ExternalInterface as \"" << port
		    << "\"\n\t";
	  }
	return iPortEIVector[ port ];
      }

      /// Remove an ExternalInterface from the GeneralManager's records
      /**
       *
       * This method removes an ExternalInterface from the
       * GeneralManager's records.
       *
       * \param port The port ID of the ExternalInterface to be removed.
       *
       * \exception Exception thrown if no such ExternalInterface is
       * registered with the GeneralManager.
       *
       **/
      void removeExternalInterface( const unsigned_t port );

      /// Remove an ExternalInterface from the GeneralManager's records
      /**
       *
       * This method removes an ExternalInterface from the GeneralManager's
       * records.
       *
       * \param anEI The pointer to the ExternalInterface to be removed.
       *
       * \exception Exception thrown if the ExternalInterface is not
       * registered with the GeneralManager.
       *
       **/
      void removeExternalInterface( ExternalInterface *anEI );

      /// Returns a number greater than the highest allocated port ID
      /**
       *
       * \returns a number guaranteed to be bigger than the highest
       * allocated port ID
       *
       **/
      unsigned_t getMaxPort() const
      {
	return iMaxPort;
      }

      /// Add a logging port to the system
      /**
       *
       * \param port the port number of the logging port to be added
       *
       **/
      void addLoggingPort( unsigned_t port )
      {
	iLogPortNumList.push_back( port );
      }

      /// Get the list of logging ports
      /**
       *
       * \returns a list of all of the logging ports in the system
       *
       **/
      std::list< unsigned_t > getLoggingPorts() const
      {
	return iLogPortNumList;
      }

    private:
      /// Destructor
      /**
       *
       * This is a private destructor as no-one can create or destroy
       * ExternalInterfaceDirectors.
       *
       **/
      virtual ~ExternalInterfaceDirector();
      
      /// Constructor
      /**
       *
       * This is a private constructor as no-one can create or destroy
       * ExternalInterfaces.
       *
       **/
      ExternalInterfaceDirector();
      
      /// Gets next port id for an External Interface
      /**
       *
       * This method gets the next free port id for an External Interface
       * being created.
       *
       **/
      unsigned_t getNextPort();

      /// A map of the type of the ExternalInterfaceHandlers to the
      /// ExternalInterfaceHandler pointers themselves.
      std::map< Enum::ConnectionType,
		std::map< Enum::EncodingType,
			  ExternalInterfaceHandler* > > iConnMsgEIHMap;

      /// A vector of ExternalInterface pointers by port id.
      std::vector< ExternalInterface* > iPortEIVector;

      /// A list of all the logging ports in the system
      std::list< unsigned_t > iLogPortNumList;

      /// The next free port id to be allocated.
      unsigned_t iNextPort;

      /// A number guaranteed to be at least the largest port id.
      unsigned_t iMaxPort;

    };
  }
}

#endif //ndef _EXTERNAL_INTERFACE_DIRECTOR_HH
