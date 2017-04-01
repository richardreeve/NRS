/*
 * Copyright (C) 2004 Edinburgh University
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
package nrs.calculation;

import java.util.logging.Logger;

/** Application entry-point for calculation component.
 *
 * @author Thomas French
 * @author Darren Smith
*/

public class nrscalculation {

    /**
     * The main method for invoking the Calculation component GUI. 
     * This class creates an instance of 
     * {@link nrs.calculation.AppManager AppManager} which is
     * responsible for managing the application.
     *
     * @param args a <code>String[]</code> holding all the command line arguments.
     */

    public static void main(String[] args){
	AppManager appManager;
	
	Logger logger = Logger.getLogger("nrs");
	logger.info(AppManager.NAME + " started. Version " 
		    + AppManager.VERSION);
	
	//Start up application manager for nrs.calculation
	appManager = AppManager.getInstance();   
	appManager.startup(args);
    }
}
