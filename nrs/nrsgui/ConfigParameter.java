package nrs.nrsgui;

import java.util.Observable;

/**
 * Base class for objects which serve as variables for indicate various
 * application configuration settings and variables.
 *
 * <p>This class extends {@link Observable}, so other parts of the
 * application can be notified upon value change.
 *
 * @author Darren Smith
 */
abstract class ConfigParameter extends Observable
{
  /**
   * Name of this parameter (eg, "Online / Offline state").
   */
  private String m_name;

  //----------------------------------------------------------------------
  /**
   * Consturctor - proviude the name of parameter
   *
   * @param name the name of this parameter. Make sure this is unique,
   * since this is how it can be refered to by other parts of the program
   */
  ConfigParameter(String name)
  {
    m_name = name;
  }
  //----------------------------------------------------------------------
  /**
   * Return value of this parameter
   */
  abstract Object getValue();
  //----------------------------------------------------------------------
  /**
   * Set the value of this parameter
   */
  abstract void setValue(Object value);
  //----------------------------------------------------------------------
  /**
   * Utililty calling hasChanged() and then notifyObservers(getValue()).
   */
  void broadcastChange()
  {
    setChanged();
    notifyObservers(getValue());
  }
  //----------------------------------------------------------------------
  String getName()
  {
    return m_name;
  }
}
