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

#ifndef _VARIABLE_HH
#define _VARIABLE_HH

#pragma interface
#include <list>
#include <map>
#include <string>

#include "Link.hh"
#include "Observable.hh"
#include "Types.hh"
#include "VariableManager.hh"

namespace NRS
{
  namespace Base
  {
    class Location;
    class Message;
    class MessageFunctionObject;
    class Log;

    /// This is the Variable base class.
    class Variable : public Observer, public Observable
    {
    public:
      /// Constructor.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param theVMRef reference to the VariableManager which manages
       * that class.
       *
       **/
      Variable( std::string aName, const VariableManager &theVMRef );

      /// Destructor.
      virtual ~Variable();
      
      /// Get name of Variable.
      /**
       *
       * \return the name of the variable.
       *
       **/
      const std::string &getName() const
      {
	return iName;
      }

      /// Get name of parent of Variable.
      /**
       *
       * \return the name of the parent of variable.
       *
       **/
      const std::string &getParentName() const
      {
	return iParentName;
      }

      /// Get local name of Variable.
      /**
       *
       * \return the local name of the variable.
       *
       **/
      const std::string &getLocalName() const
      {
	return iLocalName;
      }

      /// Get input link
      /**
       *
       * \return pointer to input link
       *
       **/
      Link &getInputLink()
      {
	return iInputLink;
      }

      /// Get output link
      /**
       *
       * \return pointer to output link
       *
       **/
      Link &getOutputLink()
      {
	return iOutputLink;
      }

      /// Get a pointer to the variable's manager.
      /**
       *
       * \return the reference to the VariableManager.
       *
       **/
      const VariableManager &getVariableManager() const
      {
	return iVM;
      }

      /// get the variable's vnid.
      /**
       *
       * \return the variable's vnid.
       *
       **/
      unsigned_t getVNID() const
      {
	return iVNID;
      }

      /// Add a Source
      /**
       *
       * \param aSourceRef the source of the message
       *
       **/
      void addSource( const Location &aSourceRef );

      /// Remove a Source
      /**
       *
       * \param aSourceRef the source of the message
       *
       **/
      bool removeSource( const Location &aSourceRef );

      /// Adds a target which will be connected to
      /**
       *
       * \param aTarget the target to which to connect
       *
       **/
      void addTarget( const Target &aTarget );

      /// Remove a target to which the Variable was connected
      /**
       *
       * \param aTarget the target from which to disconnect
       *
       **/
      bool removeTarget( const Target &aTarget );

      /// Add a Log
      /**
       *
       * \param aLogPtr the target of the message
       *
       **/
      void addLog( Log *aLogPtr );

      /// Remove a Log
      /**
       *
       * \param aLog the target of the message
       * \param logPort port of log from target component
       *
       **/
      bool removeLog( const Log *aLogPtr );

      /// resolve any dependency issues
      /**
       *
       * Note: by default a variable has no dependency issues
       *
       * \returns whether all dependencies are resolved
       *
       **/
      bool resolveDependencies();

      /// Set number of connections
      /**
       *
       * \param min_in the minimum number of inbound connections
       * \param max_in the maximum number of inbound connections
       * \param min_out the minimum number of outbound connections
       * \param max_out the maximum number of outbound connections
       * \param min_log the minimum number of log connections
       * \param max_log the maximum number of log connections
       *
       **/
      void setConnections( int min_in, int max_in,
			   int min_out, int max_out,
			   int min_log, int max_log );

      /// Set whether the variable is stateholding or not
      /**
       *
       * \param stateHolding whether the variable is state holding or
       * event based
       *
       **/
      void setStateHolding( bool stateHolding )
      {
	iStateHolding = stateHolding;
      }

      /// Set whether the variable is self-updating or not
      /**
       *
       * \param selfUpdating whether the variable is self-updating or
       * fixed to its inputs
       *
       **/
      void setSelfUpdating( bool selfUpdating );

      /// warning that the observable will no longer be available
      /**
       *
       * \param aRef the reference the Observable was given to
       * return
       *
       **/
      void removeObservable( integer_t aRef );

      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      void observe( integer_t aRef );

      /// Send any outstanding messages.
      /**
       *
       * This method inputs any external messages, and outputs any
       * pending messages through the output interface. If the
       * Variable is not selfupdating messages received are passed
       * straight through to the output side.
       *
       **/
      void sendMessages();

      /// Update state of output link.
      /**
       *
       * This method updates the state of the Variable is it is
       * self-updating. It then updates the output interface ready for
       * transmitting the new state. If the variable is not
       * self-updating, the method does nothing.
       *
       **/
      void update();

      /// Add a MessageFunctionObject to extend capabilities of
      /// Variable
      /**
       *
       * \param aMFOPtr the pointer to the MFO which specifies the
       * link to be made
       *
       **/
      void addMFO( MessageFunctionObject *aMFOPtr );

