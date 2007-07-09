package net.lshift.javax.xml;

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DocumentHelper {
    public static Transformer identityTransform()
        throws TransformerConfigurationException
    {
        return TransformerFactory.newInstance().newTransformer();
    }
    
    public static void xmlCopy(Source document, Result out)
        throws TransformerConfigurationException, TransformerException
    {
        identityTransform().transform(document, out);
    }

    public static void xmlCopy(Document document, OutputStream out)
        throws TransformerException
    {
        xmlCopy(new DOMSource(document), new StreamResult(out));
    }
    
    public static Document newDocument()
        throws ParserConfigurationException
    {
        return DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
    }
    
    public static Element newDocumentRoot(String rootname)
        throws ParserConfigurationException
    {
        Document d = newDocument();
        Element res = d.createElement(rootname);
        d.appendChild(res);
        return res;
    }
}
