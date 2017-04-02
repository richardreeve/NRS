/*
 * Copyright (C) 2005 Richard Reeve, Matthew Szenher and Edinburgh University
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

#include "Executive.hh"
#include "Location.hh"

NRS::Base::Location::Location( const std::string &theCID,
			       const route_t &theRoute,
			       integer_t theVNID,
			       bool sourceNotTarget ) :
  iCID( theCID ), iRoute( theRoute ), iVNID( theVNID ),
  iSourceNotTarget( sourceNotTarget )
{
}

bool NRS::Base::Location::operator==( const Location &rhs ) const
{
  // Some cids may be missing. If so check route
  if ((iCID.empty() && (iRoute != getEmptyRoute())) ||
      (rhs.iCID.empty() && (rhs.iRoute != getEmptyRoute())))
    return ((iCID == rhs.iCID) && 
	    (iVNID == rhs.iVNID) &&
	    (iRoute == rhs.iRoute));
  
  // If not, just use cid
  return ((iCID == rhs.iCID) && (iVNID == rhs.iVNID));
}
 
bool NRS::Base::Location::isLocal() const
{
  return ((iRoute == getEmptyRoute()) && (iCID.empty() ||
	  (iCID == Executive::getExecutive().getCID())));
}
