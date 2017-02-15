package com.metao.async.download.core.mainWorker;

import com.metao.async.repository.Repository;
import com.metao.async.download.Utils.QueueObserver;
import com.metao.async.download.core.chunkWorker.Moderator;
import com.metao.async.download.database.ChunksDataSource;
import com.metao.async.download.database.TasksDataSource;
import com.metao.async.download.database.elements.Task;
import com.metao.async.download.report.listener.DownloadManagerListenerModerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueModerator implements QueueObserver {

    private final TasksDataSource tasksDataSource;
    private final ChunksDataSource chunksDataSource;
    private final Moderator moderator;
    private final DownloadManagerListenerModerator listener;
    private final List<Task> uncompletedTasks;
    private final int downloadTaskPerTime;
    private final Repository<Task> repository;

    private ConcurrentHashMap<String, Thread> downloaderList;
    private boolean pauseFlag = false;

    public QueueModerator(Repository<Task> repository, TasksDataSource tasksDataSource, ChunksDataSource chunksDataSource,
                          Moderator localModerator, DownloadManagerListenerModerator downloadManagerListener,
                          List<Task> tasks, int downloadPerTime) {

        this.tasksDataSource = tasksDataSource;
        this.chunksDataSource = chunksDataSource;
        this.moderator = localModerator;
        this.moderator.setQueueObserver(this);
        this.listener = downloadManagerListener;
        this.downloadTaskPerTime = downloadPerTime;
        this.uncompletedTasks = tasks;
        this.repository = repository;
        downloaderList = new ConcurrentHashMap<>(downloadTaskPerTime);
    }


    public void startQueue() {
        if (uncompletedTasks != null) {
            for (Task uncompletedTask : uncompletedTasks) {
                if (!pauseFlag /*&& downloadTaskPerTime >= downloaderList.size()*/) {
                    Thread downloader =
                            new AsyncStartDownload(repository, tasksDataSource, chunksDataSource,
                                    moderator, listener, uncompletedTask);
                    downloaderList.put(uncompletedTask.id, downloader);
                    downloader.start();
                }
            }
            downloaderList.clear();//clear the downloading list
        }
    }

    public void wakeUp(String taskID) {
        downloaderList.remove(taskID);
        startQueue();
    }

    public void pause() {
        pauseFlag = true;
        for (Map.Entry entry : downloaderList.entrySet()) {
            String id = (String) entry.getKey();
            moderator.pause(id);
        }
        pauseFlag = false;
    }
}
