package net.lshift.java.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import net.lshift.java.util.Transform;

/**
 * Serialization for object graph traversal.
 * Serialization provides a reasonably convenient way to traverse
 * and copy an object graph. We are led to believe its quite efficiently
 * implemented compared to using reflection.
 */
public class Serialization
{
    /**
     * Replace objects in an object graph before writing them
     * to an output stream. You might use this to replace non
     * serializable classes with ones that are, or to do some
     * kind of normalisation before generating the stream.
     * See DyanamicDispatch.proxy for a convenient way to construct the
     * transform based on object type.
     * This is implemented using ObjectOutputStream.replaceObject(Object),
     * you should read its documentation.
     * @see net.lshift.java.dispatch.DynamicDispatch#proxy(Class, Object)
     * @see ObjectOutputStream#replaceObject(Object)
     * @param transform
     * @param out
     * @return
     * @throws IOException
     */
    public static ObjectOutputStream mapOutputStream(
        final Transform<Object,Object> transform, 
        OutputStream out) 
    throws IOException
    {
        // This is so simple you wonder if there is any point defining it,
        // its just here as documentation, so it can be unit tested.
        return new ObjectOutputStream(out) {

            { this.enableReplaceObject(true); }
            
            @Override
            protected Object replaceObject(Object obj)
                throws IOException
            {
                return transform.apply(obj);
            }
        };
    }
    
    public static ObjectInputStream mapInputStream(
        final InputStream in, 
        final Transform<Object,Object> transform) 
    throws IOException
    {
        
        ObjectInputStream mappedIn = new ObjectInputStream(in) {
            
            { this.enableResolveObject(true); }
            
            @Override
            protected Object resolveObject(Object obj)
                throws IOException
            {
                return transform.apply(obj);
            }
            
        };
        
        return mappedIn;
    }
    
    /**
     * Apply a transforms during clone.
     * @param marshalTransform applied while serialising the object
     * @param unmarshalTransform applied de-serializing the object.
     * @param unmarshalTransform
     * @param source
     * @return
     */
    public static Object map(
        Transform<Object,Object> marshalTransform,
        Transform<Object,Object> unmarshalTransform,
        Object source)
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = 
                marshalTransform == null 
                    ? new ObjectOutputStream(buffer) 
                    : mapOutputStream(marshalTransform, buffer);
            out.writeObject(source);
            out.flush();
            InputStream bufferIn = new ByteArrayInputStream(buffer.toByteArray());
            return (unmarshalTransform == null 
                            ? new ObjectInputStream(bufferIn)
                            : mapInputStream(bufferIn, unmarshalTransform)
                   ).readObject();
        }
        catch(IOException e) {
            // This probably means source, or something transform.apply returned
            // aren't serializable.
            // TODO: find out what these exceptions say, and make sure
            // meaningful messages are generated.
            throw new IllegalArgumentException(e);
        } catch (ClassNotFoundException e) {
            // This could happen because we don't have the class loader used
            // to load classes in source, or returned by transform.
            // TODO: find out what these exceptions say, and make sure
            // meaningful messages are generated.
            throw new IllegalArgumentException(e);
        }
    }
    

}
