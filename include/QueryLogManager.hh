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

#ifndef _QUERY_LOG_MANAGER_HH
#define _QUERY_LOG_MANAGER_HH

#include <string>
#include "QueryManager.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Target;
  }
  namespace Message
  {
    /// The manager for the QueryLog Variable type.
    class QueryLogManager : public QueryManager
    {
    public:
      /// Default constructor.
      /**
       *
       * This is the constructor for the basic QueryLogManager.
       *
       **/
      QueryLogManager();

      /// Destructor.
      virtual ~QueryLogManager()
      {
      }

      /// Setup segment records.
      /**
       *
       * This method sets up the segment descriptions for the CSL
       * output
       *
       **/
      virtual void setSegments();

      /// Set Description entries for CSL output
      virtual void setDescription();

      /// Create a new Variable
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( std::string aName );
      
      /// Deliver a PML message to a variable.
      /**
       *
       * This method tells the VariableManager to deliver a
       * message. Since the Message is PML, the message is two
       * attribute lists.
       *
       * \param vnid the id of the target variable
       * \param NRSAttrMap map of nrs specific attributes for variable
       * \param attrMap map of node specific attributes for variable
       * \param intelligent whether the message is intelligent
       * \param contents anything supposed to be inside the element,
       * such as CSL in the ReplyCSL messages. Normally empty.
       *
       * \return success of delivery
       *
       **/
      virtual bool deliverMessage( unsigned_t vnid,
				   std::map< std::string,
                                   std::string > &NRSAttrMap,
                                   std::map< std::string,
                                   std::string > &attrMap,
				   bool intelligent,
				   const char *contents );

      /// Receive a BMF message to deliver to a target.
      /**
       *
       * This method tells the recipient to deliver a message to a
       * target. Since the message is BMF, the message is a binary
       * stream, but the intelligent part is translated into a map.
       *
       * \param vnid the id of the target variable
       * \param data the BMF message to receive
       * \param intelligent whether the message is intelligent
       * \param intMap the intelligent part of the message
       *
       * \return success of the delivery
       *
       **/
      virtual bool deliverMessage( unsigned_t vnid,
				   char *&data,
				   bool intelligent,
				   const char* intMap[] );

      /// Translate a PML message to BMF.
      /**
       *
       * This method tells the VariableManager to translate a PML
       * message into BMF and send it to a port.
       *
       * \param port the port id of the target interface
       * \param theTarget the route for the message
       * \param NRSAttrMap map of nrs specific attributes for variable
       * \param attrMap map of node specific attributes for variable
       * \param intelligent whether the message is intelligent
       * \param contents anything supposed to be inside the element,
       * such as CSL in the ReplyCSL messages. Normally empty.
       *
       * \return success of delivery
       *
       **/
      virtual bool translateMessage( unsigned_t port,
				     Base::Target &theTarget,
				     std::map< std::string,
				     std::string > &NRSAttrMap,
				     std::map< std::string,
				     std::string > &attrMap,
				     bool intelligent = false,
				     const char *contents = NULL );

      /// Translate a BMF message to PML
      /**
       *
       * This method tells the VariableManager to translate a BMF
       * message into PML and send it to a port.
       *
       * \param port the port id of the target interface
       * \param theTarget the route for the message
       * \param data all route and data segments
       * \param msgLen the length of the message segment
       * \param intelligent whether the message is intelligent
       * \param intMap the intelligent part of the message
       *
       **/
      virtual bool translateMessage( unsigned_t port,
				     Base::Target &theTarget,
				     char *&data,
				     bool intelligent = false,
				     const char* intMap[] = NULL );

      /// Send message to get a Log from off component
      /**
       *
       * \param theTarget the route for the message
       * \param returnTarget return route for the message
       * \param aMessageID id representing message, 0 if no data
       * \param aLog number of log being queried
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				Base::Target &returnTarget,
			        unsigned_t aMessageID,
				unsigned_t aLog );
    };
  }
}
#endif //ndef _QUERY_LOG_MANAGER_HH
