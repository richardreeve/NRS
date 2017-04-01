package nrs.nrsgui;

/**
 * Since Java does not provide for C++ style referenecs, use this
 * delegate class to achieve an equivalent effect
 */
public interface AccessDelegate
{
  /**
   * Allow a value to be assigned
   */
  void set(Object value);

  /**
   * Allow a value to be retrieved
   */
  Object get();
}
