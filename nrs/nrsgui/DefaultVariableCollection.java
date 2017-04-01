package nrs.nrsgui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Element_Variable;

/*
 * Basic abilities of a node instance
 */
class DefaultVariableCollection implements VariableCollection
{
  /** Reference to the list of variables. Each element in the list is of
   * type {@link NodeVariable}. */
  protected final Collection<NodeVariable> m_vars;

  /** Node which owns this list of variables */
  protected Node m_parent;

  //----------------------------------------------------------------------
  /**
   * Constructor. Creates and stores a series of {@link NodeVariable}
   * objects based on the variable descriptions contained within the
   * type ({@link CSL_Element_NodeDescription}) of the
   * <code>parent</code>.
   */
  DefaultVariableCollection(Node parent)
  {
    m_parent = parent;
    m_vars = new ArrayList();

    int initX = GuiVariable.INITIAL_X;
    int initY = GuiVariable.INITIAL_Y;
    Rectangle r = new Rectangle(0,0,0,0);

    CSL_Element_NodeDescription nodeType = parent.getType();
    for (Iterator i = nodeType.getVariablesIterator(); i.hasNext(); )
    {
      NodeVariable nv = new NodeVariable((CSL_Element_Variable) i.next(),
                                  initX, initY, parent);
      m_vars.add(nv);

      initY += GuiVariable.VERT_SEPARATION
        + nv.getPainter().bounds(r).height;
    }


    // TODO - is it really necessary to create an individual
    // object... or can we share to reduce the number of objects (ie
    // don't want to have one for each node in existence
    //    m_dd = new GuiVariableCollection(this);
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link VariableCollection} interface
   */
  public Node getParent()
  {
    return m_parent;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link VariableCollection} interface
   */
  public Iterator variableIterator()
  {
    return m_vars.iterator();
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link VariableCollection} interface
   */
  public Iterator<NodeVariable> iterator()
  {
    return m_vars.iterator();
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link VariableCollection} interface
   */
  public int size()
  {
    return m_vars.size();
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link VariableCollection} interface
   */
  public void add(NodeVariable nv)
  {
    if ((nv == null) || (m_vars.contains(nv))) return;

    m_vars.add(nv);
  }
  //----------------------------------------------------------------------
  public NodeVariable resolveVNName(String vnName)
  {
    for (Iterator i = m_vars.iterator(); i.hasNext(); )
    {
      NodeVariable nv = (NodeVariable) i.next();

      if (nv.name().equals(vnName)) return nv;
    }

    PackageLogger.log.warning("VNName resolution failure: no variable named "
                              + vnName + " found in collection belonging to "
                              + getParent());
    return null;
  }
}
