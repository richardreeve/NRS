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
#include "Encoding.hh"
#include "MessageDescription.hh"
#include "MessageStore.hh"
#include "Segment.hh"
#include "SegmentDescription.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

NRS::Base::Encoding::Encoding( const MessageDescription &aMDRef ) :
  iMD( aMDRef ), iISegmentsPtr( NULL ),
  iEncodings( 0 ), iPMLEncodings( 0 ), iBMFEncodings( 0 )
{
  for ( unsigned_t segNum = 0; segNum < iMD.iSegmentDescription.size();
	++segNum )
    iSegmentPtr.push_back( iMD.iSegmentDescription[segNum]->createSegment() );
}

NRS::Base::Encoding::~Encoding()
{
  for ( unsigned_t segNum = 0; segNum < iSegmentPtr.size(); ++segNum )
    delete iSegmentPtr[segNum];

  for ( unsigned_t encNum = 0; encNum < iIntelligentStore.size(); ++encNum )
    delete iIntelligentStore[encNum];
}

const std::string &NRS::Base::Encoding::getType() const
{
  return iMD.iName;
}

void NRS::Base::Encoding::addEncodings( encoding_t encodings )
{
  encoding_t enc = (~iEncodings) & encodings;
  iEncodings |= encodings;

  if (enc[(int)Enum::PML])
    {
      iMessageStore.push_back( MessageStore( iSegmentPtr.size(), Enum::PML ) );

      if (iISegmentsPtr.get() == NULL)
	iIntelligentStore.push_back( NULL );
      else
	iIntelligentStore.push_back( new IntelligentStore( *iISegmentsPtr,
							   Enum::PML ) );
      iUpdated.push_back( false );

      enc[(int)Enum::PML] = 0;
    }

  if (enc[(int)Enum::BMF])
    {
      iMessageStore.push_back( MessageStore( iSegmentPtr.size(), Enum::BMF ) );

      if (iISegmentsPtr.get() == NULL)
	iIntelligentStore.push_back( NULL );
      else
	iIntelligentStore.push_back( new IntelligentStore( *iISegmentsPtr,
							   Enum::BMF ) );
      iUpdated.push_back( false );

      enc[(int)Enum::BMF] = 0;
    }

  _ASSERT_(enc == 0, "Not handling new encoding type" );

  if (encodings[(int)Enum::PML] == 1)
    ++iPMLEncodings;

  if (encodings[(int)Enum::BMF] == 1)
    ++iBMFEncodings;
}

void NRS::Base::Encoding::removeEncodings( encoding_t encodings )
{
  if (encodings[(int)Enum::PML] == 1)
    {
      encodings[(int)Enum::PML] = 0;
      if (--iPMLEncodings == 0)
	{
	  iEncodings[(int)Enum::PML] = 0;
	  std::vector< MessageStore >::iterator iterM = iMessageStore.begin();
	  std::vector< IntelligentStore* >::iterator iterI =
	    iIntelligentStore.begin();
	  std::vector< bool >::iterator iterB = iUpdated.begin();

	  while ((iterM != iMessageStore.end()) &&
		 (iterM->getEncoding() != Enum::PML))
	    {
	      ++iterM;
	      ++iterI;
	      ++iterB;
	    }
	  iMessageStore.erase( iterM );
	  iIntelligentStore.erase( iterI );
	  iUpdated.erase( iterB );
	}
    }

  if (encodings[(int)Enum::BMF] == 1)
    {
      encodings[(int)Enum::BMF] = 0;
      if (--iBMFEncodings == 0)
	{
	  iEncodings[(int)Enum::BMF] = 0;
	  std::vector< MessageStore >::iterator iterM = iMessageStore.begin();
	  std::vector< IntelligentStore* >::iterator iterI =
	    iIntelligentStore.begin();
	  std::vector< bool >::iterator iterB = iUpdated.begin();

	  while ((iterM != iMessageStore.end()) &&
		 (iterM->getEncoding() != Enum::BMF))
	    {
	      ++iterM;
	      ++iterI;
	      ++iterB;
	    }
	  iMessageStore.erase( iterM );
	  iIntelligentStore.erase( iterI );
	  iUpdated.erase( iterB );
	}
    }

  if (encodings != 0)
    _ABORT_( "Not handling new encoding type" );

}

NRS::Base::Segment *NRS::Base::Encoding::getSegment( unsigned_t num )
{
#ifdef DEBUG
  return iSegmentPtr.at(num);
#else
  return iSegmentPtr[num];
#endif
}

const NRS::Base::Segment *
NRS::Base::Encoding::getSegment( unsigned_t num ) const
{
#ifdef DEBUG
  return iSegmentPtr.at(num);
#else
  return iSegmentPtr[num];
#endif
}

void NRS::Base::Encoding::clearIntelligentSegments()
{
  iISegmentsPtr.reset( NULL );

  for (unsigned i = 0; i < iIntelligentStore.size(); ++i )
    {
      delete iIntelligentStore[i];
      iIntelligentStore[i] = NULL;
    }
}

