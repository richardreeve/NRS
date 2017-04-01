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
#include <list>
#include <cstring>

#include "ArgumentDirector.hh"
#include "Executive.hh"
#include "VariableNodeDirector.hh"
#include "Exception.hh"

int main( int argc, char **argv )
{
  try
    {
      NRS::Base::ArgumentDirector::getDirector().
	parseArguments( argc, argv );
#ifdef DEBUG
      NRS::Base::VariableNodeDirector &theVND = 
	NRS::Base::VariableNodeDirector::getDirector();
      _DEBUG_( "Testing" );
      theVND.displayNodes( std::cout );
      theVND.displayVariables( std::cout );
      _DEBUG_( "Tested" );
#endif
      NRS::Base::Executive::getExecutive().run();

#ifdef DEBUG
      // Just a bit of debugging here
      theVND.displayNodes( std::cout );
      theVND.displayVariables( std::cout );
#endif
    }
  catch ( NRS::Base::Exception &e )
    {
      _EXIT_( e );
    }

  return 0;
}
