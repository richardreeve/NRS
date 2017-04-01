package nrs.nrsgui;

import java.util.Iterator;
import nrs.csl.CSL_BasicType;
import nrs.csl.CSL_Element_Attribute;

/**
 * A wrapper for a single {@link CSL_Element_Attribute} object and an
 * associated value
 *
 * @author Darren Smith
 */
public class NodeAttribute
{
  /** The attribute definiton */
  private final CSL_Element_Attribute m_attribute;

  /** Value associated with the attribute. Will be one of the primitive
   * wrapper types. */
  private Object m_value;

  /** A reference to the basic type of the value of this attribute. So
   * this can be used to work out the actual type of the value to store
   * in instances of this class. */
  private CSL_BasicType m_type;

  /** A delegate for use by clients, to set, and also read, the m_value */
  private AccessDelegate m_valueDelegate;

  //----------------------------------------------------------------------
  /**
   * Constructor. Create a new object, and attempt to set the initial
   * value based on any default value settings available in the {@link
   * CSL_Element_Attribute} definition.
   */
  public NodeAttribute(CSL_Element_Attribute attribute)
  {
    m_attribute = attribute;

    // this creates a new object, of an appropriate kind, for storing
    // the value of the attribute
    //m_value = attribute.getResolvedUnit().getBasicType().newJavaType();

    m_type = attribute.getResolvedUnit().getBasicType();

    // anonymous class
    m_valueDelegate = new AccessDelegate()
      {
        public void set(Object value) { m_value = value; }
        public Object get() { return m_value; }
      };

    m_value = m_type.toJavaType(attribute.getDefaultValue());
  }
  //----------------------------------------------------------------------
  /**
   * Copy constructor. Create a new object, and attempt to set the
   * initial value based by taking a value from the provided {@link
   * NodeAttribute}.
   */
  public NodeAttribute(NodeAttribute na)
  {
    m_attribute = na.m_attribute;
    m_type = m_attribute.getResolvedUnit().getBasicType();
    m_value = na.m_value;

    m_valueDelegate = new AccessDelegate()
      {
        public void set(Object value) { m_value = value; }
        public Object get() { return m_value; }
      };
  }
  //----------------------------------------------------------------------
  /**
   * Retrieve the underlying {@link CSL_Element_Attribute}
   */
  public CSL_Element_Attribute getCSL()
  {
    return m_attribute;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Object} holding the value of this attribute
   */
  public Object getValue()
  {
    return m_value;
  }
  //----------------------------------------------------------------------
  /**
   * Return an {@link AccessDelegate} for reading and writing to the
   * value of this attribute
   */
  public AccessDelegate getValueDelegate()
  {
    return m_valueDelegate;
  }
  //----------------------------------------------------------------------
  /**
   * Provide a useful description of the node attribute. This has been
   * designed for compatibility with {@link nrs.toolboxes.MultiTable}
   * client objects.
   */
  public String toString()
  {
    return m_attribute.getDesc().getDescription();
  }
}
