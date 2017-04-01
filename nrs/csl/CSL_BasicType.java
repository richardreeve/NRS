package nrs.csl;

public abstract class CSL_BasicType
{
  /**
   * Return a static {@link Object} which is most suited to representing
   * values for a {@link CSL_BasicType} instance. Since the returned
   * object is static, it should not be used for storing any runtime
   * content. For objects for that purpose, use the {@link
   * #newJavaType()} method.
   *
   * TODO - not clear to me what this method does. If I require a type,
   * then why not just return a Class object?
   */
  abstract public Object getJavaType();

  /**
   * Return a new object which is most suited to representing values for
   * a {@link CSL_BasicType} instance. The returned object is newly
   * constructed, as so is suitable for storing runtime content.
   *
   * TODO - there is problem here. The types return are all immutable -
   * so they can't be used to store values.
   */
  abstract public Object newJavaType();

  /**
   * Parse the string and return an object representation. The returned
   * object is one of the Java primitive wrappers.
   */
  abstract public Object toJavaType(String value);
}
