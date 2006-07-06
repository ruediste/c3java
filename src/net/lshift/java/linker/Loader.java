package net.lshift.java.linker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Loader
{
    public static Class getMain() 
        throws Exception
    {
        Manifest manifest = new Manifest();
        manifest.read(ClassLoader.getSystemResourceAsStream("LOADER-INF/LOADER.MF"));
        Attributes main = manifest.getMainAttributes();
        String classPath = (String)main.get(Attributes.Name.CLASS_PATH);
        StringTokenizer pathTokens = new StringTokenizer(classPath);
        URL path []  = new URL[pathTokens.countTokens()];
        for(int i = 0; i != path.length; ++i) {
            InputStream in = ClassLoader.getSystemResourceAsStream
                ("LOADER-INF/lib/" + pathTokens.nextToken());
            try {
                /*
                     This isn't what I planned to do. You might expect that
                     since I can get a URL for the jar, I could give that
                     to URLClassLoader and it would be able to deal with URLs
                     returned from getSystemResource(). Unfortunately no classes
                     get found.
                     
                     This creates tempory files for all the jars and
                     constructs a class loader using those. They get
                     deleted when the JRE exits. It seems clunky, but is
                     as convenient as you would expect.
                 */
                File tmp = File.createTempFile("linker", ".jar");
                OutputStream out = new FileOutputStream(tmp);
                try {
                    byte [] buffer = new byte[1024]; 
                    int read = 0;
                    while(read != -1) {
                        read = in.read(buffer);
                        if(read > 0) {
                            out.write(buffer, 0, read);
                        }
                    }
                }
                finally {
                    out.close();
                }
                
                path[i] = tmp.toURL();
            }
            finally {
                in.close();
            }
        }
        
        ClassLoader loader = new URLClassLoader(path);
        return Class.forName((String)main.get(Attributes.Name.MAIN_CLASS), true, loader);
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try {
            Class mainClass = getMain();
            Method main = mainClass.getMethod("main", new Class [] { String[].class });
            main.invoke(null, new Object [] { args });
        }
        catch(InvocationTargetException e) {
            // main has no declared exceptions, so this must be a runtime exception
            throw (RuntimeException)e.getCause();
        }
        catch(Exception e) {
            System.err.println("This jar is invalid: " + e);
            e.printStackTrace();
        }
    }

}
