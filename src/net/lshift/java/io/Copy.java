/*

Copyright (c) 2006 LShift Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

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
	throws IOException
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
