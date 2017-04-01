package nrs.csl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a single Node containment rule. A containment rule
 * specifies a single or set of nodes another node can contain, together
 * with an upper and lower bound for the permitted number of
 * instances. Containment rules are worked out from the {@link
 * CSL_Element_Contains} specified in individual {@link
 * CSL_Element_NodeDescription} instances.
 *
 * <p>Each {@link ContainmentRule} instance is characterised by the
 * following attributes :
 *
 * <p>First, a set of node {@link CSL_Element_NodeDescription}
 * types. The rule will specify that these node types can be contained
 * in some way.
 *
 * <p>Second and third, the lower and upper bounds for the total
 * instances drawn from the permitted set of node types.
 *
 * <p>Finally, for the cases in which the rule specifies a group of
 * nodes, the name and display name of that group is held.
 *
 * @author Darren Smith
 */
public class ContainmentRule
{
  /**
   * List of nodes that this rule applies to. Each item in the list of
   * type {@link CSL_Element_NodeDescription}.
   */
  private ArrayList m_permitted = new ArrayList();

  /**
   * Minimum number of node instances
   */
  private int m_min;

  /**
   * Maximum number of node instance, or -1 for unbounded
   */
  private int m_max;

  /**
   * Indicate the name of this group, if available, or is null if this
   * is not a group
   */
  private String m_groupName;

  /**
   * Indicate the display-name of this group, if available, or is null
   * if this is not a group
   */
  private String m_groupDisplayName;

  static public final int UNBOUNDED = -1;

  //----------------------------------------------------------------------
  /** Constructor. Creates a rule to specify that a number of node
   * instances, limited by the min and max numbers, can be created from
   * the <tt>set</tt> of {@link CSL_Element_NodeDescription} specified.
   *
   * @param set a {@link ArrayList} of permitted {@link
   * CSL_Element_NodeDescription} instances
   *
   * @param min minimum number of instance permitted
   *
   * @param max maximum number of instance permitted
   *
   * @param groupName the name of the group in which these nodes reside
   *
   * @param groupDisplayName a display name verion of the group name
   */
  public ContainmentRule(ArrayList set,
                         int min,
                         int max,
                         String groupName,
                         String groupDisplayName)
  {
    m_permitted = set;
    m_min = min;
    m_max = max;
    m_groupName = groupName;
    m_groupDisplayName = groupDisplayName;
  }

  //----------------------------------------------------------------------
  /** Constructor. Creates a rule to specify that the single element can
   * be used between a minimum and maximum limit. */
  public ContainmentRule(CSL_Element_NodeDescription node, int min, int max)
  {
    m_permitted.add(node);
    m_min = min;
    m_max = max;
  }
  //----------------------------------------------------------------------
  /**
   * Add the specified node type to the list of node types permitted by
   * this rule.
   */
  public void addPermittedNode(CSL_Element_NodeDescription n)
  {
    if (m_permitted.contains(n)) return;

    m_permitted.add(n);
  }
  //----------------------------------------------------------------------
  /**
   * Get the {@link List} of {@link CSL_Element_NodeDescription}
   * types that this rule permits.
   */
  public List getPermittedList()
  {
    return m_permitted;
  }
  //----------------------------------------------------------------------
  /**
   * Return the lower-limit of instances of nodes drawn from the
   * permitted set.
   */
  public int getLower()
  {
    return m_min;
  }
  //----------------------------------------------------------------------
  /**
   * Return the upper-limit of instances of nodes drawn from the
   * permitted set. Can return -1 to indicate there is no upper-limit on
   * the number of node instances allowed.
   */
  public int getUpper()
  {
    return m_max;
  }
  //----------------------------------------------------------------------
  /**
   * Return the name of the node group associated with this containment
   * rule, or <tt>null</tt> if this rule is not associated with a group
   * but instead is associated with just a single node.
   */
  public String getGroupName()
  {
    return m_groupName;
  }
  //----------------------------------------------------------------------
  /**
   * Return the display-name of the node group associated with this
   * containment rule, or <tt>null</tt> if this rule is not associated
   * with a group.
   */
  public String getGroupDisplayName()
  {
    return m_groupDisplayName;
  }
  //----------------------------------------------------------------------
  /**
   * Return a string representation of this rule. The returned string
   * contains the list of nodes permitted by this rule, followed by a
   * pair of numbers indicating lower and upper restrictions.
   */
  public String toString()
  {
    StringBuffer retVal = new StringBuffer();

    for (Iterator i = m_permitted.iterator(); i.hasNext(); )
    {
      retVal.append(i.next().toString());
      if (i.hasNext()) retVal.append(", ");
    }

    retVal.append(", min=" + m_min);
    retVal.append(", max=" + m_max);

    return retVal.toString();
  }
}
