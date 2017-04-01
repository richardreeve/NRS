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
#include <libgen.h>

#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"
#include "ArgumentDirector.hh"

#pragma implementation "PluginDirector.hh"
#include "PluginDirector.hh"

namespace
{
  static NRS::Base::PluginDirector &sPD =
  NRS::Base::PluginDirector::getDirector();
}

NRS::Base::PluginDirector::PluginDirector() : iCurrentPlugin( "" )
{
}

NRS::Base::PluginDirector::~PluginDirector()
{
}

bool NRS::Base::PluginDirector::hasPlugin( std::string aName )
{
  return false;
}

bool NRS::Base::PluginDirector::addPlugin( std::string aName )
{
  return false;
}

bool NRS::Base::PluginDirector::removePlugin( std::string aName )
{
  return false;
}


void NRS::Base::PluginDirector::signup( std::string aName, Plugin *aPlugin )
{
}

void NRS::Base::PluginDirector::unRegister( std::string aName )
{
}

void NRS::Base::PluginDirector::clearPlugin()
{
}

std::string NRS::Base::PluginDirector::getCType()
{
  return "";
}

std::string NRS::Base::PluginDirector::queryFielder() const
{
  return NRS::Literals::Object::PLUGINDIRECTOR();
}

void NRS::Base::PluginDirector::fieldExecutable( const std::string &execName )
{
  _ABORT_( "Fielding unknown program '" << execName << "'\n\t" );
}

void NRS::Base::PluginDirector::
fieldArguments( std::list< std::string > &arguments )
{
  _ABORT_( "Fielding unknown argument '" << *arguments.begin() << "'\n\t" );
}

void NRS::Base::PluginDirector::
fieldSpareArguments( std::list< std::string > &arguments )
{
  _ABORT_( "Fielding unknown argument '" << *arguments.begin() << "'\n\t" );
}

