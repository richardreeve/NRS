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

#ifndef _CSL_VARIABLE_MANAGER_HH
#define _CSL_VARIABLE_MANAGER_HH

#pragma interface
#include <vector>

#include "SegmentDescription.hh"
#include "VariableManager.hh"
namespace NRS
{
  namespace Base
  {
    /// class for VariableManagers automatically generated from CSL
    class CSLVariableManager : public VariableManager
    {
    public:
      /// Constructor
      /**
       *
       *
       * \param aMessageName Name of the Message type that will be
       * managed.
       * (leave empty for same as aMessageName).
       * \param segDescVector SegmentDescription vector for variable
       *
       **/
      CSLVariableManager( std::string aMessageName,
			  std::vector< SegmentDescription > segDescVector );

      /// Destructor
      /**
       *
       * The destructor deregisters the manager from the
       * VariableNodeDirector.
       *
       **/
      virtual ~CSLVariableManager()
      {
      }

    protected:
      /// Configure the internal records of the message type.
      virtual void doConfigure()
      {
      }
    };
  }
}
      
#endif //ndef _CSL_VARIABLE_MANAGER_HH
