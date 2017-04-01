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

#ifndef _V_ACTUAL_MANAGER
#define _V_ACTUAL_MANAGER

#include <VoltageManager.hh>
#pragma interface

namespace NRS
{
  namespace Simulator
  {
    /// This is the manager for all VActual variables.
    /**
     *
     * The variables managed by this class send and receive messages
     * setting the current membrane potential of neurons.
     *
     **/
    class VActualManager : public NRS::Unit::VoltageManager
    {
    public:
      /// Constructor
      VActualManager() : NRS::Unit::VoltageManager( "VActual",
						    "Potential currently")
      {
      }

      /// Destructor
      ~VActualManager()
      {
      }

      /// Set Description entries for CSL output
      void setDescription()
      {
        iDescription = "This variable/message type allows recipients to be\n"
          "told that their Node has changed its current potential.";
	iSegDescriptionVector[ 0 ] = "The current potential "
	  "setting.";
      }
    };
  }
} 

#endif //ndef _V_ACTUAL_MANAGER
