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
package nrs.datalogger;

import nrs.core.base.BaseAppManager;
import nrs.core.base.BaseComponent;
import nrs.core.base.VariableManager;
import java.util.logging.*;

/**
 * Java DataLogger component
 *
 * @author Thomas French
 * @author Darren Smith
*/
public class AppManager extends BaseAppManager
{
    /** The version number of the application. */
    public static final double VERSION = 0.1;
    
    /** Name of the application. */
    public static final String NAME = "NRS.datalogger";
    
    /** Singleton instance of <code>AppManager</code> class. */
    private static AppManager m_singletonInstance = null;
    
    protected Logger nrsLogger;

    protected final Level DEFAULT_LEVEL = Level.CONFIG;
    
    //----------------------------------------------------------------------

    /**
     * Return the singleton instance of class AppManager.
     *
     * @return singleton instance of class AppManager.
     */
    public static AppManager getInstance()
    {
	if (m_singletonInstance == null) new AppManager();
	
	return m_singletonInstance;
    }
    //----------------------------------------------------------------------

    /**Private AppManager constructor, which uses singleton pattern.*/
    private AppManager()
    {
	super();
	if (m_singletonInstance == null) m_singletonInstance = this;
    }

    //----------------------------------------------------------------------

    public String getCSLFile()
    {
	return m_prefs.get("CSL_FILE", "datalogger.xml");
    }

    //----------------------------------------------------------------------

     /** Used to build a an object of a specific component.
     *
     * @param vmMan {@link VariableManager} 
     */
    protected BaseComponent buildComponent(VariableManager vmMan)
    {
	return new DataLoggerComponent(vmMan);
    }

    //----------------------------------------------------------------------

    /**
     * Start the application.
     *
     * @param args array of commandline arguments passed to application
     */
    protected void startup(String[] args)
    {
	super.startup(args);
    }
}
