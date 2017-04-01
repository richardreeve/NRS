// $Id: LogWindowHandler.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.toolboxes;

import java.util.logging.*;

public class LogWindowHandler extends Handler
{
  /** Window where log output will be placed. */
  LogWindow m_window;

  //----------------------------------------------------------------------
  /** Specialised constructor. Takes a handle to the GUI window where
   * this handler will direct output. */
  LogWindowHandler(LogWindow w)
  {
    m_window = w;
  }

  //----------------------------------------------------------------------
  /** Inherited. */
  public void close() 
  {
  }
  //----------------------------------------------------------------------
  public void flush() 
  {
  }
  //----------------------------------------------------------------------
  public void publish(LogRecord record)
  {
    if ((getFilter() != null) && (!getFilter().isLoggable(record))) return;
   
    if (m_window != null) m_window.publish(record);
  }
}
