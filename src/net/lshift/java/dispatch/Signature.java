
package net.lshift.java.dispatch;

import java.lang.reflect.Method;

class Signature
{
    public Method procedure;
    public Class<?> [] parameterTypes;

    public Signature(Method procedure)
    {
	this(procedure, procedure.getParameterTypes());
    }

    public Signature(Method procedure, Class<?> [] types)
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
