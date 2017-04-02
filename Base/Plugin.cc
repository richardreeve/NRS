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

#include <string>
#include <dlfcn.h>
#include <libgen.h>
#include <ltdl.h>

#include "VariableNodeDirector.hh"
#include "PluginDirector.hh"
#include "StringLiterals.hh"
#include "Exception.hh"
#include "Plugin.hh"

typedef int entrypoint();
// Be careful to save a copy of the error message,
// since the next API call may overwrite the original.
char *dlerrordup (char *errormsg)
{
  const char *error = lt_dlerror();
  if (error && !errormsg)
    errormsg = strdup( error );
  return errormsg;
}

NRS::Base::Plugin::Plugin( std::string aName ) : iHandle( NULL )
{
  PluginDirector &thePD = PluginDirector::getDirector();

  std::string libPath;

  char *theDirectory = 
    getenv( Literals::File::PLUGIN_DIR_ENV() );

  if (theDirectory)
    libPath = theDirectory;
  else
    libPath = Literals::File::DEFAULT_PLUGIN_DIR();

  char *file = strdup( aName.c_str() );
  iName = basename( file );
  free( file );

  if ((iName.length() > 3) && 
      (iName.substr( iName.length()-3, 3 ) == ".so"))
    {
      iName = iName.substr( 0, iName.length() - 3 );
    } else if ((iName.length() > 6) && 
      (iName.substr( iName.length()-6, 6 ) == ".dylib"))
    {
      iName = iName.substr( 0, iName.length() - 6 );
    }

  if ( thePD.hasPlugin( iName ) )
    {
      _ABORT_( "Repeated insertion attempt for plugin " << iName << "\n\t" );
    }

  thePD.signup( iName, this );
  
  std::stringstream anSS;

  // Adds in default path if no path
  if ( aName.find( '/' ) == std::string::npos )
    {
      anSS << libPath;
    }
  else
    {
      char *dir = strdup( aName.c_str() );
      anSS << dirname( dir );
      free( dir );
    }

  anSS << "/" << aName;

  char *errormsg = NULL;
  entrypoint *run = NULL;
  int errors = 0;

  // Initialise preloaded symbol lookup table.
  LTDL_SET_PRELOADED_SYMBOLS();
  
  errormsg = dlerrordup( errormsg );
  if ((errormsg != NULL) && (errormsg[0] != 'u'))
    {
      _ERROR_( "Failed to open plugin " << iName << ", error:\n" <<
	       errormsg );
    }

  // Initialise libltdl.
  errors = lt_dlinit();

  errormsg = dlerrordup( errormsg );
  if ((errormsg != NULL) && (errormsg[0] != 'u'))
    {
      _ERROR_( "Failed to open plugin " << iName << ", error:\n" <<
	       errormsg );
    }

  /* Load the module. */
  if (!errors)
    iHandle = lt_dlopenext(anSS.str().c_str());

  errormsg = dlerrordup( errormsg );
  if ((errormsg != NULL) && (errormsg[0] != 'u'))
    {
      _ERROR_( "Failed to open plugin " << iName << ", error:\n" <<
	       errormsg );
    }

  if (iHandle)
    {
      run = (entrypoint *) lt_dlsym(iHandle, "run");

      /* In principle, run might legitimately be NULL, so
         I don't use run == NULL as an error indicator
         in general. */
      errormsg = dlerrordup( errormsg );

      if ((errormsg != NULL) && (errormsg[0] != 'u'))
        {
	  _ERROR_( "Failed to open plugin " << iName << ", error:\n" <<
		   errormsg );
	  free( errormsg );
          iHandle = NULL;
          lt_dlclose( iHandle );
	  lt_dlexit();
	  errors = 1;
        }
    }
  else
    errors = 1;

  if (!errors)
    {
      int result = (*run)();
      if (result < 0)
        {
	  _ERROR_("module entry point execution failed");
	  errors = 1;
	}
    }

  if (errors)
    {
      _ABORT_( "Failed to open library" );
    }
}

NRS::Base::Plugin::~Plugin()
{
  PluginDirector &thePD = PluginDirector::getDirector();
  VariableNodeDirector &theVND =
    VariableNodeDirector::getDirector();

  if (theVND.usingPlugin( iName ))
  {
    _ABORT_( "Plugin '" << iName << "' cannot be removed as Nodes it manages "
	     << "are still defined\n\t" );
  }

  if (iHandle)
    {
      lt_dlclose( iHandle );
      lt_dlexit();
    }
  thePD.unRegister( iName );
}
