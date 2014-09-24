package net.lshift.javax.xml.sax.helpers;

import static net.lshift.javax.xml.sax.helpers.QName.qname;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.lshift.java.dispatch.Delegate;
import net.lshift.java.dispatch.DynamicDispatch;
import net.lshift.java.lang.Variable;
import net.lshift.java.util.Lists;
import net.lshift.java.util.Maps;
import net.lshift.java.util.Sets;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Build a SAX content handler using dynamic dispatch.
 * @see #override(Object)
 * @author david
 *
 */
public class ContentHandlerBuilder
{
    private List<Object> newhandlers = new ArrayList<Object>();
    private Stack<List<Object>> stack = new Stack<List<Object>>();
    private ContentHandler stackContentHandler;

    private Locator documentLocator;
    private Map<String,String> prefixMappings = new HashMap<String,String>();

    /**
     * This handler sets documentLocator if its called on the
     * content handler. It also calls setDocumentHelper on delegate,
     * which is suitable for filters.
     * @see #getDocumentLocator()
     */
    public final DocumentLocatorContentHandler documentLocatorHandler(
        final ContentHandler delegate)
    {
        return  new DocumentLocatorContentHandler() {

            public void setDocumentLocator(Locator locator) {
                ContentHandlerBuilder.this.documentLocator = locator;
                delegate.setDocumentLocator(locator);
            }
        };
    }

    public final PrefixMappingContentHandler prefixMappingHandler(
        final ContentHandler delegate)
    {
        return new PrefixMappingContentHandler() {

            public void endPrefixMapping(String prefix)
            throws SAXException
            {
                prefixMappings.remove(prefix);
                delegate.endPrefixMapping(prefix);
            }

            public void startPrefixMapping(String prefix, String uri)
            throws SAXException
            {
                prefixMappings.put(prefix, uri);
                delegate.startPrefixMapping(prefix, uri);
            }
        };
    }

    /**
     * Create a content handler which is mostly a filter.
     * It collects the document locator, and tracks name spaces,
     * and passes these on to delegate.
     * It dispatches anything it doesn't understand to 'delegate'
     * @param delegate
     */
    public ContentHandlerBuilder(ContentHandler delegate)
    {
        override(delegate);
        override(documentLocatorHandler(delegate));
        override(prefixMappingHandler(delegate));
        push();
    }

    /**
     * Constructor for a content handler.
     * This sets up a content handler with various defaults you will
     * want if you are processing the document. It ignores ignorable
     * whitespace regardless of the flag on the parser. It collects
     * the document locator, and tracks namespaces. It ignores
     * startDocument() and endDocument().
     * @see prefixMappingHandler
     * @see documentLocatorHandler
     */
    public ContentHandlerBuilder()
    {
        // We know the document is going to start, we don't need to do anything
        override(Delegate.noop(DocumentContentHandler.class));

        // Ignoring ignore-able whitespace seems like a safe default
        override(Delegate.noop(IgnorableWhitespaceContentHandler.class));

        override(documentLocatorHandler(Delegate.noop(ContentHandler.class)));
        override(prefixMappingHandler(Delegate.noop(ContentHandler.class)));

        // Various things we want to know about
        override(new Object() {

            @SuppressWarnings("unused")
            public void characters(char [] c, int off, int len)
            throws SAXException
            {
                String s = new String(c, off, len);
                if(!s.trim().isEmpty())
                    throw new SAXException("Unexpected character content: " + s);
            }

            public String toString() {
                return "utilities";
            }
        });

        push();
    }

    // ------------------------------------------------------------------------

    public interface NamedElementHandler {
        public void startElement(Attributes atts)
            throws SAXException;
        public void endElement()
            throws SAXException;
    }

    // ------------------------------------------------------------------------

    /**
     * Add a handler. The handler is only going to be used for
     * children of the current node, so it only makes sense to
     * call this in an implementation of startElement or startDocument. At
     * the corresponding endElement, the handler will be popped:
     * the contentHandler will revert to the state at the matching
     * startElement.
     * @see ContentHandler#startElement(String, String, String, Attributes)
     * @see ContentHandler#endElement(String, String, String)
     */
    public void override(Object handler)
    {
        newhandlers.add(handler);
    }

    // ------------------------------------------------------------------------

    public void push()
    {
        stack.push(Lists.copy(newhandlers));
        newhandlers.clear();
        updateDispatch();
    }

    public void pop()
    {
        stack.pop();
        updateDispatch();
    }

    protected void updateDispatch()
    {
        stackContentHandler = DynamicDispatch.proxy(
            ContentHandler.class,
            Lists.reverseIterable(Lists.concatenate(stack)));
    }

    // ------------------------------------------------------------------------


