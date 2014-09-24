/**
 *
 */
package net.lshift.javax.xml.sax.helpers;

import org.xml.sax.SAXException;

public interface IgnorableWhitespaceContentHandler {
    public void ignorableWhitespace(char[] ch, int start, int length)
    throws SAXException;
}
