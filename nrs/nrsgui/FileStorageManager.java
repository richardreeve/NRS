package nrs.nrsgui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nrs.util.ArgumentException;
import nrs.util.ArgumentFielder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.swing.JFrame;

/**
 * Manages the processes of exporting and inporting data files which
 * represent networks of nodes.
 *
 * <p>Just like the other managers, only a single instance of this
 * should be needed.
 */
class FileStorageManager implements ArgumentFielder
{
  private ParcelXMLExporter m_exporter;
  private Transformer m_transformer;

  static final String NETWORK_EXT = "net";

  private static final String OPT_INDENTXML = "indentxml";

  private JFrame m_gui;

  //----------------------------------------------------------------------
  /**
   * Constructor. Will attempt to create the XML exporter resource
   * ({@link ParcelXMLExporter}). If this fails an error message will be
   * logged, and networks will not be able to be saved.
   *
   * @param rootFrame a {@link JFrame} onto which message dialogs will
   * appear (can be null).
   */
  FileStorageManager(JFrame rootFrame)
  {
    m_gui = rootFrame;
    try
    {
      m_exporter = new ParcelXMLExporter();
    }
    catch (FactoryConfigurationError e)
    {
      PackageLogger.log.severe("No XML implementation available; "
                               + "will not be able to save; "
                               + e);
      e.printStackTrace();
    }
    catch (ParserConfigurationException e)
    {
      PackageLogger.log.severe("No XML document builder available; "
                               + "will not be able to save; "
                               + e);
      e.printStackTrace();
    }

    try
    {
      m_transformer = TransformerFactory.newInstance().newTransformer();
      m_transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    }
    catch (TransformerFactoryConfigurationError e)
    {
      PackageLogger.log.severe("Failed to construct XML transformer; "
                               + "will not be able to save; "
                               + e);
      e.printStackTrace();
    }
    catch (TransformerConfigurationException e)
    {
      PackageLogger.log.severe("Failed to construct XML transformer; "
                               + "will not be able to save; "
                               + e);
      e.printStackTrace();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Set root pane used for showing message boxes
   */
  void setGUI(JFrame rootFrame)
  {
    m_gui = rootFrame;
  }
  //----------------------------------------------------------------------
  /**
   * Returns whether {@link NetworkParcel} instances can be saved.
   *
   * @return <tt>true</tt> if they can, else <tt>false</tt>
   */
  boolean canSave()
  {
    return ((m_exporter != null) && (m_transformer != null));
  }
  //---------------------------------------------------------------------
  /**
   * Saves a network to a file on disk.
   *
   * @param nlist a {@link List} containing the {@link nrs.nrsgui.Node}
   * object to save to file.
   *
   * @param filename the name of the file to which the XML
   * representation will be saved
   */
  void saveNetwork(List nlist, String filename)
  {
    saveNetwork(nlist, new File(filename));
  }
  //----------------------------------------------------------------------
  /**
   * Saves a network to a file on disk. The network's filename attribute
   * will be used for the destination file.
   */
  void saveNetwork(Network network)
  {
    saveNetwork(network, new File(network.getFilename()));
  }
  //---------------------------------------------------------------------
  /**
   * Saves a network to a file on disk.
   *
   * @param network the {@link Network} to save
   *
   * @param file the {@link File} to which the XML representation will
   * be saved
   */
  void saveNetwork(Network network, File file)
  {
    if (!canSave())
    {
      PackageLogger.log.warning("Save not possible. Check log for earlier"
                                + " error messages for reason.");
    }
    try
    {
      NetworkParcel p = new NetworkParcel(AppManager.VERSION,
                                          AppManager.NAME,
                                          network);

      FileWriter target = new FileWriter(file);
      Document   source = m_exporter.export(p);

      transformXML(source, target);

      PackageLogger.log.fine("File save completed: " + file.getAbsoluteFile());
    }
    catch (DOMException e)
    {
      PackageLogger.log.warning("Failed to save; could not form XML "
                                +"document; "+ e);
      e.printStackTrace();
    }
    catch (IOException e)
    {
      PackageLogger.log.warning("Failed to save; " + e);
      e.printStackTrace();
    }
    catch (Exception e)
    {
      PackageLogger.log.warning("Failed to save; " + e);
      e.printStackTrace();
    }
  }
  //---------------------------------------------------------------------
  /**
   * Saves a network to a file on disk.
   *
   * @param nlist a {@link List} containing the {@link nrs.nrsgui.Node}
   * object to save to file.
   *
   * @param file the {@link File} to which the XML representation will
   * be saved
   */
  void saveNetwork(List nlist, File file)
  {
    if (!canSave())
    {
      PackageLogger.log.warning("Save not possible. Check log for earlier"
                                + " error messages for reason.");
    }
    try
    {
      NetworkParcel p = new NetworkParcel(AppManager.VERSION,
                                          AppManager.NAME,
                                          nlist);

      FileWriter target = new FileWriter(file);
      Document   source = m_exporter.export(p);

      transformXML(source, target);

      PackageLogger.log.fine("File save completed:" + file.getAbsoluteFile());
    }
    catch (DOMException e)
    {
      PackageLogger.log.warning("Failed to save; could not form XML "
                                +"document; "+ e);
      e.printStackTrace();
    }
    catch (IOException e)
    {
      PackageLogger.log.warning("Failed to save; " + e);
      e.printStackTrace();
    }
    catch (Exception e)
    {
      PackageLogger.log.warning("Failed to save; " + e);
      e.printStackTrace();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Load a network from the specified file
   *
   * _TODO_(this should throw exceptions!)
   *
   * @param file the {@link File} to load
   *
   * @param superNetwork the {@link Network} into which the loaded
   * network will be placed. Can be set to null, in which a case a new
   * network is created and returned.
   *
   * @param isNew set to <tt>true</tt> if <tt>superNetwork</tt> has just
   * been created and so should take summary information from the first
   * network found in the XML document.
   *
   */
  Network loadNetwork(File file, Network superNetwork, boolean isNew)
  {
    //    return m_parser.parseXML(file);

    try
    {
      NetworkXMLLoader loader = new NetworkXMLLoader(m_gui);
      superNetwork = loader.parse(file, superNetwork, isNew);
      return superNetwork;
    }
    catch (Throwable e)
    {
      PackageLogger.log.warning(e.toString());
      e.printStackTrace();
      return null;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Transform an XML representation into a purely textual
   * representation that is suitable for writing to file.
   */
  private void transformXML(org.w3c.dom.Node node, Writer writer)
    throws TransformerException
  {
    m_transformer.transform(new DOMSource(node), new StreamResult(writer));
  }
  //----------------------------------------------------------------------
  /**
   * Utility for appending a file extension onto a filename which does
   * not have any extension. No extension is appended if the filename
   * already has an extension.
   *
   * @param name the last name in the pathname's name sequence
   * @param pathname the full pathname to the file
   * @param extension the desired extension to add
   *
   * @return the new pathname if an extension has been added; otherwise
   * <tt>pathname</tt> is returned
   */
  static String addExtension(String name,
                             String pathname,
                             String extension)
  {
    int i = name.lastIndexOf('.');

    if (i > 0 && i < name.length() - 1)
    {
      return pathname;
    }
    else
    {
      return new String(pathname + "." + extension);
    }
  }
  //----------------------------------------------------------------------
  public void displayHelp()
  {
    // Java 1.5
    System.out.printf("\t--%s\t\t\tgenerate indented XML files\n",
                      OPT_INDENTXML);
  }
  //----------------------------------------------------------------------
  public List getOptions()
  {
    java.util.List options = new ArrayList();

    options.add(OPT_INDENTXML);

    return options;
  }
  //----------------------------------------------------------------------
  public void processOption(String option,
                            String [] args,
                            int index) throws ArgumentException
  {
    if (option.equals(OPT_INDENTXML))
    {
      m_transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    }
  }
}
