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

#pragma implementation
#include <cstring>
#include <list>
#include <signal.h>
#include <string>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "BufferedInterface.hh"
#include "Exception.hh"
#include "ExternalInterfaceHandler.hh"
#include "SocketInterface.hh"
#include "StringLiterals.hh"

NRS::Interface::SocketInterface::
SocketInterface( Enum::EncodingType anEncodingType,
		 Interface::MessageParser *aParserPtr,
		 int inSocket, bool doRead,
		 bool doWrite, bool instantTransmit,
		 bool receiveAll, int maxReceive, size_t initBufferSize ) :
  BufferedInterface( ( doRead && doWrite ?
		       Enum::Device : ( doRead ? Enum::Socket : Enum::Log ) ),
		     anEncodingType,  aParserPtr, doRead, doWrite,
		     instantTransmit, receiveAll, maxReceive, initBufferSize ),
  iSocketNum( inSocket ), iOpen( false ), iClosed( !doRead ),
  iReadWrite( doRead && doWrite )
{
  if (doRead)
    inOpen();
  else if (doWrite)
    outOpen();
  else
    _ABORT_( "Socket is for neither reading nor writing!" );

  iTV.tv_sec = 0;
  iTV.tv_usec = 0;
}

NRS::Interface::SocketInterface::~SocketInterface()
{
  if (iOpen)
    ::close( iInFD );
}

bool NRS::Interface::SocketInterface::inOpen()
{
  if (!iOpen && !iClosed)
    {
      struct sockaddr_in address;
      int addrlen;
      int retval;
      int tmp_sock_fd;
      int tries = 0;
      
      address.sin_family = AF_INET;
      address.sin_port = htons( iSocketNum );
      address.sin_addr.s_addr = htonl( INADDR_ANY );
      
      // create socket
      retval = socket( PF_INET, SOCK_STREAM, 0 );
      if (retval < 0)
	{
	  _ABORT_( "Failed TCP/IP socket request: " << errno );
	  exit(1);
	}
      else
	{
	  tmp_sock_fd=retval;
	}
      
      addrlen = sizeof(struct sockaddr_in);
      retval = bind( tmp_sock_fd, (struct sockaddr *)&address, addrlen );
      if (retval==-1)
	{
	  if (errno != EADDRINUSE)
	    {
	      _ABORT_( "cannot bind:" << errno );
	    }
	  else
	    {
	      _DEBUG_( "Connection already in use, "
		       "waiting for it to free up" );
	      retval = 1;
	    }
	}
      else
	{ // bind worked
	  // so listen on socket
	  _DEBUG_( "Listening for connection on port " << iSocketNum );
	  retval=listen(tmp_sock_fd, 1);
	  if (retval==-1)
	    {
	      _ABORT_( "Cannot listen on TCP/IP socket: " << errno );
	    }
  
	  // accept connection to socket
	  retval=accept(tmp_sock_fd, (struct sockaddr *)&address, &addrlen);
	  if (retval==-1)
	    {
	      _ABORT_( "Failed to accept TCP/IP connection: " << errno );
	    }
	  else
	    {
	      _DEBUG_( "Received connection\n" );
	    }
	  if (close(tmp_sock_fd) == 0)
	    {
	      iOpen = true;
	      iFD = retval;
	      FD_ZERO( &iFDSet );
	      FD_SET( iFD, &iFDSet );
	    }
	  else
	    {
	      _ABORT_( "Error shutting down original socket: " << errno );
	    }
	}
    }
  return iOpen;
}

bool NRS::Interface::SocketInterface::outOpen()
{
  return inOpen();
}

