package net.lshift.javax.xml.sax.helpers;

import static net.lshift.javax.xml.sax.helpers.QName.qname;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.lshift.java.dispatch.Delegate;
import net.lshift.java.dispatch.DynamicDispatch;
import net.lshift.java.lang.Variable;
import net.lshift.java.util.Lists;

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
     * content handler.
     * @see #getDocumentLocator()
     */
    public final DocumentLocatorContentHandler
        documentLocatorHandler = new DocumentLocatorContentHandler() {
            public void setDocumentLocator(Locator locator) {
                ContentHandlerBuilder.this.documentLocator = locator;
            }
    };

    public final PrefixMappingContentHandler
        prefixMappingHandler = new PrefixMappingContentHandler() {
            public void endPrefixMapping(String prefix) {
                prefixMappings.remove(prefix);
            }

            public void startPrefixMapping(String uri, String prefix) {
                prefixMappings.put(prefix, uri);
            }
        
    };
    
    public ContentHandlerBuilder()
    {
        // We know the document is going to start, we don't need to do anything
        override(Delegate.noop(DocumentContentHandler.class));
        
        // Ignoring ignore-able whitespace seems like a safe default
        override(Delegate.noop(IgnorableWhitespaceContentHandler.class));
        
        override(documentLocatorHandler);
        override(prefixMappingHandler);
        
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

    public static ElementContentHandler mixed(
        final Map<QName, NamedElementHandler> handlers)
    {
        return new ElementContentHandler() {

            private NamedElementHandler getHandler(String uri, String localName) 
            throws SAXException
            {
                QName qname = qname(uri, localName);
                if(handlers.containsKey(qname)) {
                    return handlers.get(qname);
                }
                else {
                    unexpectedElement(qname, handlers.keySet());
                    return null;
                }
            }
            
            @Override
            public void endElement(String uri, String localName, String name)
                throws SAXException
            {
                getHandler(uri, localName).endElement();
            }


            @Override
            public void startElement(
                String uri,
                String localName,
                String name,
                Attributes atts)
                throws SAXException
            {
                getHandler(uri, localName).startElement(atts);
            }
            
        };
    }

    public static ElementContentHandler listOf(
        final QName required,
        final NamedElementHandler handler)
    {
        return new ElementContentHandler() {
            
            public void startElement(
                String uri,
                String localName,
                String name,
                Attributes atts)
            throws SAXException
            {
                if(required.equals(qname(uri, localName))) {
                    handler.startElement(atts);
                }
                else {
                    unexpectedElement(qname(uri, localName), required);
                }
            }

            public void endElement(
                String uri,
                String localName,
                String name)
            throws SAXException
            {
                if(required.equals(qname(uri, localName))) {
                    handler.endElement();
                }
                else {
                    unexpectedElement(qname(uri, localName), required);
                }
            }
            
            public String toString() {
                return required + " => " + handler;
            }
        };
    }

    private static void unexpectedElement(QName qname, QName required) 
    throws SAXException
    {
        throw new SAXException("Unexpected element: " + qname +
            " Expecting " + required);

    }

    private static void unexpectedElement(QName qname, Collection<QName> required) 
    throws SAXException
    {
        throw new SAXException("Unexpected element: " + qname +
            " Expecting one of " + required);

    }

    public Locator getDocumentLocator()
    {
        return documentLocator;
    }


}
