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

package nrs.ga.base;

public class Gene
{
    private double m_value;
    private double m_min;
    private double m_max;

    public Gene(){ 
	m_value = 0.0;
	m_min = 0.0;
	m_max = 0.0;
    }

    public void setValue(double f){ m_value = f; }
    public double getValue(){ return m_value; }

    public void setMin(double min){ m_min = min; }
    public double getMin(){ return m_min; }

    public void setMax(double max){ m_max = max; }
    public double getMax(){ return m_max; }

    //need deep cloning
    public Object clone(){
	Gene g = new Gene();
	g.setValue(this.getValue());
	g.setMin(this.getMin());
	g.setMax(this.getMax());
	return g;
    }
}
