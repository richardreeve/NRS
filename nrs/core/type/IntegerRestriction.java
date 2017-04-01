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

/** Integer-type restriction
 *
 * @author Thomas French
*/

public class IntegerRestriction extends Restriction
{
  private int m_minVal, m_maxVal;

  /** Default Constructor 
   *
   */
  public IntegerRestriction(){
    super(RestrictionType.Integer);
    
    // default values
    m_minVal = Integer.MIN_VALUE;
    m_maxVal = Integer.MAX_VALUE;
  }

  /** Constructor
   *
   */
  public IntegerRestriction(int minVal, int maxVal){
    super(RestrictionType.Integer);

    m_minVal = minVal;
    m_maxVal = maxVal;
  }
  
  public void setMin(int min){
    m_minVal = min;
  }

  public void setMax(int max){
    m_maxVal = max;
  }

  public int getMin(){
    return m_minVal;
  }

  public int getMax(){
    return m_maxVal;
  }

  public boolean valid(int value){
    if ( value >= m_minVal && value <= m_maxVal )
      return true;
    else
      return false;       
  }
}


