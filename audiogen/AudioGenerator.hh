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

#ifndef _AUDIO_GENERATOR_HH
#define _AUDIO_GENERATOR_HH

#pragma interface

#include "Filename.hh"
#include "Node.hh"

#include "VariableManager.hh"
#include "Exception.hh"
#include "NodeFactory.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  /// Namespace for elements of the audio generator component
  namespace AudioGen
  {
    /// Class for root Node of audio generator    
    class AudioGenerator : public Unit::Filename, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of node to be created
       * \param aNFptr pointer to NodeFactory which created the Node
       *
       **/
      AudioGenerator( std::string aName, Base::NodeFactory *aNFptr ) :
	Unit::Filename( aName, aNFptr->getType() ), 
	Base::Node( aName, Unit::Filename::getVNID(), aNFptr )
      {}
      
      /// Empty destructor
      virtual ~AudioGenerator()
      {}
    };
  }
}

#endif //ndef _AUDIO_GENERATOR_HH
