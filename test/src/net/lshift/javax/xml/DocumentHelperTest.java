package net.lshift.javax.xml;

import java.io.ByteArrayOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.w3c.dom.Element;

public class DocumentHelperTest extends TestCase {
    public static void testXmlCopy()
        throws ParserConfigurationException, TransformerException
    {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foo/>";
        Element root = DocumentHelper.newDocumentRoot("foo");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DocumentHelper.xmlCopy(root.getOwnerDocument(), baos);
        assertEquals(expected, new String(baos.toByteArray()));
    }
}
