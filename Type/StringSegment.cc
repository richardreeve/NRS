/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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
#include "StringSegment.hh"

void NRS::Type::StringSegment::useRawData( const uint8_t *dataPtr,
					   Enum::EncodingType encoding )
{
  char ch;

  switch (encoding)
    {
    case Enum::PML:
      iValue = (const char *) dataPtr;
      break;
    case Enum::BMF:
      iValue.clear();
      _ASSERT_( dataPtr[0] == 1, "Illegal segment shape" );
      ++dataPtr;
      while (dataPtr[0] & 1<<7)
	{
	  ch = *(dataPtr++) & 127;
	  iValue.push_back( ch );
	}
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}

void NRS::Type::StringSegment::createSegment( Base::MessageStore &aMS,
					      unsigned_t num ) const
{
  int pos = 0;
  int len = iValue.length();
  uint8_t ch;
  switch (aMS.getEncoding())
    {
    case Enum::PML:
      aMS.setSegment( num, (const uint8_t *) iValue.c_str(), len );
      break;
    case Enum::BMF:
      ch = 1;
      aMS.appendSegment( num, &ch, 1 );
      while (pos < len)
	{
	  ch = iValue[pos++] | (1<<7);
	  aMS.appendSegment( num, &ch, 1 );
	}
      break;
      default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}
