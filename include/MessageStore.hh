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

#ifndef _MESSAGE_STORE_HH
#define _MESSAGE_STORE_HH

#include <vector>
#include <string>

#pragma interface
#include "Exception.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    /**
     * Data holding linked list for messages
     **/
    class MessageStore
    {
    public:
      /** 
       * 
       * Default constructor for MessageStore.
       *
       * \param anEncoding the encoding of the message store
       *
      **/
      MessageStore( unsigned_t numSegments,
		    Enum::EncodingType anEncoding ) :
	iEncoding( anEncoding ),
	iSegment( numSegments )
      {
      }

      /// Default destructor
      ~MessageStore()
      {
      }

      /// Get number of segments
      /**
       *
       * \return number of segments
       *
       **/
      unsigned_t getNumSegments() const
      {
	return iSegment.size();
      }

      /// Get encoding used in store
      /**
       *
       * \return encoding used
       *
       **/
      Enum::EncodingType getEncoding() const
      {
	return iEncoding;
      }

      /// Clear a specific segment
      /**
       *
       * note that this does not leave BMF segments in a legal state!
       *
       * \param num number of segment to clear
       * 
       **/
      void clearSegment( unsigned_t num )
      {
	_ASSERT_( num < iSegment.size(), "Illegal segment number" );
	iSegment[num].clear();
      }

      /// Set the data in a specific segment
      /**
       *
       * \param num number of segment to set
       * \param ptr the pointer to the character array containing the data
       * \param len the length of the segment
       * 
       **/
      void setSegment( unsigned_t num, const uint8_t *ptr, unsigned_t len )
      {
	_ASSERT_( num < iSegment.size(), "Illegal segment number" );
	iSegment[num].assign( ptr, len );
      }

      /// Append the data to a specific segment
      /**
       *
       * \param num number of segment to append to
       * \param ptr the pointer to the character array containing the data
       * \param len the length of the segment
       * 
       **/
      void appendSegment( unsigned_t num, const uint8_t *ptr, unsigned_t len )
      {
	_ASSERT_( num < iSegment.size(), "Illegal segment number" );
	iSegment[num].append( ptr, len );
      }

      /// Get the pointer to a specific segment
      /**
       *
       * \param num number of segment to retreive
       * 
       * \return pointer to character array containing segment
       *
       **/
      const uint8_t *getSegment( unsigned_t num ) const
      {
	_ASSERT_( num < iSegment.size(), "Illegal segment number" );
	return iSegment[num].c_str();
      }

      /// Clear the contents of the message
      void reset();

      /// copy from const object
      /**
       *
       * \param rhs object to copy from
       *
       * \return copied object
       *
       **/
      MessageStore &operator=( const MessageStore &rhs );

    private:
      /// Encoding of segments
      Enum::EncodingType iEncoding;

      /// Vector containing all data from message
      std::vector< data_t > iSegment;
    };
  }
}

#endif //ndef _MESSAGE_STORE_HH
