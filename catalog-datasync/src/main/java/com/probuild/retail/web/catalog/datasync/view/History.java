package com.probuild.retail.web.catalog.datasync.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

import com.probuild.retail.web.catalog.datasync.domain.JobExecution;
import com.probuild.retail.web.catalog.datasync.service.DashboardService;

public class History extends WebPage implements Serializable {

    @Logger
    protected Log log;
    
    @In(create=true)
    protected DashboardService dashboardService;
    
    private static final long serialVersionUID = 1L;

    List<JobExecution> executions;
    
    /**
     * 
     * @param parameters
     */
    public History( PageParameters params ) {
        super();
        
        Long jobId = params.getLong( "jobId" );
        executions = dashboardService.getJobHistory( jobId );
        
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

        final SimpleDateFormat sdf = new SimpleDateFormat ( "EEE M/d/yyyy h:mm:ss a" );
        
        add ( new Link ( "homeLnk" ) {

            @Override
            public void onClick() {
                setResponsePage( DashboardHome.class );
            }
            
        });
        
        add ( new Link ( "manageLnk" ) {

            @Override
            public void onClick() {
                setResponsePage( ManageJobs.class );
            }
            
        });
        
        // set current job name
        if ( executions != null && executions.size() > 0 )
            add( new Label ( "jobName", executions.get(0).getJobName() ) );
        else
            add( new Label ( "jobName", "No history available" ) );
        
        
        add ( new ListView ( "history", executions ) {

            @Override
            protected void populateItem( ListItem item ) {
                final JobExecution exec = (JobExecution)item.getModelObject();
                
                item.add( new Label ( "rowNumber", 
                                Integer.toString( item.getIndex() + 1 ) ) );
                item.add( new Label ( "jobRunTimeStamp", 
                                sdf.format( exec.getStartTime().getTime() ) ) );
                Link historyLink = new Link ( "historyLnk" ) {
                
                    @Override
                    public void onClick() {
                        PageParameters params = new PageParameters();
                        params.add( "jobExecId", exec.getId().toString() );
                        setResponsePage( JobResult.class, params );
                        
                
                    }
                };
                
                item.add ( historyLink );
            }
            
        });
        
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
