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

#ifndef _SOCKET_INTERFACE_HH
#define _SOCKET_INTERFACE_HH

#include <cstdio>
#include <cstring>
#include <errno.h>
#include <fcntl.h>
#include <string>
#include <sys/select.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

#include "BufferedInterface.hh"

#pragma interface

namespace NRS
{
  namespace Interface
  {
    /// This class instantiates socket readers and writers
    class SocketInterface : public BufferedInterface
    {
    public:      
      /// Constructor for one socket: either one input, or one in and out
      /**
       *
       * \param anEncodingType The type of encoding being sent (PML, BMF).
       * \param aParserPtr pointer to parser for encoding type
       * \param inSocket the input socket number to read from or write to
       * \param doRead is this socket readable?
       * \param doWrite is this socket writeable?
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       * \param initBufferSize initial buffer size
       *
       **/
      SocketInterface( Enum::EncodingType anEncodingType,
		       Interface::MessageParser *aParserPtr,
		       int inSocket, bool doRead, bool doWrite,
		       bool instantTransmit, bool receiveAll,
		       int maxReceive, size_t initBufferSize );
      
      /// Destructor.
      virtual ~SocketInterface();
      

      /// Open input socket if not already done
      bool inOpen();

      /// Open output socket if not already done
      bool outOpen();

      /// Check for new input
      /**
       *
       * \return whether the connection is still open
       *
       **/
      virtual bool pollInput();
      
      /// Flush any output
      /**
       *
       * \return whether the connection is still open
       *
       **/
      virtual bool flushOutput();

    private:
      /// the socket number
      int iSocketNum;

      /// Is the input socket open?
      bool iOpen;

      /// Has the input socket been closed?
      bool iClosed;

      /// File descriptor for input socket
      int iFD;

      /// set of file descriptors for select
      fd_set iFDSet;

      /// timeval for select
      struct timeval iTV;

      /// is the input socket read/write?
      bool iReadWrite;
    };
  }
}
#endif //ndef _SOCKET_INTERFACE_HH
