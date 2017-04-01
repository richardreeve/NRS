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

#ifndef _REVERSIBLECONDUCTANCE_HH
#define _REVERSIBLECONDUCTANCE_HH

#include <string>
#include <list>
#include "Variable.hh"
#include "Types.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReversibleConductanceManager;
    /// A variable type for double or integer message handlers.
    /**
     *
     * The Number class represents all message senders and receivers
     * which send messages consisting of one double or integer
     *
     **/
    class ReversibleConductance : public Base::Variable
    {
    public:
      /// Constructor for Variables derived from Number.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param aVM a pointer to the VariableManager which manages
       * that class.
       *
       **/
      ReversibleConductance( std::string aName, Base::VariableManager *aVM );
      
      /// Constructor for Variables derived from Number.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param aType the name of the Variable type, so that a manager
       * can be tracked down.
       *
       **/
      ReversibleConductance( std::string aName, std::string aType );
      
      /// Constructor for Number Variables.
      /**
       *
       * \param aName the name of the Number Variable being built.
       *
       **/
      ReversibleConductance( std::string aName );
      
      /// Destructor.
      virtual ~ReversibleConductance();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageValue the (float) number sent as a message
       **/
      virtual void receiveMessage( float aMessageReversalPotential,
				   float aMessageSynapticConductance )
      {
	iReversalPotential = aMessageReversalPotential;
	iSynapticConductance = aMessageSynapticConductance;
	alertObservers();
	sendMessages();
      }

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageValue the (double) number sent as a message
       **/
      virtual void receiveMessage( double aMessageReversalPotential,
				   double aMessageSynapticConductance )
      {
	iReversalPotential = aMessageReversalPotential;
	iSynapticConductance = aMessageSynapticConductance;
	alertObservers();
	sendMessages();
      }

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageValue the (integer) number sent as a message
       **/
      virtual void receiveMessage( number_t aMessageReversalPotential,
				   number_t aMessageSynapticConductance )
      {
	iReversalPotential = aMessageReversalPotential;
	iSynapticConductance = aMessageSynapticConductance;
	alertObservers();
	sendMessages();
      }

      /// Send any outstanding messages.
      /**
       *
       * This virtual method outputs any pending messages through the
       * output interface.
       *
       **/
      virtual void sendMessages();

      /// Add a target to the Variable's output interface.
      /**
       *
       * This method adds a target, which must be a Number*, to the
       * Variable's output interface. Any future outgoing messages
       * will be sent to this target as well as any others.
       *
       * \param aVT a pointer to the target Variable.
       *
       **/
      virtual void addTarget( Variable *aSource )
      {
	iLocalTargetList.
	  push_back( dynamic_cast< ReversibleConductance* >( aSource ) );
      }

      /// Remove a target from the Variable's output interface.
      /**
       *
       * \param aSource a pointer to the target Variable to be removed.
       *
       * \return bool representing whether the Variable could be
       * removed (ie whether it was a target).
       *
       **/
      virtual bool removeTarget( Variable *aSource )
      {
	std::list< ReversibleConductance* >::iterator anIter =
	  find( iLocalTargetList.begin(), iLocalTargetList.end(),
		dynamic_cast< ReversibleConductance* >( aSource ) );

	if (anIter != iLocalTargetList.end())
	  {
	    iLocalTargetList.erase( anIter );
	    return true;
	  }
	return false;
      }

      /// Add a source to the Variable's input interface.
      /**
       *
       * This method adds a source, which must be a Number*, to the
       * Variable's input interface.
       *
       * \param aSource a pointer to the source Variable.
       *
       **/
      virtual void addSource( Variable *aSource )
      {
	iLocalSourceList.
	  push_back( dynamic_cast< ReversibleConductance* >( aSource ) );
      }

      /// Remove a source from the Variable's input interface.
      /**
       *
       * \param aSource a pointer to the source Variable to be removed.
       *
       * \return bool representing whether the Variable could be
       * removed (ie whether it was a source).
       *
       **/
      virtual bool removeSource( Variable *aSource )
      {
	std::list< ReversibleConductance* >::iterator anIter =
	  find( iLocalSourceList.begin(), iLocalSourceList.end(),
		dynamic_cast< ReversibleConductance* >( aSource ) );

	if (anIter != iLocalSourceList.end())
	  {
	    iLocalSourceList.erase( anIter );
	    return true;
	  }
	return false;
      }

      /// Get current values of ReversibleConductance
      /**
       *
       * \returns values ReversibleConductance
       *
       **/
      void getValues( double& aReversalPotential, 
		      double& aSynapticConductance ) const
      {
	aReversalPotential = iReversalPotential;
	aSynapticConductance = iSynapticConductance;
      }

      /// Reset the system to its initial state
      virtual void reset()
      {
	if (iLocalSourceList.empty() && iDistalSourceList.empty())
	  if ((iReversalPotential != iDefaultReversalPotential) ||
	      (iSynapticConductance != iDefaultSynapticConductance))
	    {
	      iReversalPotential = iDefaultReversalPotential;
	      iSynapticConductance = iDefaultSynapticConductance;
	      if (iStateHolding)
		{
		  alertObservers();
		  sendMessages();
		}
	    }
      }

      /// Set default value of String
      /**
       *
       * \param aDefault the default value
       *
       **/
      void setDefault( double aDefaultReversalPotential,
		       double aDefaultSynapticConductance )
      {
	iDefaultReversalPotential = aDefaultReversalPotential;
	iDefaultSynapticConductance = aDefaultSynapticConductance;
      }
    protected:
      /// the value stored by this Number
      double iReversalPotential, iSynapticConductance;

      /// the default value for this Number
      double iDefaultReversalPotential, iDefaultSynapticConductance;

      /// Pointer to the manager for this class.
      ReversibleConductanceManager *iManager;

      /// a list of local sources (ie in the same component).
      std::list< ReversibleConductance* > iLocalSourceList;

      /// a list of local targets (ie in the same component).
      std::list< ReversibleConductance* > iLocalTargetList;
    };
  }
}

#endif //ndef _REVERSIBLECONDUCTANCE_HH
