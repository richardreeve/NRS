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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import nrs.core.base.Message;
import nrs.core.message.Constants;
import nrs.util.SharedXMLResources;

/** Parse XML into a {@link Message} object.
 * Sources for XML are {@link InputStream}, XML {@link Document} or 
 * a {@link File}.
 *
 * @author Thomas French
 */
public class XMLToMessage extends SharedXMLResources
{
  private static XMLToMessage m_singleton = null;
  
  /** Uses singleton pattern.
   *
   * @return instance of XMLToMessge object
   */
  public static XMLToMessage getInstance(){
    if ( m_singleton == null ){
      try{
        new XMLToMessage();
      }
      catch(FactoryConfigurationError fce){
        PackageLogger.log.warning(fce.getMessage());
        m_singleton = null;
      }
      catch(ParserConfigurationException pce){
        PackageLogger.log.warning(pce.getMessage());
        m_singleton = null;
      }
    }
    return m_singleton;
  }
  
  /** Private constructor used by singleton pattern.*/
  private XMLToMessage() 
    throws FactoryConfigurationError, 
           ParserConfigurationException{ 
    m_singleton = this;
  }

  /** Parse {@link InputStream} and convert to a {@link Message} object.
   *
   * @param is InputStream to read from
   * @return converted Message object
   */
  public Message convert(InputStream is){
    Document document;
    try{
      document = builder().parse(is);
    }
    catch (SAXException sax){
      PackageLogger.log.warning(sax.getMessage());
      return null;
    }
    catch (IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
      return null;
    }

    return convert(document);    
  }

  /** Parse {@link File} and convert to a {@link Message} object.
   *
   * @param file File to read from
   * @return converted Message object
   */
  public Message convert(File file){
    PackageLogger.log.fine("Loading and parsing file: " + file.toString());
    Document document;
    try{
      document = builder().parse(file);
    }
    catch (SAXException sax){
      PackageLogger.log.warning(sax.getMessage());
      return null;
    }
    catch (IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
      return null;
    }

    return convert(document);
  }

  /** Parse {@link Document} and convert to a {@link Message} object.
   *
   * @param d Document to read from
   * @return converted Message object
   */
  public Message convert(Document d){
    // build up Message object from document
    Message msg = new Message();
    
    Element root = d.getDocumentElement();

    // set Message type - root element of document
    msg.setType(root.getTagName());
    
    // retrieve attributes - PML namespace and NRS Attribute namespace
    NamedNodeMap attrs = root.getAttributes();
    
    Attr a;
    int x = Constants.Namespace.NRSA_qualifier.length() + 1;
    for(int i = 0; i < attrs.getLength(); i++){
      a = (Attr) attrs.item(i);
    
      if (a != null){
        if ( a.getName().startsWith("xmlns") )
          continue;

        else if ( a.getName().startsWith(Constants.Namespace.NRSA_qualifier
                                         + ":") )
          msg.setNRSField(a.getName().substring(x), a.getValue());

        else
          msg.setField(a.getName(), a.getValue());        
      }
    }

    return msg;
  }

}
