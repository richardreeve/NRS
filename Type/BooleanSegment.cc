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
#include "BooleanSegment.hh"

void NRS::Type::BooleanSegment::useRawData( const uint8_t *dataPtr,
					    Enum::EncodingType encoding )
{
  switch (encoding)
    {
    case Enum::PML:
      _ASSERT_( (!strcmp( "false", (const char *) dataPtr )) ||
		(!strcmp( "true", (const char *) dataPtr )),
		"Illegal value in segment" );
      iValue = strcmp( "false", (const char *) dataPtr );
      break;
    case Enum::BMF:
      if ((dataPtr[0] | 1) != 3)
	_ABORT_( "Illegal value in segment" );

      iValue = (dataPtr[0] == 3);
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}

void NRS::Type::BooleanSegment::createSegment( Base::MessageStore &aMS,
					       unsigned_t num ) const
{
  uint8_t ch;

  switch (aMS.getEncoding())
    {
    case Enum::PML:
      if (iValue)
	aMS.setSegment( num, (const uint8_t *) "true", 4 );
      else
	aMS.setSegment( num, (const uint8_t *) "false", 5 );
      break;
    case Enum::BMF:
      ch = (iValue ? 3 : 2);
      aMS.setSegment( num, &ch, 1 );
      break;
      default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}
