package nrs.nrsgui;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.toolboxes.AttributeEditor;
import nrs.toolboxes.Verifier;
import nrs.toolboxes.multitable.MultiTableBuilder;

/**
 * A utility class for using an {@link AttributeEditor} instance. This
 * class provides a very simple one-function access point to the {@link
 * AttributeEditor}. For each instance of this class created, a separate
 * instance of {@link AttributeEditor} is created and used internally.
 *
 * <p>The typical pattern of using this code is:
 *
 * <tt>
    <br><br>
    m_attrEditor = new AttributeEditorWrapper(this, "Attributes", this);<br>
    ArrayList attrs = m_attrEditor.runEditor(nType);<br>
    <br>
    if (attrs != null)<br>
    &nbsp;&nbsp;{<br>
    <br>
       &nbsp;&nbsp;&nbsp;&nbsp;// build the new node<br>
    <br>
    &nbsp;&nbsp;}<br>
    </tt>

 *
 * <p><b>Note<b>: An {@link AttributeEditor} can be used for entering
 * field values for a new component, or for editing the values of an
 * existing component. The principle difference is that the later does
 * now allow editing of the name field, and the former checks whether
 * the node name is unique when the user clicks OK. However, be careful
 * when using a single instance of {@link AttributeEditor} for both
 * these roles: there is likely to be unpredictable consequences due to
 * each role setting different values for internal switiches etc, and
 * the resulting behaviour is unpredictable. If both of these roles are
 * required, then provide separate instances.
 *
 */
