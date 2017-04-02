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

#ifndef _ENCODING_HH
#define _ENCODING_HH

#include <vector>
#include <string>

#include "IntelligentStore.hh"
#include "MessageDescription.hh"
#include "MessageStore.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class Segment;

    /**
     * Base class for all messages (BMF, PML)
     **/
    class Encoding
    {
    public:
      /** 
       * 
       * Default constructor for Encodings.
       *
       * \param aMDRef MessageDescription for message encoded
       *
      **/
      Encoding( const MessageDescription &aMDRef );

      /// Default destructor
      ~Encoding();

      /// get type being encoded
      /**
       *
       * \return type being encoded
       *
       **/
      const std::string &getType() const;

      /// add encodings from a new destination
      /**
       *
       * \param encodings the encodings this destination requires
       *
       **/
      void addEncodings( encoding_t encodings );

      /// remove encodings from a old destination
      /**
       *
       * \param encodings the encodings this destination no longer requires
       *
       **/
      void removeEncodings( encoding_t encodings );

      /// Get a segment pointer
      /**
       *
       * \param num number of segment
       *
       **/
      Segment *getSegment( unsigned_t num );

      /// Clear the intelligent segments
      void clearIntelligentSegments();

      /// Get the intelligent segments
      /**
       *
       * \return the pointer for the intelligent segments
       *
       **/
      const IntelligentSegments *getIntelligentSegments() const
      {
	return iISegmentsPtr.get();
      }

      /// Get intelligent Segment pointer
      /**
       *
       * \param iType intelligent segment type
       *
       * \return the pointer to this Intelligent Segment or NULL if none
       *
       **/
      const Segment *getIntelligentSegment( Enum::IntelligentType aType ) const
      {
	if (iISegmentsPtr.get() == NULL)
	  return NULL;
	else
	  {
#ifdef DEBUG
	    return iISegmentsPtr->iSegment.at( (unsigned_t) aType );
#else
	    return iISegmentsPtr->iSegment[ (unsigned_t) aType ];
#endif
	  }
      }

      /// Set intelligent Segment pointer
      /**
       *
       * \param iType intelligent segment type
       *
       * \param intSeg the pointer to this Intelligent Segment or NULL
       * to clear it
       *
       **/
      void setIntelligentSegment( Enum::IntelligentType aType,
				  const Segment *intSeg );

      /// Get a MessageStore corresponding to a specific encoding if present
      /**
       *
       * \param encodingType type of encoding
       *
       * \return reference to MessageStore, throws an exception if not present
       *
       **/
      const MessageStore &getMessageStore( Enum::EncodingType encodingType )
	const;

      /// Get a IntelligentStore for a specific encoding if present
      /**
       *
       * \param encodingType type of encoding
       *
       * \return pointer to IntelligentStore or NULL if none
       *
       **/
      const IntelligentStore *
      getIntelligentStore( Enum::EncodingType encodingType ) const;

      /// Get a const segment pointer
      /**
       *
       * \param num number of segment
       *
       **/
      const Segment *getSegment( unsigned_t num ) const;

      /// Set state to that of input message, invalidate rest
      /**
       *
       * \param aMessageStore received message
       * \param intelligentPtr 
       *
       **/
      void setMessage( const MessageStore &aMessageStore,
		       const IntelligentStore *intelligentPtr );

      /// Update state of required encodings
      void update();

    private:
      /// Variable manager for message
      const MessageDescription &iMD;

      /// Vector of segments from Link for generating encodings
      std::vector< Segment* > iSegmentPtr;

      /// pointer to Intelligent Segments if present
      std::auto_ptr< IntelligentSegments > iISegmentsPtr;

      /// Vector of MessageStores containing all the encodings being used
      std::vector< MessageStore > iMessageStore;

      /// Vector of MessageStores containing all the encodings being used
      std::vector< IntelligentStore* > iIntelligentStore;

      /// Whether the encodings have been updated
      std::vector< bool > iUpdated;

      /// Encoding types required
      encoding_t iEncodings;

      /// Count of PML encodings
      unsigned iPMLEncodings;

      /// Count of BMF encodings
      unsigned iBMFEncodings;
    };
  }
}

#endif //ndef _ENCODING_HH
