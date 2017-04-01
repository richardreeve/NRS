/*
 * Copyright (C) 2004 Edinburgh University
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.core.base.message;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import nrs.core.base.Message;
import nrs.core.message.Constants;
import nrs.util.SharedXMLResources;

/** Convert a {@link Message} object into an XML {@link Document} object.
 * Also provides methods to write {@link Document} to files.
 *
 * @author Thomas French
*/

public class MessageToXML extends SharedXMLResources
{
  private DOMImplementation m_docBuilder;
  
  private Transformer m_transformer;

  private static MessageToXML m_singleton;
  
  /** Singleton pattern. 
   *
   * @return instance of MessageToXML class
   */
  public static MessageToXML getInstance()
  {
    if ( m_singleton == null ){
      try{
        new MessageToXML();
      }
      catch(ParserConfigurationException pce){
        PackageLogger.log.warning(pce.getMessage());
        m_singleton = null;
      }
    }
    return m_singleton;
  }
  
  /** Private constructor used by singleton pattern. 
   * Called to create instance of MessageToXML and configure docBuilder 
   * and Transformer instance.
  */
  private MessageToXML() throws ParserConfigurationException{
    super();

    if ( m_singleton == null ) m_singleton = this;
    
    m_docBuilder = builder().getDOMImplementation();

    try
      {
        m_transformer = TransformerFactory.
          newInstance().newTransformer();
        m_transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        m_transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      }
    catch (TransformerFactoryConfigurationError e)
      {
        PackageLogger.log.severe("Failed to construct XML "
                                 + "transformer; "
                                 + "will not be able to save; "
                                 + e);
        e.printStackTrace();
      }
    catch (TransformerConfigurationException e)
      {
        PackageLogger.log.severe("Failed to construct XML "
                                 + "transformer; "
                                 + "will not be able to save; "
                                 + e);
        e.printStackTrace();
      }
  }
  
  /** Convert a {@link Message} object into an XML {@link Document} object.
   *
   * @param msg Message message to convert
   * @return XML Document
   */
  public Document convert(Message msg)
  {
    Document d = null;
    try
      {
        d = newDocument(msg.getType());
        
        addAttributes(d, msg.getNRSFields());
	
        addAttributes(d, msg.getFields());
      }
    catch(DOMException e){
      PackageLogger.log.warning(e.getMessage());
      e.printStackTrace();
      return null;
    }

    return d;
  }
  
  /** Convert a {@link Message} object into an XML {@link Document} object
   * and transform the XML into a text format and write it out to an 
   * {@link OutputStream}.
   *
   * @param msg Message message to convert
   */
  public void convert(Message msg, OutputStream target)
  {
    try
      {
        Document d = newDocument(msg.getType());

        addAttributes(d, msg.getNRSFields());

        addAttributes(d, msg.getFields());

        // transform and pipe out of Outputstream
        export(d, target);
      }
    catch(DOMException e){
      PackageLogger.log.warning(e.getMessage());
      e.printStackTrace();
    }
    catch(TransformerException e){
      PackageLogger.log.warning(e.getMessage());
      e.printStackTrace();
    }
  }

  /** Convert a {@link Message} object into an XML {@link Document} object
   * and transform the XML into a text format and write it out using a  
   * {@link FileWriter}.
   *
   * @param msg Message message to convert
   */
  public void convert(Message msg, FileWriter target)
  {
    try
      {
        Document d = newDocument(msg.getType());
        
        addAttributes(d, msg.getNRSFields());

        addAttributes(d, msg.getFields());

        // transform and write out using FileWriter
        export(d, target);
      }
    catch(DOMException e){
      PackageLogger.log.warning(e.getMessage());
      e.printStackTrace();
    }
    catch(TransformerException e){
      PackageLogger.log.warning(e.getMessage());
      e.printStackTrace();
    } 
  }

  //--------------------------------------------------------------------
  /** Add {@link Hashmap} of message fields to XML {@link Document}. 
   *
   * @param d Document to add attributes too.
   * @param h HashMap of fields to add
  */
  private void addAttributes(Document d, HashMap h)
  {
    Iterator keys = h.keySet().iterator();
    Iterator values = h.values().iterator();
    
    while (keys.hasNext())
    {
      Object name = keys.next();
      Object value = values.next();

      if (value == null)
      {
        PackageLogger.log.warning("Field " + name + " has null value;"
                                  +" providing an empty string");
        value = new String();
      }
      if ( name.toString().startsWith("nrsa:") )
        d.getDocumentElement().setAttributeNS(Constants.Namespace.NRSA,
                                              name.toString(),
                                              value.toString());
      else
        d.getDocumentElement().setAttribute(name.toString(), value.toString());
    }
  }
  //--------------------------------------------------------------------

  /**
   * Transform an XML representation into a purely textual
   * representation that is suitable for writing to file.
   */
  private void export(Document source, FileWriter target)
    throws TransformerException
  {
    m_transformer.transform(new DOMSource(source), 
                            new StreamResult(target));
  }
    
  /**
   * Transform an XML representation into a purely textual
   * representation that is suitable for writing to an 
   * {@link OutputStream}.
   */
  private void export(Document source, OutputStream target)
    throws TransformerException
  {
    m_transformer.transform(new DOMSource(source), 
                            new StreamResult(target));
  }
  
  /**
   * Generates a new, empty, XML document
   */
  private Document newDocument(String msgType) throws DOMException
  {
    return m_docBuilder.createDocument(Constants.Namespace.NRS_PML,
                                       msgType,
                                       null);
  }
}
