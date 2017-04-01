package nrs.nrsgui;

import java.util.Iterator;

/**
 * Utility class for exporting a network to PML format
 */
class PML_Exporter
{
  private StringBuffer m_buf = new StringBuffer();

  /*
   *
   */
  void clear()
  {
    m_buf.delete(0, m_buf.length());
  }

  /*
   *
   */
  void exportNode(Node n)
  {
    m_buf.append("<CreateNode ");
    appendNameValue("to", "sim");
    appendNameValue("nodeType", n.getType().getNodeDescriptionName());

    for (Iterator i = n.attributeIterator(); i.hasNext(); )
    {
      NodeAttribute na = (NodeAttribute) i.next();

      appendNameValue(na.getCSL().getAttributeName(),
                      na.getValue().toString());
    }

    m_buf.append(" />");
  }

  /*
   * Do I really need this one?
   */
  void exportNodeCollection(NodeCollection nc)
  {
    for (Iterator i = nc.nodeIterator(); i.hasNext(); )
    {
      exportNode((Node) i.next());
    }
  }

  /*
   *
   */
  private void appendNameValue(String name, String value)
  {
    m_buf.append(name);
    m_buf.append("='");
    m_buf.append(value);
    m_buf.append("' ");
  }

  /*
   *
   */
  public String toString()
  {
    return m_buf.toString();
  }

}
