// $Id: CSL_Registry_Utils.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

/**
 * Provides a suit of functions which are useful for various registry
 * enquiries.
 */
public class CSL_Registry_Utils
{
  private final CSL_Unit_Registry m_unitReg;
  private final CSL_Message_Registry m_messageReg;
  private final CSL_Node_Registry m_nodeReg;
  private final CSL_Element_Registry m_elementReg;

  //----------------------------------------------------------------------
  /**
   * Constructor. Provides the various CSL registries for which lookups
   * (ie the utility functions provided by this class) will be used.
   */
  public CSL_Registry_Utils(CSL_Unit_Registry unitReg,
                            CSL_Message_Registry messageReg,
                            CSL_Node_Registry nodeReg,
                            CSL_Element_Registry elementReg)
  {
    m_unitReg = unitReg;
    m_messageReg = messageReg;
    m_nodeReg = nodeReg;
    m_elementReg = elementReg;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the basic type associated with the specified {@link
   * CSL_Element_Attribute}
   */
  public CSL_BasicType getAttributeBasicType(CSL_Element_Attribute at)
  {
    return at.getResolvedUnit().getBasicType();
  }
}
