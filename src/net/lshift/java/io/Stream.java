
package net.lshift.java.io;

import java.io.*;

public class Stream
    extends Copy
{
    public String read(Reader in)
    {
        StringWriter out = new StringWriter();
        copy(in, out);
        return out.toString();
    }
}

