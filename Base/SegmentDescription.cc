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
#include "Exception.hh"
#include "Restriction.hh"
#include "SegmentDescription.hh"
#include "StringLiterals.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

NRS::Base::SegmentDescription::SegmentDescription() :
  iIsConfigured( false ), iType( Enum::NoType ), iIsUnit( false ),
  iInContents( false ), iRestrictionPtr( NULL )
{}

NRS::Base::SegmentDescription::
SegmentDescription( const SegmentDescription &rhs,
		    bool withRestrictions ) :
  iIsConfigured( rhs.iIsConfigured ), iName( rhs.iName ),
  iDisplayName( rhs.iDisplayName ), iDescription( rhs.iDescription ),
  iType( rhs.iType ), iIsUnit( rhs.iIsUnit ), iUnitName( rhs.iUnitName ),
  iInContents( rhs.iInContents ), iRestrictionPtr( NULL )
{
  if (withRestrictions && (rhs.iRestrictionPtr.get() != NULL))
    iRestrictionPtr.reset( rhs.iRestrictionPtr->clone() );
}

NRS::Base::SegmentDescription &
NRS::Base::SegmentDescription::operator=( const SegmentDescription &rhs )
{
  iIsConfigured = rhs.iIsConfigured;
  iName = rhs.iName;
  iDisplayName = rhs.iDisplayName;
  iDescription = rhs.iDescription;
  iType = rhs.iType;
  iIsUnit = rhs.iIsUnit;
  iUnitName = rhs.iUnitName;
  iInContents = rhs.iInContents;
  if (rhs.iRestrictionPtr.get() != NULL)
    iRestrictionPtr.reset( rhs.iRestrictionPtr->clone() );
  else
    iRestrictionPtr.reset( NULL );

  return *this;
}


NRS::Base::SegmentDescription::~SegmentDescription()
{
}

void NRS::Base::SegmentDescription::setType( Enum::Type aType,
					     std::string aUnitName,
					     std::string aDisplayName )
{
  iIsConfigured = true;
  iType = aType;
  iUnitName = aUnitName;
  iIsUnit = !aUnitName.empty();
  
  if (iName.empty())
    {
      if (aUnitName.empty())
	iName = Literals::Type::getTypeName( aType );
      else
	iName = aUnitName;
    }
  
  iDisplayName = aDisplayName;
}

void
NRS::Base::SegmentDescription::setAttributeName( std::string aName,
						 std::string aDisplayName )
{
  iName = aName;
  iDisplayName = aDisplayName;
}

void NRS::Base::SegmentDescription::setInContents( bool inContents )
{
  iInContents = inContents;
}

void  NRS::Base::SegmentDescription::setDescription( std::string aDescription )
{
  iDescription = aDescription;
}

void
NRS::Base::SegmentDescription::setRestriction( Restriction *aRestrictionPtr )
{
  if (aRestrictionPtr->getType() != iType)
    _ABORT_( "Types do not match for restriction and segment" );
  
  iRestrictionPtr.reset( aRestrictionPtr );
}

std::ostream &
NRS::Base::SegmentDescription::getCSLSegment( std::ostream& csl ) const
{
  if (!iIsConfigured)
    _ABORT_( "CSL request for unconfigured segment" );
  
  csl << "<Segment name='" << iName << "' ";
  
  if (!iDisplayName.empty())
    csl << "displayName='" << iDisplayName << "' ";
  
  if (iIsUnit)
    csl << "unit='" << iUnitName << "' ";
  else
    csl << "unit='" << Literals::Type::getTypeName( iType ) << "' ";
  
  csl << "segmentInContents='" << std::boolalpha << iInContents << "' ";
  
  if ((iDescription.empty()) && (iRestrictionPtr.get() == NULL))
    {
      csl << "/>" << std::endl;
    }
  else
    {
      csl << ">" << std::endl;
      if (!iDescription.empty())
	{
	  csl << " <Description>" << std::endl
	      << iDescription
	      << " </Description>" << std::endl;
	}
      if (iRestrictionPtr.get() != NULL)
	{
	  iRestrictionPtr->getCSL( csl );
	}
      csl << "</Segment>" << std::endl;
    }
  return csl;
}

std::ostream &
NRS::Base::SegmentDescription::getCSLUnit( std::ostream& csl ) const
{
  if (!iIsConfigured)
    _ABORT_( "CSL request for unconfigured segment" );
  
  if (!iIsUnit)
    _ABORT_( "CSL unit request for not unit" );
  
  csl << "<Unit name='" << iName << "' ";
  
  if (!iDisplayName.empty())
    csl << "displayName='" << iDisplayName << "' ";
  
  csl << "type='" << Literals::Type::getTypeName( iType ) << "' ";
  
  if ((iDescription.empty()) && (iRestrictionPtr.get() == NULL))
    {
      csl << "/>" << std::endl;
    }
  else
    {
      csl << ">" << std::endl;
      if (!iDescription.empty())
	{
	  csl << " <Description>" << std::endl
	      << iDescription
	      << " </Description>" << std::endl;
	}
      if (iRestrictionPtr.get() != NULL)
	{
	  iRestrictionPtr->getCSL( csl );
	}
      csl << "</Unit>" << std::endl;
    }
  return csl;
}

NRS::Base::Segment *NRS::Base::SegmentDescription::createSegment() const
{

  if (!iUnitName.empty())
    return VariableNodeDirector::getDirector().getVariableManager( iUnitName ).
      createSegment( *this );

  return VariableNodeDirector::getDirector().
    getVariableManager( Literals::Type::getTypeName( iType ) ).
    createSegment( *this );
}
