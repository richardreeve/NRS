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

#ifndef _STRING_MANAGER_HH
#define _STRING_MANAGER_HH

#include <string>
#include "VariableManager.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Target;
  }
  namespace Type
  {
    /// The manager for the String Variable type.
    /** 
     *
     * This is the base class for all managers of Variables with 1 string
     * argument - Variables of String type.
     *
     **/
    class StringManager : public NRS::Base::VariableManager
    {
    public:
      /// Default constructor.
      /**
       *
       * This is the constructor for the basic StringManager.
       *
       **/
      StringManager();

      /// Constructor.
      /**
       *
       * This is the constructor for a Manager derived from the
       * StringManager.
       *
       * \param aMessageName Name of the Message type that will be
       * managed.
       * \param aDisplayName Prettified name of the Message type
       * (leave empty for same as aMessageName).
       * \param isUnit is it a unit (or a message)?
       *
       **/
      StringManager( std::string aMessageName,
		     std::string aDisplayName,
		     bool isUnit );

      /// Destructor.
      virtual ~StringManager()
      {
      }

      /// Create a segment for a link of this type
      /**
       *
       * \param aSDRef segment description for the segment
       *
       * \returns segment of same type as variable, belonging to caller
       *
       **/
      virtual Base::Segment *
      createSegment( const Base::SegmentDescription &aSDRef ) const;

      /// Create a default variable of this type
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( const std::string &aName ) const;
    };
  }
}
#endif //ndef _STRING_MANAGER_HH
