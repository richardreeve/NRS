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

#include <sstream>

#include "Exception.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntelligentStore.hh"
#include "Segment.hh"
#include "SegmentDescription.hh"
#include "StringLiterals.hh"
#include "Variable.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

NRS::Base::VariableManager::VariableManager( Enum::Type rootType ) :
  iRootType( rootType ), iNumSegments( 1 ), iIsType( true ),
  iIsUnit( false ), iIsRegistered( false ), iIsConfigured( false )
{
  iSegmentDescription.resize( iNumSegments );
  iMessageDescription.iName = Literals::Type::getTypeName( rootType );
  VariableNodeDirector::getDirector().
    signupVariableManager( iMessageDescription.iName, this );
}

NRS::Base::VariableManager::VariableManager( std::string aMessageName,
					     std::string aDisplayName,
					     unsigned_t numSegments,
					     bool isUnit,
					     Enum::Type baseType ) :
  iRootType( baseType ),
  iDisplayName( aDisplayName ), iNumSegments( numSegments ),
  iIsType( false ), iIsUnit( isUnit ), iIsRegistered( false ),
  iIsConfigured( false )
{
  _ASSERT_( !isUnit || (numSegments == 1), "Illegal manager" );

  iMessageDescription.iName = aMessageName;
  iSegmentDescription.resize( iNumSegments );
  VariableNodeDirector::getDirector().
    signupVariableManager( iMessageDescription.iName, this );
}

NRS::Base::VariableManager::~VariableManager()
{
  unRegister();
}

bool NRS::Base::VariableManager::differ( const VariableManager &aVM ) const
{
  std::ostringstream aSS, bSS;
  getCSL( aSS );
  aVM.getCSL( bSS );
  return ( aSS.str() != bSS.str() );
}

std::ostream &NRS::Base::VariableManager::getCSL( std::ostream& csl ) const
{
  _ASSERT_( iIsConfigured, "Request for unconfigured CSL" );

  if (iIsType)
    {
      csl << "<!-- Type "
	  << "name='" << iMessageDescription.iName << "' ";
      if (!iDisplayName.empty())
	{
	  csl << "displayName='" << iDisplayName << "' ";
	}

      if (iDescription.empty())
	{
	  csl << "-->\n";
	}
      else
	{
	  csl << ">\n"
	      << " <Description>\n"
	      << iDescription << "\n"
	      << " </Description>\n"
	      << "</Type -->\n";
	}
    }
  else if (iIsUnit) // always one segment
    {
      iSegmentDescription[0].getCSLUnit( csl );
    }
  else // message
    {
      csl << "<Message "
	  << "name='" << iMessageDescription.iName << "' ";
      if (!iDisplayName.empty())
	{
	  csl << "displayName='" << iDisplayName << "' ";
	}

      if ((iDescription.empty()) && (iNumSegments == 0))
	{
	  csl << "/>\n";
	}
      else
	{
	  csl << ">\n";
	  if (!iDescription.empty())
	    {
	      csl << " <Description>\n"
		  << iDescription << "\n"
		  << " </Description>\n";
	    }
	  for ( unsigned i = 0; i < iNumSegments; ++i )
	    iSegmentDescription[i].getCSLSegment( csl );
	  
	  csl << "</Message>\n";
	}
    }
  return csl;
}

void NRS::Base::VariableManager::doRegister()
{
  if (!iIsRegistered)
    {
      configure();
      _TRY_( VariableNodeDirector::getDirector().
	     addVariableManager( this ), _RETHROW_ );
      iIsRegistered = true;
    }
}

void NRS::Base::VariableManager::unRegister()
{
  if (iIsRegistered)
    {
      _TRY_( VariableNodeDirector::getDirector().
	     removeVariableManager( this ), _RETHROW_ );
      iIsRegistered = false;
    }
}

void NRS::Base::VariableManager::configure()
{
  if (!iIsConfigured)
    {
      doConfigure();
      iIsConfigured = true;
      for ( unsigned_t segNum = 0; segNum < iNumSegments; ++segNum )
	{
	  iMessageDescription.iSegmentDescription.
	    push_back( &iSegmentDescription[segNum] );
	}
    }
}

const NRS::Base::SegmentDescription &
NRS::Base::VariableManager::getSegmentDescription( unsigned_t num ) const
{
  if (num >= iNumSegments)
    _ABORT_( "Illegal segment number request:" << iMessageDescription.iName
	     << num );
  
  _ASSERT_( iIsConfigured, "Request for unconfigured segment" );
  
  return iSegmentDescription[num];
}

void
NRS::Base::VariableManager::createVariable( const std::string &aName ) const
{
  _ABORT_( "Variable construction not correctly overridden creating '" 
	   << aName << "' variable" );
}

NRS::Base::Segment *
NRS::Base::VariableManager::createSegment( const SegmentDescription & )
  const
{
  if (iNumSegments != 1)
    _ABORT_( "Trying to make a segment in a non-single segment manager" );
  else
    _ABORT_( "segment creation not correctly overridden" );

  return NULL;
}

void NRS::Base::VariableManager::doConfigure()
{
  _ASSERT_( iIsType || iIsUnit,
	    "base configure called on a Message" );

  if (iIsType)
    iSegmentDescription[0].setType( iRootType );
  else // iIsUnit
    iSegmentDescription[0].setType( iRootType, iMessageDescription.iName,
				    iDisplayName );
}

void
NRS::Base::VariableManager::
translateMessage( ExternalInterface *anEIPtr,
		  Target &aTarget, 
		  const MessageStore &aMessageStore,
		  const IntelligentStore *intelligentPtr ) const
{
  MessageStore newMS( iNumSegments, anEIPtr->getEncoding() );

  // This creates several object to do the translation. This is slow,
  // but thread safe.
  for ( unsigned_t segNum = 0; segNum < iNumSegments; ++segNum )
    { // create appropriate segment and translate
      _DEBUG_( "Segment translation " << segNum );
      Segment *segmentPtr =
	iSegmentDescription[segNum].createSegment();
      _DEBUG_( iSegmentDescription[segNum].getUnitName() << " "
	       <<  iSegmentDescription[segNum].getAttributeName() );
      segmentPtr->useData( aMessageStore, segNum );
      segmentPtr->createSegment( newMS, segNum );
    }
  
  // Is there an intelligent segment?
  if (intelligentPtr)
    {
      IntelligentStore anIS( *intelligentPtr, anEIPtr->getEncoding() );
      anEIPtr->sendMessage( aTarget, newMS, &anIS );
    }
  else
    anEIPtr->sendMessage( aTarget, newMS, NULL );
}


const NRS::Base::VariableManager *NRS::Base::VariableManager::getParent() const
{
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();
  if (theVND.hasVariableManager( iParentName ))
    return &theVND.getVariableManager( iParentName );

  return NULL;
}
