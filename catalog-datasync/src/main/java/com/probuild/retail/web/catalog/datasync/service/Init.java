package com.probuild.retail.web.catalog.datasync.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.python.util.PythonInterpreter;

@Name("initializeDashboard")
@Startup
@Scope(ScopeType.APPLICATION)
public class Init {

    @Create
    public void startup ( ) {
        System.out.println ( "----->->-> startup" );
        
        //ClassLoader cl = createJobClassLoader();
        ////Py.getSystemState().setClassLoader( cl );
        //PySystemState systemState = new PySystemState();
        //systemState.setClassLoader( cl );
        PythonInterpreter interp = new PythonInterpreter();//null, systemState);
        
        System.out.println ( "Python prestarted" );
    }
    
    @Destroy
    public void destory ( ) {
        
    }
    
    private ClassLoader createJobClassLoader ( ) {
        
        String home = System.getProperty( "dashboard.home" );
        
        
        File classes = new File ( home + "/classes/" );
        Collection<File> files = FileUtils.listFiles( new File(home + "/lib/"), new SuffixFileFilter(".jar"), TrueFileFilter.INSTANCE );

        List<File> fileList = new ArrayList(files);
        
        int fileSize = files.size();
        if ( classes.exists() )
            fileSize++;
        
        List<URL> urls = new ArrayList<URL>(fileSize); 
        //URL[] path = new URL[fileSize];
        

        try {
            
            if ( classes.exists() ) {
                System.out.println ( "adding classes folder" );
                urls.add( classes.toURI().toURL() );
                System.out.println ( urls.get(0).getPath() );
            }
            
            for ( File f : fileList ) {
                System.out.println ( "adding file to classpath: " + f.getName() );
                urls.add( f.toURI().toURL() );
            }
            
            
        } catch (MalformedURLException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        URL[] urlList = urls.toArray( new URL[fileSize] );
        for ( URL u : urlList )
            System.out.println ( "add: " + u.getPath() );
        
        ClassLoader loader = new URLClassLoader( urlList, Thread.currentThread().getContextClassLoader() );
        
        return loader;
    }
    
    
}
