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

#include <map>
#include <list>
#include <string>
#include <cstring>
#include <cctype>
#include <algorithm>
#include <libgen.h>

#pragma implementation
#include "PluginDirector.hh"

#include "StringLiterals.hh"
#include "Plugin.hh"
#include "VariableNodeDirector.hh"
#include "ArgumentDirector.hh"

namespace
{
  static NRS::Base::PluginDirector &sPD =
  NRS::Base::PluginDirector::getDirector();
}

NRS::Base::PluginDirector::PluginDirector() :
  iCurrentPlugin( NRS::Literals::Object::MAIN_COMPONENT() )
{
  ArgumentDirector &theAD = ArgumentDirector::getDirector();

  theAD.addArgumentFielder( 'p', "plugin", "\t\t[ -p|--plugin <plugin> ]\n",
			    "\t-p|--plugin <plugin>\t\t"
			    "name or filename of plugin to load\n"
			    "\t\t\t\t\tcan be in form 'nsim', 'nsim.so',\n"
			    "\t\t\t\t\tor '~/libs/plugins/nsim.so'\n",
			    this );
  theAD.addSpareArgumentFielder( "\t\t[ <plugin1> [ <plugin2> [ ... ] ] ]\n",
				 "\t<plugin>\t\t\t"
				 "names of additional plugins to\n"
				 "\t\t\t\t\tbe loaded, just as -p <plugin>\n",
				 this );
  theAD.addExecutableFielder( "This program is an NRS component.\n", this );
}

NRS::Base::PluginDirector::~PluginDirector()
{
  // plugins remove themselves from the map as we progress, so we need
  // to be continually restarting
  for( typeof( iPluginMap.begin() ) anIter = iPluginMap.begin();
       anIter != iPluginMap.end(); anIter = iPluginMap.begin() )
    {
      removePlugin( anIter->first );
    }
}

bool NRS::Base::PluginDirector::hasPlugin( std::string aName )
{
  char *file = strdup( aName.c_str() );
  std::string pluginName = basename( file );
  free( file );

  if ((pluginName.length() > 6) && (pluginName.substr(0, 3) == "lib") && 
      (pluginName.substr( pluginName.length()-3, 3 ) == ".so"))
    {
      pluginName = pluginName.substr( 3, pluginName.length() - 6 );
    }

  return ( iPluginMap.find( pluginName ) != iPluginMap.end() );
}

bool NRS::Base::PluginDirector::addPlugin( std::string aName )
{
  if ( hasPlugin( aName ) )
    {
      _WARN_( "Repeated insertion of plugin " << aName << "\n\t" );
      return true;
    }

  try
    {
      new Plugin( aName );
      clearPlugin();
    }
  catch ( Exception &e )
    {
      _ERROR_( e );
      return false;
    }

  VariableNodeDirector::getDirector().registration();

  return true;
}

bool NRS::Base::PluginDirector::removePlugin( std::string aName )
{
  
  if ( !hasPlugin( aName ) )
    {
      _ABORT_( "No such plugin as \"" << aName << "\"\n\t" );
    }
  
  char *file = strdup( aName.c_str() );
  std::string pluginName = basename( file );
  free( file );

  if ((pluginName.length() > 6) && (pluginName.substr(0, 3) == "lib") && 
      (pluginName.substr( pluginName.length()-3, 3 ) == ".so"))
    {
      pluginName = pluginName.substr( 3, pluginName.length() - 6 );
    }

  delete iPluginMap[ pluginName ];

  return true;
}


void NRS::Base::PluginDirector::signup( std::string aName, Plugin *aPlugin )
{
  if (aName == NRS::Literals::Object::MAIN_COMPONENT())
    {
      _ABORT_( "'" << NRS::Literals::Object::MAIN_COMPONENT()
	       << "' is a reserved plugin name\n\t" );
    }

  if (iCurrentPlugin == NRS::Literals::Object::MAIN_COMPONENT())
    clearPlugin();

  if (iCurrentPlugin.empty())
    {
      iCurrentPlugin = aName;
      iPluginMap[ aName ] = aPlugin;
    }
  else
    {
      _ABORT_( "Loading plugins simultaneously\n\t" );
    }
}

void NRS::Base::PluginDirector::unRegister( std::string aName )
{
  iPluginMap.erase( aName );
}

void NRS::Base::PluginDirector::clearPlugin()
{
  iCurrentPlugin.clear();
}

std::string NRS::Base::PluginDirector::getCType()
{
  std::string ctype;
  
  for ( typeof( iPluginMap.begin() ) iter = iPluginMap.begin();
	iter != iPluginMap.end(); iter++ )
    ctype += iter->first;

  return ctype;
}

std::string NRS::Base::PluginDirector::queryFielder() const
{
  return NRS::Literals::Object::PLUGINDIRECTOR();
}

void NRS::Base::PluginDirector::fieldExecutable( const std::string &execName )
{
  ArgumentDirector &theAD = ArgumentDirector::getDirector();
  
  std::string defName = NRS::Literals::File::NRS();
  std::transform( defName.begin(), // original string's beginning
		  defName.end(), // original string's end
		  defName.begin(), // where to write the new string?
		  toupper ); // unary operator
  defName += ".";
  defName += NRS::Literals::File::COMPONENT();

  if (execName != defName)
    {
      defName = defName.substr( 0, strlen( NRS::Literals::File::NRS() ) + 1 );
      if ( ( execName.length() <= defName.length() ) ||
	   ( execName.substr( 0, defName.length() ) != defName ) )
	{
	  _ABORT_( "Unrecognised executable name " << execName );
	}

      std::list< std::string > args;
      args.push_back( "--plugin" );
      args.push_back( execName.substr( defName.length() ) );
      theAD.parseArguments( args );

    }
}

void NRS::Base::PluginDirector::
fieldArguments( std::list< std::string > &arguments )
{
  std::string arg = *arguments.begin();
  arguments.pop_front();

  if (arg == "plugin")
    {
      if (arguments.size() < 1)
	{
	  _ABORT_( "Not enough arguments to plugin\n\t" );
	}
	if (!addPlugin( *arguments.begin() ))
	{
	  _ABORT_( "Failed to load plugin '"
		   << *arguments.begin() << "'\n\t" );
	}
      arguments.pop_front();
    }
  else
  _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
}

void NRS::Base::PluginDirector::
fieldSpareArguments( std::list< std::string > &arguments )
{
  ArgumentDirector &theAD = ArgumentDirector::getDirector();
  std::list< std::string > args;
  for ( typeof( arguments.begin() ) anIter = arguments.begin();
	anIter != arguments.end(); anIter++ )
    {
      args.push_back( "--plugin" );
      args.push_back( *anIter );
    }
  theAD.parseArguments( args );
}