      /// Reset the system to its initial state
      /**
       *
       * Calls doReset() for variable-specific stuff
       *
       **/
      void reset();

      /// Force variable to send message to connections.

      /**
       *
       * This method can be used by new local connections to make sure
       * they get the current state of the source. It can also be used
       * to force a transmission of a message by a
       * MessageFunctionObject.
       *
       * \param sendEvents should non-stateholding variables send messages?
       * \param doUpdate force a recalculation
       *
       **/
      void forceSend( bool sendEvents, bool doUpdate = false );

      /// Add a new node-local connection for a specialised type
      /**
       *
       * Must be implemented by any class using a non-standard
       * connection (ie one for which a MessageFunctionObject has not
       * been designed). The connection is indicated in the
       * NodeFactory by use of a SpecialisedMFO.
       *
       * \param aRef reference the Observable was given to
       * return. Always strictly negative for reactions
       *
       **/
      virtual void addReaction( integer_t aRef );

      /// React to a specialised input on a node-local connection
      /**
       *
       * Must be implemented by any class using a non-standard
       * connection (ie one for which a MessageFunctionObject has not
       * been designed). The connection is indicated in the
       * NodeFactory by use of a SpecialisedMFO.
       *
       * \param aRef reference the Observable was given to
       * return. Always strictly negative for reactions
       *
       **/
      virtual void react( integer_t aRef );

    protected:
      /// Implement any variable specific behaviour for adding a Source
      /**
       *
       * Must be overriden if a variable wants to do something
       * specific when a new source is added, like creating a local
       * pointer to its output values.
       *
       * \param aSourceRef the source of the message
       * \param aRef reference for message source. Always 0 for remote
       * sources, strictly positive for local connections.
       *
       **/
      virtual void doAddSource( const Location &aSourceRef,
				integer_t aRef );

      /// Implement any variable specific behaviour to remove Observable
      /**
       *
       * Needs to be overriden if any specific behaviour needs to be
       * done when a source is removed.
       *
       * \param aRef the reference the Observable was given to
       * return. Negative for reactions, 0 for remote connections,
       * positive for local connections.
       *
       **/
      virtual void doRemoveSource( integer_t aRef );

      /// Variable-specific reset of system
      /**
       *
       * Must be implemented by any class which alters the state of
       * internal variables which need to be reset to reach a
       * consistent state for restarting experiments.
       *
       **/
      virtual void doReset();

      /// New data is available to observe from a source of the same type
      /**
       *
       * Must be implemented by any class which receives non-void
       * data, and wishes to do some processing on it. Typically
       * implemented by the basic types, but can be overridden
       *
       * \param aRef the reference the Observable was given to
       * return. Always 0 for remote messages and strictly positive
       * for local connections
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Update internal state of variable
      /**
       *
       * Must be overridden by all self-updating variables
       *
       * This method does non-generic internal update on the
       * variable. It is only called if the method is self-updating
       *
       **/
      virtual void doUpdate();

      /// resolve complex dependencies of derived classes
      /**
       *
       * Needs to be overriden if anything above the usual
       * dependencies need to be satisfied before the variable is
       * ready to be used. Less required now node-local connections
       * are handled automatically.
       *
       * \return whether any complex dependencies have been resolved
       *
       **/
      virtual bool resolveComplexDependencies();

      /// Return the next observer reference
      /**
       *
       * \return the next observer reference
       *
       **/
      integer_t getNextObserver();
      
      /// The pointer to the VariableManager.
      const VariableManager &iVM;

      /// The name of the variable.
      std::string iName;

      /// The name of the parent of the variable
      std::string iParentName;

      /// The local name of this variable
      std::string iLocalName;

     /// Should the variable send a message?
      bool iSendMessage;

      /// number for next log
      int iNextLog;

      /// number output link is expecting for observe call
      integer_t iObserveVal;

      /// Is the Variable event based or state holding
      bool iStateHolding;

      /// Is the Variable self-updating or fixed to its inputs
      bool iSelfUpdating;

      /// the next observer reference
      integer_t iNextObserver;

      /// Vector of sources being observed, position in vector is
      /// reference given to Observable
      std::vector< Observable* > iObserved;

      /// Vector of MessageFunctionObjects which extend the
      /// capabilities of the variable to handle new input types
      /// without having to make a new subclass, position in vector is
      /// -reference given to source Observable
      std::vector< MessageFunctionObject* > iMFOPtr;

      /// The variable's vnid.
      unsigned_t iVNID;

      /// Input link
      Link iInputLink;

      /// Output link pointer
      Link iOutputLink;
    };
  }
}
#endif //ndef _VARIABLE_HH
