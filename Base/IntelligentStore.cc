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
#include "BooleanSegment.hh"
#include "IntegerSegment.hh"
#include "IntelligentStore.hh"
#include "RouteSegment.hh"
#include "Segment.hh"
#include "StringSegment.hh"

NRS::Base::IntelligentStore::
IntelligentStore( const IntelligentStore &rhs,
		  Enum::EncodingType anEncoding ) :
  iMS( NUM_INTELLIGENT_ATT, anEncoding ),
  iIntelligentPresent( rhs.iIntelligentPresent )
{
  if (anEncoding != rhs.getEncoding())
    {
      std::auto_ptr< IntelligentSegments > segmentsPtr( rhs.createSegments() );
      // increment translation count during translation!
      if (segmentsPtr->iSegment[ (unsigned_t) Enum::TranslationCount ] != NULL)
	++dynamic_cast< Type::IntegerSegment* >( segmentsPtr->
						 iSegment[ (unsigned_t) Enum::
							   TranslationCount ] )
	  ->iValue;
      useSegments( *segmentsPtr );
    }
  else
    iMS = rhs.iMS;
}

NRS::Base::IntelligentStore::
IntelligentStore( const IntelligentSegments &segments,
		  Enum::EncodingType anEncoding ) :
  iMS( NUM_INTELLIGENT_ATT, anEncoding ),
  iIntelligentPresent( 0 )
{
  useSegments( segments );
}

NRS::Base::IntelligentSegments*
NRS::Base::IntelligentStore::createSegments() const
{
  IntelligentSegments *segmentsPtr = new IntelligentSegments;

  for ( unsigned_t segNum = 0; segNum < NUM_INTELLIGENT_ATT; ++segNum )
    {
      if (iIntelligentPresent[segNum] == 1)
	{
	  switch ((Enum::IntelligentType) segNum)
	    {
	    case Enum::IsIntelligent:
	    case Enum::IsBroadcast:
	    case Enum::AckMsg:
	    case Enum::FailedRouteMsg:
	      segmentsPtr->iSegment[segNum] = new Type::BooleanSegment;
	      segmentsPtr->iSegment[segNum]->useData( iMS, segNum );
	      break;
	    case Enum::EndIMessage:
	    case Enum::DummyIntSeg:
	      break;
	    case Enum::ForwardRoute:
	    case Enum::ReturnRoute:
	      segmentsPtr->iSegment[segNum] = new Type::RouteSegment;
	      segmentsPtr->iSegment[segNum]->useData( iMS, segNum );
	      break;
	    case Enum::HopCount:
	    case Enum::AckVNID:
	    case Enum::FailedRouteVNID:
	    case Enum::MsgID:
	    case Enum::TranslationCount:
	      segmentsPtr->iSegment[segNum] = new Type::IntegerSegment;
	      segmentsPtr->iSegment[segNum]->useData( iMS, segNum );
	      break;
	    case Enum::TargetCID:
	    case Enum::SourceCID:
	    case Enum::TargetVNName:
	      segmentsPtr->iSegment[segNum] = new Type::StringSegment;
	      segmentsPtr->iSegment[segNum]->useData( iMS, segNum );
	      break;
	    default:
	      _ABORT_( "Illegal intelligent segment" );
	    }
	}
    }
  return segmentsPtr;
}


void
NRS::Base::IntelligentStore::
useSegments( const IntelligentSegments &segments )
{
  for ( unsigned_t segNum = 0; segNum < NUM_INTELLIGENT_ATT; ++segNum )
    {
      if (!segments.iSegment[segNum])
	{
	  clearSegment( (Enum::IntelligentType) segNum );
	}
      else
	{
	  iIntelligentPresent[ segNum ] = 1;
	  switch ((Enum::IntelligentType) segNum)
	    {
	    case Enum::EndIMessage:
	    case Enum::DummyIntSeg:
	      _ABORT_( "Illegal message segment" );
	      break;
	    case Enum::IsIntelligent:
	    case Enum::IsBroadcast:
	    case Enum::AckMsg:
	    case Enum::FailedRouteMsg:
	    case Enum::ForwardRoute:
	    case Enum::ReturnRoute:
	    case Enum::HopCount:
	    case Enum::AckVNID:
	    case Enum::FailedRouteVNID:
	    case Enum::MsgID:
	    case Enum::TranslationCount:
	    case Enum::TargetCID:
	    case Enum::SourceCID:
	    case Enum::TargetVNName:
	      segments.iSegment[segNum]->createSegment( iMS, segNum );
	      break;
	    default:
	      _ABORT_( "Illegal intelligent segment" );
	    }
	}
    }
}
