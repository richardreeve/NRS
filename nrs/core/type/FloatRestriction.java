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

/** Float-type restriction
 *
 * @author Thomas French
*/

public class FloatRestriction extends Restriction
{
  private String m_abbreviation;
  private String m_scale;
  private double m_minVal, m_maxVal;

  /** Default Constructor
   *
   */
  public FloatRestriction(){
    super(RestrictionType.Float);

    m_abbreviation = "";
    m_scale = "";
    m_minVal = Double.MIN_VALUE;
    m_maxVal = Double.MAX_VALUE;
  }

  /** Constructor - supply abbreviation,scale, min and max.
   *
   */
  public FloatRestriction(String abbrv, String scale, 
                          double min, double max){
    super(RestrictionType.Float);

    m_abbreviation = abbrv;
    m_scale = scale;
    m_minVal = min;
    m_maxVal = max;
  }

  /** Constructor - supply only abbreviation and scale
   *
   */
  public FloatRestriction(String abbrv, String scale){
    super(RestrictionType.Float);
    
    m_abbreviation = abbrv;
    m_scale = scale;
    m_minVal = Double.MIN_VALUE;
    m_maxVal = Double.MAX_VALUE;
  }

  
  public void setMin(double min){
    m_minVal = min;
  }
  
  public double getMin(){
    return m_minVal;
  }

  public void setMax(double max){
    m_maxVal = max;
  }
  
  public double getMax(){
    return m_maxVal;
  }

  /** Check is value is a valid float given minimum and maximum values, 
   * inclusive. 
   */
  public boolean valid(double value){
    if ( value <= m_maxVal && value >= m_minVal )
      return true;
    else
      return false;
  }
}
