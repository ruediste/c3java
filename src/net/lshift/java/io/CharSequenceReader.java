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
