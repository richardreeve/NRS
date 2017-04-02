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

#ifndef _ARGUMENT_FIELDER_HH
#define _ARGUMENT_FIELDER_HH

#include <list>
#include <string>

namespace NRS
{
  namespace Base
  {
    /// This class is the base class for all elements which require
    /// interaction with the command line
    class ArgumentFielder
    {
    public:
      /// Dummy destructor
      virtual ~ArgumentFielder()
      {
      }

      /// Returns description of fielder
      /**
       *
       * \returns a description of the fielder for debugging
       *
       **/
      virtual std::string queryFielder() const = 0;

      /// Field the executable name of the program, and deal with appropriately
      /**
       *
       * \param execName the executable name of the program
       *
       **/
      virtual void fieldExecutable( const std::string &execName );

      /// Parse requested command line arguments for the program
      /**
       * NB: this method will modify the arguments in place
       *
       * \param arguments the arguments to the system as an array of
       * strings
       *
       **/      
      virtual void fieldArguments( std::list< std::string > &arguments );

      /// Parse requested spare command line arguments for the program
      /**
       * NB: this method will modify its arguments in place
       *
       * \param arguments the arguments to the system as an array of
       * strings
       *
       **/      
      virtual void fieldSpareArguments( std::list< std::string > &arguments );
    };
  }
}

#endif //ndef _ARGUMENT_FIELDER_HH
