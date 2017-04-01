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
package nrs.core.base;

import nrs.core.comms.PortManager;
import nrs.core.message.Constants;
import nrs.util.ArgumentException;
import nrs.util.ArgumentFielder;

import java.util.logging.*;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.ArrayList;
import java.util.Iterator;

/** Class which all Java NRS component AppManagers can inherit from.
 *
 * @author Thomas French
 * @author Darren Smith
*/

public abstract class BaseAppManager implements ArgumentFielder
{
  /** The version number of the application. */
  public static final double VERSION = 0.0;

  /** Name of the application. */
  public static final String NAME = "";

  /** Port Manager */
  private PortManager m_portMan;

  /** Variable Manager */
  private VariableManager m_vMan;

  /** Application component. */
  private BaseComponent m_thisComponent;

  /** Inbound Pipeline for application. */
  private Pipeline m_inboundPipeline;

  /** RouteManager for application. */
  private RouteManager m_routeManager;

  /** Preferences of application. */
  protected Preferences m_prefs;

  /** Logger for application. */
  protected Logger nrsLogger;

  /** Default level for logger to output information. */
  protected final Level DEFAULT_LEVEL = Level.CONFIG;

  /** Objects that will accept command line arguments */
  protected java.util.List m_argumentFielders = new ArrayList();

  /** Outbound pipeline, for sending messages out of this component */
  private OutboundPipeline m_outPipeline;

  /** Singleton instance */
  private static BaseAppManager m_instance = null;
  //----------------------------------------------------------------------
  /**
   * Return the singleton instance of this class
   */
  public static BaseAppManager instance()
  {
    return m_instance;
  }

  /** Protected BaseAppManager constructor, which uses singleton pattern.*/
  protected BaseAppManager(){

    if (m_instance == null)
      {
        m_instance = this;
        //        System.out.println("Assigned to singleton");
      }

    configureLogging();
    initialisePreferences();

    // main modules
    m_vMan = new VariableManager();
    m_outPipeline = new OutboundPipeline();
    m_inboundPipeline = new Pipeline ();
    m_routeManager = new RouteManager();
    m_portMan = new PortManager();
    m_thisComponent = buildComponent(m_vMan);

    OutboundMessageCache outboundCache = new OutboundMessageCache();
    MessageIDStamp IDprovider = new MessageIDStamp (outboundCache);
    MessageStorage storage = new MessageStorage (outboundCache);
    BroadcastHandler bcastHandler =
      new BroadcastHandler(m_thisComponent.info());

    // configuration
    m_portMan.setOutboundPipeline(m_outPipeline);
    m_portMan.setDefaultInboundPipeline(m_inboundPipeline);

    m_thisComponent.setRouteManager(m_routeManager);
    m_thisComponent.setOutboundDest(m_outPipeline);
    m_thisComponent.setPortManager(m_portMan);

    m_outPipeline.setModules(m_portMan, IDprovider, storage);

    m_routeManager.setOutboundPipeline(m_outPipeline);
    m_routeManager.
      configureVariables(m_thisComponent
                         .getVariable(Constants.MessageTypes.ReplyRoute));

    Router router = new Router();
    router.setExternalDest(m_portMan);

    // wire up MessageProcessors on inbound pipeline
    m_inboundPipeline.setDest(bcastHandler);
    bcastHandler.setDest(router);
    bcastHandler.setBroadcastDest(m_portMan);
    router.setDest(storage);
    storage.setInboundDest(m_vMan);

    m_vMan.setDest(m_thisComponent);
    m_vMan.setOutboundPipeline(m_outPipeline);

    // add ArgumentFielders
    m_argumentFielders.add(this);
    m_argumentFielders.add(m_portMan);
  }

  private void initialisePreferences(){
    PackageLogger.log.fine ("Retrieving preferences for node="
                            + getClass ());
    m_prefs = Preferences.userNodeForPackage(this.getClass());
  }

  public PortManager getPortManager(){
    return m_portMan;
  }

  public RouteManager getRouteManager(){
    return m_routeManager;
  }

  public Pipeline getInboundPipeline(){
    return m_inboundPipeline;
  }

  public OutboundPipeline getOutboundPipeline()
  {
    return m_outPipeline;
  }

  public BaseComponent getComponent(){
    return m_thisComponent;
  }

  public Preferences getPreferences(){
    return m_prefs;
  }

  public VariableManager getVariableManager()
  {
    return m_vMan;
  }

  /** Virtual constructor used to build a an object of a specific component.
   * Abstract method to be implented by derived class.
   *
   * @param vmMan {@link VariableManager}
   */
  protected abstract BaseComponent buildComponent(VariableManager vmMan);

  /** Configure the NRS namespace logger, and also the root logger.  The
   * NRS logger is detached away from the root ("") logger - i.e., will
   * not use any root handlers. Handlers are added to the NRS namespace
   * logger with logging level set to Level.ALL. The NRS namespace
   * logger is also set to Level.ALL.
   */
  private void configureLogging()
  {
    // Get hold of the namespace logger for "nrs"
    nrsLogger = Logger.getLogger("nrs");

    // Remove any default handlers they may exist of the application logger
    Handler[]defaultHandlers = nrsLogger.getHandlers ();
    for (int i = 0; i < defaultHandlers.length; i++)
      {
        System.out.println("Removing default handler...");
        nrsLogger.removeHandler(defaultHandlers[i]);
      }

    // Don't want to use parent handlers. Will assume control in this
    // class.
    nrsLogger.setUseParentHandlers (false);

    nrsLogger.setLevel (DEFAULT_LEVEL);

    // Add a hanlder. Set the level to ALL, since we will control level
    // through the loggers and not through the handlers
    ConsoleHandler ch = new ConsoleHandler ();
    ch.setLevel (Level.ALL); //set level supplied by user, or default
    nrsLogger.addHandler (ch);
  }

