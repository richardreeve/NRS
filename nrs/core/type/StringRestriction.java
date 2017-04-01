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
package nrs.core.type;

import nrs.core.base.Restriction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

/** String-type restriction
 *
 * @author Thomas French
*/

public class StringRestriction extends Restriction
{
  /** Enumeration of String restriction types. */
  public enum Type { List, Token, Filename, VNName, NoStringRestriction };

  /** String restriction type from enum StringRestrictionType. */
  private Type m_type = null;

  /** Set of list members if restriction type 'list'. */
  private HashSet<String> m_listMembers;
  
  /** Default constructor
   * No restriction type.
   */
  public StringRestriction(){
    super(RestrictionType.String);

    m_type = Type.NoStringRestriction;
  }

  /** Constructor 
   * @param type Type of String restriction
   */
  public StringRestriction( Type type ){
    super(RestrictionType.String);

    m_type = type;
    
    if ( m_type == Type.List )
      m_listMembers = new HashSet<String>();
  }

  public Type getStringType(){
    return m_type;
  }

  /** Add list member. */
  public void addListMember( String m ){
    m_listMembers.add(m);
  }
  
  /**  Is this a member of the list? */
  public boolean isListMember( String m ){
    for(String s : m_listMembers )
      if ( s.equals(m) )
        return true;

    return false;
  }

  /** Get iterator to traverse members of list. */
  public Iterator<String> getListIterator(){
    return m_listMembers.iterator();
  }

  /** Check if valid value, given restriction type. */
  public boolean valid(String s){
    if ( s == null )
      return false;

    switch (m_type) 
      {
      case List: // list constraint
        if ( isListMember(s) )
          return true;
        else
          return false;
      case Token: 
        return isToken(s);
      case Filename:
        // not allowed are '\' ('\057') and null character '\0'
        if ( s.indexOf('\\') != -1 || s.indexOf("\0") != -1 )
          return false;
        return true;
      case VNName:    
        // one or more tokens with full-stops between them
        // parse string as tokens delimited by full-stops
        StringTokenizer st = new StringTokenizer(s, ".");
        while(st.hasMoreTokens())
          if ( !isToken(st.nextToken()) )
            return false;
        
        return true;
      case NoStringRestriction:
        return true;

      default: 
        return true;  
      }

  }

  /** Checks to see if s is a legal NRS token. 
   * Legal tokens are alphanumeric strings starting with a letter, which can 
   * also contain the underscore character.
   */
  private boolean isToken(String s){
    if ( !Character.isLetterOrDigit(s.charAt(0)))
      return false;

    char c;
    for(int x = 1; x < s.length(); x++){
      c = s.charAt(x);
      if ( !Character.isLetterOrDigit(c) && c != '_' )
        return false;
    }
    return true;
  }

}



