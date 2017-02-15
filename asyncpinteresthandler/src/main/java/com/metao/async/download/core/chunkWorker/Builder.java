package com.metao.async.download.core.chunkWorker;

import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;
import com.metao.async.download.report.listener.DownloadManagerListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class Builder extends Thread {

    List<Chunk> taskChunks;
    Task task;
    Moderator observer;
    DownloadManagerListener downloadManagerListener;

    public Builder(Task task, List<Chunk> taskChunks, Moderator moderator) {
        this.taskChunks = taskChunks;
        this.task = task;
        this.observer = moderator;
    }

    @Override
    public void run() {
        // notify to developer------------------------------------------------------------
        observer.downloadManagerListener.OnDownloadRebuildStart(task.id);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (Chunk chunk : taskChunks) {
            buffer.write(chunk.data, 0, chunk.data.length);
        }
        try {
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        task.data = buffer.toByteArray();
        observer.reBuildIsDone(task, taskChunks);
    }
}
