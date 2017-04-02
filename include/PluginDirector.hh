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

#ifndef _PLUGIN_DIRECTOR_HH
#define _PLUGIN_DIRECTOR_HH

#include <string>
#include <map>

#include "Exception.hh"
#include "ArgumentFielder.hh"

namespace NRS
{
  namespace Base
  {
    class Plugin;
    class NodeFactory;

    /// This class holds the information about plugins for the framework
    /**
     * 
     * A single PluginDirector, created at initialisation, holds all
     * of the plugin information about the system, and acts as the
     * framework within which are loaded and unloaded
     * 
     **/
    class PluginDirector : public ArgumentFielder
    {
    public:
      /// static accessor for the PluginDirector.
      /**
       *
       * \return A reference to the PluginDirector.
       *
       **/
      static PluginDirector &getDirector ()
      {
	static PluginDirector sPD;
	return sPD;
      }
    
      /// Accessor for the name of the plugin currently being loaded.
      /**
       *
       * \return The name of the plugin currently being loaded, or a
       * default string if we haven't yet left the main initialisation
       * code, or an empty string if nothing is being loaded.
       *
       **/
      std::string getCurrentPlugin()
      {
	return iCurrentPlugin;
      }

      /// Check whether a specific plugin is installed.
      /**
       *
       * This method checks whether a plugin is installed. Since
       * records only show the basename of the plugin, the answer
       * cannot be definitive as to the version and location of the
       * plugin, but only if a plugin of the same name has been
       * loaded.
       *
       * \param aName The name of the plugin to be checked (either the
       * plugin name itself, or a filename with path, which will be
       * stripped off before checking).
       *
       * \return bool signifying whether the plugin is installed.
       *
       **/
      bool hasPlugin( std::string aName );

      /// Insert a plugin into the system
      /**
       *
       * This method inserts a plugin if it can be found and no such
       * plugin is already installed.
       *
       * \param aName The name of the plugin to be installed: this can
       * either be a name, which will be searched for in the standard
       * path, or a filename with a relative or absolute path which
       * will be used directly. The plugin will only be inserted if no
       * plugin of the same name is already installed.
       *
       * \return bool signifying whether the plugin was successfully
       * installed or a plugin of the same name is already installed.
       *
       **/
      bool addPlugin( std::string aName );

      /// Remove an installed plugin.
      /**
       *
       * This method removes a plugin of the given name. Since records
       * only show the basename of the plugin, any plugin with the
       * same basename will be removed. If the plugin is not installed
       * an exception is thrown; if the plugin is installed but is
       * being used, then it will not be removed.
       *
       * \param aName The name of the plugin to be removed (either the
       * plugin name itself, or a filename with path, which will be
       * stripped off before checking).
       *
       * \return bool signifying whether the plugin could be removed.
       *
       * \exception Exception thrown if the plugin is not installed.
       *
       **/
      bool removePlugin( std::string aName );

      /// Get CType of component
      /**
       *
       * \returns CType of component, which is names of plugins
       * concatenated in alphabetical order
       *
       **/
      std::string getCType();

     /// Register a plugin as it loads and set the current plugin name.
      /**
       *
       * This method registers a plugin and consequently sets the
       * current plugin name; it is called by Plugins in their
       * constructor before loading the plugin.
       *
       * \param aName name of the plugin being loaded.
       * \param aPlugin the pointer to the plugin
       *
       **/
      void signup( std::string aName, Plugin *aPlugin );

      /// Unregister a plugin
      /**
       *
       * \param aName of plugin to be unregistered
       *
       **/
      void unRegister( std::string aName );

      /// Returns description of fielder
      /**
       *
       * \returns a description of the fielder for debugging
       *
       **/
      std::string queryFielder() const;

      /// Field the executable name of the program, and deal with appropriately
      /**
       *
       * \param execName the executable name of the program
       *
       **/
      void fieldExecutable( const std::string &execName );

      /// Parse requested command line arguments for the program
      /**
       * NB: this method will modify the arguments in place
       *
       * \param arguments the arguments to the system as an array of
       * strings
       *
       **/      
      void fieldArguments( std::list< std::string > &arguments );

      /// Parse requested spare command line arguments for the program
      /**
       * NB: this method will modify its arguments in place
       *
       * \param arguments the arguments to the system as an array of
       * strings
       *
       **/      
      void fieldSpareArguments( std::list< std::string > &arguments );

    private:
      /// Destructor
      /**
       *
       * This is a private destructor as no-one can create or destroy
       * PluginDirectors.
       *
       **/
      virtual ~PluginDirector ();

      /// Constructor
      /**
       *
       * This is a private constructor as no-one can create or destroy
       * PluginDirectors.
       *
       **/
      PluginDirector ();

      /// Clears the current plugin name.
      /**
       *
       * This method clears the current plugin name; it is called by
       * addPlugin after loading has finished.
       *
       **/
      void clearPlugin();

      /// The name of the current plugin being loaded.
      std::string iCurrentPlugin;

      /// A map of names of plugins, and the opaque handles to the
      /// shared libraries used by libdl.
      std::map< std::string, Plugin* > iPluginMap;

      /// A map of NodeFactory pointers to the name of the plugins
      /// they belong to.
      std::map< NodeFactory*, std::string > iNFPluginMap;

    };
  }
}
#endif //ndef _PLUGIN_DIRECTOR_HH
