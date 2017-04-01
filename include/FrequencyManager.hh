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

#ifndef _FREQUENCY_MANAGER_HH
#define _FREQUENCY_MANAGER_HH

#include <string>

#pragma interface
#include "FloatManager.hh"

namespace NRS
{
  namespace Unit
  {
    /// The manager for the Frequency Units.
    /** 
     *
     * This is the class for the Frequency Manager
     *
     **/
    class FrequencyManager : public Type::FloatManager
    {
    public:
      /// Constructor.
      
      /// Default constructor.
      /**
       *
       * This is the constructor for the basic FrequencyManager.
       *
       **/
      FrequencyManager();
      
      /// Constructor.
      /**
       *
       * This is the constructor for a Manager derived from the
       * FloatManager.
       *
       * \param aMessageName Name of the Message type that will be
       * managed.
       * \param aDisplayName Prettified name of the Message type
       * (leave empty for same as aMessageName).
       *
       **/
      FrequencyManager( std::string aMessageName,
			  std::string aDisplayName );

      /// Destructor.
      virtual ~FrequencyManager()
      {
      }

      /// Repository for text string "Frequency"
      /**
       *
       * \return the string "Frequency"
       *
       **/
      static const char *getName()
      {
	static const char *name = "Frequency";
	return name;
      }

      /// Create a new Variable
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( const std::string &aName ) const;
    };
  }
}



#endif //ndef _FREQUENCY_MANAGER_HH
