
package net.lshift.java.io;

import java.io.*;

/**
 * Copy byte and character data about the place
 */
public class Copy
{
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * Copy an input stream to an output stream using the
     * specified buffer.
     * @param in the stream to read from.
     * @param out the stream to write to.
     * @param buffer the buffer to temporarily store data - any contents
     * will be overwritten by this method.
     */
    public static void copy(InputStream in, OutputStream out, byte [] buffer)
        throws IOException
    {
        int read = 0;
        while(read != -1) {
            read = in.read(buffer);
            if(read > 0) {
                out.write(buffer, 0, read);
            }
        }
    }

    /**
     * Copy using a default sized buffer.
     * @see #copy(InputStream,OutputStream,byte[])
     */
    public static void copy(InputStream in, OutputStream out)
        throws IOException
    {
        copy(in, out, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copy an input stream to a file
     */
    public static void copy(InputStream in, File dest)
        throws IOException
    {
        OutputStream out = new FileOutputStream(dest);
        try {
            copy(in, out);
        }
        finally {
            out.close();
        }
    }

    public static void copy(Reader in, Writer out, char [] buffer)
        throws IOException
    {
        int read = 0;
        while(read != -1) {
            read = in.read(buffer);
            if(read > 0) {
                out.write(buffer, 0, read);
            }
        }
    }

    public static void copy(Reader in, Writer out)
        throws IOException
    {
        copy(in, out, new char[DEFAULT_BUFFER_SIZE]);
    }

    public static void copy(Reader in, File dest)
    {
        Writer out = new FileWriter(dest);
        try {
            copy(in, out);
        }
        finally {
            out.close();
        }
    }
}
