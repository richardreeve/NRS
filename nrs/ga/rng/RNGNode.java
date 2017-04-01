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

import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.ga.base.PackageLogger;
import nrs.ga.base.DependencyException;

/** Base class for all RNG Nodes.
 * These nodes provide a set of methods for access to random numbers between
 * certain ranges and certain types, e.g. ints and doubles.
 *
 * @author Thomas French
 */
public abstract class RNGNode extends Node
{
  protected final boolean intType;

  /** Constructor. */
  public RNGNode(VariableManager vmMan, String vnName, String type, boolean intType){
    super(vmMan, vnName, type);
    this.intType = intType;
  }
  
  /** Generate random double numbers between hi and lo.
   * The returned value is chosen from a distribution defined by subclass.
   *
   * @param lo  
   * @param hi
   *
   * @return double number between hi and lo.
   */
  public abstract double getRandDouble(double lo, double hi);
  
  /** Generate random double number.
   * Distribution, e.g. Uniform, Gaussian, is implemented by subclass.
   *
   * @return random double number between 0.0 (inclusive) to 1.0 (exclusive).
   */
  public abstract double getRandDouble();
  
  /** Get random number between 0 (inclusive) and high (exclusive). 
   * @param high bound for random number generator
   * @return random number
   */
  public abstract int getRandInt(int high);
  
  /** Generate random number from a Gaussian distribution, with a 
   * mean and standard deviation supplied by caller.
   *
   * @param mean mean value
   * @param std standard deviation
   */
  public abstract double getRandGaussian(double mean, double std);
  
  //-----------------------------------------------------------------------//
  /** {@link Message} to deliver to this Node. */
  public void deliver(Message m){
    PackageLogger.log.fine("Received message at RNGNode!");
  }
  
  /** Check dependencies for this node and all the nodes it contains. 
   *
   * @throws DependencyException thrown if any dependencies are not satisfied
   */
  public void checkDependencies() throws DependencyException {
    return; //have no dependencies
  }
}
