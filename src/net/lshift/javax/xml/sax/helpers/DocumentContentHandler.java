/**
 *
 */
package net.lshift.javax.xml.sax.helpers;

import org.xml.sax.SAXException;

public interface DocumentContentHandler {
    public void startDocument() throws SAXException;
    public void endDocument() throws SAXException;
}
