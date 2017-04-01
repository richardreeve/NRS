/*
 * Copyright (C) 2004 Ben Torben-Nielsen
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
package nrs.audio.simulator ;

import java.util.* ;

public class Chrono
{
    private long startT ;
    private long stopT ;
    private Date date ;
    
    public Chrono()
    {
	Date date = new Date() ;	
    }
    
    public void start()
    {
	startT = new Date().getTime() ;	
    }
    
    public void stop()
    {
	stopT = new Date().getTime() ;	
    }	
    
    public long getMillis()
    {
	return stopT - startT ;	
    }
}
