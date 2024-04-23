package com.probuild.retail.web.catalog.datasync.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

import com.probuild.retail.web.catalog.datasync.domain.Job;
import com.probuild.retail.web.catalog.datasync.domain.JobExecution;
import com.probuild.retail.web.catalog.datasync.service.DashboardService;

public class JobResult extends WebPage implements Serializable {

    @Logger
    protected Log log;
    
    @In(create=true)
    protected DashboardService dashboardService;
    
    private static final long serialVersionUID = 1L;

    
    protected Job job;
    protected JobExecution jobExecution;
    
    /**
     * Arriving at this page will have parameters indicating
     * job and execution ids.
     * 
     * @param parameters
     */
    public JobResult( PageParameters parameters ) {
        super();
        
        //Long jobId = parameters.getLong( "jobId" );
        Long jobExecId = parameters.getLong( "jobExecId" );
        
        jobExecution = dashboardService.findJobExecution( jobExecId );
        job = jobExecution.getJob();
        
        init ( );
    }
    
    
    // remove this after DB logic placed in service?
    protected void onBeforeRender() {
       log.debug( "-/-/-/-/-/-/-/ Begin transaction" );
       try {
           org.jboss.seam.transaction.Transaction.instance().begin();
        }
        catch(NotSupportedException e) {
            System.out.println ( "Unsupported operation" + e.getMessage() );
        }
        catch(SystemException e) {
            System.out.println ( "System exception " + e.getMessage() );
        }
        
        super.onBeforeRender();
    }
    
    // remove this after DB logic placed in service?
    protected void onAfterRender() {
        log.debug( "-/-/-/-/-/-/-/ End transaction" );
        
        try {
            org.jboss.seam.transaction.Transaction.instance().commit();
        }
        catch(Exception e) {
            System.out.println ( "Commit exception " + e.getMessage() );
        }
        
        super.onAfterRender();
    }

    
    private void init ( ) {
        SimpleDateFormat sdf = new SimpleDateFormat ( "EEE M/d/yyyy h:mm:ss a" );
        String home = System.getProperty( "dashboard.home" );
        
        add ( new Link ( "homeLnk" ) {

            @Override
            public void onClick() {
                setResponsePage( DashboardHome.class );
            }
            
        });
        
        add ( new Link ( "manageLnk" ) {

            @Override
            public void onClick() {
                setResponsePage( JobResult.class );
            }
            
        });
        
        // set current job name
        add ( new Label ( "jobName", job.getJobName() ) );
        add ( new Label ( "jobStart", sdf.format( jobExecution.getStartTime().getTime() ) ) );
        
        String output = readInConsoleOut( 
                     home + "/history/" + jobExecution.getConsoleOutFile() );
       
        add ( new MultiLineLabel ( "jobOutput", output ) );
    }
    
    
    
    private String readInConsoleOut ( String fileName ) {
        String buff = "";
        
        try {
            buff = FileUtils.readFileToString( new File ( fileName ) );
        }
        catch(IOException e) {
            log.warn( "Could not read file #0", e.getMessage(), e );
        }
        
        return buff;
    }

    
}
