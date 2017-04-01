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

#ifndef _OBSERVABLE_HH
#define _OBSERVABLE_HH

#include <list>
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    /// The class for observers of information in the system (mostly Variables)
    /**
     *
     * This base class provides a common interface for observers who
     * have a fixed relationship with other elements of the system -
     * any variable which depends on the state of another source of
     * information should provide this interface to be alerted
     * through. For instance in a Node which has various parameters,
     * and has some output which depends on them, the output variable
     * would be a observer of information, and the variables which
     * hold the parameter values would be observables.
     *
     **/
    class Observer
    {
    public:
      /// Dummy Destructor
      virtual ~Observer()
      {
      }

      /// New data is available to observe from a source
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void observe( integer_t aReference ) = 0;

      /// warning that the observable will no longer be available
      /**
       *
       * \param aReference the reference the Observable was given to
       * return
       *
       **/
      virtual void removeObservable( integer_t aReference ) = 0;
    };

    /// The class for observables of information (mostly Variables)
    /**
     *
     * This base class provides a common interface for observables who
     * have a fixed relationship with other elements of the system -
     * any variable (or other source of information) which has a state
     * which changes which other variables might wish to be aware of
     * should provide this interface. For instance in a Node which has
     * various parameters, and has some output which depends on them,
     * the output variable would be a observer of information, and the
     * variables which hold the parameter values would be observables.
     *
     **/
    class Observable
    {
    public:
      /// Constructor
      Observable();

      /// Destructor
      virtual ~Observable();

      /// add a Observer of information you provide
      /**
       *
       * \param aObserver the observer to be added
       * \param aReference the reference to return to the observer
       *
       **/
      void addObserver( Observer *anObserverPtr, integer_t aReference );

      /// remove a Observer from your records
      /**
       *
       * \param aObserver the observer to be removed
       * \param aReference the reference returned to the observer
       *
       **/
      void removeObserver( Observer *anObserverPtr, integer_t aReference );

      /// alert observers that new information is available
      void alertObservers() const
      {
	for( typeof( iObserverPtrList.begin() ) anIter =
	       iObserverPtrList.begin();
	     anIter != iObserverPtrList.end(); anIter++ )
	  {
	    anIter->first->observe( anIter->second );
	  }
      }
    private:
      /// A list of all observers of this observable's information
      std::list< std::pair< Observer*, integer_t > > iObserverPtrList;
    };
  }
}
#endif //ndef _OBSERVABLE_HH
