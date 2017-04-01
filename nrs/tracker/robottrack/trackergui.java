// $Id: trackergui.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.tracker.robottrack;

import java.util.logging.Logger;

/** This class provides the public-static-void main method for the
 * NRS/TrackerGUI application.
 */
public class trackergui
{
    /**
     * The main method for invoking the GUI. This class creates an
     * instance of {@link AppManager} which is
     * responsible for managing the application.
     *
     * @param args a <code>String[]</code> value holding the commandline
     * arguments the user provided when invoking this application.
     */
    public static void main(String[] args)
    {
        // The main application manager
        AppManager appManager;

        Logger logger = Logger.getLogger("nrs");

        logger.info(AppManager.NAME + " started. Version " 
                    + AppManager.VERSION);

        appManager = AppManager.getInstance();

        appManager.startup(args);
    }
}
