package net.lshift.java.linker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import net.lshift.java.io.Copy;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class Linker
    extends Task
{
    public static final String INF = "LOADER-INF";
    public static final String LIB = INF + File.separator + "lib" + File.separator;
    
    private String mainClass;
    private File dest;
    private List<FileSet> jars = new ArrayList<FileSet>();
    
    public FileSet createJars()
    {
        FileSet fileset = new FileSet();
        this.jars.add(fileset);
        return fileset;
    }

    public File getDest()
    {
        return dest;
    }

    public void setDest(File dest)
    {
        this.dest = dest;
    }
    


    public String getMainClass()
    {
        return mainClass;
    }

    public void setMainClass(String mainClass)
    {
        this.mainClass = mainClass;
    }

    public static ZipEntry directory(String name)
    {
        return new ZipEntry(name + '/');
    }
    
    public void link()
        throws FileNotFoundException, IOException
    {
        byte [] buffer = new byte[1024];
        
        JarOutputStream out = new JarOutputStream(new FileOutputStream(dest));
        out.putNextEntry(directory("LOADER-INF"));
        out.putNextEntry(directory("LOADER-INF/lib"));

        StringBuffer classPath = new StringBuffer();
        for(Iterator i = this.jars.iterator(); i.hasNext(); ) {
            FileSet fileset = (FileSet)i.next();
            String jars []  = fileset.getDirectoryScanner(getProject()).getIncludedFiles();
            File base = fileset.getDirectoryScanner(getProject()).getBasedir();
            for(int j = 0; j != jars.length; ++j) {
                classPath.append(jars[j] + " ");
                out.putNextEntry(new ZipEntry(LIB + jars[j]));
                InputStream in = new FileInputStream(new File(base, jars[j]));
                Copy.copy(in, out, buffer);
            }
        }
        
        {
            Manifest loaderInf = new Manifest();
            Attributes main = loaderInf.getMainAttributes();
            main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            main.put(Attributes.Name.MAIN_CLASS, mainClass);
            main.put(Attributes.Name.CLASS_PATH, classPath.toString());
            out.putNextEntry(new ZipEntry("LOADER-INF/LOADER.MF"));
            loaderInf.write(out);
        }
        
        {
            out.putNextEntry(directory("META-INF"));
            Manifest metaInf = new Manifest();
            Attributes main = metaInf.getMainAttributes();
            main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            main.put(Attributes.Name.MAIN_CLASS, Loader.class.getName());
            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            metaInf.write(out);
        }
        
        {
            String path = Loader.class.getName().replaceAll("\\.", "/");
            InputStream in = getClass().getClassLoader().getResourceAsStream(path + ".bin");
            if(in == null)
                throw new FileNotFoundException("could not find loader at " + path + ".bin");
            out.putNextEntry(new ZipEntry(path + ".class"));
            Copy.copy(in, out, buffer);
        }
        
        out.close();
    }
    
    public void execute()
        throws BuildException
    {
        try {
            link();
        }
        catch(Exception e) {
            throw new BuildException(e);
        }
    }
}
