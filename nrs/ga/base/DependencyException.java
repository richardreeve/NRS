package nrs.ga.base;

/** DependencyException.
 * Thrown when a NRS.ga dependency has not been satisfied.
 *
 * @author Thomas French
*/

public class DependencyException extends Exception
{
    /** Constructor.
     * @param message String message to be displayed.
     */
    public DependencyException(String message){
	super(message);
    }
}
