package com.probuild.retail.web.catalog.datasync.view;

import java.io.Serializable;
import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

import com.probuild.retail.web.catalog.datasync.domain.Job;
import com.probuild.retail.web.catalog.datasync.service.DashboardService;
import com.probuild.retail.web.catalog.datasync.service.SchedulerService;

public class ManageJobs extends WebPage implements Serializable {

    @Logger
    protected Log log;
    
    @In(create=true)
    protected DashboardService dashboardService;
    @In(create=true)
    protected SchedulerService schedulerService;
    
    private static final long serialVersionUID = 1L;

    
    protected List<Job> jobs;
    protected Job newJob;
    
    private Form newJobForm;
    private ListView jobList;
    
    public ManageJobs() {
        super();
        
        initJobs();
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
    
    
    private void initJobs ( ) {
        jobs = dashboardService.getAllJobs();
    }

    
    private void init ( ) {
        add ( new FeedbackPanel( "errors" ).setOutputMarkupId( true ) );
        
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
        
        jobList = new ListView ( "jobs", jobs ) {

            
            @Override
            protected void populateItem(ListItem item) {
                final Job job = (Job)item.getModelObject();
                
                // fill out the job's details
                CompoundPropertyModel model = new CompoundPropertyModel( job );
                Form form = new JobDetailsForm( "manageJobForm", model );
                item.add ( form );
                Button deleteBtn = new Button ( "deleteBtn", model ) {

                    @Override
                    public void onSubmit() {
                        Job job = (Job)getModelObject();
                        removeJob( job );
                        super.onSubmit();
                    } 
                    
                };
                deleteBtn.setDefaultFormProcessing( false );
                form.add( deleteBtn );

                
                Button saveBtn = new Button ( "saveBtn", model ) {

                    @Override
                    public void onSubmit() {
                        Job job = (Job)getModelObject();
                        saveJob( job );
                        super.onSubmit();
                    } 
                    
                };
                form.add( saveBtn );
                
            }
            
        }.setReuseItems( true );
        add ( jobList );
        
        // add new job form
        newJob = new Job ( );
        CompoundPropertyModel model = new CompoundPropertyModel( newJob );
        newJobForm = new JobDetailsForm( "newJobForm", model ) {

            @Override
            protected void onSubmit() {
                createNewJob ( );
                super.onSubmit();
            }
            
        };
        newJobForm.add( new RequiredTextField( "newJobName", model.bind( "jobName" ) ) );
        add ( newJobForm );
    }
    
    
    private void createNewJob ( ) {

        log.debug( "Create new job #0", newJob.getJobName() );
        
        try {
            dashboardService.saveJob( newJob );
            schedulerService.scheduleJob( newJob );
        } catch ( Exception e ) {
            log.error( "Save of job #0 failed", newJob, e );
        }
        log.debug( "Job created with ID #0", newJob.getId() );
        jobs.add( newJob );
        
        newJob = new Job();
        newJobForm.getModel().setObject( newJob );

        
    }
    
    private void removeJob ( Job job ) {
        log.debug( " Delete Job Button => #0", job.getJobName() );
        
        try { 
            dashboardService.removeJob( job.getId() );
            schedulerService.deleteJob( job );
        } catch ( Exception e ) {
            log.error( "Failed to delete job #0", job.getJobName(), e );
        }
        
        // reload the list from the data store
        initJobs();

        jobList.setList( jobs );
        jobList.modelChanged();
        
    }
    
    private void saveJob ( Job job ) {
        log.debug( " Save Job Button => #0", job.getJobName() );
        
        try { 
            dashboardService.saveJob( job );
            schedulerService.deleteJob( job ); // we should probably check if scheduled changed
            schedulerService.scheduleJob( job );
        } catch ( Exception e ) {
            log.debug( "Failed on #0", e.getMessage() );
            log.error( "Failed to save job #0", job.getJobName(), e );
        }
        
        // reload the list from the data store
        initJobs();

        jobList.setList( jobs );
        jobList.modelChanged();
        
    }
    
    /** builds the job form **/
    public class JobDetailsForm extends Form { 
        
        private CompoundPropertyModel model;
        
        public JobDetailsForm(String id, CompoundPropertyModel model) {
            super(id, model);
            
            this.model = model;
            init( );
        }

        public JobDetailsForm(String id) {
            super(id);

        }

        private void init ( ) {
            Job job = (Job)model.getObject();
            
            // job name
            add ( new Label ( "jobName", job.getJobName() ) );
            
            // script name
            add ( new RequiredTextField( "jobScript", model.bind( "scriptName" ) ) );
            
            // add schedule string (cron)
            add ( new RequiredTextField( "jobSchedule", model.bind( "schedule" ) ) );
            
            // add email(s)
            add ( new TextField ( "jobEmail", model.bind( "emailList" ) ) );
            
            // add history keep count
            add ( new TextField ( "jobHistoryCount", model.bind( "maxHistoryCount" ) ) );
            
        }
    }
    
}
