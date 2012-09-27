package org.cloudgraph.web.common.io;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


public class FILEUtils
{
	
    // Charset and decoder for ISO-8859-15
    private static Charset charset = Charset.forName("ISO-8859-15");
    private static CharsetDecoder decoder = charset.newDecoder();

    
    /**
     * Read a file.
     * @param fyl the file to read from.
     * @return the byte array contents of the file.
     * @throws FileNotFoundException, IOException if something goes wrong.
     */
    public synchronized static byte[] readFile(File fyl) throws FileNotFoundException, IOException
    {
    	
    	int len = (int) fyl.length ();
    	byte[] bfr = new byte[len];
    	BufferedInputStream bis = new BufferedInputStream (new FileInputStream (fyl));
    	bis.read (bfr, 0, len);
    	bis.close ();
    	
    	return bfr;

    } // readFile
    
    
    /**
     * Write a file.
     * @param txt the text to output to file.
     * @param fyl the file to write to.
     * @throws FileNotFoundException, IOException if something goes wrong.
     */
    public synchronized static void writeFile(String txt, File fyl) throws FileNotFoundException, IOException
    {
    	
    	writeFile(txt.getBytes(), fyl);

    } // writeFile

    
    /**
     * Write a file.
     * @param bites the array of bytes to output to file.
     * @param fyl the file to write to.
     * @throws FileNotFoundException, IOException if something goes wrong.
     */
    public synchronized static void writeFile(byte[] bites, File fyl) throws FileNotFoundException, IOException
    {
    	
    	BufferedOutputStream bos = new BufferedOutputStream (new FileOutputStream (fyl));
    	bos.write (bites);
    	bos.flush();
    	bos.close ();

    } // writeFile

    
    /**
     * Read a file using NIO.
     * @param fyl the file to read from.
     * @return the char array contents of the file.
     * @throws FileNotFoundException, IOException if something goes wrong.
     */
    public synchronized static char[] readFileNIO(File fyl) throws FileNotFoundException, IOException
    {

        // Open the file and then get a channel from the stream.
        FileChannel fc = new FileInputStream(fyl).getChannel();

        // Get the file's size and then map it into memory.
        int sz = (int) fc.size();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

        // Decode the file into a char buffer.
        CharBuffer cb = decoder.decode(mbb);

        // Close the channel and the stream.
        fc.close();

        // Return the file contents as a char[].
        return cb.array();

    } // readFileNIO

    
    /**
     * Write a file using NIO.
     * @param txt the text to output to file.
     * @param fyl the file to write to.
     * @return the number of bytes written.
     * @throws FileNotFoundException, IOException if something goes wrong.
     */
    public synchronized static int writeFileNIO(String txt, File fyl) throws FileNotFoundException, IOException
    {
    	
    	return writeFileNIO(txt.getBytes(), fyl);

    } // writeFileNIO
    

    /**
     * Write a file using NIO.
     * @param bites the array of bytes to output to file.
     * @param fyl the file to write to.
     * @return the number of bytes written.
     * @throws FileNotFoundException, IOException if something goes wrong.
     */
    public synchronized static int writeFileNIO(byte[] bites, File fyl) throws FileNotFoundException, IOException
    {

        int numWritten = 0;

        // Open the file and then get a channel from the stream.
        WritableByteChannel wbc = new FileOutputStream(fyl).getChannel();

        // Allocate a buffer the size of the output and load it with the text
        // bytes.
        ByteBuffer bfr = ByteBuffer.allocateDirect(bites.length + 256);
        bfr.put(bites);

        // Set the limit to the current position and the position to 0
        // making the new bytes visible for write ().
        bfr.flip();

        // Write the bytes to the channel.
        numWritten = wbc.write(bfr);

        // Close the channel and the stream.
        wbc.close();

        // Return the number of bytes written.
        return numWritten;

    } // writeFileNIO
    

    /**
     * Move (rename) a file from one location to another.
     * @param srcFyl the file to be moved.
     * @param tarFyl the destination file.
     */
    public static void moveFile(File srcFyl, File tarFyl) throws IOException
    {

        if (tarFyl.exists())
            tarFyl.delete();

        srcFyl.renameTo(tarFyl);

        if (!tarFyl.exists())
            throw new IOException("Move " + srcFyl.toString() + " To " + tarFyl.toString() + " Failed");

    } // moveFile

} // class FILEUtils
