package com.probuild.retail.web.catalog.datasync.service;

import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.probuild.retail.web.catalog.datasync.domain.Job;
import com.probuild.retail.web.catalog.datasync.job.BasicJythonJob;

@Name("schedulerService")
@Scope(ScopeType.APPLICATION)
public class SchedulerService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Logger
    protected Log log;
    
    @In(create=true)
    protected DashboardService dashboardService;
    
    
    public SchedulerService() {
        super();
    }

    /**
     * Starts up quartz scheduler and creates a new job for each 
     * active job in the datastore.
     * 
     * @throws SchedulerException 
     * @throws ParseException 
     */
    public void initializeScheduler ( ) throws SchedulerException, ParseException {
        log.info ( "Scheduling jobs now" );
        
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // and start it off
        scheduler.start();

        
        List<Job> jobs = dashboardService.getAllJobs();
        for ( Job job : jobs ) {
            log.info( "Job #0, will be scheduled using #1 schedule", 
                                        job.getJobName(), job.getSchedule() );
            
            JobDetail jobDetail = new JobDetail(job.getJobName(), "group1", BasicJythonJob.class);
            jobDetail.getJobDataMap().put( "job", job );
            
            // Define a Trigger that will fire "now"
            Trigger trigger = new CronTrigger("trigger_" + job.getJobName(), "group1", job.getSchedule() );
                
            // Schedule the job with the trigger
            scheduler.scheduleJob(jobDetail, trigger);

        }
        
    }

    /**
     * Close down the scheduler, which should kill all the scheduled
     * jobs when using RAMStore.
     * 
     * @throws SchedulerException
     */
    public void shutdownScheduler ( ) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.shutdown();
    }
    
    
    public void runJobNow ( Job job ) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.triggerJob( job.getJobName(), "group1" );
        }
        catch(SchedulerException e) {
            log.error( "Could not run job #0 : #1", job.getJobName(), e.getMessage(), e );
        }
        
        
    }
    
    public void deleteJob ( Job job ) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.deleteJob( job.getJobName(), "group1" );
        }
        catch(SchedulerException e) {
            log.error( "Could not delete job #0 : #1", job.getJobName(), e.getMessage(), e );
        }
        
        
    }
    
    public void scheduleJob ( Job job ) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            
            JobDetail jobDetail = new JobDetail(job.getJobName(), "group1", BasicJythonJob.class);
            jobDetail.getJobDataMap().put( "job", job );
            
            // Define a Trigger that will fire "now"
            Trigger trigger = new CronTrigger("trigger_" + job.getJobName(), "group1", job.getSchedule() );
                
            // Schedule the job with the trigger
            scheduler.scheduleJob(jobDetail, trigger);
        }
        catch(SchedulerException e) {
            log.error( "Could not schedule job #0 : #1", job.getJobName(), e.getMessage(), e );
        }
        catch(ParseException e) {
            log.error( "Could not schedule job #0 : #1", job.getJobName(), e.getMessage(), e );
        }
        
        
    }
}
