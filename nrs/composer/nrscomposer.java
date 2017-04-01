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
package nrs.composer;

import java.util.logging.Logger;
import nrs.core.base.MessageProcessor;
import nrs.core.base.Pipeline;
import nrs.core.base.Message;

/** Application entry-point for Composer component.
 *
 * @author Sarah Cope
 * @author Thomas French
*/

public class nrscomposer {

    /**
     * The main method for invoking the Composer component GUI. This
     * class creates an instance of {@link nrs.composer.AppManager
     * AppManager} which is responsible for managing the application.
     *
     * @param args a <code>String[]</code> holding all the command line arguments.
     */

    public static void main(String[] args){
	Logger logger = Logger.getLogger("nrs");
	logger.info(AppManager.NAME + " started. Version " 
		    + AppManager.VERSION);
	
	//Start up application manager for nrs.composer
	AppManager appManager = AppManager.getInstance();
        
        MainFrame m_mainFrame = appManager.getMainFrame();
        Pipeline m_pipeline = appManager.getInboundPipeline();

        MessageInterceptor m_messageInterceptor = 
          new MessageInterceptor(m_mainFrame);

        MessageProcessor m_broadcastHandler = m_pipeline.getDest(null);

        m_pipeline.setDest(m_messageInterceptor);

        m_messageInterceptor.setDest(m_broadcastHandler);

        m_mainFrame.setMessageInterceptor(m_messageInterceptor);

	appManager.startup(args);
    }
}
