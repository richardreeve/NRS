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

#ifndef _POSITIONMANAGER_HH
#define _POSITIONMANAGER_HH

#pragma implementation
#include <string>
#include "Position.hh"
#include "StringLiterals.hh"
#include "IntegerManager.hh"
#include "FloatManager.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

namespace NRS
{
  namespace Tracker
  {
    class PositionManager : public Base::VariableManager
    {
    public:
      /// Default constructor
      PositionManager() :
	Base::VariableManager( "Position",
			       "Robot tracking data",
			       6 )
      {
	iDescription = "A representation of a robot's position and heading at "
	  "given frame and time";
      }

      /// Constructor for derivatives of Position
      /**
       *
       * \param aName name of manager
       * \param description pretty print version of name
       *
       **/
      PositionManager( std::string aName, std::string description ) :
	Base::VariableManager( aName, description, 6 )
      {
      }

      /// Destructor
      virtual ~PositionManager()
      {
      }

      /// Repository for text string "Position"
      /**
       *
       * \return the string "Position"
       *
       **/
      static const char *getName()
      {
        static const char *name = "Position";
        return name;
      }

    protected:
      /// Configure the internal records of the message type.
      /**
       *
       * This method configures the internal records of the manager so
       * that it can correctly output its CSL description on
       * request. This cannot conveniently be done at construction
       * time since too much work would be devolved to disparate
       * constructors. Called by configure(). Must create all of the
       * SegmentDescriptions for the manager.
       *
       **/
      virtual void doConfigure()
      {
	Base::VariableNodeDirector &theVND =
	  Base::VariableNodeDirector::getDirector();
	
	iSegmentDescription[0] =
	  theVND.getVariableManager( Literals::Type::getTypeName( Enum::Float ) ).
	  getSegmentDescription( 0 );
	iSegmentDescription[0].
	  setAttributeName( "x", "" );
	
	iSegmentDescription[1] =
	  theVND.getVariableManager( Literals::Type::getTypeName( Enum::Float ) ).
	  getSegmentDescription( 0 );
	iSegmentDescription[1].
	  setAttributeName( "y", "" );
	
	iSegmentDescription[2] =
	  theVND.getVariableManager( Literals::Type::getTypeName( Enum::Float ) ).
	  getSegmentDescription( 0 );
	iSegmentDescription[2].
	  setAttributeName( "heading", "" );
	
	iSegmentDescription[3] =
	  theVND.getVariableManager( Literals::Type::getTypeName( Enum::Integer ) ).
	  getSegmentDescription( 0 );
	iSegmentDescription[3].
	  setAttributeName( "timestep", "" );
	
	iSegmentDescription[4] =
	  theVND.getVariableManager( Literals::Type::getTypeName( Enum::Integer ) ).
	  getSegmentDescription( 0 );
	iSegmentDescription[4].
	  setAttributeName( "frame", "" );
	
	iSegmentDescription[5] =
	  theVND.getVariableManager( Literals::Type::getTypeName( Enum::Integer ) ).
	  getSegmentDescription( 0 );
	iSegmentDescription[5].
	  setAttributeName( "trackerid", "" );
      }
    };
  }
}

namespace
{
  NRS::Tracker::PositionManager sPositionM;
}

#endif //ndef _POSITIONMANAGER_HH
