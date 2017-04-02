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

#include "ExternalInterfaceHandler.hh"
#include "ArgumentDirector.hh"
#include "ArgumentFielder.hh"
#include "BMFParser.hh"
#include "FileBMFInterface.hh"

namespace NRS
{
  namespace Interface
  {
    /// Handler for FileBMFInterfaces which allow the opening of file,
    /// fifo and device ports, handles command-line arguments which
    /// are used to open the ports.
    class FileBMFHandler : public Base::ExternalInterfaceHandler,
			   public Base::ArgumentFielder
    {
    public:
      /// Simple constructor
      FileBMFHandler() :
	NRS::Base::ExternalInterfaceHandler( Enum::File, Enum::BMF )
      {
	NRS::Base::ArgumentDirector &theAD =
	  NRS::Base::ArgumentDirector::getDirector();
	
	theAD.addArgumentFielder( 'r', "read-bmf",
				  "\t\t[ -r|--read-bmf <file.bmf> ]\n",
				  "\t-r|--read-bmf <file.bmf>\t"
				  "read BMF input from a specified file\n",
				  this );
	theAD.addArgumentFielder( 'w', "write-bmf",
				  "\t\t[ -w|--write-bmf <file.bmf> ]\n",
				  "\t-w|--write-bmf <file.bmf>\t"
				  "write BMF output to a specified file\n",
				  this );
	theAD.addArgumentFielder( 'f', "fifo-bmf",
				  "\t\t[ -f|--fifo-bmf <in> <out> ]\n",
				  "\t-f|--fifo-bmf <in> <out>\t"
				  "communicate in BMF using two fifos\n",
				  this );
	theAD.addArgumentFielder( 'd', "device-bmf",
				  "\t\t[ -d|--device-bmf <device> ]\n",
				  "\t-d|--device-bmf <device>\t"
				  "communicate in BMF using a device\n",
				  this );
	theAD.addArgumentFielder( 'l', "log-bmf",
				  "\t\t[ -l|--log-bmf <file.xml> ]\n",
				  "\t-l|--log-bmf <file.xml>\t\t"
				  "log BMF output to a specified file\n",
				  this );
      }

      std::string queryFielder() const
      {
	return "FileBMFHandler";
      }

      void fieldArguments( std::list< std::string > &arguments )
      {
	std::string arg = *arguments.begin();
	std::list< std::string > files;
	std::list< int > ints;

	arguments.pop_front();
	if (arg == "read-bmf")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to read-bmf\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints,
				     true, false );
	  }
	else if (arg == "write-bmf")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to write-bmf\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints,
				     false, true );
	  }
	else if (arg == "fifo-bmf")
	  {
	    if (arguments.size() < 2)
	      _ABORT_( "Not enough arguments to fifo-bmf\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints,
				     true, true );
	  }
	else if (arg == "device-bmf")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to device-bmf\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints,
				     true, true, false );
	  }
	else if (arg == "log-bmf")
	  {
	    if (arguments.size() < 1)
	      _ABORT_( "Not enough arguments to log-bmf\n\t" );
	    files.push_back( *arguments.begin() );
	    arguments.pop_front();
	    createExternalInterface( files, ints, false, true )->
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
			       bool receiveAll = true,
			       int maxReceive = 1 ) const
      {
	Base::ExternalInterface *anEI = NULL;
	if (!intParamList.empty())
	  {
	    _ABORT_( "Don't recognise these parameters" );
	  }
	if ( doRead && doWrite && (stringParamList.size() == 2))
	  {
	    anEI = new FileBMFInterface( *(stringParamList.begin()),
					 *(++stringParamList.begin()),
					 instantTransmit, receiveAll,
					 maxReceive );
	  }
	else if (stringParamList.size() == 1)
	  {
	    anEI = new FileBMFInterface( *(stringParamList.begin()),
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
  NRS::Interface::FileBMFHandler sFBH;
}

NRS::Interface::FileBMFInterface::FileBMFInterface( std::string inFile,
						    std::string outFile,
						    bool instantTransmit,
						    bool receiveAll,
						    int maxReceive ) :
  FileInterface( Enum::BMF, new BMFParser(), inFile, outFile,
		 instantTransmit, receiveAll, maxReceive,
		 BMFParser::DefaultBufferSize )
{
}

NRS::Interface::FileBMFInterface::FileBMFInterface( std::string inFile,
						    bool doRead,
						    bool doWrite,
						    bool instantTransmit,
						    bool receiveAll,
						    int maxReceive,
						    int serialSpeed ) :
  FileInterface( Enum::BMF, new BMFParser(), inFile, doRead, doWrite,
		 instantTransmit, receiveAll, maxReceive,
		 BMFParser::DefaultBufferSize, serialSpeed )
{
}