bool NRS::Interface::SocketInterface::pollInput()
{
  if (iInOpen || inOpen())
    {
      // Are we past halfway through the buffer and there's another free?
      if ( ( (size_t) (iInEndPtr[iInBufferActiveWrite] -
	      iInBuffer[iInBufferActiveWrite]) > (iInAllocSize >> 1) ) &&
	   ( iInBufferActiveRead == iInBufferActiveWrite ) )
	{
	  // Are we at the end of a message?
	  if ( iInStopPtr == iInEndPtr[iInBufferActiveWrite] )
	    { // then just go on to the next
	      iInBufferActiveWrite = 1 - iInBufferActiveWrite;
	      iInEndPtr[iInBufferActiveWrite] = iInStopPtr =
		iInBuffer[iInBufferActiveWrite];
	    }
	  else // If not
	    { // Is this part message only one part of the buffer?
	      if (iInStopPtr != iInBuffer[iInBufferActiveWrite])
		{ // then copy it into a new buffer
		  //_INFO_( iInEndPtr[iInBufferActiveWrite] - iInStopPtr );
		  int next = 1 - iInBufferActiveWrite;
		  memcpy( iInBuffer[ next ], iInStopPtr,
			  iInEndPtr[iInBufferActiveWrite] - iInStopPtr );
		  iInEndPtr[ next ] = iInBuffer[ next ] +
		    (int) (iInEndPtr[ iInBufferActiveWrite ] - iInStopPtr);
		  iInEndPtr[iInBufferActiveWrite] = iInStopPtr;
		  iInStopPtr = iInBuffer[ next ];
		  iInBufferActiveWrite = next;
		}
	      else // otherwise extend the buffer
		{
		  uint8_t *buff = iInBuffer[iInBufferActiveWrite];
		  iInAllocSize <<= 1;
		  iInBuffer[iInBufferActiveWrite] = 
		    (uint8_t *) realloc( iInBuffer[iInBufferActiveWrite],
				      iInAllocSize );
		  iInEndPtr[iInBufferActiveWrite] =
		    iInBuffer[iInBufferActiveWrite] +
		    (int) (iInEndPtr[iInBufferActiveWrite] - buff);
		  iInStartPtr = iInBuffer[iInBufferActiveWrite] +
		    (int) (iInStartPtr - buff);
		  iInStopPtr = iInBuffer[iInBufferActiveWrite];
		  buff = iInBuffer[1 - iInBufferActiveWrite];
		  iInBuffer[1 - iInBufferActiveWrite] = 
		    (uint8_t *) realloc( iInBuffer[1 - iInBufferActiveWrite],
				      iInAllocSize );
		}
	    }
	}

      if ( (size_t) (iInEndPtr[iInBufferActiveWrite] -
		     iInBuffer[iInBufferActiveWrite]) <= (iInAllocSize >> 1) )
	{
	  int result = select( iInFD + 1, &iInFDSet, NULL, NULL, &iTV );
	  if (result == -1)
	    {
	      // NB strerror is not thread safe
	      _ABORT_( "Error in select ("
		       << errno << "): " << strerror( errno ) << "\n\t" );
	    }
	  else if (result == 0)
	    {
	      FD_SET( iInFD, &iInFDSet );
	    }
	  else
	    {
	      int num_bytes = 
		::read( iInFD, iInEndPtr[iInBufferActiveWrite],
			iInAllocSize - (int)
			(iInEndPtr[iInBufferActiveWrite] -
			 iInBuffer[iInBufferActiveWrite]) );
	      if (num_bytes == -1)
		{
		  if (errno == EAGAIN)
		    {
		      _INFO_( "EAGAIN" );
		    }
		  else
		    {
		      // NB strerror is not thread safe
		      _ABORT_( "Error in read (" << errno << "): "
			       << strerror( errno ) << "\n\t" );
		    }
		}
	      else if (num_bytes != 0)
		{
		  iInEndPtr[iInBufferActiveWrite] += num_bytes;
		  for ( iInStopPtr = iInEndPtr[iInBufferActiveWrite] - 1;
			( ( *iInStopPtr != (uint8_t) 0 ) && 
			  ( iInStopPtr != 
			    iInBuffer[iInBufferActiveWrite] ) ) ;
			iInStopPtr-- );
		  if ( iInStopPtr != iInBuffer[iInBufferActiveWrite] )
		    iInStopPtr++;
		}
	    }
	}
    }
  return iInOpen;
}

bool NRS::Interface::SocketInterface::flushOutput()
{
  if ( (iOutOpen || outOpen()) && (iOutEndPtr != iOutStartPtr) )
    {
      int result = select( iOutFD + 1, NULL, &iOutFDSet, NULL, &iTV );
      if (result == -1)
	{
	  // NB strerror is not thread safe
	  _ABORT_( "Error in select (" << errno << "): " 
		   << strerror( errno ) << "\n\t" );
	}
      else
	{
	  if (result == 0)
	    {
	      FD_SET( iOutFD, &iOutFDSet );
	    }
	  else
	    {
// 	      int num_bytes = ::write( iOutFD, iOutStartPtr,
// 				       (int) (iOutEndPtr - iOutStartPtr) );
	      int msglen = strlen( (char*) iOutStartPtr ) + 1;
	      int num_bytes = ::write( iOutFD, iOutStartPtr, msglen );
	      if (num_bytes == -1)
		{
		  if (errno == EPIPE)
		    {
		      _WARN_( "pipe closed\n\t" );
		      ::close( iOutFD );
		      iOutOpen = false;
		    }
		  else
		    {
		      // NB strerror is not thread safe
		      _ABORT_( "Error in write (" << errno << "): "
			       << strerror( errno ) << "\n\t" );
		    }
		}
	      else if (num_bytes != 0)
		{
		  iOutStartPtr += num_bytes;
		  if (iOutStartPtr == iOutEndPtr)
		    iOutStartPtr = iOutEndPtr = iOutBuffer;
		}
	    }
	}
    }
  return iOutOpen;
}
