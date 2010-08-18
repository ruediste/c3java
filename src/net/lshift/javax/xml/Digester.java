package net.lshift.javax.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import net.lshift.java.dispatch.DynamicDispatch;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Digester
{
    private DocumentBuilder builder;
    private Document document;
    private Node contextNode;
 
    private List<Rule> rules;
    
    public interface Rules
    {
        
    }

    
    
    public interface Rule
    {
        public XPathExpression getXPathExpression();
        public void process(XMLEventReader in);
    }

    // ------------------------------------------------------------------------
    
    /**
     * Dynamic dispatch interface
     * @see ContextTrackerImpl
     */
    public interface ContextTracker
    {
        public void process(XMLEvent event);
    }
    
    /**
     * Track the context node.
     */
    class ContextTrackerImpl
    {
        public void process(StartDocument event) {
            document = builder.newDocument();
            contextNode = document;
        }
        
        public void process(EndDocument event) {
            document = builder.newDocument();
            contextNode = document;
        }
        
        public void process(StartElement event) {
            QName name = event.getName();
            Element element = document.createElementNS(
                name.getNamespaceURI(), name.getLocalPart());
            
            Iterator<Attribute> attributes = attributes(event);
            while(attributes.hasNext()) {
                Attribute attr = attributes.next();
                element.setAttributeNS(attr.getName().getNamespaceURI(),
                    attr.getName().getLocalPart(), 
                    attr.getValue());
            }
            
            contextNode.appendChild(element);
            contextNode = element;
        }

        @SuppressWarnings("unchecked")
        private Iterator<Attribute> attributes(StartElement event) {
            return (Iterator<Attribute>)event.getAttributes();
        }
        
        public void process(EndElement event) {
            Node parent = contextNode.getParentNode();
            parent.removeChild(contextNode);
            contextNode = parent;
        }
        
        public void process(XMLEvent event) {
            
        }
    }
    
    public ContextTracker contextTracker = 
        DynamicDispatch.proxy(ContextTracker.class, new ContextTrackerImpl());
    
    // ------------------------------------------------------------------------

    /**
     * Dynamic dispatch interface
     * @see ApplyImpl
     */
    public interface Apply
    {
        public void process(Object context, XMLEvent event, XMLEventReader in)
        throws XPathExpressionException, XMLStreamException;
    }

    /**
     * Search for matching methods to dispatch this event to
     * @see Apply
     */
    public class ApplyImpl
    {
        public void process(Object context, StartElement event, XMLEventReader in) 
        throws XPathExpressionException, XMLStreamException 
        {
            for(Rule rule: rules) {
                if(matches(rule)) {
                    XMLEventReader elementReader = childrenReader(in);
                    rule.process(elementReader);

                    // consume any input that the rule doesn't consume
                    while(elementReader.hasNext())
                        elementReader.next();
                    break;
                }
            }
        }
    }
    
    private boolean matches(Rule rule)
    throws XPathExpressionException
    {
        return ((NodeList)rule.getXPathExpression().evaluate(contextNode, XPathConstants.NODESET)).getLength() > 0;
    }

    public Apply apply = 
        DynamicDispatch.proxy(Apply.class, new ApplyImpl());

    // ------------------------------------------------------------------------
    
    public void apply(Object context, XMLEventReader in)
    throws XMLStreamException, XPathExpressionException
    {
        while(in.hasNext()) {
            XMLEvent event = in.nextEvent();
            apply.process(context, event, in);
        }
    }

    public XMLEventReader contextReader(XMLEventReader in)
    {
        return new EventReaderDelegate(in) {
            public XMLEvent next() {
                XMLEvent event = (XMLEvent)super.next();
                contextTracker.process(event);
                return event;
            }
        };
    }

    /**
     * A reader which allows reading from the current event,
     * through to the matching end element event.
     * @param in the stream, with the current event being an element
     * @return a stream which allows reading up to and including
     * the matching end element;
     */
    public static XMLEventReader childrenReader(final XMLEventReader in)
    {
        return new EventReaderDelegate(in) {

            int depth = 0;
            
            public boolean hasNext() {
                try {
                    return !(depth == 0 && (super.peek() instanceof EndElement));
                } catch (XMLStreamException e) {
                    return false;
                }
            }

            public XMLEvent next() {
                if(!hasNext())
                    throw new IllegalStateException();

                XMLEvent event = (XMLEvent)super.next();
                
                switch(event.getEventType()) {
                case START_ELEMENT:
                    depth++;
                    break;
                case END_ELEMENT:
                    depth--;
                    break;
                default:
                    break;
                }

                return event;
            }
        };
    }
}
