package com.probuild.retail.web.catalog.datasync.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

import com.probuild.retail.web.catalog.datasync.domain.DashboardItem;
import com.probuild.retail.web.catalog.datasync.domain.Job;
import com.probuild.retail.web.catalog.datasync.service.DashboardService;
import com.probuild.retail.web.catalog.datasync.service.SchedulerService;

public class DashboardHome extends WebPage {

    private enum StatusIcon { 
        GOOD("img/weather-clear.png"),
        FAILING("img/weather-few-clouds.png"),
        FAILED("img/weather-showers-scattered.png");
        
        private String path;
        StatusIcon ( String imgPath ) {
            this.path = imgPath;
        }
        
        public String getImgPath ( ) { return path; }
    } 
    
    @Logger
    protected Log logger;
    
    @In
    protected EntityManager em;

    @In(create=true)
    protected DashboardService dashboardService;
    
    @In(create=true)
    protected SchedulerService schedulerService;
    
    // remove this after DB logic placed in service?
    protected void onBeforeRender() {
       logger.debug( "-/-/-/-/-/-/-/ Begin transaction" );
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
        logger.debug( "-/-/-/-/-/-/-/ End transaction" );
        
        try {
            org.jboss.seam.transaction.Transaction.instance().commit();
        }
        catch(Exception e) {
            System.out.println ( "Commit exception " + e.getMessage() );
        }
        
        super.onAfterRender();
    }
    
    
    public DashboardHome( PageParameters parameters ) {
        
        // load jobs
        List<DashboardItem> jobs = dashboardService.getItems();
        
        logger.debug( "Jobs found " + jobs.size() );
        
        add ( new ListView ( "jobs", jobs ) {

            @Override
            protected void populateItem(ListItem item) {
                final DashboardItem ditem = (DashboardItem)item.getModelObject();
                SimpleDateFormat sdf = new SimpleDateFormat ( "EEEE yyyy-M-d, h:mm:ss a" );
                
                // fill out the job's details
                item.add( new Label ( "jobTitle", ditem.getJob().getJobName() ) );
                if ( ditem.getLastRun() == null )
                    item.add( new Label ( "jobRunTimeStamp", "Last run [unknown], " + ditem.getOutcome() ) );
                else
                    item.add( new Label ( "jobRunTimeStamp", "Last run " + sdf.format( ditem.getLastRun().getTime() ) + ", " + ditem.getOutcome() ) );
                
                item.add( new Label ( "jobCount", ditem.getSuccessCount() + "/" + ditem.getJob().getMaxHistoryCount() ) );

                Image img = new Image("jobStatusIcon") {
                    @Override
                    protected void onComponentTag( ComponentTag tag ) {
                        
                        super.onComponentTag(tag);
                        String src = (String) tag.getAttributes().get("src");
                        if ( "SUCCESS".equals( ditem.getOutcome() ) )
                            src = StatusIcon.GOOD.getImgPath();
                        else if ( "FAILED".equals( ditem.getOutcome() ) )
                            src = StatusIcon.FAILED.getImgPath();
                        else
                            src = StatusIcon.FAILING.getImgPath();
                        
                        tag.getAttributes().put("src", src);
                    }
                };
                
                item.add( img );
                
                Link historyLink = new Link ( "historyLnk" ) {

                    @Override
                    public void onClick() {
                        PageParameters params = new PageParameters();
                        params.add( "jobId", ditem.getJob().getId().toString() );
                        
                        setResponsePage( History.class, params );
                    }
                    
                };
                item.add( historyLink );
                
                Model model = new Model( ditem.getJob() );
                
                Link runNowLink = new Link ( "runNowLnk", model ) {

                    @Override
                    public void onClick() {
                        Job job = (Job)getModelObject();
                        logger.debug( "Running #0 now", job.getJobName() );
                        schedulerService.runJobNow( job );
                        
                        PageParameters params = new PageParameters();
                        params.add( "jobId", ditem.getJob().getId().toString() );
                        
                        setResponsePage( History.class, params );
                    }
                    
                };
                item.add( runNowLink );
            }
            
        }.setReuseItems( true ) );
     
        add ( new Link( "manageBtn" ) {

            @Override
            public void onClick() {
                setResponsePage( ManageJobs.class );
            }
            
        });
        
    }
    
    public class AddRecordForm extends Form {

        private static final long serialVersionUID = 1L;

        public AddRecordForm ( String id ) {
            super ( id );
        }
        
        public void onSubmit ( ) {
            System.out.println ( "Submit, adding record" );
            
            Job job = new Job ( );
            job.setJobName( "My Second Job" );
            job.setCreateDate( Calendar.getInstance() );
            job.setActive( true );
            job.setEmailList( "joseph.simmons@probuild.com" );
            job.setMaxHistoryCount( 5 );
            job.setScriptName( "run.jpy" );
            job.setSchedule( "* * * * *" );
            
            em.persist( job );
            
            System.out.println ( "Persist complete " + job.getId() );
            
            //setResponsePage( DashboardHome.class );
        }
    }



}