    public ContentHandler contentHandler()
    {
        // stackhandler is the super of the content handler
        final ContentHandler stackhandler = Delegate.delegate(
            ContentHandler.class,
            new Variable<ContentHandler>() {
                public ContentHandler get() {
                    return stackContentHandler;
                }
        });

        return DynamicDispatch.proxy(ContentHandler.class,
            new Object() {

                @SuppressWarnings("unused")
                public void startElement(
                    String uri,
                    String localName,
                    String name,
                    Attributes atts)
                throws SAXException
                {
                    stackhandler.startElement(uri, localName, name, atts);
                    push();
                }

                @SuppressWarnings("unused")
                public void endElement(String uri, String localName, String name)
                throws SAXException
                {
                    pop();
                    stackhandler.endElement(uri, localName, name);
                    // I think this is what you would expect - that if you
                    // called override during
                    newhandlers.clear();
                }

                @SuppressWarnings("unused")
                public void startDocument()
                throws SAXException
                {
                    stackhandler.startDocument();
                    push();
                }

                @SuppressWarnings("unused")
                public void endDocument()
                throws SAXException
                {
                    pop();
                    stackhandler.endDocument();
                    newhandlers.clear();
                }
            },
            stackhandler);
    };

    public static final ElementContentHandler unexpected(final Set<QName> expected)
    {
        return new ElementContentHandler() {

            @Override
            public void endElement(String uri, String localName, String name)
                throws SAXException
            {
                    unexpectedElement(qname(uri, localName), expected);
            }

            @Override
            public void startElement(
                String uri,
                String localName,
                String name,
                Attributes atts)
                throws SAXException
            {
                unexpectedElement(qname(uri, localName), expected);
            }
        };
    }


    public static ElementContentHandler byElementName(
        final Map<QName, NamedElementHandler> handlers,
        final ElementContentHandler undefined)
    {
        return new ElementContentHandler() {

            private NamedElementHandler getHandler(String uri, String localName)
            throws SAXException
            {
                return handlers.get(qname(uri, localName));
            }

            private boolean hasHandler(String uri, String localName)
            {
                QName qname = qname(uri, localName);
                return handlers.containsKey(qname);
            }

            @Override
            public void endElement(String uri, String localName, String name)
                throws SAXException
            {
                if(hasHandler(uri, localName))
                    getHandler(uri, localName).endElement();
                else
                    undefined.endElement(uri, localName, name);
            }


            @Override
            public void startElement(
                String uri,
                String localName,
                String name,
                Attributes atts)
                throws SAXException
            {
                if(hasHandler(uri, localName))
                    getHandler(uri, localName).startElement(atts);
                else
                    undefined.startElement(uri, localName, name, atts);
            }

        };
    }
    public static ElementContentHandler mixed(
        final Map<QName, NamedElementHandler> handlers)
    {
        return byElementName(handlers, unexpected(handlers.keySet()));
    }

    public static ElementContentHandler listOf(
        final QName required,
        final NamedElementHandler handler)
    {
        return byElementName(required, handler,
            unexpected(Sets.set(required)));
    }


    public static ElementContentHandler asElementContentHandler(final ContentHandler ch)
    {
        return new ElementContentHandler() {

            @Override
            public void endElement(String uri, String localName, String name)
                throws SAXException
            {
                ch.endElement(uri, localName, name);
            }

            @Override
            public void startElement(
                String uri,
                String localName,
                String name,
                Attributes atts)
                throws SAXException
            {
                ch.startElement(uri, localName, name, atts);
            }

        };
    }

    private static void unexpectedElement(QName qname, Collection<QName> required)
    throws SAXException
    {
        throw new SAXException("Unexpected element: " + qname +
            " Expecting one of " + required);

    }


    @SuppressWarnings("unchecked")
    public static ElementContentHandler byElementName(
        QName required,
        NamedElementHandler handler,
        ElementContentHandler undefined)
    {
        return byElementName(
            Maps.map(Maps.entry(required, handler)), undefined);
    }

    public static Writer charactersWriter(final ContentHandler importch)
    {
        return new Writer() {
            public void close() { }
            public void flush() { }
            public void write(char[] cbuf, int off, int len)
                throws IOException
            {
                try {
                    importch.characters(cbuf, off, len);
                } catch (SAXException e) {
                    throw new IOException("Error writing characters", e);
                }
            }

        };
    }

    public static ContentHandler contentHandler(Writer writer)
    throws SAXException
    {
        SAXTransformerFactory factory =
            (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler handler;
        try {
            handler = factory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new SAXException("Error creating identity transform", e);
        }
        handler.setResult(new StreamResult(writer));
        return handler;
    }

    public void applyPrefixMappings(ContentHandler other)
    throws SAXException
    {
        for(Map.Entry<String, String> mapping: getPrefixMappings().entrySet())
            other.startPrefixMapping(mapping.getKey(), mapping.getValue());
    }

    // ------------------------------------------------------------------------

    public Locator getDocumentLocator()
    {
        return documentLocator;
    }

    public Map<String, String> getPrefixMappings()
    {
        return Collections.unmodifiableMap(prefixMappings);
    }


}
