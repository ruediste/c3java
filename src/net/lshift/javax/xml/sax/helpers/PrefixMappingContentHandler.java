package net.lshift.javax.xml.sax.helpers;

import org.xml.sax.SAXException;

public interface PrefixMappingContentHandler
{
    public void startPrefixMapping(String uri, String prefix)
        throws SAXException;

    public void endPrefixMapping(String prefix)
        throws SAXException;

}
