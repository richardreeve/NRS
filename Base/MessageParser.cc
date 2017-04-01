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
#include "MessageParser.hh"
#include "MessageStore.hh"
#include "PMLParser.hh"
#include "RouteSegment.hh"
#include "Types.hh"

void NRS::Interface::MessageParser::setPort( unsigned_t port )
{
  iPort = port;
}

void
NRS::Interface::MessageParser::routePushPortFront( Type::RouteSegment &rs )
{
  // Create PML version of port
  std::basic_string< uint8_t > data, tmp;
  unsigned_t port = iPort;
  uint8_t ch = PMLParser::LowEndPort + (port & 7);


  port >>= 3;
  data = ch;
  
  while ( port != 0 )
    {
      ch = PMLParser::LowContPort + (port & 7);
      port >>= 3;
      tmp = data;
      data = ch;
      data += tmp;
    }

  // Get PML version of route
  Base::MessageStore aMS( 1, Enum::PML );
  rs.createSegment( aMS, 0 );
  // Concatenate
  data.append( aMS.getSegment( 0 ) );

  // Return to RouteSegment
  rs.useRawData( data.c_str(), Enum::PML );

}

void
NRS::Interface::MessageParser::routePushPortEnd( Type::RouteSegment &rs )
{
  // Create PML version of port
  std::basic_string< uint8_t > data, tmp;
  unsigned_t port = iPort;
  uint8_t ch = PMLParser::LowEndPort + (port & 7);


  port >>= 3;
  data = ch;

  while ( port != 0 )
    {
      ch = PMLParser::LowContPort + (port & 7);
      port >>= 3;
      tmp = data;
      data = ch;
      data += tmp;
    }

  // Get PML version of route
  Base::MessageStore aMS( 1, Enum::PML );
  rs.createSegment( aMS, 0 );

  // Concatenate
  tmp = data;
  data = aMS.getSegment( 0 );
  data += tmp;

  // Return to RouteSegment
  rs.useRawData( data.c_str(), Enum::PML );

}
