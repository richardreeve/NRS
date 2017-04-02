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

#ifndef _VARIABLE_INFO_HH
#define _VARIABLE_INFO_HH

#include <vector>
#include <string>
#include "VariableManager.hh"

namespace NRS
{
  namespace Base
  {
    class SegmentDescription;
    /// This is the class containing Variable information for links
    struct VariableInfo
    {
    public:
      /// Constructor
      /**
       *
       * \param theVMRef variable manager from which to extract information
       *
       **/
      VariableInfo( const VariableManager &theVMRef );

      /// Variable Manager associated with this object
      const VariableManager &iVM;

      /// pointers to segment descriptions of each segment
      std::vector< const SegmentDescription* > iSegDescriptionVector;

      /// Name of message
      std::string iMessageName;

      /// Number of segments in message
      unsigned iNumSegments;

      /// Number of boolean segments in message
      unsigned iNumBoolean;

      /// Number of integer segments in message
      unsigned iNumInteger;

      /// Number of float segments in message
      unsigned iNumFloat;

      /// Number of string segments in message
      unsigned iNumString;

      /// Number of route segments in message
      unsigned iNumRoute;

      /// Number of vector segments in message
      unsigned iNumVector;

      /// Types of segments
      std::vector< Enum::Type > iSegTypeVector;

      /// indices from segment number into vectors of individual types
      std::vector< unsigned > iSegIndexVector;
    };
  }
}
#endif //ndef _VARIABLE_INFO_HH
