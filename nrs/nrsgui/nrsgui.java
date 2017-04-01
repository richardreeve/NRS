package nrs.nrsgui;

/** This class provides the public-static-void main method.
 */
public class nrsgui
{
    /**
     * The main method for invoking the GUI. This class creates an
     * instance of {@link nrs.nrsgui.AppManager AppManager} which is
     * responsible for managing the application.
     *
     * @param args a <code>String[]</code> value holding the commandline
     * arguments the user provided when invoking this application.
     */
    public static void main(String[] args)
    {
        // The main application manager
        AppManager appManager = AppManager.getInstance();

        appManager.startup(args);
    }
}