void NRS::Base::Encoding::setIntelligentSegment( Enum::IntelligentType aType,
						 const Segment *intSeg )
{
  if (iISegmentsPtr.get() == NULL)
    {
      if (intSeg != NULL)
	{
	  iISegmentsPtr.reset( new IntelligentSegments() );
	  iISegmentsPtr->iSegment[ (uint8_t) aType ] = intSeg->clone();
	}
    }
  else
    {
      delete iISegmentsPtr->iSegment[ (uint8_t) aType ];
      if (intSeg == NULL)
	iISegmentsPtr->iSegment[ (uint8_t) aType ] = NULL;
      else
	iISegmentsPtr->iSegment[ (uint8_t) aType ] = intSeg->clone();
    }

  if (iISegmentsPtr.get() != NULL)
    {
      bool intMessage = false;
      for (unsigned_t i = (unsigned_t) Enum::EndIMessage;
	   i < iISegmentsPtr->iSegment.size(); ++i)
	{
	  intMessage |= (iISegmentsPtr->iSegment[i] != NULL);
	}
      
      if (intMessage)
	{
	  delete iISegmentsPtr->iSegment[ (uint8_t) Enum::IsIntelligent ];
	  Type::BooleanSegment *isInt = new Type::BooleanSegment();
	  isInt->iValue = true;
	  iISegmentsPtr->iSegment[ (uint8_t) Enum::IsIntelligent ] = isInt;
	}
      else
	iISegmentsPtr.reset( NULL );
    }

  for ( unsigned_t msNum = 0; msNum < iIntelligentStore.size(); ++msNum )
    {
      if (iISegmentsPtr.get() != NULL)
	{
	  if (iIntelligentStore[msNum] != NULL)
	    iIntelligentStore[msNum]->useSegments( *iISegmentsPtr );
	  else
	    iIntelligentStore[msNum] =
	      new IntelligentStore( *iISegmentsPtr,
				    iMessageStore[msNum].getEncoding() );
	}
      else
	{
	  if (iIntelligentStore[msNum] != NULL)
	    {
	      delete iIntelligentStore[msNum];
	      iIntelligentStore[msNum] = NULL;
	    }
	}
    }
}

void NRS::Base::Encoding::setMessage( const MessageStore &aMessageStore,
				      const IntelligentStore *intelligentPtr )
{
  if (intelligentPtr != NULL)
    {
      iISegmentsPtr.reset( intelligentPtr->createSegments() );
    }

  for ( unsigned_t i = 0; i < iSegmentPtr.size(); ++i )
    iSegmentPtr[i]->useData( aMessageStore, i );


  for ( unsigned_t i = 0; i < iMessageStore.size(); ++i )
    {
      if (iMessageStore[i].getEncoding() == aMessageStore.getEncoding())
	{
	  iMessageStore[i] = aMessageStore;
	  if (intelligentPtr == NULL)
	    {
	      if (iIntelligentStore[i] != NULL)
		{
		  delete iIntelligentStore[i];
		  iIntelligentStore[i] = NULL;
		}
	    }
	  else
	    {
	      if (iIntelligentStore[i] == NULL)
		{
		  iIntelligentStore[i] =
		    new IntelligentStore( *intelligentPtr,
					  iMessageStore[i].getEncoding() );
		}
	      else
		{
		  *iIntelligentStore[i] = *intelligentPtr;
		}
	    }
	  iUpdated[i] = true;
	}
      else // message is now invalid
	iUpdated[i] = false;
    }
}

void NRS::Base::Encoding::update()
{
  for ( unsigned_t isNum = 0; isNum < iIntelligentStore.size(); ++isNum )
    if (!iUpdated[isNum])
      {
	if (iISegmentsPtr.get() != NULL)
	  {
	    if (iIntelligentStore[isNum] == NULL)
	      iIntelligentStore[isNum] =
		new IntelligentStore( *iISegmentsPtr,
				      iMessageStore[isNum].getEncoding() );
	    else
	      iIntelligentStore[isNum]->useSegments( *iISegmentsPtr );
	  }
	else
	  {
	  if (iIntelligentStore[isNum] != NULL)
	    {
	      delete iIntelligentStore[isNum];
	      iIntelligentStore[isNum] = NULL;
	    }
	  }
      }
  
  for ( unsigned_t msNum = 0; msNum < iMessageStore.size(); ++msNum )
    {
      if (!iUpdated[msNum])
	for ( unsigned_t segNum = 0; segNum < iSegmentPtr.size(); ++segNum )
	  iSegmentPtr[segNum]->createSegment( iMessageStore[msNum], segNum );
      else
	iUpdated[msNum] = false;
    }
}

const NRS::Base::MessageStore &
NRS::Base::Encoding::getMessageStore( Enum::EncodingType encodingType ) const
{
  for ( unsigned_t i = 0; i < iMessageStore.size(); ++i )
    {
      if (iMessageStore[i].getEncoding() == encodingType)
	return iMessageStore[i];
    }
  throw Exception( "Illegal request for non-existent MessageStore", _FL_ );
}

const NRS::Base::IntelligentStore *
NRS::Base::Encoding::getIntelligentStore( Enum::EncodingType encodingType )
  const
{
  if (iISegmentsPtr.get() == NULL)
    return NULL;
  
  for ( unsigned_t i = 0; i < iIntelligentStore.size(); ++i )
    {
      if (iIntelligentStore[i]->getEncoding() == encodingType)
	return iIntelligentStore[i];
    }

  throw Exception( "Illegal request for non-existent IntelligentStore", _FL_ );
}
