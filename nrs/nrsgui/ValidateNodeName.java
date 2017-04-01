package nrs.nrsgui;

interface ValidateNodeName
{
  /**
   * Return <tt>true</tt> if the specified name is acceptable for the
   * name of a new node about to be created. Return <tt>false</tt>
   * otherwise.
   */
  boolean nodeNameAcceptable(String name);
}
