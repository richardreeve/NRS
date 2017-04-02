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

#ifndef _MESSAGE_DESCRIPTION_HH
#define _MESSAGE_DESCRIPTION_HH

#include <string>
#include <vector>

namespace NRS
{
  namespace Base
  {
    class SegmentDescription;

    /// This is the class which holds message descriptions
    class MessageDescription
    {
    public:
      /// The name of the Variable or Message type being managed.
      std::string iName;

      /// SegmentDescriptions for Message
      std::vector< const SegmentDescription* > iSegmentDescription;
    };
  }
}
      
#endif //ndef _MESSAGE_DESCRIPTION_HH