public class AttributeEditorWrapper implements Verifier
{
  private AttributeEditor m_attrEditor;
  private MultiTableBuilder m_builder;
  private NodeAttribute m_nameAttribute;
  private ValidateNodeName m_nodeNameValidator;

  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param owner the window which owns the {@link AttributeEditor}
   * window
   *
   * @param title title of the {@link AttributeEditor} window
   *
   * @param nodeNameValidator the {@link ValidateNodeName} which will
   * validate the node name upon user clicking OK. Should be set to
   * <tt>null</tt> for {@link AttributeEditorWrapper} instances which
   * edit existing nodes rather than creating new.
   */
  AttributeEditorWrapper(Frame owner,
                         String title,
                         ValidateNodeName nodeNameValidator)
  {
    m_nodeNameValidator = nodeNameValidator;
    m_builder = new MultiTableBuilder();
    m_attrEditor = new AttributeEditor(owner,
                                       title,
                                       m_builder.getTable(),
                                       this);
    m_attrEditor.setModal(true);

    m_attrEditor.restoreSettings(AppManager.getInstance().getPreferences());
    AppManager.getInstance().persistWindowLater(m_attrEditor);
  }
  //----------------------------------------------------------------------
  /**
   * Configure and show the {@link AttributeEditor} window.
   *
   * @param nType the {@link CSL_Element_NodeDescription} which is going
   * to be created, and which the {@link AttributeEditor} window will
   * present the attributes of;
   *
   * @return an {@link ArrayList} of {@link NodeAttribute} objects which
   * represent the values entered by the user for the attributes of
   * <tt>nType</tt>; or <tt>null</tt> if the user cancelled the dialog box.
   */
  ArrayList runEditor(CSL_Element_NodeDescription nType)
  {
    // This will store the list of NodeAttribute objects, to be used
    // during the later construction of the node instance
    ArrayList attrList = new ArrayList();

    /* Add the attributes to be edited */
    m_builder.removeAll();
    for (Iterator iter = nType.getAttributesIterator();
         iter.hasNext(); )
    {
      CSL_Element_Attribute attr = (CSL_Element_Attribute) iter.next();
      NodeAttribute nodeAttr = new NodeAttribute(attr);

      // Identify the attribute which is the 'name' field.  This needs
      // to be done because it is the display-name which is added to the
      // actual table, and later this name will need to be checked. So
      // here we set up a reference to the node attribute which will
      // definately be storing the name of the node
      if (attr.getAttributeName()
          .equals(CSL_Element_Attribute.NODE_ATTRIBUTE_NAME))
      {
        m_nameAttribute = nodeAttr;
      }

      m_builder.addRow(buildRowKey(attr),
                       nodeAttr.getValueDelegate(),
                       !attr.getIsConst(),
                       nodeAttr);

      // This list is used later, when the actual node is constructed
      attrList.add(nodeAttr);
    }

    /* Now show the window */
    m_attrEditor.setVisible(true);

    // No further action necessary if user cancelled the attribute box
    if (m_attrEditor.getCloseState() == false)
    {
      return null;
    }
    else
    {
      return attrList;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NodeAttribute} which will is storing the value of
   * the 'name' attribute.
   */
  NodeAttribute getNameAttribute()
  {
    return m_nameAttribute;
  }
  //----------------------------------------------------------------------
  /**
   * Configure and show the {@link AttributeEditor} window to edit the
   * attribute values of an existing node.
   *
   * @param node the {@link Node} which is going to be created, and
   * which the {@link AttributeEditor} window will present the
   * attributes of;
   *
   * @return an {@link ArrayList} of {@link NodeAttribute} objects which
   * represent the values entered by the user for the attributes of
   * <tt>nType</tt>; or <tt>null</tt> if the user cancelled the dialog box.
   */
  ArrayList runEditor(Node node)
  {
    m_nameAttribute = null;

    ArrayList attrList = new ArrayList();

    /* Add the attributes to be edited */
    m_builder.removeAll();
    for (Iterator iter = node.attributeIterator(); iter.hasNext(); )
    {
      NodeAttribute na = new NodeAttribute((NodeAttribute) iter.next());
      attrList.add(na);

      boolean constValue;
      if ((na.getCSL().getInNRSNamespace())
          && (na.getCSL().getAttributeName().equals(CSL_Element_Attribute.NODE_ATTRIBUTE_NAME) ))
      {
        constValue = false;
      }
      else
      {
        constValue = !na.getCSL().getIsConst();
      }


      m_builder.addRow(buildRowKey(na.getCSL()),
                       na.getValueDelegate(),
                       constValue,
                       na);

    }

    /* Now show the window */
    m_attrEditor.setVisible(true);

    // No further action necessary if user cancelled the attribute box
    if (m_attrEditor.getCloseState() == false)
    {
      return null;
    }
    else
    {
      return attrList;
    }
  }
  //----------------------------------------------------------------------
  private String buildRowKey(CSL_Element_Attribute attr)
  {
    StringBuffer rowKey = new StringBuffer(attr.toString());

    if ((attr.getResolvedUnit() != null)
        && (attr.getResolvedUnit().getFloatInfo() != null) &&
        (attr.getResolvedUnit().getFloatInfo().getFloatInfoAbbrev() != null))
    {
      rowKey.append(" (");

      if (attr.getResolvedUnit().getFloatInfo().getFloatInfoScale() != null)
      {
        rowKey.append(attr.getResolvedUnit().getFloatInfo().
                      getFloatInfoScale());
      }

      rowKey.append(attr.getResolvedUnit().getFloatInfo().
                    getFloatInfoAbbrev());

      rowKey.append(")");
    }
    return rowKey.toString();
  }
  //----------------------------------------------------------------------
  /**
   * Return the underlying {@link AttributeEditor} component.
   */
  AttributeEditor getComponent()
  {
    return m_attrEditor;
  }
  //----------------------------------------------------------------------
  // Implements Verifier interface
  public boolean isOkay(JTable table)
  {
    if (
        (m_nameAttribute != null) &&
        (m_nodeNameValidator != null)
        )
    {
      if (m_nodeNameValidator.
          nodeNameAcceptable(m_nameAttribute.getValue().toString()))
      {
        return true;
      }
      else
      {
        JOptionPane.showMessageDialog(m_attrEditor,
                                      "Node called "
                                      + m_nameAttribute.getValue().toString()
                                      + " already exists",
                                      "Node error",
                                      JOptionPane.WARNING_MESSAGE);
        return false;
      }
    }

    return true;
  }
}
