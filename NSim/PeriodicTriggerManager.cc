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

#include <iostream>
#include <string>

#include "PeriodicTrigger.hh"
#include "Exception.hh"
#include "StringLiterals.hh"
#include "NumberManager.hh"
#include "VoltageManager.hh"
#include "ConductanceManager.hh"
#include "CapacitanceManager.hh"
#include "TimeManager.hh"
#include "VariableManager.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Manager for PeriodicTrigger nodes, which send out regular
    /// events
    class PeriodicTriggerManager : public Base::VariableManager
    {
    public:
      /// Default constructor
      PeriodicTriggerManager() :
	Base::VariableManager( "PeriodicTrigger",
			       "Periodic trigger",
			       3, "", false )
      {
      }

      /// Destructor
      virtual ~PeriodicTriggerManager()
      {
      }

      /// Setup segment records.
      /**
       *
       * This method sets up the segment descriptions for the CSL
       * output
       *
       **/
      virtual void setSegments()
      {
	Base::VariableManager::setSegments();

	iSegIsNumberVector[ 0 ] = true;
	iSegTypeVector[ 0 ] = Type::NumberManager::getName();
	iSegIsUnitVector[ 0 ] = true;
	iSegUnitVector[ 0 ] = Unit::TimeManager::getName();
	iSegNumberScaleVector[ 0 ] = "m";
	iSegNameVector[ 0 ] = "t_offset";

	iSegIsNumberVector[ 1 ] = true;
	iSegTypeVector[ 1 ] = Type::NumberManager::getName();
	iSegIsUnitVector[ 1 ] = true;
	iSegUnitVector[ 1 ] = Unit::TimeManager::getName();
	iSegNumberScaleVector[ 1 ] = "m";
	iSegNameVector[ 1 ] = "t_period";

	iSegIsNumberVector[ 2 ] = true;
	iSegTypeVector[ 2 ] = Type::NumberManager::getName();
	iSegNameVector[ 2 ] = "count";
      }

      /// Set Description entries for CSL output
      virtual void setDescription()
      {
	iDescription = "This variable/message type is the periodic trigger "
	  "of the simulation";
      }
      
      /// Deliver a PML message to a variable.
      /**
       *
       * This method tells the VariableManager to deliver a
       * message. Since the Message is PML, the message is two
       * attribute lists.
       *
       * \param vnid the id of the target variable
       * \param NRSAttrMap map of nrs specific attributes for variable
       * \param attrMap map of link specific attributes for variable
       * \param intelligent whether the message is intelligent
       * \param contents anything supposed to be inside the element,
       * such as CSL in the ReplyCSL messages. Normally empty.
       *
       * \return success of delivery
       *
       **/
      virtual bool deliverMessage( unsigned_t vnid,
				   std::map< std::string,
                                   std::string > &NRSAttrMap,
                                   std::map< std::string,
                                   std::string > &attrMap,
				   bool intelligent,
				   const char *contents )
      {
	Base::VariableNodeDirector &theVND =
	  Base::VariableNodeDirector::getDirector();

	bool isIntNotFloat = true;

	float aTOFloat = 0.0;
	number_t aTOInt = 0;
	
	float aTPFloat = 0.0;
	number_t aTPInt = 0;

	number_t aCInt = 0;


	if (!msgHasAttribute( attrMap, iSegNameVector[0].c_str() ))
	  return false;

	if (attrMap[ iSegNameVector[0] ].find( '.' ) ==
	    std::string::npos )
	  {
	    isIntNotFloat = true;
	    aTOFloat = aTOInt = atoi( attrMap[ iSegNameVector[0] ].c_str() );
	  }
	else
	  {
	    isIntNotFloat = false;
	    aTOFloat = atof( attrMap[ iSegNameVector[0] ].c_str() );
	  }
	
	if (!msgHasAttribute( attrMap, iSegNameVector[1].c_str() ))
	  return false;

	if (attrMap[ iSegNameVector[1] ].find( '.' ) ==
	    std::string::npos )
	  {
	    isIntNotFloat = true;
	    aTPFloat = aTPInt = atoi( attrMap[ iSegNameVector[1] ].c_str() );
	  }
	else
	  {
	    isIntNotFloat = false;
	    aTPFloat = atof( attrMap[ iSegNameVector[1] ].c_str() );
	  }

	if (!msgHasAttribute( attrMap, iSegNameVector[2].c_str() ))
	  return false;

	aCInt = atoi( attrMap[ iSegNameVector[2] ].c_str() );

	if (isIntNotFloat)
	  dynamic_cast< PeriodicTrigger* >( theVND.getVariable( vnid ))->
	    receiveMessage( aTOInt, aTPInt, aCInt );
	else
	  dynamic_cast< PeriodicTrigger* >( theVND.getVariable( vnid ))->
	    receiveMessage( aTOFloat, aTPFloat, aCInt );

	// remove extracted attributes from attrMap
	attrMap.erase( "t_offset" );
	attrMap.erase( "t_period" );
	attrMap.erase( "count" );

	return true;
      }

      /// Receive a BMF message to deliver to a target.
      /**
       *
       * This method tells the recipient to deliver a message to a
       * target. Since the message is BMF, the message is a binary
       * stream, but the intelligent part is translated into a map.
       *
       * \param vnid the id of the target variable
       * \param data the BMF message to receive
       * \param intelligent whether the message is intelligent
       * \param intMap the intelligent part of the message
       *
       * \return success of the delivery
       *
       **/
      virtual bool deliverMessage( unsigned_t vnid,
				   char *&data,
				   bool intelligent,
				   const char* intMap[] )
      {
	_BUG_( "Unhandled deliverMessage" );
	int segment = 0;
	
	double aTDouble, aSDouble;
	number_t aTInt, aSInt;
	
	if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str()))
	  return false;
	
	bool isFloatNotIntT =
	  Interface::BMF::segToNumberM( data, aTDouble, aTInt );

	if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str()))
	  return false;
	
	bool isFloatNotIntS =
	  Interface::BMF::segToNumberM( data, aSDouble, aSInt );

	if (isFloatNotIntS && !isFloatNotIntT)
	  {
	    isFloatNotIntS = false;
	    aSDouble = aSInt;
	  }
	else if (!isFloatNotIntS && isFloatNotIntT)
	  {
	    aTDouble = aTInt;
	  }
	/*
	if (isFloatNotIntS)
	  dynamic_cast< PeriodicTrigger* >( theVND.getVariable( vnid ))->
	    receiveMessage( aTDouble, aSDouble );
	else
	  dynamic_cast< PeriodicTrigger* >( theVND.getVariable( vnid ))->
	    receiveMessage( aTInt, aSInt );
	*/

	return true;
      }

      /// Translate a PML message to BMF.
      /**
       *
       * This method tells the VariableManager to translate a PML
       * message into BMF and send it to a port.
       *
       * \param port the port id of the target interface
       * \param theTarget the route for the message
       * \param NRSAttrMap map of nrs specific attributes for variable
       * \param attrMap map of link specific attributes for variable
       * \param intelligent whether the message is intelligent
       * \param contents anything supposed to be inside the element,
       * such as CSL in the ReplyCSL messages. Normally empty.
       *
       * \return success of delivery
       *
       **/
      virtual bool translateMessage( unsigned_t port,
				     Base::Target &theTarget,
				     std::map< std::string,
				     std::string > &NRSAttrMap,
				     std::map< std::string,
				     std::string > &attrMap,
				     bool intelligent = false,
				     const char *contents = NULL )
      {
	_BUG_( "Unhandled translateMessage" );
	Base::ExternalInterfaceDirector &theEID =
	  Base::ExternalInterfaceDirector::getDirector();
	
	char *intMap[ Interface::BMF::IMSize ];
	for ( int i = 0; i < Interface::BMF::IMSize; i++ )
	  intMap[ i ] = NULL;
	static char *msg = 
	  (char *) malloc( Interface::BMF::DefaultMessageSize );
	static size_t alloc = Interface::BMF::DefaultMessageSize;
	char *data = msg;
	size_t size = alloc;
	
	Interface::PML::intelligentToBMFM( intMap, NRSAttrMap, intelligent );
	
	bool isIntNotFloat = true;

	float aTFloat = 0.0;
	number_t aTInt = 0;
	
	float aSFloat = 0.0;
	number_t aSInt = 0;

	if (!msgHasAttribute( attrMap, iSegNameVector[0].c_str() ))
	  return false;

	if (attrMap[ iSegNameVector[0] ].find( '.' ) ==
	    std::string::npos )
	  {
	    isIntNotFloat = true;
	    aTInt = atoi( attrMap[ iSegNameVector[0] ].c_str() );
	  }
	else
	  {
	    isIntNotFloat = false;
	    aTFloat = atof( attrMap[ iSegNameVector[0] ].c_str() );
	  }
	
	if (!msgHasAttribute( attrMap, iSegNameVector[1].c_str() ))
	  return false;

	if ( isIntNotFloat && ( attrMap[ iSegNameVector[1] ].find( '.' ) ==
				std::string::npos ) )
	  {
	    isIntNotFloat = true;
	    aSInt = atoi( attrMap[ iSegNameVector[1] ].c_str() );
	  }
	else
	  {
	    isIntNotFloat = false;
	    aTFloat = aTInt;
	    aSFloat = atof( attrMap[ iSegNameVector[1] ].c_str() );
	  }
	
	if (isIntNotFloat)
	  {
	    while (!Interface::BMF::segFromNumberM( data, aTInt, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	    while (!Interface::BMF::segFromNumberM( data, aSInt, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	  }
	else
	  {
	    while (!Interface::BMF::segFromNumberM( data, aTFloat, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	    while (!Interface::BMF::segFromNumberM( data, aSFloat, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	  }

	Interface::BMF::segFromFinishedM( data, size );
	
	if (!theEID.hasExternalInterface( port ))
	  _ABORT_( "Port not found\n\t" );
	theEID.getExternalInterface( port )->
	  sendMessage( theTarget, msg, alloc - size,
		       intelligent, (const char**) intMap );
	
	for ( int i = 0; i < Interface::BMF::IMSize; i++ )
	  free( intMap[ i ] );
	
	// remove extracted attributes from attrMap
	attrMap.erase( "v_rest" );
	attrMap.erase( "v_actual" );
	attrMap.erase( "g_memb" );
	attrMap.erase( "c_memb" );
	attrMap.erase( "v_recovery" );
	attrMap.erase( "v_threshold" );
	attrMap.erase( "t_refractory" );

	return true;
      }


      /// Translate a BMF message to PML
      /**
       *
       * This method tells the VariableManager to translate a BMF
       * message into PML and send it to a port.
       *
       * \param port the port id of the target interface
       * \param theTarget the route for the message
       * \param data all route and data segments
       * \param msgLen the length of the message segment
       * \param intelligent whether the message is intelligent
       * \param intMap the intelligent part of the message
       *
       **/
      virtual bool translateMessage( unsigned_t port,
				     Base::Target &theTarget,
				     char *&data,
				     bool intelligent = false,
				     const char* intMap[] = NULL )
      {
	_BUG_( "Unhandled translateMessage" );
	int segment = 0;
	Base::ExternalInterfaceDirector &theEID = 
	  Base::ExternalInterfaceDirector::getDirector();
	
	std::map< std::string, std::string > NRSAttrMap;
	std::map< std::string, std::string > attrMap;
	
	Interface::BMF::intelligentToPMLM( NRSAttrMap, intMap, intelligent );
	
	double aTDouble, aSDouble;
	number_t aTInt, aSInt;
	
	if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str()))
	  return false;
	
	bool isFloatNotIntT =
	  Interface::BMF::segToNumberM( data, aTDouble, aTInt );

	if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str()))
	  return false;
	
	bool isFloatNotIntS =
	  Interface::BMF::segToNumberM( data, aSDouble, aSInt );

	std::stringstream anSS;

	if (isFloatNotIntT)
	  anSS << aTDouble;
	else
	  anSS << aTInt;
	attrMap[ iSegNameVector[0] ] = anSS.str();

	anSS.str( std::string() );

	if (isFloatNotIntS)
	  anSS << aSDouble;
	else
	  anSS << aSInt;
	attrMap[ iSegNameVector[1] ] = anSS.str();

	if (!theEID.hasExternalInterface( port ))
	  _ABORT_( "Port not found\n\t" );
	
	theEID.getExternalInterface( port )->sendMessage( theTarget,
							  getType().c_str(),
							  NRSAttrMap, attrMap,
							  intelligent );
	return true;
      }


      /// Send a PeriodicTrigger message
      /**
       *
       * This virtual method tells the PeriodicTriggerManager to send a
       * message
       *
       * \param theTarget the route for the message
       * \param aTimestep the timestep of the simulation
       * \param aSimTime the current simulation time
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				double aTimestep,
				double aSimTime )
      {
	_BUG_( "Unhandled sendMessage" );
	Base::ExternalInterfaceDirector &theEID = 
	  Base::ExternalInterfaceDirector::getDirector();
	unsigned_t port;
	Base::ExternalInterface * eI = NULL;
	
	char *intMap[ Interface::BMF::IMSize ];
	for ( int i = 0; i < Interface::BMF::IMSize; i++ )
	  intMap[ i ] = NULL;
	static char *msg = 
	  (char *) malloc( Interface::BMF::DefaultMessageSize );
	static size_t alloc = Interface::BMF::DefaultMessageSize;
	char *data = msg;
	size_t size = alloc;
	
	bool intelligent = false;
	bool isBroadcast = theTarget.isBroadcast();
	bool isPMLNotBMF = false;
	std::map< std::string, std::string > NRSAttrMap;
	std::map< std::string, std::string > attrMap;
	
	if (!isBroadcast)
	  {
	    port = theTarget.getPort();
	    if (!theEID.hasExternalInterface( port ))
	      _ABORT_( "Message going to non-existent port " 
		       << port << "\n\t" );
	    eI = theEID.getExternalInterface( port );
	    isPMLNotBMF = eI->isPMLNotBMF();
	  }
	
	if (isBroadcast || isPMLNotBMF)
	  {
	    std::stringstream anSS;
	    anSS << aTimestep;
	    
	    attrMap[ iSegNameVector[0] ] = anSS.str();
	    anSS.str( std::string() );

	    anSS << aSimTime;
	    attrMap[ iSegNameVector[1] ] = anSS.str();

	    if (!isBroadcast)
	      eI->sendMessage( theTarget, iMessage.c_str(),
			       NRSAttrMap, attrMap, intelligent );
	  }
	
	if (isBroadcast || !isPMLNotBMF)
	  {
	    while (!Interface::BMF::segFromNumberM( data, aTimestep, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	    while (!Interface::BMF::segFromNumberM( data, aSimTime, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	    
	    Interface::BMF::segFromFinishedM( data, size );
	    
	    size = alloc - size;
	    
	    if (!isBroadcast)
	      eI->sendMessage( theTarget, msg, size, intelligent,
			       (const char **) intMap );
	  }
	
	if (isBroadcast)
	  {
	    for ( port = 0; port < theEID.getMaxPort(); port++ )
	      {
		if (theEID.hasExternalInterface( port ))
		  {
		    eI = theEID.getExternalInterface( port );
		    if (eI->isPMLNotBMF())
		      eI->sendMessage( theTarget, iMessage.c_str(),
				       NRSAttrMap, attrMap, intelligent );
		    else
		      eI->sendMessage( theTarget, msg, size, intelligent,
				       (const char **) intMap );
		  }
	      }
	  }
	for ( int i = 0; i < Interface::BMF::IMSize; i++ )
	  free( intMap[ i ] );
      }
      
      /// Send a PeriodicTrigger message
      /**
       *
       * This virtual method tells the PeriodicTriggerManager to send a
       * message
       *
       * \param theTarget the route for the message
       * \param aTimestep the timestep of the simulation
       * \param aSimTime the current simulation time
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				number_t aTimestep,
				number_t aSimTime )
      {
	_BUG_( "Unhandled sendMessage" );
	Base::ExternalInterfaceDirector &theEID = 
	  Base::ExternalInterfaceDirector::getDirector();
	unsigned_t port;
	Base::ExternalInterface * eI = NULL;
	
	char *intMap[ Interface::BMF::IMSize ];
	for ( int i = 0; i < Interface::BMF::IMSize; i++ )
	  intMap[ i ] = NULL;
	static char *msg = 
	  (char *) malloc( Interface::BMF::DefaultMessageSize );
	static size_t alloc = Interface::BMF::DefaultMessageSize;
	char *data = msg;
	size_t size = alloc;
	
	bool intelligent = false;
	bool isBroadcast = theTarget.isBroadcast();
	bool isPMLNotBMF = false;
	std::map< std::string, std::string > NRSAttrMap;
	std::map< std::string, std::string > attrMap;
	
	if (!isBroadcast)
	  {
	    port = theTarget.getPort();
	    if (!theEID.hasExternalInterface( port ))
	      _ABORT_( "Message going to non-existent port " 
		       << port << "\n\t" );
	    eI = theEID.getExternalInterface( port );
	    isPMLNotBMF = eI->isPMLNotBMF();
	  }
	
	if (isBroadcast || isPMLNotBMF)
	  {
	    std::stringstream anSS;
	    anSS << aTimestep;
	    
	    attrMap[ iSegNameVector[0] ] = anSS.str();
	    anSS.str( std::string() );

	    anSS << aSimTime;
	    attrMap[ iSegNameVector[1] ] = anSS.str();

	    if (!isBroadcast)
	      eI->sendMessage( theTarget, iMessage.c_str(),
			       NRSAttrMap, attrMap, intelligent );
	  }
	
	if (isBroadcast || !isPMLNotBMF)
	  {
	    while (!Interface::BMF::segFromNumberM( data, aTimestep, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	    while (!Interface::BMF::segFromNumberM( data, aSimTime, size ))
	      Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
	    
	    Interface::BMF::segFromFinishedM( data, size );
	    
	    size = alloc - size;
	    
	    if (!isBroadcast)
	      eI->sendMessage( theTarget, msg, size, intelligent,
			       (const char **) intMap );
	  }
	
	if (isBroadcast)
	  {
	    for ( port = 0; port < theEID.getMaxPort(); port++ )
	      {
		if (theEID.hasExternalInterface( port ))
		  {
		    eI = theEID.getExternalInterface( port );
		    if (eI->isPMLNotBMF())
		      eI->sendMessage( theTarget, iMessage.c_str(),
				       NRSAttrMap, attrMap, intelligent );
		    else
		      eI->sendMessage( theTarget, msg, size, intelligent,
				       (const char **) intMap );
		  }
	      }
	  }
	for ( int i = 0; i < Interface::BMF::IMSize; i++ )
	  free( intMap[ i ] );
      }
      
    };
  }
}

namespace
{
  NRS::Simulator::PeriodicTriggerManager sPeriodicTriggerM;
}
