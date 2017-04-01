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

#include <sstream>

#pragma implementation
#include "IntegerSegment.hh"
#include "MessageStore.hh"
#include "Types.hh"

void NRS::Type::IntegerSegment::useRawData( const uint8_t *dataPtr,
					    Enum::EncodingType encoding )
{
  uint8_t ch;

  switch (encoding)
    {
    case Enum::PML:
      iValue = strtol( (const char *) dataPtr, NULL, 10 );
      break;
    case Enum::BMF:
      ch = *(dataPtr++);
      if (ch == 1)
	{
	  iValue = 0;
	}
      else if ((ch & 64) != 64)
	{
	  _ABORT_( "Illegal segment encoding" );
	}
      else
	{
	  if (ch & 32)
	    {
	      iValue = -1;
	      iValue <<= 5;
	      iValue |= (ch & 31);
	    }
	  else
	    iValue = ch & 31;

	  while (dataPtr[0] & (1<<7))
	    {
	      iValue <<= 7;
	      iValue |= *(dataPtr++) & 127;
	    }
	}
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}

void NRS::Type::IntegerSegment::createSegment( Base::MessageStore &aMS,
					       unsigned_t num ) const
{
  std::stringstream anSS;
  uint8_t ptr[MAX_INTEGER_STORE];
  integer_t val = iValue;
  int ref = 0;
  int sze = 0;
  bool negative = false;

  switch (aMS.getEncoding())
    {
    case Enum::PML:
      anSS << iValue;
      aMS.setSegment( num, (const uint8_t *) anSS.str().c_str(),
		      anSS.str().length() );
      break;
    case Enum::BMF:
      if (val < 0)
	{
	  negative = true;
	  val = -val;
	}
      while (val >= 32)
	{
	  ++ref;
	  val >>= 7;
	}
      val = iValue;
      sze = ref + 1;
      while (ref > 0)
	{
	  ptr[ref--] = (1 << 7) | (val & 127);
	  val >>= 7;
	}
      ptr[0] = (1 << 6) | (negative ? (1<<5) : 0) | (val & 31);
      aMS.setSegment( num, ptr, sze );
      break;
      default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}
