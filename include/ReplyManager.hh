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

#ifndef _REPLY_MANAGER_HH
#define _REPLY_MANAGER_HH

#include <map>
#include <string>
#include "VariableManager.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Target;
  }
  namespace Message
  {
    /// The manager for the ReplyVNID Variable type.
    /** 
     *
     * This is the base class for all managers of Variables which can
     * reply node ids - it calls the node factories to do the construction
     *
     **/
    class ReplyManager : public NRS::Base::VariableManager
    {
    public:
      /// Default constructor.
      /**
       *
       * This constructor is the base constructor for all
       * Reply...Managers. 
       *
       * \param aMessage Name of the Variable/Message type that will
       * be managed.
       * \param displayName Prettified name of the Variable/Message
       * type (leave empty for same as aMessage).
       * \param numSeg number of segments in the message.
       *
       **/
       ReplyManager( std::string aMessage, std::string displayName, 
		     int numSeg );

      /// Destructor.
      virtual ~ReplyManager()
      {
      }

      /// Setup segment records.
      /**
       *
       * This method sets up the segment descriptions for the CSL
       * output
       *
       **/
      virtual void setSegments();

      /// Extract a message ID from a PML message
      /**
       *
       * \param attrMap map of nrs specific attributes for variable
       * \param msgID the message ID to extract into
       *
       * \return success of extraction
       *
       **/
      bool extractMsgID( std::map< std::string, std::string > &attrMap,
			 unsigned_t &msgID );
      
      /// Extract a message ID from a BMF message
      /**
       *
       * \param data the BMF message to process
       * \param msgID the message ID to extract into
       *
       * \return success of extraction
       *
       **/
      bool extractMsgID( char *&data, unsigned_t &msgID );

      /// Insert a message ID into a PML message
      /**
       *
       * \param attrMap map of nrs specific attributes for variable
       * \param msgID the message ID to insert
       *
       **/
      void insertMsgID( std::map< std::string, std::string > &attrMap,
			unsigned_t msgID );

      /// Insert a message ID into a BMF message
      /**
       *
       * \param msg the BMF message to insert into
       * \param alloc the total buffer size
       * \param data the point in the BMF message to insert
       * \param size the amount of memory left to the end of the buffer
       * \param msgID the message ID to insert
       *
       **/
      void insertMsgID( char *&msg, size_t &alloc,
			char *&data, size_t &size,
			unsigned_t msgID );
    };
  }
}
#endif //ndef _REPLY_MANAGER_HH
