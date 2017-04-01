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
package nrs.composer;

import java.util.Collection;
import java.util.Iterator;
import nrs.core.base.BaseComponent;
import nrs.core.base.Message;
import nrs.core.base.FieldNotFoundException;
import nrs.core.base.MessageTools;
import nrs.core.base.Node;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.message.*;
import static nrs.core.message.Constants.Fields.*;

/**
 * Root Component class for composer component, which handles all
 * message handling.
 *
 * @author Thomas French
 */
public class ComposerComponent extends BaseComponent
{
    /** Name of component. */
    private final static String m_Name = "NRS.composer";
    /** Component type. */
    private final static String m_CType = "composer";
    /** Component version. */
    private final static String m_Version = "1.0";
    /** Whether this component has a CSL descriptor file. */
    private final static boolean m_HasCSL = true;
    /** Can the component speak BMF. */
    private final static boolean m_SpeaksBMF = false;
    /** Can the component speak PML. */
    private final static boolean m_SpeaksPML = true;

    /** Root node for this component. */
    //???

    //----------------------------------------------------------------------
    /**Constructor
     *
     * @param vMan reference to {@link VariableManager} object.
     */
    public ComposerComponent(VariableManager vMan)
    {
	super(vMan, m_Name, m_CType, m_SpeaksBMF, m_SpeaksPML, 
	      m_HasCSL, m_Version);
	m_vMan = vMan;
    }
   
}
