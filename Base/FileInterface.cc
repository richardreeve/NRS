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

#include <cstring>
#include <list>
#include <signal.h>
#include <string>
#include <termios.h>
#include <unistd.h>

#include "BufferedInterface.hh"
#include "Exception.hh"
#include "ExternalInterfaceHandler.hh"
#include "FileInterface.hh"
#include "StringLiterals.hh"

NRS::Interface::FileInterface::
FileInterface( Enum::EncodingType anEncodingType,
	       Interface::MessageParser *aParserPtr,
	       std::string inFile, std::string outFile,
	       bool instantTransmit, bool receiveAll,
	       int maxReceive, size_t initBufferSize ) :
  BufferedInterface( Enum::Fifo, anEncodingType, aParserPtr,
		     true, true, instantTransmit, receiveAll,
		     maxReceive, initBufferSize ),
  iInFilename( inFile ), iInOpen( false ), iInClosed( false ),
  iOutFilename( outFile ), iOutOpen( false ), iOutClosed( false ),
  iReadWrite( false ), iSerialSpeed( 0 )
{
  ::signal( SIGPIPE, SIG_IGN );
  inOpen();
  outOpen();
  
  iTV.tv_sec = 0;
  iTV.tv_usec = 0;

  _DEBUG_( iEncodingType );
}

NRS::Interface::FileInterface::
FileInterface( Enum::EncodingType anEncodingType,
	       Interface::MessageParser *aParserPtr,
	       std::string inFile, bool doRead,
	       bool doWrite, bool instantTransmit,
	       bool receiveAll, int maxReceive, size_t initBufferSize,
	       int serialSpeed ) :
  BufferedInterface( ( doRead && doWrite ?
		       Enum::Device : ( doRead ? Enum::File : Enum::Log ) ),
		     anEncodingType,  aParserPtr, doRead, doWrite,
		     instantTransmit, receiveAll, maxReceive, initBufferSize ),
  iInFilename( inFile ), iInOpen( false ), iInClosed( !doRead ),
  iOutFilename( inFile ), iOutOpen( false ), iOutClosed( !doWrite ),
  iReadWrite( doRead && doWrite ), iSerialSpeed( serialSpeed )
{
  if (doRead)
    inOpen();
  else if (doWrite)
    outOpen();
  else
    _ABORT_( "File is for neither reading nor writing!" );

  iTV.tv_sec = 0;
  iTV.tv_usec = 0;
}

NRS::Interface::FileInterface::~FileInterface()
{
  if (iInOpen)
    ::close( iInFD );
  if (iOutOpen)
    ::close( iOutFD );
}

