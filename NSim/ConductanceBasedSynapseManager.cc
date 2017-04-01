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

#include <iostream>
#include <string>

//#include "Exception.hh"
#include "StringLiterals.hh"
#include "FloatManager.hh"
#include "VoltageManager.hh"
#include "ConductanceManager.hh"
#include "StringLiterals.hh"
#include "TimeManager.hh"
#include "VariableManager.hh"
//#include "Target.hh"
#include "VariableNodeDirector.hh"
//#include "ExternalInterfaceDirector.hh"

#include "ConductanceBasedSynapse.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Manager for ConductanceBasedSynapse node class, describing
    /// layout of message
    class ConductanceBasedSynapseManager : public Base::VariableManager
    {
    public:
      /// Default constructor
      ConductanceBasedSynapseManager() :
	Base::VariableManager( "ConductanceBasedSynapse",
			       "Conductance based synapse for neural simulator",
			       7)
      {
	iDescription = "This variable/message type is the conductance based "
	  "synapse of the simulation";
      }

      /// Destructor
      virtual ~ConductanceBasedSynapseManager()
      {
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
	
	iSegmentDescription[0] = theVND.getVariableManager( Unit::ConductanceManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[0].
	  setAttributeName( "g_syni", "Intrinsic Conductance" );
	
	iSegmentDescription[1] =
	  theVND.getVariableManager( Unit::VoltageManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[1].
	  setAttributeName( "e_syn",
			    "Reversal Potential for synapse" );

	iSegmentDescription[2] =
	  theVND.getVariableManager( Unit::TimeManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[2].
	  setAttributeName( "decay_half_life",
			    "Decay Half Life" );

	iSegmentDescription[3] = theVND.getVariableManager( Unit::ConductanceManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[3].
	  setAttributeName( "g_fac",
			    "Facilitation Conductance" );

	iSegmentDescription[4] = theVND.getVariableManager( Unit::TimeManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[4].
	  setAttributeName( "fac_half_life",
			    "Facilitation Half Life" );

	iSegmentDescription[5] =
	  theVND.getVariableManager( Literals::Type::FLOAT() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[5].
	  setAttributeName( "dep_frac",
			    "Depression Fraction" );

	iSegmentDescription[6] =
	  theVND.getVariableManager( Unit::TimeManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[6].
	  setAttributeName( "dep_half_life",
			    "Depression Half Life" );

      }
    };
  }
}

namespace
{
  NRS::Simulator::ConductanceBasedSynapseManager sConductanceBasedSynapseM;
}
