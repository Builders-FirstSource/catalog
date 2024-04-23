package com.probuild.retail.web.catalog.datasync.domain;

import java.io.Serializable;
import java.util.Calendar;

public class DashboardItem implements Serializable {

    private Job job;
    private int successCount;
    private Calendar lastRun;
    private String outcome;
    private long runtime;

    /**
     *	Default constructor
     */
    public DashboardItem() {
        super();
    }

    public Job getJob() {
        return job;
    }
    public void setJob(Job job) {
        this.job = job;
    }

    public int getSuccessCount() {
        return successCount;
    }
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public Calendar getLastRun() {
        return lastRun;
    }
    public void setLastRun(Calendar lastRun) {
        this.lastRun = lastRun;
    }

    public String getOutcome() {
        return outcome;
    }
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public long getRuntime() {
        return runtime;
    }
    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public static class Builder {
        private Job job;
        private int successCount;
        private Calendar lastRun;
        private String outcome;
        private long runtime;

        public Builder job(Job job) {
            this.job = job;
            return this;
        }

        public Builder successCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder lastRun(Calendar lastRun) {
            this.lastRun = lastRun;
            return this;
        }

        public Builder outcome(String outcome) {
            this.outcome = outcome;
            return this;
        }

        public Builder runtime(long runtime) {
            this.runtime = runtime;
            return this;
        }

        public DashboardItem build() {
            return new DashboardItem(this);
        }
    }

    private DashboardItem(Builder builder) {
        this.job = builder.job;
        this.successCount = builder.successCount;
        this.lastRun = builder.lastRun;
        this.outcome = builder.outcome;
        this.runtime = builder.runtime;
    }
}
