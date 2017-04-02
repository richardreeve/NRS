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

#ifndef _EXECUTIVE_HH
#define _EXECUTIVE_HH

#include <string>
#include <map>

#include "ArgumentFielder.hh"
#include "Exception.hh"

/// The namespace for all NRS components.
namespace NRS
{
  /// This namespace contains all the base classes of the system.
  namespace Base
  {

    /// This class holds the framework within which NRS sits.
    /**
     * 
     * A single Executive, created at initialisation, holds all
     * of the information about the system, and acts as the framework
     * within which Nodes and Variables are created and managed.
     * 
     **/
    class Executive : public ArgumentFielder
    {
    public:
      /// static accessor for the Executive.
      /**
       *
       * \return A reference to the Executive.
       *
       **/
      static Executive &getExecutive()
      {
	static Executive sE;
	return sE;
      }
      
      /// start the main loop of the system.
      void run();

      /// Startup basic variable set
      void startup();

      /// Add a startup variable to the system
      /**
       * Note: maximum of one variable of any type
       *
       * \param aManager name of variable type
       * \param aVariable name of variable
       *
       **/
      void addStartupVariable( std::string aManager, std::string aVariable );

      /// Is the component CID defined yet
      /**
       *
       * \returns whether the component CID is defined
       *
       **/
      inline bool hasCID () const
      { 
	return (!iCID.empty());
      }

      /// Gets component id
      /**
       *
       * \returns the ID of the component to which this General Director 
       * belongs or NULL if the ID hasn't yet been assigned.
       *
       **/
      inline const std::string &getCID() const
      {
	return iCID;
      }

      /// Sets component id
      /**
       * 
       *
       * \param cID the string to be used as the component id
       *
       **/
      void setCID ( const std::string &cID ) 
      {
	if (!iCID.empty())
	  _ABORT_( "Trying to reset CID, when already fixed" );

	_DEBUG_( "Set CID to '" << cID << "'\n\t" );

	iCID = cID;
      }

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

      /// Returns delay between system updates in microseconds when not running
      /**
       *
       * \returns delay between system updates in microseconds when not running
       *
       **/
      int getTimestep()
      {
	return iTimestep;
      }

    private:
      /// Destructor
      /**
       *
       * This is a private destructor as no-one can create or destroy
       * Executives.
       *
       **/
      virtual ~Executive();

      /// Constructor
      /**
       *
       * This is a private constructor as no-one can create or destroy
       * Executives.
       *
       **/
      Executive();

      /// The ID of the component of which this is the Executive
      std::string iCID;
 
      /// The set of variables which need to be loaded at startup
      std::map< std::string, std::string > iStartupVariableMap;

      /// delay between system updates in microseconds when not running
      int iTimestep;
    };
  }
}
#endif //ndef _EXECUTIVE_HH
