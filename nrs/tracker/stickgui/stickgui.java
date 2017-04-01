// $Id: stickgui.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.tracker.stickgui;

import java.util.logging.Logger;

/** This class provides the public-static-void main method for the
 * NRS/StickTrack application.
 */
public class stickgui
{
    /**
     * The main method for invoking the GUI. This class creates an
     * instance of {@link nrs.tracker.stickgui.STAppManager} which is
     * responsible for managing the application.
     *
     * @param args a <code>String[]</code> value holding the commandline
     * arguments the user provided when invoking this application.
     */
    public static void main(String[] args)
    {
        // The main application manager
        STAppManager appManager;

        Logger logger = Logger.getLogger("nrs");

        logger.info(STAppManager.NAME + " started. Version " 
                    + STAppManager.VERSION);

        appManager = STAppManager.getInstance();

        appManager.startup(args);
    }
}
