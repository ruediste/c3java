package net.lshift.javax.xml.sax.helpers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This interface contains the two methods relevant to
 * element processing from ContentHandler. This allows us to
 * conveniently dispatch them independent of other methods
 * in content handler
 */
public interface ElementContentHandler {
    public void startElement(
        String uri,
        String localName,
        String name,
        Attributes atts)
    throws SAXException;

    public void endElement(
        String uri,
        String localName,
        String name)
    throws SAXException;
}
