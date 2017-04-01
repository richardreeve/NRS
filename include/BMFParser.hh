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

#ifndef _BMF_PARSER_HH
#define _BMF_PARSER_HH

#pragma interface

#include "MessageParser.hh"

namespace NRS
{
  namespace Interface
  {
    /// This is the BMF Parser class
    class BMFParser : public MessageParser
    {
    public:
      /// Simple constructor
      BMFParser();

      /// null destructor
      virtual ~BMFParser();
      
      /// Parse a new buffer
      /**
       *
       * \param aBuffer a buffer to parse
       *
       * \returns whether the buffer could be parsed
       *
       **/
      bool parseNew( uint8_t *aBuffer );

      /// Do next parse of existing buffer
      /**
       *
       * \returns whether the parse could be done
       *
       **/
      bool parseAgain();

      /// Encode a message into a data object
      /**
       *
       * This method tells the Parser to create a message and return it
       *
       * \param data store to put encoded message into
       * \param aTarget target to send message to
       * \param aMessageStore data to send
       * \param intelligentPtr intelligent parts if present
       *
       **/
      virtual void 
      createOutput( data_t &data,
		    Base::Target &aTarget, 
		    const Base::MessageStore &aMessageStore,
		    const Base::IntelligentStore *intelligentPtr );

      /// Is the segment empty?
      /**
       *
       * \param seg the segment to check
       *
       * \returns whether the segment is empty
       *
       **/
      static inline bool segIsEmpty( const uint8_t *seg )
      {
	return ((!seg) || ((seg[0] == 1) && ((seg[1] & 1<<7) == 0)));
      }

      /// Is this the end of the message?
      /**
       *
       * \param seg the current segment
       *
       * \returns whether this is an end of message byte
       **/
      static inline bool segIsFinished( const uint8_t *seg )
      {
	return ((!seg) || ( *seg == (uint8_t) 0 ));
      }

      /// Skip a segment
      /**
       *
       * \param seg the segment to pass over
       *
       **/
      static inline void segSkip( uint8_t *&seg )
      {
	while (*(++seg) & 1<<7);
      }

      /// Give the length of a segment
      /**
       *
       * \param seg the segment to be read, 0 if end of message
       *
       * \returns the length of the segment
       **/
      static inline size_t segLength( const uint8_t *seg )
      {
	// NULL pointer
	if (!seg)
	  return 1;

	// end of message
	if (*seg == 0)
	  return 1;

	size_t len = 1;

	while (*(++seg) & 1<<7)
	  ++len;

	return len;
      }

      static const size_t DefaultBufferSize = 2000;
      static const size_t DefaultMessageSize = 2000;
      static const size_t DefaultSegmentSize = 100;
      static const size_t EmptySegmentSize = 2;

      static const uint8_t True;
      static const uint8_t False;

    private:
      /// The pointer to the current position in the parse buffer for
      /// burst mode
      uint8_t *iCurrentBuffer;
    };
  }
}
#endif //ndef _BMF_PARSER_HH
