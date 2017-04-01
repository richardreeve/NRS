package nrs.nrsgui;

/**
 * Represents a configuration parameter that has a boolean value.
 *
 * @author Darren Smith
 */
class ConfigParameterBoolean extends ConfigParameter
{
  private Boolean m_value;

  //----------------------------------------------------------------------
  /**
   * Consturctor - provide the name of parameter, and its initial value
   */
  ConfigParameterBoolean(String name,
                         Boolean value)
  {
    super(name);
    m_value = value;
  }
  //----------------------------------------------------------------------
  /**
   * Consturctor - provide the name of parameter, and its initial value
   */
  ConfigParameterBoolean(String name,
                         boolean value)
  {
    this(name, Boolean.valueOf(value));
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Boolean} value of this parameter.
   */
  Object getValue()
  {
    return m_value;
  }
  //----------------------------------------------------------------------
  /**
   * Set the value of this parameter, which should be of type {@link
   * Boolean}
   */
  void setValue(Object value)
  {
    if (value instanceof Boolean)
    {
      m_value = (Boolean) value;
    }
    else throw new IllegalArgumentException("expected Boolean parameter,"
                                            + " but received "
                                            + value.getClass().getName());
  }
  //----------------------------------------------------------------------
  /**
   * Set the value to Boolean.TRUE, if currently false
   */
  void enable()
  {
    if (!value())
    {
      m_value = Boolean.TRUE;
      broadcastChange();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Set the value to Boolean.FALSE, if currently true
   */
  void disable()
  {
    if (value())
    {
      m_value = Boolean.FALSE;
      broadcastChange();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Toggle the state of the boolean value
   */
  void toggle()
  {
    if (value())
    {
      disable();
    }
    else
    {
      enable();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return the value as a boolean primitive
   */
  boolean value()
  {
    return m_value.booleanValue();
  }
  //----------------------------------------------------------------------
  /**
   * Utililty for checking the hasChanged() value and calling
   * notifyObservers(this) upon change. Derived classes might like to
   * provide their own implementations of hasChanged(), although it is
   * not necessary.
   */
  void notifyOnChange()
  {
    if (hasChanged()) notifyObservers(this);
  }
}
