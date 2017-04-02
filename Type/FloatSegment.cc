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

#include <cstdlib>
#include <sstream>

#include "FloatSegment.hh"
#include "MessageStore.hh"
#include "Types.hh"

void NRS::Type::FloatSegment::useRawData( const uint8_t *dataPtr,
					  Enum::EncodingType encoding )
{
  uint8_t ptr[MAX_FLOAT_STORE];
  
  switch (encoding)
    {
    case Enum::PML:
      iValue = atof( (const char *)dataPtr );
      break;
    case Enum::BMF:
      if ((240 & dataPtr[0]) == 1<<4)
	{ // float
	  ptr[0] = ((dataPtr[0] & 15) << 4) | ((dataPtr[1] >> 3) & 15);
	  ptr[1] = ((dataPtr[1] & 7) << 5) | ((dataPtr[2] >> 2) & 31);
	  ptr[2] = ((dataPtr[2] & 3) << 6) | ((dataPtr[3] >> 1) & 63);
	  ptr[3] = ((dataPtr[3] & 1) << 7) | (dataPtr[4] & 127);
	  iValue = *reinterpret_cast< float * >( ptr );
	}
      else if ((126 & dataPtr[0]) != 1<<1)
	{ // double
	  ptr[0] = ((dataPtr[0] & 1) << 7) | (dataPtr[1] & 127);
	  ptr[1] = ((dataPtr[2] & 127) << 1) | ((dataPtr[3] >> 6) & 1);
	  ptr[2] = ((dataPtr[3] & 63) << 2) | ((dataPtr[4] >> 5) & 3);
	  ptr[3] = ((dataPtr[4] & 31) << 3) | ((dataPtr[5] >> 4) & 7);
	  ptr[4] = ((dataPtr[5] & 15) << 4) | ((dataPtr[6] >> 3) & 15);
	  ptr[5] = ((dataPtr[6] & 7) << 5) | ((dataPtr[7] >> 2) & 31);
	  ptr[6] = ((dataPtr[7] & 3) << 6) | ((dataPtr[8] >> 1) & 63);
	  ptr[7] = ((dataPtr[8] & 1) << 7) | (dataPtr[9] & 127);
	  iValue = *reinterpret_cast< double * >( ptr );
	}
      else
	{
	  _ABORT_( "Illegal value in segment" );
	}
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}

void NRS::Type::FloatSegment::createSegment( Base::MessageStore &aMS,
					     unsigned_t num ) const
{
  std::stringstream anSS;
  uint8_t ptr[MAX_FLOAT_STORE];
  const uint8_t *data = reinterpret_cast< const uint8_t * >( &iValue );
  
  switch (aMS.getEncoding())
    {
    case Enum::PML:
      anSS << iValue;
      aMS.setSegment( num, (const uint8_t *) anSS.str().c_str(),
		      anSS.str().length() );
      break;
    case Enum::BMF:
      if (FLOAT_T_BITS == 32)
	{ // float
	  ptr[0] = (1 << 4) | (data[0] >> 4);
	  ptr[1] = (1 << 7) | ((data[0] & 15) << 3) | (data[1] >> 5);
	  ptr[2] = (1 << 7) | ((data[1] & 31) << 2) | (data[2] >> 6);
	  ptr[3] = (1 << 7) | ((data[2] & 63) << 1) | (data[3] >> 7);
	  ptr[4] = (1 << 7) | (data[3] & 127);
	  aMS.setSegment( num, ptr, 5 );
	}
      else
	{ // double
	  ptr[0] = (1 << 1) | (data[0] >> 7);
	  ptr[1] = (1 << 7) | (data[0] & 127);
	  ptr[2] = (1 << 7) | (data[1] >> 1);
	  ptr[3] = (1 << 7) | ((data[1] & 1) << 6) | (data[2] >> 2);
	  ptr[4] = (1 << 7) | ((data[2] & 3) << 5) | (data[3] >> 3);
	  ptr[5] = (1 << 7) | ((data[3] & 7) << 4) | (data[4] >> 4);
	  ptr[6] = (1 << 7) | ((data[4] & 15) << 3) | (data[5] >> 5);
	  ptr[7] = (1 << 7) | ((data[5] & 31) << 2) | (data[6] >> 6);
	  ptr[8] = (1 << 7) | ((data[6] & 63) << 1) | (data[7] >> 7);
	  ptr[9] = (1 << 7) | (data[7] & 127);
	  aMS.setSegment( num, ptr, 10 );
	}
      break;
      default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}
