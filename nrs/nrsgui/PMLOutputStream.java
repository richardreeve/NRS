package nrs.nrsgui;

import java.io.IOException;
import nrs.core.comms.OutputFIFO;

/**
 * This takes PML encoded messages and writes them out to a stream
 */
class PMLOutputStream
{
  private OutputFIFO m_fifo;

  public final static byte[] PML_TERIMATOR = {0};
  
  //----------------------------------------------------------------------
  /**
   * Creates an instance of {@link PMLOutputStream}. No output stream
   * has been provided, so PML messages are streamed to the console.
   */
  public PMLOutputStream()
  {
  }
  //----------------------------------------------------------------------
   /**
   * Creates an instance of {@link PMLOutputStream}. PML messages will
   * be written to the specified FIFO. If <code>fifo</code> is null or
   * is not ready for writing, an exception is asserted.
   */
  public PMLOutputStream(OutputFIFO fifo) throws NullPointerException
  {
    if (fifo == null) throw new NullPointerException("fifo can't be null");

    if (fifo.getState() != OutputFIFO.OPENED_OK)
      throw new IllegalStateException("FIFO not ready for output");

    m_fifo = fifo;
  }
  //----------------------------------------------------------------------
  /**
   * Write out the PML message to whatever stream this class has been
   * configured to use. 
   */
  public void writePML(String PML) throws IllegalStateException, IOException
  {
    if (PML == null) return;

    if (m_fifo != null)
    {
      m_fifo.write(PML.getBytes());
      m_fifo.write(PML_TERIMATOR);
    }
    else
    {
      System.out.println(PML);
    }
  }
  
}
