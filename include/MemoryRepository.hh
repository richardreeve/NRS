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

#ifndef _MEMORY_REPOSITORY_HH
#define _MEMORY_REPOSITORY_HH

#include <cstring>

#include "Exception.hh"
#include "ArgumentFielder.hh"

namespace NRS
{
  namespace Base
  {
    class MessageStore;
    /**
     *
     * Allocator of all run-time memory - acts as pool from which
     * MessageStores and char* are collected and to which they are
     * returned.
     *
     **/
    class MemoryRepository : public ArgumentFielder
    {
      /// Storage class for buffers used by MemoryRepository
      class BufferHolder
      {
      public:
	/// Constructor
	/**
	 *
	 * \param size size of buffer
	 *
	 **/
	BufferHolder( size_t size );
	
	/// Destructor
	~BufferHolder();

	/// next BufferHolder in linked list
	BufferHolder *next;

	/// pointer to memory allocated
	char *ptr;
      };

      /// Storage class for MessageStores used by MemoryRepository
      class MSHolder
      {
      public:

	/// Constructor
	MSHolder();

	/// Destructor
	~MSHolder();

	/// next MSHolder in linked list
	MSHolder *next;

	/// pointer to MessageStore allocated
	MessageStore *ptr;
      };

    public:
      /// static accessor for MemoryRepository.
      /**
       *
       * \return A reference to MemoryRepository.
       *
       **/
      static MemoryRepository &getMR()
      {
	static MemoryRepository sMR;
	return sMR;
      }
 
      /// Returns buffer size for char arrays
      size_t getBufferSize()
      {
	return iBufferSize;
      }

      
      int getChunkSize()
      {
	return iChunkSize;
      }
      
      void allocateBuffers();

      void allocateMessageStores();

      char *getBuffer()
      {
	BufferHolder *head = iBufferHolderPtr;

	if (head == NULL)
	  {
	    _ERROR_( "Allocating memory for buffers!" );
	    allocateBuffers();
	    head = iBufferHolderPtr;
	  }
	iBufferHolderPtr = head->next;
	head->next = iEmptyBufferHolderPtr;
	iEmptyBufferHolderPtr = head;

	char *ptr = head->ptr;
	head->ptr = NULL;

	return ptr;
      }

      void returnBuffer( char *ptr )
      {
	BufferHolder *head = iEmptyBufferHolderPtr;
	if (head == NULL)
	  _ABORT_( "Memory for empty buffers should be available" );
	head->ptr = ptr;
	iEmptyBufferHolderPtr = head->next;
	head->next = iBufferHolderPtr;
	iBufferHolderPtr = head;
      }

      MessageStore *getMS()
      {
	MSHolder *head = iMSHolderPtr;

	if (head == NULL)
	  {
	    _ERROR_( "Allocating memory for buffers!" );
	    allocateMessageStores();
	    head = iMSHolderPtr;
	  }
	iMSHolderPtr = head->next;
	head->next = iEmptyMSHolderPtr;
	iEmptyMSHolderPtr = head;

	MessageStore *ptr = head->ptr;
	head->ptr = NULL;

	return ptr;
      }

      void returnMS( MessageStore *ptr )
      {
	MSHolder *head = iEmptyMSHolderPtr;
	if (head == NULL)
	  _ABORT_( "Memory for empty buffers should be available" );
	head->ptr = ptr;
	iEmptyMSHolderPtr = head->next;
	head->next = iMSHolderPtr;
	iMSHolderPtr = head;
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

    private:
      /// Default constructor for MemoryRepository private to avoid creation.
      MemoryRepository();

      /// Destructor private to avoid destruction
      virtual ~MemoryRepository();

      int iBufferSize;

      int iChunkSize;

      BufferHolder *iBufferHolderPtr;

      BufferHolder *iEmptyBufferHolderPtr;

      MSHolder *iMSHolderPtr;

      MSHolder *iEmptyMSHolderPtr;

    };
  }
}

#endif //ndef _MEMORY_REPOSITORY_HH
