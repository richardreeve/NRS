package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_NodeDescription;

/**
 * The entire heirarhcy of simulated nodes, as contructed by the user
 * with the gui, are all ultimately contained within a {@link RootNode}
 * instance, which represents the base of a network hierarchy. For
 * example, the first set of nodes contained immediately in the root
 * network represent NRS components.
 *
 * @deprecated Not sure if I will be continuing with this class. Instead
 * of having a RootNode, or other special class to indicate the lowest
 * layer of a network structure, why can't I just use a
 * DefaultNodeCollection? I am current following that option.
 */
public class RootNode implements Node
{
  /** Reference to the type of this node */
  protected final CSL_Element_NodeDescription m_type = null;

  /** List of the attributes. Each element is of type {@link
   * NodeAttribute} */
  protected final Collection m_attrs;

  /** Name of the node */
  protected String m_name = "Root";

  /** Object used for display */
  protected DisplayDelegate m_dd;

  /** Internal CSL-style variables */
  protected VariableCollection m_variables;

  /** Child nodes */
  protected NodeCollection m_children = new DefaultNodeCollection(this);

  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param attrs a {@link Collection} containing a set of {@link
   * NodeAttribute} objects which provide the initial set of attribute
   * values
   */
  RootNode(Collection attrs)
  {
    m_attrs = attrs;

    m_dd = null; // new GuiNode(this, 10, 10);

    //    m_variables = new DefaultVariableCollection(this);
    m_variables = new ArrayList();
  }
  //----------------------------------------------------------------------
  /** Constructor */
  RootNode()
  {
    this(null);
  }
  //----------------------------------------------------------------------
  /**
   * Implements {@link Node} interface
   */
  public String toString()
  {
    return m_name;
  }
  //----------------------------------------------------------------------
  /**
   * Currently returns <tt>null</tt>.
   */
  public CSL_Element_NodeDescription getType()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  /**
   * Implements {@link Node} interface
   */
  public Iterator attributeIterator()
  {
    return m_attrs.iterator();
  }
  //----------------------------------------------------------------------
  /**
   * Implements {@link Node} interface
   */
  public DisplayDelegate getPainter()
  {
    return m_dd;
  }
  //----------------------------------------------------------------------
  /**
   * Implements {@link Node} interface
   */
  public NodeCollection getChildNodes()
  {
    return m_children;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link Node} interface
   */
  public VariableCollection getVariables()
  {
    return m_variables;
  }
}
