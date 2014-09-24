
package net.lshift.java.io;

import java.io.*;

public class Stream
    extends Copy
{
    /**
     * Get a character sequence from a reader.
     * Returns a character sequence rather than a string
     * because you can easily convert that to a string,
     * but if you don't need to, its wasteful.
     */
    public CharSequence read(Reader in)
        throws IOException
    {
        StringWriter out = new StringWriter();
        copy(in, out);
        return out.getBuffer();
    }
}
