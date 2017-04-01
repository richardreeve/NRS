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

#ifndef _INTELLIGENT_STORE_HH
#define _INTELLIGENT_STORE_HH

#pragma interface
#include <vector>

#include "Exception.hh"
#include "MessageStore.hh"
#include "Segment.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    /// Intelligent segments in a single class
    class IntelligentSegments
    {
    public:
      /// Default constructor
      IntelligentSegments() :
	iSegment( NUM_INTELLIGENT_ATT )
      {
	for (unsigned_t num = 0; num < NUM_INTELLIGENT_ATT; ++num)
	  iSegment[num] = NULL;
      }

      /// Default destructor
      ~IntelligentSegments()
      {
	for (unsigned_t num = 0; num < NUM_INTELLIGENT_ATT; ++num)
	  delete iSegment[num];	
      }

      /// Intelligent segments 
      std::vector< Segment* > iSegment;

    private:
      /// No copy contructors
      IntelligentSegments( const IntelligentSegments & );

      /// No copying
      IntelligentSegments &operator=( const IntelligentSegments & );

    };


    /**
     * Data holding linked list for intelligents
     **/
    class IntelligentStore
    {
    public:
      /// Default constructor for IntelligentStore.
      /** 
       *
       * \param anEncoding the encoding of the intelligent store
       *
      **/
      IntelligentStore( Enum::EncodingType anEncoding ) :
	iMS( NUM_INTELLIGENT_ATT, anEncoding ),
	iIntelligentPresent( 0 )
      {
      }

      /// Copy (and translate) constructor for IntelligentStore.
      /** 
       *
       * note: increments translation count if appropriate
       *
       * \param rhs the IntelligentStore to copy
       * \param anEncoding the encoding of the intelligent store
       *
      **/
      IntelligentStore( const IntelligentStore &rhs,
			Enum::EncodingType anEncoding );

      /// Constructor for IntelligentStore from segments
      /**
       *
       * \param intelligentSegments intelligent segments to copy from
       * \param anEncoding the encoding of the intelligent store
       *
      **/
      IntelligentStore( const IntelligentSegments &intelligentSegments,
			Enum::EncodingType anEncoding );

      /// Default destructor
      ~IntelligentStore()
      {
      }

      /// Get encoding used in store
      /**
       *
       * \return encoding used
       *
       **/
      Enum::EncodingType getEncoding() const
      {
	return iMS.getEncoding();
      }

      /// Clear a specific segment
      /**
       *
       * \param aType type of intelligent segment to clear
       * 
       **/
      void clearSegment( Enum::IntelligentType aType )
      {
	iIntelligentPresent[ (unsigned_t) aType ] = 0;
	iMS.clearSegment( (unsigned_t) aType );
      }

      /// Set the data in a specific segment
      /**
       *
       * \param aType type of intelligent segment to set
       * \param ptr the pointer to the character array containing the data
       * \param len the length of the segment
       * 
       **/
      void setSegment( Enum::IntelligentType aType,
		       const uint8_t *ptr, unsigned_t len )
      {
	iIntelligentPresent[ (unsigned_t) aType ] = 1;
	iMS.setSegment( (unsigned_t) aType, ptr, len );
      }

      /// Append the data to a specific segment
      /**
       *
       * \param aType type of intelligent segment to append to
       * \param ptr the pointer to the character array containing the data
       * \param len the length of the segment
       * 
       **/
      void appendSegment( Enum::IntelligentType aType,
			  const uint8_t *ptr, unsigned_t len )
      {
	iIntelligentPresent[ (unsigned_t) aType ] = 1;
	iMS.appendSegment( (unsigned_t) aType, ptr, len );
      }

      /// Get the pointer to a specific segment
      /**
       *
       * \param aType type of intelligent segment to retreive
       * 
       * \return pointer to character array containing segment or null
       * if not present
       *
       **/
      const uint8_t *getSegment( Enum::IntelligentType aType ) const
      {
	if (iIntelligentPresent[ (unsigned_t) aType ] == 1)
	  return iMS.getSegment( (unsigned_t) aType );
	else
	  return NULL;
      }

      /// Does the store have a specific segment
      /**
       *
       * \param aType type of intelligent segment to retreive
       * 
       * \return whether segment exists
       *
       **/
      bool hasSegment( Enum::IntelligentType aType ) const
      {
	return (iIntelligentPresent[ (unsigned_t) aType ] == 1);
      }

      /// Clear the contents of the message
      void reset()
      {
	iMS.reset();
	iIntelligentPresent = 0;
      }

      /// copy from const object
      /**
       *
       * \param rhs object to copy from
       *
       * \return copied object
       *
       **/
      IntelligentStore &operator=( const IntelligentStore &rhs )
      {
	iMS = rhs.iMS;
	iIntelligentPresent = rhs.iIntelligentPresent;
	return *this;
      }

      /// Create a set of segments which match the intelligent data
      /**
       *
       * \return Intelligent segment which belong to the caller which
       * match the store
       *
       **/
      IntelligentSegments *createSegments() const;

      /// Copy segments into intelligent data
      /**
       *
       * \param segments IntelligentSegments to use
       *
       **/
      void useSegments( const IntelligentSegments &segments );

      /// Get the message store
      /**
       *
       * \return message store in whcih data is kept
       *
       **/
      const MessageStore &getMessageStore() const
      {
	return iMS;
      }

      /// Get the message store
      /**
       *
       * \return message store in whcih data is kept
       *
       **/
      MessageStore &getMessageStore()
      {
	return iMS;
      }

    private:
      /// MessageStore contained in IntelligentStore
      MessageStore iMS;

      /// Is the intelligent attribute present
      intelligent_t iIntelligentPresent;

    };
  }
}

#endif //ndef _INTELLIGENT_STORE_HH
