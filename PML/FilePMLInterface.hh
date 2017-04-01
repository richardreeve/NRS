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

#ifndef _FILE_PML_INTERFACE_HH
#define _FILE_PML_INTERFACE_HH

#pragma interface
#include <string>

#include "FileInterface.hh"
#include "PMLParser.hh"

namespace NRS
{
  namespace Interface
  {
    /// This class instantiates PML file readers and writers
    class FilePMLInterface : public FileInterface
    {
    public:
      /// Constructor for two files, one in, one out
      /**
       *
       * \param inFile the input file to read from
       * \param outFile the output file to write to
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       *
       **/
      FilePMLInterface( std::string inFile, std::string outFile,
			bool instantTransmit, bool receiveAll,
			int maxReceive );
      
      /// Constructor for one file: either one input, or one in and out
      /**
       *
       * \param inFile the input file to read from
       * \param doRead is this file readable?
       * \param doWrite is this file writeable?
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       * \param serialSpeed serial speed if appropriate
       *
       **/
      FilePMLInterface( std::string inFile, bool doRead, bool doWrite,
			bool instantTransmit, bool receiveAll,
			int maxReceive, int serialSpeed );
    };
  }
}
#endif //ndef _FILE_PML_INTERFACE_HH
