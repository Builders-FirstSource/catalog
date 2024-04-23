package com.probuild.retail.web.catalog.datasync.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import com.probuild.retail.web.catalog.datasync.domain.DashboardItem;
import com.probuild.retail.web.catalog.datasync.domain.Job;
import com.probuild.retail.web.catalog.datasync.domain.JobExecution;

@Name("dashboardService")
@Scope(ScopeType.STATELESS)
public class DashboardService implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Logger
    protected Log log;
    
    @In
    protected EntityManager em;
    
    /**
     *  Default constructor
     */
    public DashboardService() {
        super();
    }
    
    public Job findJob ( Long id ) {
        return em.find( Job.class, id );
    }

    public void saveJob ( Job job ) throws 
                    NotSupportedException, SystemException, SecurityException, 
                    IllegalStateException, RollbackException, 
                    HeuristicMixedException, HeuristicRollbackException {
        org.jboss.seam.transaction.Transaction.instance().begin();
        log.debug( "saving job #0", job.getJobName() );
        if ( job.getId() == null ) {
            em.persist( job );
        } else {
            em.merge( job );
        }
        org.jboss.seam.transaction.Transaction.instance().commit();
        
        log.debug( "Job #0 with new id #1 has been saved", job.getJobName(), job.getId() );
    }
    
    public void removeJob ( Long id ) throws 
                    NotSupportedException, SystemException, SecurityException, 
                    IllegalStateException, RollbackException, 
                    HeuristicMixedException, HeuristicRollbackException {
        org.jboss.seam.transaction.Transaction.instance().begin();
        
        // remove all job executions
        List<JobExecution> execs = getJobHistory( id );
        for ( JobExecution ex : execs ) {
            em.remove( ex );
        }
        em.flush();
        
        Job job = em.find( Job.class, id );
        log.debug( "Remove job #0 with id #1", job.getJobName(), job.getId() );
        em.remove( job );
        
        org.jboss.seam.transaction.Transaction.instance().commit();
    }
    
    public List<Job> getAllJobs ( ) {
        
        List<Job> jobs = 
                em.createQuery( "SELECT job FROM Job job" ).getResultList();
        
        return jobs;

    }
    
    public List<DashboardItem> getItems ( ) {
    
        List<DashboardItem> items = new ArrayList<DashboardItem>();
        
        List<Job> jobs = 
                em.createQuery( "SELECT job FROM Job job" ).getResultList();
        
        // for each job figure out the statistics
        for ( Job job : jobs ) {
            log.debug( "Found job #0", job.getJobName() );
            List<JobExecution> execs = em.createQuery( "SELECT exec FROM JobExecution exec WHERE exec.jobName = :jobName ORDER BY exec.endTime desc" ).setParameter( "jobName", job.getJobName() ).setMaxResults( job.getMaxHistoryCount() ).getResultList();
            
            items.add( makeDashboard( job, execs ) );
            
        }
        
        return items;
    }

    public List<JobExecution> getJobHistory ( Long jobId ) {
        
        List<JobExecution> items;
        
        items = em.createQuery( 
             "SELECT exec FROM JobExecution exec WHERE exec.job.id = :jobId ORDER BY exec.startTime desc" ).
                                     setParameter( "jobId", jobId ).
                                     getResultList();
        
        return items;
    }
    
    public JobExecution findJobExecution ( Long id ) {
        return em.find( JobExecution.class, id );
    }
    
    public void saveJobExecution ( JobExecution exec ) throws 
                    NotSupportedException, SystemException, SecurityException, 
                    IllegalStateException, RollbackException, 
                    HeuristicMixedException, HeuristicRollbackException {
        org.jboss.seam.transaction.Transaction.instance().begin();
        log.debug( "saving job #0", exec.getJobName() );
        if ( exec.getId() == null ) {
            em.persist( exec );
        } else {
            em.merge( exec );
        }
        org.jboss.seam.transaction.Transaction.instance().commit();

        log.debug( "Job Exec #0 with new id #1 has been saved", exec.getJobName(), exec.getId() );
    }
    
    public void removeJobExecution(Long id) throws 
                                    NotSupportedException, SystemException, 
                                    SecurityException, IllegalStateException,
                                    RollbackException, HeuristicMixedException, 
                                                    HeuristicRollbackException {
        org.jboss.seam.transaction.Transaction.instance().begin();
        JobExecution jobExec = em.find(JobExecution.class, id);
        em.remove(jobExec);
        org.jboss.seam.transaction.Transaction.instance().commit();
    }
    
    private DashboardItem makeDashboard ( Job job, List<JobExecution> execs ) {
        
        int successCount = 0;
        for ( JobExecution exec : execs ) {
            if ( "SUCCESS".equals( exec.getStatus() ) )
                successCount++;
        }
        
        DashboardItem item;
        if ( execs != null && execs.size() > 0 ) {
            JobExecution ex = execs.get( 0 ); // should be most recent
            item = new DashboardItem.Builder().
                            job( job ).
                            lastRun( ex.getEndTime() ).
                            outcome( ex.getStatus() ).
                            runtime( ex.getRunTime() ).
                            successCount( successCount ).build();
        } else {
            item = new DashboardItem.Builder().
                            job( job ).
                            lastRun( null ).
                            outcome( "Never Run" ).
                            runtime( 0 ).
                            successCount( successCount ).build();
        }
        
        
        
        return item;
        
    }
    
}
