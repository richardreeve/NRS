package nrs.core.comms;

/**
 * <p>Represents the numerical ID of a port ({@link CommsRoute}), and
 * provides a method for encoding that ID in pseudo-hex, suitable for
 * construction of worm-hole routing strings. The numeric value of the
 * port can be retrieved using {@link #getValue()} and its psuedo-hex
 * encoding using {@link #getEncoding()}.</p>
 *
 * <p>The psuedo-hex encoding works by transforming the port's integer
 * value into a sequence of characters ranging from 'A' through to 'P',
 * representing the numbers 0 to 15. Each character encodes two pieces
 * of information: 3 bits of the actual integer number; and one bit
 * indicating whether the character represents the end of an encoding
 * sequence. Thus the characters which represent the end of a coding
 * sequence are 'A' through 'H'; any route encoding terminates with one
 * of those characters.
 *
 * <p>Static utility methods are provided to convert an encoded port
 * number to an integer value;to extract the first encoded port number
 * from an encoded route string; and the extract the first encoded hop
 * from a route.
 *
 * @see #toInt(String)
 * @see #chop(String)
 * @see #firstHop(String)
 *
 * @author Darren Smith
 */
public class PortNumber
{
  private int m_number;

  private StringBuffer m_encoding;

  private static final char[] PSUEDO_HEX = { 'A', 'B', 'C', 'D',
                                             'E', 'F', 'G', 'H',
                                             'I', 'J', 'K', 'L', 
                                             'M', 'N', 'O', 'P'};
  //----------------------------------------------------------------------
  /** Create a port number to represent the integer <tt>n</tt>, where
   * <tt>n</tt> can take a value between 0 and 1,073,741,823
   * (inclusive). This limitation is due to <tt>n</tt> being restricted
   * to the first 30 bits of the 32 bit integer.
   *
   * @throws IllegalArgumentException if the port number exceeds the
   * specifed limits
   */
  public PortNumber(int n)
  {
    if (n < 0 || n > 1073741823) 
    {
      throw new IllegalArgumentException("port number out of range");
    }
    m_number = n;

    // ensure the two highest bits are unset
    m_number &= 0x3FFFFFFF;
  }
  //----------------------------------------------------------------------
  /**
   * Get the port number
   */
  public int getValue()
  {
    return m_number;
  }
  //----------------------------------------------------------------------
  /**
   * Return the port number encoded into a psuedo hex string. This is
   * provided for representating port numbers in PML route fields.
   */
  public String getEncoding()
  {
    if (m_encoding == null)
    {
      m_encoding = new StringBuffer();

      int temp = m_number;
      int threeLowesetBits;

      // 30 bits of content are decoded in 10 groups of 3
      for (int i = 0; i < 10; i++)
      {
        threeLowesetBits = temp & 7;

        if (i > 0) threeLowesetBits += 8;
        m_encoding.append(PSUEDO_HEX[threeLowesetBits]);

        temp >>>= 3;

        if (temp == 0) break;
      }
    }
    
    m_encoding.reverse();
    
    return m_encoding.toString();
  }
  //----------------------------------------------------------------------
  /** Return a string representation */
  public String toString()
  {
    return Integer.toString(m_number);
  }
  //----------------------------------------------------------------------
  /**
   * Return the encoding of the first port-hop in the encoded route. If
   * a <tt>null</tt> object is passed in, a <tt>null</tt> object is
   * returned.
   *
   * @throws IllegalArgumentException if <tt>encodedRoute</tt> is
   * non-empty and does not contain a route terminating character
   */
  static public String firstHop(String encodedRoute)
  {
    if (encodedRoute == null) return null;

    for (int i = 0; i < encodedRoute.length(); i++)
      if (encodedRoute.charAt(i) < 'I')  return encodedRoute.substring(0, i+1);

    throw new IllegalArgumentException("encodedRoute not terminated");
  }
  //----------------------------------------------------------------------
  /**
   * Return a new route which represents the <tt>encodedRoute</tt> with
   * the leading hop chopped off. If a <tt>null</tt> object is passed
   * in, a <tt>null</tt> object is returned.
   *
   * @throws IllegalArgumentException if <tt>encodedRoute</tt> is
   * non-empty and does not contain a route terminating character
   */
  static public String chop(String encodedRoute)
  {
    if (encodedRoute == null) return null;

    for (int i = 0; i < encodedRoute.length(); i++)
    {
      if (encodedRoute.charAt(i) < 'I') 
      {
        if (++i < encodedRoute.length()) {
          return encodedRoute.substring(i);
        }
       else {
         return "";
       }
      }
    }
    
    throw new IllegalArgumentException("encodedRoute not terminated");
  }
  //----------------------------------------------------------------------
  /**
   * Transform a psuedo-hex encoded route into an integer value. Any
   * characters that may exist after the first terminator character
   * found are quietly ignored.
   *
   * @throws NullPointerException if <tt>encodedRoute</tt> is <tt>null</tt>
   *
   * @throws IllegalArgumentException if <tt>encodedRoute</tt> has zero
   * length or is not terminated by a character in the range 'A' to 'H'
   */
  static public int toInt(String encodedRoute)
  {
    if (encodedRoute == null) 
      throw new NullPointerException("encodedRoute can't be null");

    if (encodedRoute.length() == 0) 
      throw new IllegalArgumentException("encodedRoute can't be zero length");

    int total = 0;

    for (int i = 0; i < encodedRoute.length(); i++)
    {
      total += (encodedRoute.charAt(i) - 'A');
      
      if (encodedRoute.charAt(i) < 'I') return total;

      total <<= 3;
    }
    
    // if we get here, string did not contain a terminating character
    throw new IllegalArgumentException("route=" + encodedRoute +
                                       " is not terminated correctly");
  }
}
