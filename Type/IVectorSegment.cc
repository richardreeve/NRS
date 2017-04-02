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

#include "IVectorSegment.hh"

void NRS::Type::IVectorSegment::useRawData( const uint8_t *dataPtr,
					    Enum::EncodingType encoding )
{
  //const uint8_t *data = aMS.getSegment( num );
  
  switch (encoding)
    {
    case Enum::PML:
      _BUG_( "Unimplemented" );
      break;
    case Enum::BMF:
      _BUG_( "Unimplemented" );
      break;
    default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}

void NRS::Type::IVectorSegment::createSegment( Base::MessageStore &aMS,
					       unsigned_t num ) const
{
  switch (aMS.getEncoding())
    {
    case Enum::PML:
      _BUG_( "Unimplemented" );
      break;
    case Enum::BMF:
      _BUG_( "Unimplemented" );
      break;
      default:
      _ABORT_( "Unknown encoding" );
      break;
    }
}
