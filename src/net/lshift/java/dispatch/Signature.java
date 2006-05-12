/*
     This file is part of the LShift Java Library.

    The LShift Java Library is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    The LShift Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The LShift Java Library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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
