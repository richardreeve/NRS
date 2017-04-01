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

import java.awt.*;

/**
   AntiAlias.

  <p>Static (toolkit) class to enable and disable antialiasing for a Graphics2D object.

   <p>Version 0.5
   <p>by Steven de Jong
   <p>20/Sep/2001
*/
public class AntiAlias
{
    /**
       Enable antialiasing for the given Graphics2D object.
       @param g2 The Graphics2D object.
    */
    public static void enableFor(Graphics2D g2)
    {
	RenderingHints antiAlias =
	    new RenderingHints( RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON );
	antiAlias.put( RenderingHints.KEY_TEXT_ANTIALIASING,
		       RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
	antiAlias.put( RenderingHints.KEY_RENDERING,
		       RenderingHints.VALUE_RENDER_QUALITY);
	g2.addRenderingHints(antiAlias);
    }

    /**
       Disable antialiasing for the given Graphics2D object.
       @param g2 The Graphics2D object.
    */
    public static void disableFor(Graphics2D g2)
    {
	RenderingHints antiAlias =
	    new RenderingHints( RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF );
	antiAlias.put( RenderingHints.KEY_TEXT_ANTIALIASING,
		       RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	antiAlias.put( RenderingHints.KEY_RENDERING,
		       RenderingHints.VALUE_RENDER_SPEED );
	g2.addRenderingHints(antiAlias);
    }
}
