
package net.lshift.java.io;

import java.io.*;

/**
 * A character stream whose source is a character sequence
 *
 */

public class CharSequenceReader extends Reader {

    private CharSequence sequence;
    private int length;
    private int next = 0;
    private int mark = 0;

    public CharSequenceReader(CharSequence s)
    {
        this.sequence = s;
        this.length = s.length();
    }

    private void ensureOpen()
        throws IOException
    {
        if (sequence == null)
            throw new IOException("Sequenceeam closed");
    }

    public int read()
        throws IOException
    {
        synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return -1;
            return sequence.charAt(next++);
        }
    }

    public int read(char cbuf[], int off, int len)
        throws IOException
    {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (next >= length)
                return -1;
            int n = Math.min(length - next, len);
            for(int i = off; i != off + n; ++i)
                cbuf[i] = sequence.charAt(next++);
            return n;
        }
    }

    public long skip(long ns)
        throws IOException
    {
        synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return 0;
            long n = Math.min(length - next, ns);
            next += n;
            return n;
        }
    }

    public boolean ready()
        throws IOException
    {
        synchronized (lock) {
            ensureOpen();
            return true;
        }
    }

    public boolean markSupported()
    {
        return true;
    }

    public void mark(int readAheadLimit)
        throws IOException
    {
        if (readAheadLimit < 0){
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (lock) {
            ensureOpen();
            mark = next;
        }
    }

    public void reset()
        throws IOException
    {
        synchronized (lock) {
            ensureOpen();
            next = mark;
        }
    }

    public void close()
    {
        sequence = null;
    }

}
