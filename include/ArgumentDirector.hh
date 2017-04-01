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

#ifndef _ARGUMENT_DIRECTOR_HH
#define _ARGUMENT_DIRECTOR_HH

#include <string>
#include <map>
#include "Exception.hh"
#include "ArgumentFielder.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    /// This class holds the information about argument handling
    /**
     * 
     * A single ArgumentDirector, created at initialisation, holds all
     * of the information about argument handling.
     * 
     **/
    class ArgumentDirector : public ArgumentFielder
    {
    public:
      /// static accessor for the ArgumentDirector.
      /**
       *
       * \return A reference to the ArgumentDirector.
       *
       **/
      static ArgumentDirector &getDirector()
      {
	static ArgumentDirector sAD;
	return sAD;
      }
      

      /// Parse Command line arguments for the program
      /**
       *
       * \param argc the number of arguments to the program
       * \param argv the arguments to the system as an array of
       * char*'s
       *
       **/
      void parseArguments( int argc, char **argv );

      /// Parse Command line arguments for the program
      /**
       *
       * \param arguments the arguments to the system as a list of
       * strings
       *
       **/
      void parseArguments( std::list< std::string > &arguments );

      /// Register a command line argument with the system
      /**
       *
       * \param abbrev the single letter abbreviation to the switch: 0
       * if there is no abbreviation
       * \param full the full argument
       * \param shortDesc the short description of the argument to
       * show what parameters it has
       * \param description the description of the argument for the
       * help function
       * \param theAF the ArgumentFielder which will field this argument
       *
       **/
      void addArgumentFielder( const char abbrev,
			       std::string full,
			       std::string shortDesc,
			       std::string description,
			       ArgumentFielder *theAF );

      /// Remove a command line argument fielder from the system
      /**
       *
       * \param theAF the ArgumentFielder which will field this argument
       *
       **/
      void removeArgumentFielder( ArgumentFielder *theAF );

      /// Register the component which will field additional arguments
      /// without switches in the command line
      /**
       *
       * \param shortDesc the short description of spare arguments
       * \param description the description of the arguments for the
       * help function
       * \param theAF the ArgumentFielder which will field this
       * argument
       *
       **/
      void addSpareArgumentFielder( std::string shortDesc,
				    std::string description,
				    ArgumentFielder *theAF);

      /// Register the component which will field the executable name
      /**
       *
       * \param ramble a general description of the program
       * \param theAF the ArgumentFielder which will field the
       * executable name
       *
       **/
      void addExecutableFielder( std::string ramble,
				 ArgumentFielder *theAF );


      /// Get concatenated help scripts from ArgumentFielders
      /**
       *
       * \returns the help description
       *
       **/
      std::string getHelpDescription( const bool withDebugging = false );

      /// Returns description of fielder
      /**
       *
       * \returns a description of the fielder for debugging
       *
       **/
      std::string queryFielder() const;

      /// Parse requested command line arguments for the program
      /**
       * NB: this method will modify the arguments in place
       *
       * \param arguments the arguments to the system as an array of
       * strings
       *
       **/      
      void fieldArguments( std::list< std::string > &arguments );

      /// Determines if the compenent has yet been assigned an ID
      /**
       *
       * \returns true if CID has been assigned and false otherwise
       *
       **/
    private:
      /// Destructor
      /**
       *
       * This is a private destructor as no-one can create or destroy
       * ArgumentDirectors.
       *
       **/
      virtual ~ArgumentDirector();

      /// Constructor
      /**
       *
       * This is a private constructor as no-one can create or destroy
       * ArgumentDirectors.
       *
       **/
      ArgumentDirector();

      /// The executable fielder
      ArgumentFielder *iExecFielder;

      /// The spare argument fielder
      ArgumentFielder *iSpareFielder;

      /// The spare argument's short documentation
      std::string iSpareArgShortDesc;

      /// The spare argument's main documentation
      std::string iSpareArgDescription;

      /// The executable's general description
      std::string iExecutableDescription;

      /// The map of abbreviated switches to their full names
      std::map< char, std::string > iAbbrevArgFullMap;

      /// The map of full switches to their short documentation
      std::map< std::string, std::string > iFullArgShortMap;
      
      /// The map of full switches to their documentation
      std::map< std::string, std::string > iFullArgHelpMap;
      
      /// The map of full switches to their ArgumentFielders
      std::map< std::string, ArgumentFielder* > iFullFielderMap;

      /// The name of the executable being run
      std::string iExecutableName;

    };
  }
}
#endif //ndef _ARGUMENT_DIRECTOR_HH
