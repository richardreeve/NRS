/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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

#ifndef _BOOLEAN_SEGMENT_HH
#define _BOOLEAN_SEGMENT_HH

#include "Types.hh"
#include "Segment.hh"
#include "MessageStore.hh"

namespace NRS
{
  namespace Type
  {
    /// A store for a bool, derived from Segment. Provides accessor
    /// methods to anonymously set and translate the underlying value
    /// into communication languages.
    class BooleanSegment : public Base::Segment
    {
    public:
      /// Default constructor
      BooleanSegment() : Segment(), iValue( false )
      {}

      /// Default destructor
      virtual ~BooleanSegment()
      {}

      /// Set segment from encoded data
      /**
       *
       * \param dataPtr raw data
       * \param encoding encoding of data
       *
       **/
      virtual void useRawData( const uint8_t *dataPtr,
			       Enum::EncodingType encoding );

      /// Use segment to create encoding
      /**
       *
       * \param aMS MessageStore to contain data
       * \param num number of segment
       *
       **/
      void createSegment( Base::MessageStore &aMS,
			  unsigned_t num ) const;

      /// Clone segment using virtual call
      /**
       *
       * \return pointer to new object with same state as called
       *
       **/
      virtual Segment *clone() const
      {
	BooleanSegment *anS = new BooleanSegment();
	anS->iValue = iValue;
	return anS;
      }

      /// boolean segment value
      bool iValue;
    };
  }
}

#endif //ndef _BOOLEAN_SEGMENT_HH
