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
#include "Exception.hh"
#include "ArgumentDirector.hh"
#include "StringLiterals.hh"

#ifdef DEBUG

namespace
{
  static NRS::Base::DebugDirector &sDD =
  NRS::Base::DebugDirector::getDirector();
}

NRS::Base::DebugDirector::DebugDirector() : iDebugLevel( 1 )
{
  ArgumentDirector &theAD = ArgumentDirector::getDirector();

  theAD.addArgumentFielder( '#', "debug", "\t\t[ -#|--debug <level> ]\n",
			    "\t-#|--debug <level>\t"
			    "\tset debug level to -1 (none) |\n"
			    "\t\t\t\t\t0 (little) | 1 (normal) | 2 (all)\n",
			    this );
}

NRS::Base::DebugDirector::~DebugDirector()
{
}

void NRS::Base::DebugDirector::
fieldArguments( std::list< std::string > &arguments )
{
  std::string arg = *arguments.begin();
  arguments.pop_front();

  if (arg == "debug")
    {
      if (arguments.size() < 1)
	{
	  _ABORT_( "Not enough arguments to debug\n\t" );
	}
      std::stringstream anSS( *arguments.begin() );
      anSS >> iDebugLevel;
      arguments.pop_front();
    }
  else
    _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
}

std::string NRS::Base::DebugDirector::queryFielder() const
{
  return NRS::Literals::Object::DEBUGDIRECTOR();
}
#endif //def DEBUG

namespace NRS
{
  namespace Base
  {
    std::ostream& operator<<( std::ostream& stream, 
                              const NRS::Base::Exception& ex )
    {
      stream << ex.m_sstream.str() << " [Thrower=" << ex.m_thrower << "]";
      
      return stream;
    }  
  }
}

