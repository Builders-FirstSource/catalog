package com.probuild.retail.web.catalog.datasync.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="job_executions")
public class JobExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String scriptName;
    private String jobName;
    private Calendar startTime;
    private Calendar endTime;
    private Long runTime;
    private String status;
    private String consoleOutFile;
    
    @ManyToOne
    @JoinColumn(name="jobId")
    private Job job;
    
    public JobExecution() {
        super();
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    public String getScriptName() {
        return scriptName;
    }
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }


    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }


    public Calendar getStartTime() {
        return startTime;
    }
    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }


    public Calendar getEndTime() {
        return endTime;
    }
    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }


    public Long getRunTime() {
        return runTime;
    }
    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }


    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    public String getConsoleOutFile() {
        return consoleOutFile;
    }
    public void setConsoleOutFile(String consoleOutFile) {
        this.consoleOutFile = consoleOutFile;
    }

    public Job getJob() {
        return job;
    }
    public void setJob(Job job) {
        this.job = job;
    }


}
