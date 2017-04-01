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

#include <iostream>

#pragma implementation
#include "VariableInfo.hh"
#include "Exception.hh"
#include "SegmentDescription.hh"
#include "IntegerRestriction.hh"
#include "FloatRestriction.hh"
#include "StringRestriction.hh"
#include "VectorRestriction.hh"

NRS::Base::VariableInfo::VariableInfo( const VariableManager &theVMRef ) :
  iVM( theVMRef ), iMessageName( theVMRef.getMessageName() ),
  iNumSegments( theVMRef.getNumSegments() ),
  iNumBoolean( 0 ), iNumInteger( 0 ), iNumFloat( 0 ),
  iNumString( 0 ), iNumRoute( 0 ), iNumVector( 0 )
{
  iSegDescriptionVector.resize( iNumSegments );
  iAttributeNameVector.resize( iNumSegments );
  iSegTypeVector.resize( iNumSegments );
  iSegIndexVector.resize( iNumSegments );
  
  for ( unsigned_t i = 0; i < iNumSegments; ++i )
    {
      iSegDescriptionVector[i] = iVM.getSegmentDescription( i );
      iSegTypeVector[i] = iSegDescriptionVector[i]->getType();
      iAttributeNameVector[i] =
	iSegDescriptionVector[i]->getAttributeName();
      
      switch (iSegTypeVector[i])
	{
	case Enums::Boolean:
	  iSegIndexVector[i] = iNumBoolean++;
	  break;
	case Enums::Integer:
	  iSegIndexVector[i] = iNumInteger++;
	  break;
	case Enums::Float:
	  iSegIndexVector[i] = iNumFloat++;
	  break;
	case Enums::String:
	  iSegIndexVector[i] = iNumString++;
	  break;
	case Enums::Route:
	  iSegIndexVector[i] = iNumRoute++;
	  break;
	case Enums::Vector:
	  iSegIndexVector[i] = iNumVector++;
	  break;
	default:
	  _ABORT_( "Illegal segment type" );
	}
    }
}
