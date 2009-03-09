package net.lshift.javax.xml.sax.helpers;

import net.lshift.java.util.TwoTuple;

public class QName
extends TwoTuple<String,String>
{
    private static final long serialVersionUID = 1L;

    public QName(String namespaceUri, String localName)
    {
        super(namespaceUri, localName);
    }
    
    public String toString()
    {
        return "{" + this.first + "}" + this.second;
    }
    
    public String getNamespaceUri()
    {
        return first;
    }
    
    public String getLocalName()
    {
        return second;
    }
    
    public static QName qname(String namespaceUri, String localName)
    {
        return new QName(namespaceUri, localName);
    }
}
