package com.metao.async.repository;

import java.io.Serializable;

/**
 * Created by metao on 1/11/2017.
 */
public class JobResponse implements Serializable {
    private Repository.RepositoryType jobRepositoryType;
    private Object jobObject;

    public JobResponse(Repository.RepositoryType jobRepositoryType) {
        this.jobRepositoryType = jobRepositoryType;
    }

    public Repository.RepositoryType getJobRepositoryType() {
        return jobRepositoryType;
    }

    public void setJobRepositoryType(Repository.RepositoryType jobRepositoryType) {
        this.jobRepositoryType = jobRepositoryType;
    }

    public Object getJobObject() {
        return jobObject;
    }

    public void setJobObject(Object jobObject) {
        this.jobObject = jobObject;
    }
}
