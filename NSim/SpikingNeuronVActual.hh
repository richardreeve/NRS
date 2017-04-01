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

#ifndef _SPIKINGNEURONVACTUAL_HH
#define _SPIKINGNEURONVACTUAL_HH

#pragma interface

#include "Voltage.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Class derived from Voltage which keeps track of the 
    /// membrane potential of a Spiking Neuron over time
    class SpikingNeuronVActual : public Unit::Voltage
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of variable to be constructed
       *
       **/
      SpikingNeuronVActual( std::string aName );
      
      /// Empty destructor
      virtual ~SpikingNeuronVActual();

      /// Note arrival of specialised connection
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Internally connected variable has changed
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void react( integer_t aReference );
    protected:
      /// Remove connection to another variable of the SpikingNeuron
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Update state of membrane potential
      virtual void doUpdate();

      /// Timestep reference
      integer_t iTimestepReference;

      /// Remote timestep value pointer (NULL if missing)
      float_t *iTimestepValuePtr;

      /// A local cache of the 
      float_t iTimestepValue;

      /// Refractory Period reference
      integer_t iRefractoryReference;

      /// Remote Refractory value pointer (NULL if missing)
      float_t *iRefractoryValuePtr;

      /// A local cache of the Refractory Period
      float_t iRefractoryValue;

      /// BaseV reference
      integer_t iBaseVReference;

      /// Remote BaseV value pointer (NULL if missing)
      float_t *iBaseVValuePtr;

      /// A local cache of BaseV
      float_t iBaseVValue;

      /// SpikeV reference
      integer_t iSpikeVReference;

      /// Remote SpikeV value pointer (NULL if missing)
      float_t *iSpikeVValuePtr;

      /// A local cache of SpikeV
      float_t iSpikeVValue;

      /// ResetV reference
      integer_t iResetVReference;

      /// Remote ResetV value pointer (NULL if missing)
      float_t *iResetVValuePtr;

      /// A local cache of ResetV 
      float_t iResetVValue;

      /// BaseG reference
      integer_t iBaseGReference;

      /// Remote BaseG value pointer (NULL if missing)
      float_t *iBaseGValuePtr;

      /// A local cache of BaseG 
      float_t iBaseGValue;

      /// MembC reference
      integer_t iMembCReference;

      /// Remote MembC value pointer (NULL if missing)
      float_t *iMembCValuePtr;

      /// A local cache of MembC
      float_t iMembCValue;

      /// GTotal reference
      integer_t iGTotalReference;

      /// Remote GTotal value pointer (NULL if missing)
      float_t *iGTotalValuePtr;

      /// A local cache of GTotal 
      float_t iGTotalValue;

      /// ITotal  reference
      integer_t iITotalReference;

      /// Remote ITotal value pointer (NULL if missing)
      float_t *iITotalValuePtr;

      /// A local cache of ITotal
      float_t iITotalValue;

      //Member variables used in calculations

      /// Decay constant
      float_t iDecayValue;

      ///Target Voltage
      float_t iVTargetValue;

      /// boolean indicating whether we are in post-spike refractory period
      bool iInRefractoryPeriod;
      
      /// Time spent in refractory period
      float_t iTimeInTRefract;
    };
  }
}

#endif //ndef _SPIKINGNEURONVACTUAL_HH
