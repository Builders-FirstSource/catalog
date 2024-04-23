package com.probuild.retail.web.catalog.datasync.job;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Lifecycle;
import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.probuild.retail.web.catalog.datasync.domain.JobExecution;
import com.probuild.retail.web.catalog.datasync.service.DashboardService;

public class BasicJythonJob implements Job {


    /**
     *  Default constructor
     */
    public BasicJythonJob() {
        super();
    }

    public void execute(JobExecutionContext context ) throws JobExecutionException {
       
        String home = System.getProperty( "dashboard.home" );
        
        // get the job out the context
        com.probuild.retail.web.catalog.datasync.domain.Job execJob = 
                (com.probuild.retail.web.catalog.datasync.domain.Job)
                            context.getJobDetail().getJobDataMap().get( "job" );

        System.out.println ( "Running job " + 
                execJob.getJobName() + " script " + execJob.getScriptName() );
        
        
        Lifecycle.beginCall(); // so we can use seam
        DashboardService service = 
            (DashboardService)Component.getInstance( "dashboardService", true );
        

        // create execution record
        JobExecution exec = null;
        try {
            exec = createJobExecRecord( execJob, service );
        }
        catch(Exception e) {
            System.out.println ( 
                        "Save of execution record failed " + e.getMessage() );
            Lifecycle.endCall();
            return;
        }
        
         
        // redirect stdout, stderr
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        PrintStream ps = getNewStdOutStdErrPrintStream( 
                        home + "/history/" + exec.getConsoleOutFile() );
        System.setOut( ps );
        System.setErr( ps );
        
        
        System.out.println ( "Start python interp" );
        ClassLoader cl = createJobClassLoader();
        Thread.currentThread().setContextClassLoader( cl ); // need this to isolate jython classpath
        
        System.out.println ( "Classloader created" );

        PythonInterpreter interp = new PythonInterpreter( );
        System.out.println ( "Pyhton interp started" );
        
        interp.setOut( ps ); // tell jython to redirect out to file
        interp.setErr( ps );
        
        
        
        String successValue = "FAILED";
        long startMillis = System.currentTimeMillis();
        try {
            System.out.println ( "running " +  execJob.getScriptName() );
            ps.print( "script: " + execJob.getScriptName() );
            
            interp.execfile( home + "/scripts/" + execJob.getScriptName() );
            
            
            successValue = "SUCCESS";
            System.out.println ( "Script executed without error" );
            //ps.print( "- - END - -" );
        } catch ( Exception e ) {
            
            if ( e instanceof PyException ) {
                PyException pe = (PyException)e;
                //System.out.println ( "Python exception " + pe.type.toString() );
                if ( "<type 'exceptions.SystemExit'>".equals( pe.type.toString() ) ) {
                    successValue = "SUCCESS";
                    System.out.println ( "Scripted ended via sys.exit()" );
                } else {
                    System.out.println ( "Script failed while running " + e.getMessage() );
                    e.printStackTrace();
                }
            } else {
                // if any exceptions script probably failed
                System.out.println ( "Script failed while running " + e.getMessage() );
                e.printStackTrace();
            }
            
        } finally {
            interp.cleanup();
            ps.flush();
            ps.close();            
        }
        long endMillis = System.currentTimeMillis();


        
        // switch back stout, stderr
        System.setOut( stdout );
        System.setErr( stderr );
        
        // update execution record
        try {
            System.out.println ( "Update job run records" );
            updateJobExecRecord( exec, service, successValue, 
                                                    endMillis - startMillis );
        }
        catch(Exception e) {
            System.out.println ( 
                       "Update of execution record failed " + e.getMessage() );
            Lifecycle.endCall();
            return;
        }
        
        
        // delete older history if needed
        deleteHistory( execJob, service );
        
        Lifecycle.endCall(); // tell seam we are done
        
        
    }
    
    
    private JobExecution createJobExecRecord ( 
            com.probuild.retail.web.catalog.datasync.domain.Job j,
                    DashboardService service ) throws Exception {
        JobExecution exec = new JobExecution();
        exec.setStartTime( Calendar.getInstance() );
        exec.setStatus( "RUNNING" );
        exec.setJobName( j.getJobName() );
        exec.setScriptName( j.getScriptName() );
        exec.setJob( j );
        service.saveJobExecution( exec );
        
        String outFileName = "exec_" + exec.getId() + "_job" + j.getId() + ".out"; 
        exec.setConsoleOutFile( outFileName );
        
        service.saveJobExecution( exec ); // save the outFileName
        
        return exec;
    }

    
    private void updateJobExecRecord ( 
                        JobExecution exec, DashboardService service, 
                              String outcome, long runtime ) throws Exception {

        exec.setEndTime( Calendar.getInstance() );
        exec.setStatus( outcome );
        exec.setRunTime( runtime );
        
        service.saveJobExecution( exec );

    }
    
    private PrintStream getNewStdOutStdErrPrintStream ( String fileName ) {
        PrintStream ps = null;
        try {
            ps = new PrintStream( new BufferedOutputStream(
                                    new FileOutputStream( fileName )));
            
        }
        catch(FileNotFoundException e) {
            System.out.println ( "Could not switch output " + e.getMessage() );
        }
        
        return ps;
    }
    
    
    private void deleteHistory ( 
            com.probuild.retail.web.catalog.datasync.domain.Job j, 
                                                   DashboardService service ) {
        
        String home = System.getProperty( "DATASYNC_HOME" );
        
        // how much history exists now
        List<JobExecution> executions = service.getJobHistory( j.getId() );
        
        if ( executions.size() <= j.getMaxHistoryCount() )
            return; // nothing to do
        
        // list is ordered starting with most recent
        for ( int i = executions.size() - 1; i > j.getMaxHistoryCount(); i-- ) {
            
            JobExecution exec = executions.get( i );
            System.out.println ( "Deleting history " + exec.getConsoleOutFile() );
            try {
                service.removeJobExecution( exec.getId() );
                
                // delete the output on the file system
                FileUtils.forceDelete( new File ( 
                        home + "/history/" + exec.getConsoleOutFile() ) );
                
            } catch ( Exception e ) { 
                System.out.println ( "Could not delete job execution " + exec.getJobName() );
            }

        }
        
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

        ClassLoader loader = new URLClassLoader( urlList, Thread.currentThread().getContextClassLoader() );
        
        return loader;
    }
    
}
