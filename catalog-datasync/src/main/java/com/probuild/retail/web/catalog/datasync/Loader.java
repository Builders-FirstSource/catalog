package com.probuild.retail.web.catalog.datasync;


import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
//import org.python.util.PythonInterpreter;

public class Loader {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty( "dashboard.home", "C:/Documents and Settings/jsimmons/workspace/fundcontrol/fundcontrol/fundcontrol-inspection-mgr/target/datasync" );
        
        String home = System.getProperty( "dashboard.home" );
        
        // redirect stdout, stderr
        //PrintStream stdout = System.out;
        //PrintStream stderr = System.err;
        //PrintStream ps = getNewStdOutStdErrPrintStream( 
        //                home + "/history/out.txt" );
        //System.setOut( ps );
        //System.setErr( ps );
        
        
        Collection<File> files = FileUtils.listFiles( new File(home + "/lib/"), new SuffixFileFilter(".jar"), TrueFileFilter.INSTANCE );

        List<File> file = new ArrayList(files);
        
        URL[] path = new URL[files.size()];
        try {
            
            for ( int i = 0; i < files.size(); i++ ) {
                System.out.println ( "adding file to classpath: " + file.get(i).getName() );
                path[i] = file.get(i).toURI().toURL();
            }
            
        } catch (MalformedURLException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        ClassLoader loader = new URLClassLoader( path );
        
        
        

        
        
        System.out.println ( "Start python interp" );
        
        Class<?> interp = null;
        Object instance = null;
        try {
            interp = loader.loadClass("org.python.util.PythonInterpreter");
            instance = interp.newInstance();
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
        //PythonInterpreter interp = new PythonInterpreter();
        System.out.println ( "Pyhton interp started" );
        //interp.setOut( ps ); // tell jython to redirect out to file
        //interp.setErr( ps );
        String successValue = "FAILED";
        long startMillis = System.currentTimeMillis();
        try {
            System.out.println ( "running script" );
            //ps.print( "script: " + execJob.getScriptName() );
            
            Method theMethod = interp.getMethod("execfile", String.class);
            theMethod.invoke(instance, home + "/scripts/helloworld.py" );
            
            interp.getMethod( "cleanup" ).invoke( instance );
            
            //interp.execfile( home + "/scripts/helloworld.py" );
            //interp.cleanup();
            successValue = "SUCCESS";
            //ps.print( "- - END - -" );
        } catch ( Exception e ) {
            // if any exceptions script probably failed
            System.out.println ( "Script failed while running " + e.getMessage() );
            e.printStackTrace();
        } finally {
            //ps.flush();
            //ps.close();            
        }
        long endMillis = System.currentTimeMillis();

    }

}
