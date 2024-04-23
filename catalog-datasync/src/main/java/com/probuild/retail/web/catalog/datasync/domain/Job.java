package com.probuild.retail.web.catalog.datasync.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="jobs")
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String jobName;
    private String scriptName;
    private Calendar createDate;
    //private Object quartzHandle;
    private Boolean active;
    private Integer maxHistoryCount;
    private String emailList;
    private String schedule;
    
    
    public Job() {
        super();
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }


    public String getScriptName() {
        return scriptName;
    }
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }


    public Calendar getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }


//    public Object getQuartzHandle() {
//        return quartzHandle;
//    }
//    public void setQuartzHandle(Object quartzHandle) {
//        this.quartzHandle = quartzHandle;
//    }


    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }


    public Integer getMaxHistoryCount() {
        return maxHistoryCount;
    }
    public void setMaxHistoryCount(Integer maxHistoryCount) {
        this.maxHistoryCount = maxHistoryCount;
    }


    public String getEmailList() {
        return emailList;
    }
    public void setEmailList(String emailList) {
        this.emailList = emailList;
    }


    public String getSchedule() {
        return schedule;
    }
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    
    
}
