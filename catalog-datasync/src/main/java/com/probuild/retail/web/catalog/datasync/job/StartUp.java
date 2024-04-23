package com.probuild.retail.web.catalog.datasync.job;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.probuild.retail.web.catalog.datasync.domain.Job;
import com.probuild.retail.web.catalog.datasync.service.DashboardService;

@Name("startUp")
@Scope(ScopeType.APPLICATION)
@Startup
public class StartUp {

    @Logger
    protected Log log;
    
    @In(create=true)
    protected DashboardService dashboardService;
    
    public StartUp() {
        super();
    }

    @Create
    public void init( ) {
        log.info ( "Scheduling jobs now" );
        
        try {
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
        } catch ( Exception e ) {
            log.error( "failed...", e.getMessage() );
            log.error( "fails to schedule jobs", e );
        }
    }
    
    @Destroy
    public void destory ( ) {
        
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.shutdown();
        }
        catch(SchedulerException e) {
            log.error( "failed...", e.getMessage() );
            log.error( "fails to stop schedule jobs", e );
        }
        
    }

}