  /** Set a specific level of logging output.
   *
   * @param level level to set logging output at.
   */
  private void setLogLevel(int level){
    Level l = Level.WARNING;
    // Set initial level of the application logger
    switch(level)
      {
        // config, info, warning and seveer
      case 0: l = Level.CONFIG; break;
        // fine, config, info, warning and severe
      case 1: l = Level.FINE; break;
        // finer, plus all above ones
      case 2: l = Level.FINER; break;

      }
    nrsLogger.setLevel (l); //set level supplied by user, or default
  }
  //---------------------------------------------------------------------
  public void displayHelp(){
    System.out.println("\t-h|--help\t\t\tproduces this help text and"
                       +" exits");
    System.out.println("\t-L|-# [0,1,2]\tset output debug level -\n"
                       + "\t\t 0 is none (default)\n"
                       + "\t\t 1 is debug info\n"
                       + "\t\t 2 is all output.");
  }
  //----------------------------------------------------------------------
  public java.util.List getOptions()
  {
    java.util.List options = new ArrayList();

    options.add("h");
    options.add("help");
    options.add("L");
    options.add("#");

    return options;
  }
  //----------------------------------------------------------------------
  public void processOption(String option, String [] args, int index)
    throws ArgumentException
  {
    if (option.equals("h") || option.equals("help"))
      {
        cmdlineHelp();
      }
    else if ( option.equals("L") || option.equals("#"))
      {
        setLogLevel(Integer.parseInt(args[index+1]));
        args[index+1] = null;
      }
  }
  //----------------------------------------------------------------------
  /**
   * Process each commandline argument, and dispatch it to each argument
   * fielder. This method has been copied verbatim from another nrs
   * program. Instead of this copy, this functionality should be
   * provided by the argument handlers: ie, we should just have to pass
   * each one the list of options, and they should be able to perform
   * all scanning etc. Make this change at some point. DJS June 2005
   *
   * _TODO_(Simplifiy this method - allow handlers to do this automatically)
   */
  protected void processArguments(String [] args) throws ArgumentException
  {
    int index = 0;
    ArrayList separatedArgs = new ArrayList();

    // Process options
    while ((index < args.length) && (args[index].startsWith("-")))
    {
      separatedArgs.clear();

      if (args[index].startsWith("--"))
      {
        if (args[index].length() > 2)
        {
          separatedArgs.add(args[index].substring(2));
        }
        else
        {
          throw new ArgumentException(args[index],
                                      index,
                                      ArgumentException.INVALID);
        }
      }
      else if (args[index].length() > 1)
      {
        for (int i = 1; i < args[index].length(); i++)
        {
          separatedArgs.add(Character.toString(args[index].charAt(i)));
        }
      }
      else
      {
        throw new ArgumentException(args[index],
                                    index,
                                    ArgumentException.INVALID);
      }

      for (Iterator j = separatedArgs.iterator(); j.hasNext(); )
      {
        String arg = (String) j.next();
        boolean handled = false;
        //PackageLogger.log.info("Processing argument: " + arg);

        for (Iterator k = m_argumentFielders.iterator(); k.hasNext(); )
        {
          ArgumentFielder fielder = (ArgumentFielder) k.next();

          if (fielder.getOptions().contains(arg))
          {
            fielder.processOption(arg, args, index);
            handled = true;
          }
        }

        if (!handled)
        {
          throw new ArgumentException(arg,index,
				      ArgumentException.UNEXPECTED);
        }
      }

      index++;

      // Skip used arguments
      while ((index < args.length) && (args[index] == null)) index++;
    }

    // Now process the left-overs. These are options that don't begin
    // with the - or -- prefix
    while (index < args.length)
    {
      processLeftover((String) args[index], args, index);
      index++;
    }
  }
  //----------------------------------------------------------------------
  /** Process arguments left over.
   * Can be overriden by subclasses to parse specific options.
   *
   * @param option current option
   * @param args list of arguments
   * @param index index into args array
   *
   */
  protected boolean processLeftover(String option, String [] args, int index)
  {
    return true;
  }

  //----------------------------------------------------------------------
  protected void cmdlineHelp()
  {
    System.out.println("Usage: " + NAME + "\n");
    System.out.println("Options");

    for (Iterator k = m_argumentFielders.iterator(); k.hasNext(); )
      {
        ArgumentFielder fielder = (ArgumentFielder) k.next();
        fielder.displayHelp();
      }

    System.exit(0);
  }
  //----------------------------------------------------------------------
  /**
   * Start the application.
   *
   * @param args array of commandline arguments passed to application
   */
  protected void startup(String[] args)
  {
    try
      {
        processArguments(args);
      }
    catch (ArgumentException e)
      {
        PackageLogger.log.severe("Bad command-line arguments. "
                                 + e.getMessage());
        System.exit(1);
      }
  }

  //----------------------------------------------------------------------
}
