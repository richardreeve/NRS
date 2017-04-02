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

#ifndef _VOLTAGE_HH
#define _VOLTAGE_HH

#include <string>

#include "VariableManager.hh"
#include "Float.hh"

namespace NRS
{
  namespace Unit
  {
    /// A variable type for voltage message handlers.
    /**
     *
     * The Voltage class represents all message senders and receivers
     * which send messages consisting of one voltage
     *
     **/
    class Voltage : public Type::Float
    {
    public:
      /// Constructor for Voltage Variables.
      /**
       *
       * \param aName the name of the Voltage Variable being built.
       *
       **/
      Voltage( std::string aName );
      
      /// Constructor for Variables derived from Voltage.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param aVMRef reference to the VariableManager which manages
       * that class.
       *
       **/
      Voltage( std::string aName,
	       const NRS::Base::VariableManager &aVMRef );
      
      /// Destructor.
      virtual ~Voltage();
    };
  }
}

#endif //ndef _VOLTAGE_HH
