package com.metao.async.download.database.elements;

import com.metao.async.download.appConstants.Helper;

public class Task {

    public String id;
    public String name;
    public long size;
    public int state;
    public String url;
    public Long percent;
    public int chunks;
    public boolean notify;
    public boolean resumable;
    public String repositoryId;
    public String extension;
    public byte[] data;
    public boolean priority;

    public Task() {
        this.id = Helper.createNewId();
        this.name = null;
        this.size = 0;
        this.state = 0;
        this.url = null;
        this.percent = 0l;
        this.chunks = 0;
        this.notify = true;
        this.resumable = true;
        this.repositoryId = null;
        this.extension = null;
        this.priority = false;  // low priority
    }

    public Task(long size, String name, String url,
                int state, int chunks, String repositoryId, boolean priority) {
        this.id = Helper.createNewId();
        this.name = name;
        this.size = size;
        this.state = state;
        this.url = url;
        this.percent = 0L;
        this.chunks = chunks;
        this.notify = true;
        this.resumable = true;
        this.repositoryId = repositoryId;
        this.extension = "";
        this.priority = priority;
    }
}
