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

import java.util.logging.Logger;

/**
 * Logger object to use for classes within this package. Classes within
 * this package can use the public static Logger defined below rather
 * than requesting their own Logger. Example usage is: <br><br>
 * <code>PackageLogger.log.warning("This is a warning");</code>
 *
 * @author Darren Smith
 * @author Thomas French
 */
public abstract class PackageLogger
{
  /** 
   * Reference to the Logger available for classes within this package
   * Modified from {@link nrs.core.base.PackageLogger} for
   * <code>nrs.control</code>
   */
  public static final Logger log = Logger.getLogger("nrs.composer"); 
}
