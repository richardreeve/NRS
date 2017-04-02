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

#include "RouteSegment.hh"

void NRS::Type::RouteSegment::useRawData( const uint8_t *dataPtr,
					  Enum::EncodingType encoding )
{
  const uint8_t *ptr = dataPtr;
  uint8_t ch = 1;
  int len = 0;
  int pos;

  switch (encoding)
    {
    case Enum::PML:
      iValue.clear();
      while (ptr[0] != 0)
	{
	  if ((ptr[0] == '0') || (ptr[0] == '1'))
	    ++len;
	  else if ((ptr[0] >= 'A') && (ptr[0] <= 'P'))
	    len += 4;
	  else
	    _ABORT_( "Illegal segment encoding" );
	  ++ptr;
	}
      pos = len % 7;
      ptr = dataPtr;
      while (ptr[0] != 0)
	{
	  if (pos == 0)
	    {
	      iValue.push_back( ch );
	      ch = 1;
	      pos = 7;
	    }
	  if ((ptr[0] == '0') || (ptr[0] == '1'))
	    {
	      ch <<= 1;
	      ch |= ptr[0] - '0';
	      pos--;
	    }
	  else
	    {
	      if (pos >= 4)
		{
		  ch <<= 4;
		  ch |= ptr[0] - 'A';
		  pos -= 4;
		}
	      else
		{
		  ch <<= pos;
		  ch |= (ptr[0] - 'A') >> (4 - pos);
		  iValue.push_back( ch );
		  ch = 1;
		  ch <<= (4 - pos);
		  ch |= (ptr[0] - 'A') & ((1 << (4 - pos)) - 1);
		  pos += 3;
		}
	    }
	  ++ptr;
	}
      _ASSERT_( pos == 0, "How did I get here?" );
      iValue.push_back( ch );
      break;
    case Enum::BMF:
      len = 1;
      while (*(++ptr) & (1<<7))
	{
	  ++len;
	}
      iValue.assign( dataPtr, len );
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}

void NRS::Type::RouteSegment::createSegment( Base::MessageStore &aMS,
					     unsigned_t num ) const
{
  int len = iValue.length();
  uint8_t ch, val;
  int pos = 7;
  int index = 0;

  switch (aMS.getEncoding())
    {
    case Enum::PML:
      _ASSERT_( !iValue.empty(), "Illegal route stored" );
      aMS.clearSegment( num );
      ch = iValue[0];
      while ((ch & 1<<7) == 0)
	{
	  --pos;
	  ch <<= 1;
	}
      while (index < len)
	{
	  if (pos >= 4)
	    { // got a free nibble
	      val = 'A' + ((ch >> 3) & 15);
	      aMS.appendSegment( num, &val, 1 );
	      pos -= 4;
	      ch <<= 4;
	    }
	  if (++index < len)
	    { // can construct a new nibble
	      switch (pos)
		{
		case 3:
		  val = 'A' + ((ch >> 3) & 14) + ((iValue[index] >> 6) & 1);
		  pos = 6;
		  ch = iValue[index] << 1;
		  break;
		case 2:
		  val = 'A' + ((ch >> 3) & 12) + ((iValue[index] >> 5) & 3);
		  pos = 5;
		  ch = iValue[index] << 2;
		  break;
		case 1:
		  val = 'A' + ((ch >> 3) & 8) + ((iValue[index] >> 4) & 7);
		  pos = 4;
		  ch = iValue[index] << 3;
		  break;
		case 0:
		  val = 'A' + ((iValue[index] >> 3) & 15);
		  pos = 3;
		  ch = iValue[index] << 4;
		  break;
		}
	      aMS.appendSegment( num, &val, 1 );
	    }
	  else
	    { // use up left over bits
	      while (--pos >= 0)
		{
		  val = '0' + ((ch >> 6) & 1);
		  aMS.appendSegment( num, &val, 1 );
		  ch <<= 1;
		}
	    }
	}
//       if (pos >= 4)
// 	{
// 	  val = 'A' + ((ch >> 3) & 15);
// 	  aMS.appendSegment( num, &val, 1 );
// 	  pos -= 4;
// 	  ch <<= 4;
// 	}
//       while (--pos >= 0)
// 	{
// 	  val = '0' + ((ch >> 6) & 1);
// 	  aMS.appendSegment( num, &val, 1 );
// 	  ch <<= 1;
// 	}
//       while (++index < len)
// 	{
// 	  ch = iValue[index];
// 	  val = 'A' + ((ch >> 3) & 15);
// 	  aMS.appendSegment( num, &val, 1 );
// 	  pos = 3;
// 	  ch <<= 4;
// 	  while (--pos >= 0)
// 	    {
// 	      val = '0' + ((ch >> 6) & 1);
// 	      aMS.appendSegment( num, &val, 1 );
// 	      ch <<= 1;
// 	    }
// 	}
      break;
    case Enum::BMF:
      aMS.setSegment( num, iValue.c_str(), len );
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}
