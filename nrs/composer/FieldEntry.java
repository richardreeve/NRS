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


/** Represents a Field Entry in a Message table
 *
 * @author Sarah Cope
*/

public class FieldEntry {
   
    private String fieldName;

    private String type;

    private Boolean inNRS;

    /**
     * Constructor
     */
    public FieldEntry(String fieldName, String type, Boolean inNRS) {
        this.fieldName = fieldName; 
        this.type = type;
        this.inNRS = inNRS;
    }

    /**
     * Sets the fieldName field for this FieldEntry
     */
    public String getFieldName() {
        return fieldName;
    }

     /**
     * Sets the type field for this FieldEntry
     */
    public String getType() {
        return type;
    }
    
     /**
     * Sets the inNRSA field for this FieldEntry
     */
    public Boolean getInNRS() {
        return inNRS;
    }

     /**
     * Returns the fieldName for this FieldEntry
     */
    public void setFieldName (String fieldName) {
         this.fieldName = fieldName;
    }

    /**
     * Returns the type for this FieldEntry
     */
    public void setType (String type) {
        this.type = type;
    }
    
    /**
     * Returns whether this FieldEntry is in the NRSA namespace
     */
    public void setInNRS (Boolean inNRS) {
        this.inNRS = inNRS;
    }  
}
