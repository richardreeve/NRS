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

#ifndef _FILE_INTERFACE_HH
#define _FILE_INTERFACE_HH

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

namespace NRS
{
  namespace Interface
  {
    /// This class instantiates file readers and writers
    class FileInterface : public BufferedInterface
    {
    public:
      /// Constructor for two files, one in, one out
      /**
       *
       * \param anEncodingType The type of encoding being sent (PML, BMF).
       * \param aParserPtr pointer to parser for encoding type
       * \param inFile the input file to read from
       * \param outFile the output file to write to
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       * \param initBufferSize initial buffer size
       *
       **/
      FileInterface( Enum::EncodingType anEncodingType,
		     Interface::MessageParser *aParserPtr,
		     std::string inFile, std::string outFile,
		     bool instantTransmit, bool receiveAll,
		     int maxReceive, size_t initBufferSize );
      
      /// Constructor for one file: either one input, or one in and out
      /**
       *
       * \param anEncodingType The type of encoding being sent (PML, BMF).
       * \param aParserPtr pointer to parser for encoding type
       * \param inFile the input file to read from
       * \param doRead is this file readable?
       * \param doWrite is this file writeable?
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       * \param initBufferSize initial buffer size
       * \param serialSpeed serial speed if appropriate
       *
       **/
      FileInterface( Enum::EncodingType anEncodingType,
		     Interface::MessageParser *aParserPtr,
		     std::string inFile, bool doRead, bool doWrite,
		     bool instantTransmit, bool receiveAll,
		     int maxReceive, size_t initBufferSize,
		     int serialSpeed );
      
      /// Destructor.
      virtual ~FileInterface();
      

      /// Open input file if not already done
      bool inOpen();

      /// Open output file if not already done
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
      /// the input filename
      std::string iInFilename;

      /// Is the input file open?
      bool iInOpen;

      /// Has the input file been closed?
      bool iInClosed;

      /// File descriptor for input file
      int iInFD;

      // input filestream
      FILE *iInFILE;

      /// set of file descriptors for select
      fd_set iInFDSet;

      /// the output filename
      std::string iOutFilename;

      /// Is the output file open?
      bool iOutOpen;

      /// Has the output file been closed?
      bool iOutClosed;

      /// File descriptor for output file
      int iOutFD;

      // output filestream
      FILE *iOutFILE;

      /// set of file descriptors for select
      fd_set iOutFDSet;

      /// timeval for select
      struct timeval iTV;

      /// is the input file read/write?
      bool iReadWrite;

      // Speed for serial port
      int iSerialSpeed;
    };
  }
}
#endif //ndef _FILE_INTERFACE_HH
