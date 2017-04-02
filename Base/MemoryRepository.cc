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

#include "MemoryRepository.hh"
#include "MessageStore.hh"
#include "ArgumentDirector.hh"
#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"

static NRS::Base::MemoryRepository &sMR = NRS::Base::MemoryRepository::getMR();

NRS::Base::MemoryRepository::BufferHolder::BufferHolder( size_t size ) :
  next( NULL )
{
  ptr = new char[size];
}

NRS::Base::MemoryRepository::BufferHolder::~BufferHolder()
{
  if (next)
    delete next;
  if (ptr)
    delete[] ptr;
}

NRS::Base::MemoryRepository::MSHolder::MSHolder() : next( NULL )
{
  ptr = new MessageStore;
}

NRS::Base::MemoryRepository::MSHolder::~MSHolder()
{
  if (next)
    delete next;
  if (ptr)
    delete[] ptr;
}

NRS::Base::MemoryRepository::MemoryRepository() :
  iBufferSize( 100 ), iChunkSize( 1000 ),
  iBufferHolderPtr( NULL ), iEmptyBufferHolderPtr( NULL ),
  iMSHolderPtr( NULL ), iEmptyMSHolderPtr( NULL )
{
  ArgumentDirector::getDirector().
    addArgumentFielder( 'c', "chunksize", "\t\t[ -c | --chunksize <num> ]\n",
			"\t-c|--chunksize <num>\t\t"
			"size of memory allocation chunks\n", this );

  ArgumentDirector::getDirector().
    addArgumentFielder( 'b', "buffersize", "\t\t[ -b | --buffersize <num> ]\n",
			"\t-b|--buffersize <num>\t\t"
			"size of allocated memory buffers\n", this );
}

NRS::Base::MemoryRepository::~MemoryRepository()
{
  delete iBufferHolderPtr;
  delete iEmptyBufferHolderPtr;
  delete iMSHolderPtr;
  delete iEmptyMSHolderPtr;
}

void NRS::Base::MemoryRepository::allocateBuffers()
{
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();

  if (theVND.hasUnresolvedDeps())
    _INFO_( "Allocating buffers" );
  else
    _ERROR_( "Allocating buffers" );

  BufferHolder *head = iBufferHolderPtr;
  for ( int i = 0; i < getChunkSize(); ++i )
    {
      iBufferHolderPtr = new BufferHolder( getBufferSize() );
      iBufferHolderPtr->next = head;
      head = iBufferHolderPtr;
    }
}

void NRS::Base::MemoryRepository::allocateMessageStores()
{
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();

  if (theVND.hasUnresolvedDeps())
    _INFO_( "Allocating MessageStores" );
  else
    _ERROR_( "Allocating MessageStores" );

  MSHolder *head = iMSHolderPtr;
  for ( int i = 0; i < getChunkSize(); ++i )
    {
      iMSHolderPtr = new MSHolder();
      iMSHolderPtr->next = head;
      head = iMSHolderPtr;
    }
}

std::string NRS::Base::MemoryRepository::queryFielder() const
{
  return NRS::Literals::Object::MEMORYREPOSITORY();
}

void NRS::Base::MemoryRepository::
fieldArguments( std::list< std::string > &arguments )
{
  std::string arg = arguments.front();
  arguments.pop_front();

  if (arg == "chunksize")
    {
      if (arguments.size() < 1)
	_ABORT_( "Not enough arguments to chunksize\n\t" );
      std::stringstream anSS( arguments.front() );
      anSS >> iChunkSize;
      arguments.pop_front();
    }
  else if (arg == "buffersize")
    {
      if (arguments.size() < 1)
	_ABORT_( "Not enough arguments to buffersize\n\t" );
      std::stringstream anSS( arguments.front() );
      anSS >> iBufferSize;
      arguments.pop_front();
    }
  else
    _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
}
