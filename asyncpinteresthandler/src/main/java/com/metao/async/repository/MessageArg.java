package com.metao.async.repository;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by metao on 1/2/2017.
 */
public class MessageArg implements Serializable {

    private final String id;
    private String url;
    private Repository.RepositoryType jobRepositoryType;
    private Object object;
    private Type type;

    public MessageArg(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Repository.RepositoryType getJobRepositoryType() {
        return jobRepositoryType;
    }

    public void setJobRepositoryType(Repository.RepositoryType jobRepositoryType) {
        this.jobRepositoryType = jobRepositoryType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
