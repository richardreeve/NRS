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

#include <iostream>
#include <fstream>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <strings.h>
#include <cerrno>
#include <cstring>

#include "Exception.hh"
#include "StringLiterals.hh"

#define ROBOT_BOOT_SCP "Serial Communication Protocol\015\n"

int main( int argc, char* argv[] )
{
  std::string robot = "koala";
  bool setSpeed = false;
  int speed = 0;
  bool loop = false;
  bool flash = false;
  std::string portname = "/dev/ttyS0";
  int mode;
  tcflag_t baud = B57600;
  std::string download;

  while ((argc > 1) && (argv[1][0] == '-'))
    {
      if ((!strcmp( "--robot", argv[1] )) || (!strcmp( "-r", argv[1] )))
        {
	  robot = argv[2];
          argc-=2;
          argv+=2;
        }
      else if ((!strcmp( "--speed", argv[1] )) || (!strcmp( "-s", argv[1] )))
        {
	  setSpeed = true;
	  speed = atoi( argv[2] );
          argc-=2;
          argv+=2;
        }
      else if ((!strcmp( "--port", argv[1] )) || (!strcmp( "-p", argv[1] )))
        {
	  portname = argv[2];
          argc-=2;
          argv+=2;
        }
      else if ((!strcmp( "--loop", argv[1] )) || (!strcmp( "-l", argv[1] )))
        {
	  loop = true;
          argc--;
          argv++;
        }
      else if ((!strcmp( "--flash", argv[1] )) || (!strcmp( "-f", argv[1] )))
        {
	  flash = true;
          argc--;
          argv++;
        }
      else if ((!strcmp( "--help", argv[1] )) || (!strcmp( "-h", argv[1] )))
        {
          std::cout << "usage: NRS.download [-h] [--robot <robot>] "
		    << "[--speed <speed>] [--loop]"
                    << "<download_name>" << std::endl
                    << std::endl << " --help\t\t\t"
                    << "display this information and exit" << std::endl
                    << " --robot <robot>\t"
                    << "set the robot type for download (" << robot << ")" 
                    << std::endl
                    << " --speed <speed>\t"
                    << "set the speed for download if not the fastest \n\t\t\t"
		    << "available for the robot"
                    << std::endl
                    << " --flash\t"
                    << "set the program to burn to flash instead of RAM" 
                    << std::endl
                    << " --loop\t"
                    << "set the program to continue running after download" 
                    << std::endl
		    << " --port <port_name>\t"
		    << "set the port to download the program through ("
		    << portname << ")"
		    << std::endl
                    << "<download_name>\t"
                    << "filename of program to download onto robot"
                    << std::endl
                    << "\t\t\t can be in form 'Demo', 'Demo.srec',"
                    << std::endl
                    << "\t\t\t or '~/share/kteam/khepera/Demo.srec'"
                    << std::endl;
          
          exit(0);
        }
      else
        {
          _ABORT_( "Unknown argument '" << argv[1] 
                   << "' to NRS.download\n\t" );
        }
    }

  if (argc < 2)
    {
      _ABORT_( "No download program indicated for NRS.download\n\t" );
    }
  else if (argc > 2)
    {
      _ABORT_( "Too many download programs indicated for NRS.download\n\t" 
	       << argv[1] << " ...\n\t" );
    }
  else // one file available
    {
      download = argv[1];
    }

  if ((robot == "khepera2") || (robot == "kh2"))
    {
      robot = "khepera2";
      if (!setSpeed)
	speed = 57600;
      
      switch (speed)
	{
	case 9600:
	  mode = 1;
	  baud = B9600;
	  break;
	case 19200:
	  mode = 2;
	  baud = B19200;
	  break;
	case 38400:
	  mode = 3;
	  baud = B38400;
	  break;
	case 57600:
	  mode = 8;
	  baud = B57600;
	  break;
	case 115200:
	  mode = 9;
	  baud = B115200;
	  break;
	default:
	  _ABORT_( "Unrecognized speed " << speed << " for Khepera 2\n\t" );
	}
      _INFO_( "Please set the Khepera 2 to mode " << mode
	      << " and reset.\n\t" );
    }
  else if (( robot == "khepera") || (robot == "khe"))
    {
      robot = "khepera";
      if (!setSpeed)
	speed = 38400;

      switch (speed)
	{
	case 9600:
	  mode = 1;
	  baud = B9600;
	  break;
	case 19200:
	  mode = 2;
	  baud = B19200;
	  break;
	case 38400:
	  mode = 3;
	  baud = B38400;
	  break;
	default:
	  _ABORT_( "Unrecognized speed " << speed << " for Khepera\n\t" );
	}
      _INFO_( "Please set the Khepera to mode " << mode << " and reset.\n\t" );
    }
  else if ((robot ==  "koala") || (robot == "koa"))
    {
      robot = "koala";
      if (!setSpeed)
	speed = 115200;

      switch (speed)
	{
	case 9600:
	  mode = 1;
	  baud = B9600;
	  break;
	case 19200:
	  mode = 2;
	  baud = B19200;
	  break;
	case 38400:
	  mode = 3;
	  baud = B38400;
	  break;
	case 115200:
	  mode = 0xA;
	  baud = B115200;
	  break;
	default:
	  _ABORT_( "Unrecognized speed " << speed << " for Koala\n\t" );
	}
      _INFO_( "Please set the Koala to mode " << mode << " and reset.\n\t" );
    }
  else
    {
      _ABORT_( "Unrecognised robot type '" << robot << "'\n\t" );
    }

  int port_fd = open( portname.c_str(),  O_RDWR | O_NOCTTY ); // open serial 
  if ( port_fd == -1 )
    {
      _ABORT_( "Failed to open port '" << portname << "':\n\t"
	       << strerror( errno ) << "\n\t" );
    }

  struct termios oldtio, newtio; // for storing old and new io states

  tcgetattr( port_fd, &oldtio ); // save current port settings 
  bzero( &newtio, sizeof( newtio ) ); // set new port settings
  newtio.c_cflag= baud | CSTOPB | CS8 | CLOCAL | CREAD;
  newtio.c_iflag= IGNPAR;
  newtio.c_oflag= 0;
  newtio.c_lflag= 0; // set input mode (non-canonical, no echo,...)
  newtio.c_cc[VTIME] = 5; // inter-character timer  
  newtio.c_cc[VMIN]  = 1; // blocking read until x chars received 
  tcflush( port_fd, TCIFLUSH ); // flush buffer
  tcsetattr( port_fd, TCSANOW, &newtio ); // set new settings
  
  std::stringstream anSS;

  // Adds in default path if no path
  if ( download.find( '/' ) == std::string::npos )
    {
      anSS << NRS::Literals::File::DEFAULT_SHARE_DIR() << robot << "/";
    }

  anSS << download;

  // Adds in .srec if not there.
  if ( download.find( '.' ) == std::string::npos )
    anSS << ".srec";

  std::ifstream dStream;

  dStream.open( anSS.str().c_str() );

  if (!dStream.is_open())
    {
      _ABORT_( "Failed to open download file\n\t"
	       "'" << anSS.str() << "':\n\t" );
    }

  std::string aBuffer;
  unsigned int length;
  unsigned int total_length = 0;

  //  _WARN_( "Assuming buffer always reads up to CR\n\t" );

  unsigned int pos = 0;
  std::string msg = ROBOT_BOOT_SCP;
  unsigned char val;

      while (pos < msg.length())
        {
          if (read(port_fd, &val, 1)==1)
            {
              if (val==msg[pos])
		pos++;
              else
		pos=0;
	      if (((val >= ' ') && (val <= '~')) || (val == '\n') ||
		  (val =='\r'))
		std::cout << val << std::flush;
	      else
		std::cout << '\\' << (unsigned int) val << std::flush;
            }
        }
  

  if (flash)
    aBuffer = "sfill\n";
  else
    aBuffer = "run sloader\n";

  length = write( port_fd, aBuffer.c_str(), aBuffer.length() );
  if (length != aBuffer.length())
    {
      _WARN_( "Length of segment = " << aBuffer.length()
	      << ", length reported = " << length << "\n\t" );
    }

  while (!dStream.eof())
    {
      dStream >> aBuffer;
      aBuffer += "\n";
      length = write( port_fd, aBuffer.c_str(), aBuffer.length() );
      if (length != aBuffer.length())
	{
	  _WARN_( "Length of segment = " << aBuffer.length()
		  << ", length reported = " << length << "\n\t" );
	}
      total_length += length;
    }
    
  if (flash)
    {
      aBuffer = "flash E\nflash W\nrun user-flash\n";
      length = write( port_fd, aBuffer.c_str(), aBuffer.length() );
      if (length != aBuffer.length())
	{
	  _WARN_( "Length of segment = " << aBuffer.length()
		  << ", length reported = " << length << "\n\t" );
	}
    }

  dStream.close();

  if (loop)
    {
      while (true)
	{
          if (read(port_fd, &val, 1)==1)
            {
	      if (((val >= ' ') && (val <= '~')) || (val == '\n') ||
		  (val =='\r'))
		std::cout << val << std::flush;
	      else
		std::cout << '\\' << (int) val << std::flush;
            }
	}
    }

//  tcsetattr( port_fd, TCSANOW, &oldtio );

  close( port_fd );

  return 0;
}
