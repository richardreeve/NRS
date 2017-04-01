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
package nrs.ga.rng;

import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.ga.base.PackageLogger;

import java.util.Random;

/** Generate psuedo-random double numbers from a uniform distribution.
 *
 * @author Thomas French
 */

public class UniformRNGNode extends RNGNode
{
  private Random m_rn;
  
  /** Constructor. 
   * Use {@link VariableManager} to suggest vnid and register node. 
   *
   * @param vmMan VariableManager to register with.
   * @param vnName vnName of this node.
   */
  public UniformRNGNode(VariableManager vmMan, String vnName, boolean intType)
  {
    super(vmMan, vnName, "UniformRNGNode", intType);
    
    //seeded with current time, to accuracy to 1 millisecond.
    m_rn = new Random(); 
  }
  
  /** Generate random double number, from a Uniform distribution.
   *
   * @return random double number between 0.0 (inclusive) to 1.0 (exclusive).
   */
  public double getRandDouble(){
    return m_rn.nextDouble();
  }
  
  /** Generate random double numbers between hi and lo.
   * The returned value is chosen from a Uniform distribution.
   *
   * @param lo 
   * @param hi
   *
   * @return double number between hi and lo.
   */
  public double getRandDouble(double lo, double hi)
  {
    if ( lo > hi ){
      double i = hi;
      hi = lo;
      lo = i;
    }
    double r = lo + (m_rn.nextDouble() * (hi-lo));

    if (intType) {
        return Math.floor(r);
    }

    return r;
  }
  
  /** Get random number between 0 (inclusive) and high (exclusive). 
   * @return random number from 0 to high-1.
   */
  public int getRandInt(int high){
    return m_rn.nextInt(high);
  }
  
  /** Generate random number from a Gaussian distribution, with a 
   * mean and standard deviation supplied by caller. 
   * Return value can be negative.
   *
   * @param mean mean value
   * @param std standard deviation
   */
  public double getRandGaussian(double mean, double std){
    double d = m_rn.nextGaussian() + 0.5; 
    double out = mean + d * std;
    if (intType) {
        return Math.floor(out);
    }
    return out;
  }
  
  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at UniformRNGNode!");
  }
}