bool NRS::Interface::FileInterface::inOpen()
{
  if (!iInOpen && !iInClosed)
    {
      if (iReadWrite)
	{
	  iOutFD = iInFD = ::open( iInFilename.c_str(), O_RDWR | O_NONBLOCK );
	  struct termios Oldtio, newtio; // for storing old and new io states
	  tcgetattr(iOutFD,&Oldtio); // save current port settings
	  bzero(&newtio, sizeof(newtio)); // set new port settings
	  // newtio.c_cflag= baudrate | CRTSCTS | CS8 | CLOCAL | CREAD;
	  newtio.c_iflag= IGNPAR;
	  newtio.c_oflag= 0;
	  switch (iSerialSpeed)
	    {
	    case 0:
	      newtio.c_cflag = B0;
	      break;
	    case 50:
	      newtio.c_cflag = B50;
	      break;
	    case 75:
	      newtio.c_cflag = B75;
	      break;
	    case 110:
	      newtio.c_cflag = B110;
	      break;
	    case 134:
	      newtio.c_cflag = B134;
	      break;
	    case 150:
	      newtio.c_cflag = B150;
	      break;
	    case 200:
	      newtio.c_cflag = B200;
	      break;
	    case 300:
	      newtio.c_cflag = B300;
	      break;
	    case 600:
	      newtio.c_cflag = B600;
	      break;
	    case 1200:
	      newtio.c_cflag = B1200;
	      break;
	    case 1800:
	      newtio.c_cflag = B1800;
	      break;
	    case 2400:
	      newtio.c_cflag = B2400;
	      break;
	    case 4800:
	      newtio.c_cflag = B4800;
	      break;
	    case 9600:
	      newtio.c_cflag = B9600;
	      break;
	    case 19200:
	      newtio.c_cflag = B19200;
	      break;
	    case 38400:
	      newtio.c_cflag = B38400;
	      break;
	    case 57600:
	      newtio.c_cflag = B57600;
	      break;
	    case 115200:
	      newtio.c_cflag = B115200;
	      break;
	    case 230400:
	      newtio.c_cflag = B230400;
	      break;
	    default:
	      _ABORT_( "Unknown speed: " << iSerialSpeed );
	      break;
	    }
	  newtio.c_cflag |= CSTOPB | CS8 | CLOCAL | CREAD;
	  newtio.c_lflag= 0; // set input mode (non-canonical, no echo,...)
	  newtio.c_cc[VTIME] = 5; // inter-character timer
	  newtio.c_cc[VMIN]  = 1; // blocking read until x chars received
	  tcflush(iOutFD, TCIFLUSH); // flush buffer
	  tcsetattr(iOutFD,TCSANOW,&newtio); // set new settings
	}
      else
	iInFD = ::open( iInFilename.c_str(), O_RDONLY | O_NONBLOCK );

      if (iInFD == -1)
	{
	  _ABORT_( "Error in opening file (" << errno << "): "
		   << iInFilename << ": " << strerror( errno ) << "\n\t" );
	}
      if (iReadWrite)
	{
	  iInFILE = fdopen( iInFD, "r+" );
	  iOutFILE = iInFILE;
	}
      else
	{
	  iInFILE = fdopen( iInFD, "r" );
	}

      FD_ZERO( &iInFDSet );
      FD_SET( iInFD, &iInFDSet );
      iInOpen = true;
      if (iReadWrite)
	{
	  FD_ZERO( &iOutFDSet );
	  FD_SET( iOutFD, &iOutFDSet );
	  iOutOpen = true;
	}
    }
  
  return iInOpen;
}

bool NRS::Interface::FileInterface::outOpen()
{
  static bool fifoError = false;
  if (!iOutOpen && !iOutClosed)
    {
      iOutFD = ::open( iOutFilename.c_str(), 
		       O_WRONLY | O_NONBLOCK | O_CREAT | O_TRUNC,
		       S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH );

      if (iOutFD == -1)
	{
	  if (errno == ENXIO) // fifo read side not connected
	    { // non-serious error, so just warn and try later
	      // NB strerror is not thread safe
	      if (!fifoError)
		{
		  _WARN_( "opening '" << iOutFilename << "' for writing:\n\t"
			  << "read side of fifo not yet opened\n\t" );
		  fifoError = true;
		}
	    }
	  else
	    { // fatal error
	      _ABORT_( "Error in opening file ("
		       << errno << "): " << iOutFilename 
		       << ": " << strerror( errno ) << "\n\t" );
	    }
	}
      else
	{
	  iOutFILE = fdopen( iOutFD, "a" );
	  FD_ZERO( &iOutFDSet );
	  FD_SET( iOutFD, &iOutFDSet );
	  iOutOpen = true;
	  fifoError = false;
	}
    }
 
  return iOutOpen;
}

bool NRS::Interface::FileInterface::pollInput()
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
	      else if (num_bytes == 0)
		{
		  if (feof( iInFILE ))
		    {
		      _INFO_( "closing" );
		      ::close( iInFD );
		      iInClosed = true;
		      iInOpen = false;
		      if (iReadWrite)
			{
			  iOutClosed = true;
			  iOutOpen = false;
			}
		    }
		}
	      else
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

bool NRS::Interface::FileInterface::flushOutput()
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
	      else if (num_bytes == 0)
		{
		  if (feof( iOutFILE ))
		    {
		      ::close( iOutFD );
		      iOutClosed = true;
		      iOutOpen = false;
		      if (iReadWrite)
			{
			  iInClosed = true;
			  iInOpen = false;
			}
		    }
		}
	      else
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
