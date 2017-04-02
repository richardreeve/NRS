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

#ifndef _PLUGIN_HH
#define _PLUGIN_HH

#include <string>
#include <ltdl.h>

namespace NRS
{
  namespace Base
  {
    /// This class holds a plugin
    class Plugin
    {
    public:
      /// Constructor - loads a plugin
      /**
       *
       * \param aName name of plugin to be loaded: this can either be
       * a filename with or without path, or a basename (such as nsim
       * for libnsim.so)
       *
       **/
      Plugin( std::string aName );

      /// Destructor - unloads a plugin
      ~Plugin();

      /// gets the name of the plugin
      std::string getName()
      {
	return iName;
      }

    private:
      /// name of plugin
      std::string iName;

      /// Opaque handle to the shared library used by libltdl
      lt_dlhandle iHandle;
    };
  }
}
#endif //ndef _GENERAL_DIRECTOR_HH
