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

#include <list>
#include <string>

#include "StringLiterals.hh"
#include "ExternalInterfaceHandler.hh"
#include "ArgumentDirector.hh"
#include "ArgumentFielder.hh"
#include "PMLParser.hh"
#include "FilePMLInterface.hh"

namespace NRS
{
  namespace Interface
  {
    /// Handler for FilePMLInterfaces which allow the opening of file,
    /// fifo and device ports, handles command-line arguments which
    /// are used to open the ports.
    class FilePMLHandler : public Base::ExternalInterfaceHandler,
			   public Base::ArgumentFielder
    {
    public:
      /// Simple constructor
      FilePMLHandler() :
	NRS::Base::ExternalInterfaceHandler( Enum::File, Enum::PML )
      {
	NRS::Base::ArgumentDirector &theAD =
	  NRS::Base::ArgumentDirector::getDirector();
	
	theAD.addArgumentFielder( 'R', "read-pml",
				  "\t\t[ -R|--read-pml <file.xml> ]\n",
				  "\t-R|--read-pml <file.xml>\t"
				  "read PML input from a specified file\n",
				  this );
	theAD.addArgumentFielder( 'W', "write-pml",
				  "\t\t[ -W|--write-pml <file.xml> ]\n",
				  "\t-W|--write-pml <file.xml>\t"
				  "write PML output to a specified file\n",
				  this );
	theAD.addArgumentFielder( 'F', "fifo-pml",
				  "\t\t[ -F|--fifo-pml <in> <out> ]\n",
				  "\t-F|--fifo-pml <in> <out>\t"
				  "communicate in PML using two fifos\n",
				  this );
	theAD.addArgumentFielder( 'D', "device-pml",
				  "\t\t[ -D|--device-pml <device> ]\n",
				  "\t-D|--device-pml <device>\t"
				  "communicate in PML using a device\n",
				  this );
	theAD.addArgumentFielder( 'L', "log-pml",
				  "\t\t[ -L|--log-pml <file.xml> ]\n",
				  "\t-L|--log-pml <file.xml>\t\t"
				  "log PML output to a specified file\n",
				  this );
      }

      std::string queryFielder() const
      {
	return "FilePMLHandler";
      }

      void fieldArguments( std::list< std::string > &arguments )
      {
	std::string arg = *arguments.begin();
	std::list< std::string > files;
	std::list< int > ints;
	static bool sXercesStart = false;
	if (!sXercesStart)
	  {
	    sXercesStart = true;
	    XERCES_CPP_NAMESPACE::XMLPlatformUtils::Initialize();
	  }
	arguments.pop_front();
	if (arg == "read-pml")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to read-pml\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints, true, false, true, true );
	  }
	else if (arg == "write-pml")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to write-pml\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints, false, true, true, true );
	  }
	else if (arg == "fifo-pml")
	  {
	    if (arguments.size() < 2)
	      _ABORT_( "Not enough arguments to fifo-pml\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints, true, true, true, true );
	  }
	else if (arg == "device-pml")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to device-pml\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints, true, true, true, true );
	  }
	else if (arg == "log-pml")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to log-pml\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints, false, true, true, true )->
	      setLoggingPort();
	    
	  }
	else
	  _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
      }
      

      /// Create a new ExternalInterface
      /**
       *
       * \param stringParamList list of string parameters for interface
       * \param intParamList list of integer parameters for interface
       * \param doRead Is the interface an input interface?
       * \param doWrite Is the interface an output interface?
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       *
       * \return ExternalInterface pointer which has been constructed
       *
       **/
      Base::ExternalInterface*
      createExternalInterface( std::list< std::string > &stringParamList,
			       std::list< int > &intParamList,
			       bool doRead,
			       bool doWrite,
			       bool instantTransmit = true,
			       bool receiveAll = false,
			       int maxReceive = 1 ) const
      {
	Base::ExternalInterface *anEI = NULL;
	if (!intParamList.empty())
	  {
	    _ABORT_( "Don't recognise these parameters" );
	  }
	if ( doRead && doWrite && (stringParamList.size() == 2))
	  {
	    anEI = new FilePMLInterface( *(stringParamList.begin()),
					 *(++stringParamList.begin()),
					 instantTransmit, receiveAll,
					 maxReceive );
	  }
	else if (stringParamList.size() == 1)
	  {
	    anEI = new FilePMLInterface( *(stringParamList.begin()),
					 doRead, doWrite,
					 instantTransmit, receiveAll,
					 maxReceive, 57600 );
	  }
	else
	  {
	    _ABORT_( "Don't recognise these parameters" );
	  }
	return anEI;
      }
    };
  }
}

namespace
{
  NRS::Interface::FilePMLHandler sFPH;
}

NRS::Interface::FilePMLInterface::FilePMLInterface( std::string inFile,
						    std::string outFile,
						    bool instantTransmit,
						    bool receiveAll,
						    int maxReceive ) :
  FileInterface( Enum::PML,
		 new PMLParser( inFile.c_str() ),
		 inFile, outFile, instantTransmit, receiveAll, maxReceive,
		 PMLParser::DefaultBufferSize )
{
}

NRS::Interface::FilePMLInterface::FilePMLInterface( std::string inFile,
						    bool doRead,
						    bool doWrite,
						    bool instantTransmit,
						    bool receiveAll,
						    int maxReceive,
						    int serialSpeed ) :
  FileInterface( Enum::PML,
		 new PMLParser( inFile.c_str() ),
		 inFile, doRead, doWrite,
		 instantTransmit, receiveAll, maxReceive,
		 PMLParser::DefaultBufferSize,
		 serialSpeed )
{
}

