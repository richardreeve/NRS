package nrs.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The base class for the XML loader and exporter classes. This base
 * class provides access to static XML objects, that instances of the
 * dervided classes can both use.
 */
public abstract class SharedXMLResources
{
  private static DocumentBuilder m_docBuilder;
  private static DocumentBuilderFactory m_docBuilderFactory;

  //----------------------------------------------------------------------
  public SharedXMLResources() throws FactoryConfigurationError,
                              ParserConfigurationException
  {
    m_docBuilderFactory = DocumentBuilderFactory.newInstance();
    m_docBuilderFactory.setNamespaceAware(true);

    m_docBuilder = m_docBuilderFactory.newDocumentBuilder();
  }
  //----------------------------------------------------------------------
  public DocumentBuilder builder()
  {
    return m_docBuilder;
  }

}
