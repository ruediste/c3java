package net.lshift.javax.xml;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import net.lshift.java.util.Lists;
import net.lshift.java.util.Transform;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

    public interface NodeFactory
    {
        public List<? extends Node> asNodes(Object x);
    }

    public static class NodeFactoryImpl
    implements NodeFactory
    {
        private final Document document;
        private final NodeFactory delegate;

        public NodeFactoryImpl(Document document, NodeFactory delegate)
        {
            this.document = document;
            this.delegate = delegate;
        }

        @Override
        public List<? extends Node> asNodes(Object x)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public <K,V> List<? extends Node> asNodes(Map<K,V> map)
        {
            return Lists.map(new Transform<Map.Entry<K,V>,Node>() {
                @Override
                public Node apply(Entry<K, V> x) {
                    Element element = document.createElement(x.getKey().toString());
                    for(Node node: delegate.asNodes(x.getValue()))
                        element.appendChild(node);
                    return element;
                }

            }, map.entrySet());
        }

        public <E> List<? extends Node> asNodes(List<E> list)
        {
            return Lists.map(new Transform<E,Node>() {
                @Override
                public Node apply(E x) {
                    Element element = document.createElement("item");
                    for(Node node: delegate.asNodes(x))
                        element.appendChild(node);
                    return element;
                }
            }, list);
        }

        public List<? extends Node> asNodes(String s)
        {
            return Lists.list(document.createTextNode(s));
        }
    }
}
