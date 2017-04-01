// $Id: CSL_XMLEventProcessor.java,v 1.4 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.logging.*;
import java.util.*;

/** Implement the element stack used when processing CSL files */
public class CSL_XMLEventProcessor
{
  private Stack m_stack;

  private CSL_Element_Registry m_registry;

  Logger m_log = Logger.getLogger("nrs.csl");

  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_XMLEventProcessor(CSL_Element_Registry elementReg)
  {
    m_stack = new Stack();

    m_registry = elementReg;
  }
  //----------------------------------------------------------------------
  /** Process occurrance of an XML event - character data*/
  public void characterData(String data)
  {
    m_stack.push(data);
  }
  //----------------------------------------------------------------------
  /** Process occurrance of an XML event - startElement */
  public void startElement(String URI, String name, HashMap atts)
  {
    // Ignore those elements we don't recognise
    if (!elementRecognised(URI, name))
      {
        m_log.warning("Element '" + name + "' not recognised. Ignored.");
        return;
      }

    CSL_Element newElement = buildElement(URI, name, atts);

    // Now push onto stack
    if (newElement != null) m_stack.push(newElement);

    m_log.finest("Stack:" + m_stack);
  }
  //----------------------------------------------------------------------
  /** Process occurrance of an XML event - endElement.
   *
   * @throws CSL_Exception An exception is thrown if the endElement can
   * not be matched up with a start element. */
  public void endElement(String URI, String name) throws CSL_Exception
  {
    Stack temp = new Stack();

    // Ignore those elements we don't recognise
    if (!elementRecognised(URI, name))
      {
        m_log.warning("Element '" + name + "' not recognised. Ignored.");
        return;
      }

    // First unwind the stack to find the original element
    boolean res = unwindStack(temp, name);

    if (res == false)
      {
        m_log.warning("When unwinding stack to find"
                      +" start of element '"
                      + name
                      + "' no start element was found.");
        throw new CSL_Exception("No start element found to match end element '"
                                + name + "'.");
      }

    // Now process the temporary stack, and make each entry a call to
    // the element we are holding
    CSL_Element completedElement = processTempStack(temp);
    if (completedElement == null)
      {
        throw new CSL_Exception("Failed to process CSL unwind stack while attempting to complete element '" + name + "'.");
     }

    // Finally push the element back onto the stack.
    m_stack.push(completedElement);

    m_log.finest("Stack:" + m_stack);
  }
  //----------------------------------------------------------------------
  /** Identify the class associated with the XML element
   * <code>name</code> and create and return an instance.
   *
   * @return An instance of the CSL_Element corresonding to
   * <code>name</code>, or null if the parameter is not recognised. */
  CSL_Element buildElement(String URI, String name, HashMap atts)
  {
    CSL_Element retVal = m_registry.buildElement(URI, name, atts);

    if (retVal == null)
      {
        m_log.warning("XML element '"
                      + name
                      + "' not recognised. Ignoring.");
      }

    return retVal;
  }
  //----------------------------------------------------------------------
  /** Determine whether the element named <code>localName</code> with
   * namespace <code>URI</code> is recognised by the program - i.e., can
   * be handled, which means there is a class which corresponds to the
   * element, and that class has been correctly registered. */
  public boolean elementRecognised(String URI, String localName)
  {
    if (URI.length() == 0)
      {
        m_log.warning("XML element '" + localName
                      + "' defined without a namespace.");
      }
    return m_registry.elementSupported(URI, localName);
  }
  //----------------------------------------------------------------------
  /** Override the standard toString conversion to provide a debug style
   * output which outputs the present entries in the stack. */
  public String toString()
  {
    String retVal = "";

    for (int i = 0; i < m_stack.size(); i++)
      {
        retVal += m_stack.elementAt(i) + "\n";
      }

    return retVal;
  }
  //----------------------------------------------------------------------
  /** Process all the XML entries that were found between a pair of
   * start and end elements. The top element in the stack is the one
   * which the following elements will be applied to.
   *
   * @return The CSL_Element at the top of <code>temp</code> after all
   * the other elements in the stack have been applied to it. If there
   * was a problem, the return value is null. */
  CSL_Element processTempStack(Stack temp)
  {
    if (temp.empty())
      {
        m_log.warning("CSL temporary unwind stack is empty");

        return null;
      }

    Object o;
    o = temp.pop();

    if (o instanceof CSL_Element)
      {
        CSL_Element top = (CSL_Element) o;

        while (!temp.empty())
          {
            o = temp.pop();

            if (o instanceof CSL_Element)
              {
                top.addElement((CSL_Element) o);
              }
            else if (o instanceof String)
              {
                top.addCharacterData((String) o);
              }
            else
              {
                m_log.warning("Object found in CSL unwind stack is neither corresponding to a XML sub-element or character data. Object=" + o);
              }
          }

        return top;
      }
    else
      {
         m_log.warning("Top Object in CSL temporary "
                       + "unwind stack is not a"
                       +" CSL_Element type. Object="
                       + o);
         return null;
      }
  }
  //----------------------------------------------------------------------
  /** Unwind the main stack (m_stack) until the element with the
   * specified name has been found. When unwinding, elements popped
   * off are pushed onto the temporary stack.
   *
   * @return whether the desired element was found. */
  boolean unwindStack(Stack temp, String eName)
  {
    boolean elementFound = false;
    Object o;
    CSL_Element el;

   while ((!m_stack.empty()) && (!elementFound))
      {
        o = m_stack.pop();
        temp.push(o);

        m_log.finest("Pushing [" + o + "] onto unwind stack");

        // Is the popped object a CSL element? - if so we check
        // if its the one we are looking for.
        if (o instanceof CSL_Element)
          {
            el = (CSL_Element) o;
            elementFound = eName.equalsIgnoreCase(el.getElementName());
          }
      }

    return elementFound;
  }
}
