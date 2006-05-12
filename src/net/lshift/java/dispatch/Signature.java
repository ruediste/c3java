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

package net.lshift.java.dispatch;

import java.lang.reflect.Method;

class Signature
{
    public Method procedure;
    public Class [] parameterTypes;

    public Signature(Method procedure)
    {
	this(procedure, procedure.getParameterTypes());
    }

    public Signature(Method procedure, Class [] types)
    {
	this.procedure = procedure;
	this.parameterTypes = types;
    }

    public int hashCode()
    {
	int hash = procedure.hashCode();
	for(int i = 0; i < parameterTypes.length; ++i)
	    hash = hash ^ parameterTypes[i].hashCode();
	return hash;
    }

    public boolean equals(Object o)
    {
	Signature other = (Signature)o;
	boolean result = (this.procedure == other.procedure);
	for(int i = 0; result && i != this.parameterTypes.length; ++i)
	    result = (this.parameterTypes[i] == other.parameterTypes[i]);
	return result;
    }

    public String toString()
    {
	StringBuffer b = new StringBuffer();
	b.append(procedure.getDeclaringClass().getName());
	b.append('.');
	b.append(procedure.getName());
	b.append('(');
	for(int i = 0; i != parameterTypes.length; ++i) {
	    if(i != 0) b.append(',');
	    b.append(parameterTypes[i].getName());
	}
	b.append(')');

	return b.toString();
    }
}
