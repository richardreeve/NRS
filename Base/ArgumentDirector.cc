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

#include <list>
#include <map>
#include <sstream>
#include <string>
#include <libgen.h>

#include "ArgumentDirector.hh"
#include "ArgumentFielder.hh"
#include "ExternalInterfaceDirector.hh"
#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"

namespace
{
  static NRS::Base::ArgumentDirector &sAD =
  NRS::Base::ArgumentDirector::getDirector();
}

void NRS::Base::ArgumentDirector::parseArguments( int argc, char **argv )
{
  VariableNodeDirector::getDirector().registration();

  iExecutableName = basename( argv[0] );
  argc--;
  argv++;
  
  if (iExecFielder != NULL)
    {
      iExecFielder->fieldExecutable( iExecutableName );
    }


  std::list< std::string > arguments;

  for ( int i = 0; i < argc; ++i )
    {
      arguments.push_back( argv[i] );
    }

  parseArguments( arguments );
}

void NRS::Base::ArgumentDirector::
parseArguments( std::list< std::string > &arguments )
{

  while ((!arguments.empty()) && (arguments.front()[0] == '-'))
    {
      if (arguments.front()[1] == '-')
	{
	  if ( iFullFielderMap.find( arguments.front().substr( 2 ) )
	       == iFullFielderMap.end() )
	    {
	      std::cerr << "Argument not recognised: '" << arguments.front()
			<< "'\n\n" << getHelpDescription();
	      exit( 1 );
	    }
	  arguments.front() = arguments.front().substr( 2 );
	  try
	    {
	      iFullFielderMap[arguments.front()]->fieldArguments( arguments );
	    }
	  catch ( NRS::Base::Exception &e )
	    {
	      _ERROR_( e );
	      arguments.push_front( "help" );
	      iFullFielderMap[ "help" ]->fieldArguments( arguments );
	    }
	}
      else
	{
	  if (iAbbrevArgFullMap.find( arguments.front()[1] ) ==
	      iAbbrevArgFullMap.end())
	    {
	      std::cerr << "Argument not recognised: '" << arguments.front()
			<< "'\n\n" << getHelpDescription();
	      exit( 1 );
	    }
	  std::string possible = arguments.front().substr(2);
	  std::string first = iAbbrevArgFullMap[arguments.front()[1]];
	  arguments.pop_front();
	  if (!possible.empty())
	    arguments.push_front( possible );
	  arguments.push_front( first );
	  size_t len = arguments.size();
	  try
	    {
	      iFullFielderMap[arguments.front()]->fieldArguments( arguments );
	      if ((arguments.size() + 1 == len) &&
		  (!possible.empty()) &&
		  (arguments.front() == possible))
		{
		  arguments.front() = "-" + possible;
		}
	    }
	  catch ( NRS::Base::Exception &e )
	    {
	      _ERROR_( e );
	      arguments.push_front( "help" );
	      iFullFielderMap[ "help" ]->fieldArguments( arguments );
	    }
	}
    }
  
  if (!arguments.empty())
    {
      if (!iSpareFielder)
	{
	  std::cerr << "Extra argument not recognised: '" << arguments.front()
		    << "'\n\n" << getHelpDescription();
	  exit( 1 );
	}
      else
	{
	  iSpareFielder->fieldSpareArguments( arguments );
	}
    }
}


void NRS::Base::ArgumentDirector::
addArgumentFielder( const char abbrev, std::string full,
		    std::string shortDesc, std::string description,
		    ArgumentFielder *theAF )
{
  if (((abbrev != (char) 0) &&
       (iAbbrevArgFullMap.find( abbrev ) != iAbbrevArgFullMap.end())) ||
      (iFullArgHelpMap.find( full ) != iFullArgHelpMap.end()))
    {
      _ABORT_( "Repeating argument switch '-" << abbrev << "'\n\t" );
    }

  if (abbrev != (char) 0)
    iAbbrevArgFullMap[abbrev] = full;
  iFullArgShortMap[full] = shortDesc;
  iFullArgHelpMap[full] = description;
  iFullFielderMap[full] = theAF;
}

void 
NRS::Base::ArgumentDirector::removeArgumentFielder( ArgumentFielder *theAF )
{
  _ABORT_( "Not implemented\n\t" );
}

void
NRS::Base::ArgumentDirector::addSpareArgumentFielder( std::string shortDesc,
						      std::string description,
						      ArgumentFielder *theAF )
{
  if (iSpareFielder != NULL)
    {
      _ABORT_( "Already have a spare argument fielder" );
    }
  iSpareFielder = theAF;
  iSpareArgShortDesc = shortDesc;
  iSpareArgDescription = description;
}

void
NRS::Base::ArgumentDirector::addExecutableFielder( std::string ramble,
						 ArgumentFielder *theAF )
{
  if (iExecFielder != NULL)
    {
      _ABORT_( "Already have a executable name fielder" );
    }
  iExecFielder = theAF;
  iExecutableDescription = ramble;
}

std::string
NRS::Base::ArgumentDirector::getHelpDescription( bool withDebugging )
{
  std::ostringstream theHelp;

  theHelp << "Usage: " << iExecutableName << std::endl;

  for ( typeof( iFullArgShortMap.begin() ) anIter = iFullArgShortMap.begin();
	anIter != iFullArgShortMap.end(); anIter++ )
    {
      if (withDebugging)
	theHelp << "{from " << iFullFielderMap[anIter->first]->queryFielder()
		<< "} ";
      theHelp << anIter->second;
    }
  if (iSpareFielder != NULL)
    {
      if (withDebugging)
	theHelp << "{from " << iSpareFielder->queryFielder()
		<< "} ";
      theHelp << iSpareArgShortDesc << std::endl;
    }
  if (iExecFielder != NULL)
    {
      if (withDebugging)
	theHelp << "{from " << iExecFielder->queryFielder()
		<< "} ";
      theHelp << iExecutableDescription << std::endl;
    }
  for ( typeof( iFullArgHelpMap.begin() ) anIter = iFullArgHelpMap.begin();
	anIter != iFullArgHelpMap.end(); anIter++ )
    {
      theHelp << anIter->second;
    }

  if (iSpareFielder != NULL)
    theHelp << iSpareArgDescription << std::endl;

  return theHelp.str();
}
 
NRS::Base::ArgumentDirector::~ArgumentDirector()
{ 
}

NRS::Base::ArgumentDirector::ArgumentDirector() :
  iExecFielder( NULL ), iSpareFielder( NULL )
{
  iExecutableName = NRS::Literals::File::NRS();
  iExecutableName += ".";
  iExecutableName += NRS::Literals::File::COMPONENT();

  addArgumentFielder( 'h', "help", "\t\t\[ -h|--help ]\n",
		      "\t-h|--help\t\t\tproduces this help text\n", this );
  addArgumentFielder( (char)0, "HELP", "\t\t\[ --HELP ]\n",
		      "\t--HELP\t\t\t\tproduces a debugged help text\n",
		      this );
}

std::string NRS::Base::ArgumentDirector::queryFielder() const
{
  return NRS::Literals::Object::ARGUMENTDIRECTOR();
}

void NRS::Base::ArgumentDirector::
fieldArguments( std::list< std::string > &arguments )
{
  std::string arg = arguments.front();
  arguments.pop_front();
  if (arg == "help")
    {
      std::cout << getHelpDescription();
      exit( 0 );
    }
  else if (arg == "HELP")
    {
      std::cout << getHelpDescription( true );
      exit( 0 );
    }
  else
  _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
}
 
