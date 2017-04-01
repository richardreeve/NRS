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
#pragma implementation
#include <sstream>
#include <unistd.h>

#include "ArgumentDirector.hh"
#include "Executive.hh"
#include "ExternalInterfaceDirector.hh"
#include "MainLoop.hh"
#include "StringLiterals.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

void NRS::Base::Executive::addStartupVariable( std::string aManager,
					       std::string aVariable )
{
  if (iStartupVariableMap.find( aManager ) != iStartupVariableMap.end())
    _ABORT_( "Trying to create a second variable of type '"
	     << aManager << "'\n\t" );
  iStartupVariableMap[ aManager ] = aVariable;
}

void NRS::Base::Executive::startup()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  while (iStartupVariableMap.begin() != iStartupVariableMap.end())
    {
      std::map< std::string, std::string >::iterator anIter =
	iStartupVariableMap.begin();
      if (!theVND.hasVariableManager( anIter->first ))
	_ABORT_( "Asked to create a nonexistent '" << anIter->first 
		 << "' Variable\n\t" );
      theVND.getVariableManager( anIter->first ).
	createVariable( anIter->second );
      iStartupVariableMap.erase( anIter );
    }
}

void NRS::Base::Executive::run()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();

  theVND.registration();

  startup();

  theVND.resolveDependencies();
  NRS::Message::MainLoop *theMainLoop = 
    dynamic_cast< NRS::Message::MainLoop* >
    ( theVND.getVariable( Literals::Object::MAINLOOP_VARIABLE() ) );
  _ASSERT_( theMainLoop != NULL, "Main loop not created" );
  bool reasonToLoop = true;

  while (reasonToLoop)
    {
      reasonToLoop = theEID.mainLoop();
      if (!theVND.resolveDependencies())
	{
	  //_DEBUG_( "Unresolved dependencies" );
	  //theVND.displayNodes( std::cout );
	  //theVND.displayVariables( std::cout );
	  usleep( iTimestep );
	}
      else
	theMainLoop->mainLoop();
    }
  
}
NRS::Base::Executive::~Executive()
{
}

NRS::Base::Executive::Executive() : iTimestep( 1000 )
{
  ArgumentDirector::getDirector().
    addArgumentFielder( 't', "timestep", "\t\t[ -t | --timestep <usec> ]\n",
			"\t-t|--timestep <usec>\t\t"
			"sets time delay between updates when idle\n", this );
}


std::string NRS::Base::Executive::queryFielder() const
{
  return NRS::Literals::Object::EXECUTIVE();
}

void
NRS::Base::Executive::fieldArguments( std::list< std::string > &arguments )
{
  std::string arg = arguments.front();
  arguments.pop_front();

  if (arg == "timestep")
    {
      if (arguments.size() < 1)
	_ABORT_( "Not enough arguments to timestep\n\t" );
      std::stringstream anSS( arguments.front() );
      anSS >> iTimestep;
      arguments.pop_front();
    }
  else
  _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
}
