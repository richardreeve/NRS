package nrs.nrsgui;

import java.util.HashMap;

/**
 * Mmanages the collection of session parameter {@link ConfigParameter}
 * that control various functions of the application, that is, the
 * application settings.
 *
 * <p>A parameter is composed of a name and value pair. The name can be
 * used throughout the application to refer to the setting (rather than
 * using a reference), although a reference can also be used. This name
 * is taken from the name field associated with the {@link
 * ConfigParameter} (via the {@link ConfigParameter#getName()}
 * method).
 *
 * <p>The storage used for this class uses a {@link HashMap}. So ensure that
 * the names of parameter objects are unique.
 *
 * @author Darren Smith
 */
class Settings
{
  private HashMap m_params = new HashMap();

  //----------------------------------------------------------------------
  /**
   * Register a parameter with this collection.
   *
   * @param cp the {@link ConfigParameter} to register. It will be
   * stored in this class according to its name.
   */
  void add(ConfigParameter cp)
  {
    m_params.put(cp.getName(), cp);
  }
  //----------------------------------------------------------------------
  /**
   * Find a parameter of the specified name, or return <tt>null</tt> if
   * no parameter with that name is found.
   */
  ConfigParameter get(String name)
  {
    return (ConfigParameter) m_params.get(name);
  }
  //----------------------------------------------------------------------
  /**
   * Remove the specified parameter object
   */
  void remove(ConfigParameter cp)
  {
    m_params.remove(cp);
  }
}
