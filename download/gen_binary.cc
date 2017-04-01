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

#include <fstream>
#include <iostream>
#include "Exception.hh"

int main( int argv, char **argc )
{
  std::string aByte;
  unsigned char val;
  if (argv != 2)
    {
      _EXIT_( "Must have exactly one argument - the save file" );
    }

  std::ofstream of( argc[1] );

  while (!std::cin.eof())
    {
      std::cin >> aByte;
      if (!std::cin.eof())
	{
	  if (aByte.length() != 8)
	    _WARN_( "Byte is wrong length, ignoring" );
	  else
	    {
	      val = 0;
	      for ( int i=0; i<8; ++i )
		{
		  if (aByte[i] == '1')
		    val |= (1<<(7-i));
		}
	      of << val;
	    }
	}
    }
}
