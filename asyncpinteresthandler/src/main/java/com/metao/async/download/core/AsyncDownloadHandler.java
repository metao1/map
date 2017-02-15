package com.metao.async.download.core;

import android.util.Log;
import com.metao.async.repository.Repository;
import com.metao.async.download.core.chunkWorker.Moderator;
import com.metao.async.download.core.enums.QueueSort;
import com.metao.async.download.core.enums.TaskStates;
import com.metao.async.download.core.mainWorker.AsyncStartDownload;
import com.metao.async.download.core.mainWorker.QueueModerator;
import com.metao.async.download.database.ChunksDataSource;
import com.metao.async.download.database.TasksDataSource;
import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;
import com.metao.async.download.report.ReportStructure;
import com.metao.async.download.report.exceptions.QueueDownloadInProgressException;
import com.metao.async.download.report.exceptions.QueueDownloadNotStartedException;
import com.metao.async.download.report.listener.DownloadManagerListener;
import com.metao.async.download.report.listener.DownloadManagerListenerModerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsyncDownloadHandler {

    static int maximumUserChunks;
    private final int MAX_CHUNKS = 16;
    private Moderator moderator;
    private TasksDataSource tasksDataSource;
    private ChunksDataSource chunksDataSource;
    private DownloadManagerListenerModerator downloadManagerListener;

    private QueueModerator qt;
    private Repository<Task> taskRepository;
    private Repository<Chunk> chunkRepository;

    /**
     * <p>
     * Download manager pro Object constructor
     * </p>
     */
    public AsyncDownloadHandler(Repository<Chunk> chunkRepository, Repository<Task> taskRepository) {
        this.taskRepository = taskRepository;
        this.chunkRepository = chunkRepository;
        tasksDataSource = new TasksDataSource(taskRepository);
        chunksDataSource = new ChunksDataSource(chunkRepository);
        // moderate chunks to download one task
        moderator = new Moderator(taskRepository, tasksDataSource, chunksDataSource);
    }

    /**
     * <p>
     * i don't want to force developer to init download manager
     * so i can't get downloadManagerListener at constructor but that way seems better than now
     * </p>
     *
     * @param maxChunks
     * @param listener
     */
    public void init(int maxChunks, DownloadManagerListener listener) {
        // ready Repository to save download content in it
        maximumUserChunks = setMaxChunk(maxChunks);
        downloadManagerListener = new DownloadManagerListenerModerator(listener);
    }

    /**
     * <p>
     * add a new download Task
     * </p>
     *
     * @param repositoryName Repository  address
     * @param saveName       file name
     * @param url            url file address
     * @param chunk          number of chunks
     * @param overwrite      if exist an other file with same name
     *                       "true" over write that file
     *                       "false" find new name and save it with new name     @return id
     *                       inserted task id
     */
    public String addTask(String repositoryName, String saveName, String url, int chunk,
                          boolean overwrite,
                          boolean priority) {
        if (!overwrite) {
            saveName = getUniqueName(saveName);
        } else {
            deleteSameDownloadNameTask(saveName);
        }
        Log.d("--------", "overwrite " + url);
        chunk = setMaxChunk(chunk);
        Log.d("--------", "ma chunk " + chunk);
        return insertNewTask(repositoryName, saveName, url, chunk, priority);
    }

    public String addTask(String repositoryName, String saveName, String url, boolean overwrite, boolean priority) {
        return this.addTask(repositoryName, saveName, url, maximumUserChunks, overwrite, priority);
    }

    /**
     * <p>
     * first of all check task state and depend on start download process from where ever need
     * </p>
     *
     * @param token now token is download task id
     * @throws IOException
     */
    public void startDownload(String token) throws IOException {
        // switch on task state
        Task task = tasksDataSource.getTaskInfo(token);
        Thread asyncStartDownload
                = new AsyncStartDownload(
                taskRepository, tasksDataSource, chunksDataSource, moderator, downloadManagerListener, task);
        asyncStartDownload.start();
    }

    /**
     * @param downloadTaskPerTime
     */
    public void startQueueDownload(int downloadTaskPerTime, int sortType)
            throws QueueDownloadInProgressException {
        if (qt == null) {
            Moderator localModerator = new Moderator(taskRepository, tasksDataSource, chunksDataSource);
            List<Task> unCompletedTasks = tasksDataSource.getUnCompletedTasks(sortType);
            qt = new QueueModerator(taskRepository, tasksDataSource, chunksDataSource,
                    localModerator, downloadManagerListener, unCompletedTasks, downloadTaskPerTime);
            qt.startQueue();
        } else {
            // throw new QueueDownloadInProgressException();
        }
    }

    /**
     * <p>
     * pause separate download task
     * </p>
     *
     * @param token
     */
    public void pauseDownload(String token) {
        moderator.pause(token);
    }

    /**
     * pause queue download
     *
     * @throws QueueDownloadNotStartedException
     */
    public void pauseQueueDownload()
            throws QueueDownloadNotStartedException {

        if (qt != null) {
            qt.pause();
            qt = null;
        } else {
            throw new QueueDownloadNotStartedException();
        }
    }


    //-----------Reports

    /**
     * report task download status in "ReportStructure" style
     *
     * @param token when you add a new download task it's return to you
     * @return
     */
    public ReportStructure singleDownloadStatus(String token) {
        ReportStructure report = new ReportStructure();
        Task task = tasksDataSource.getTaskInfo(token);
        if (task != null) {
            List<Chunk> taskChunks = chunksDataSource.chunksRelatedTask(task.id);
            report.setObjectValues(task, taskChunks);
            return report;
        }
        return null;
    }

    /**
     * <p>
     * it's an report method for
     * return list of download task in same state that developer want as ReportStructure List object
     * </p>
     *
     * @param state 0. get all downloads Status
     *              1. init
     *              2. ready
     *              3. downloading
     *              4. paused
     *              5. download finished
     *              6. end
     * @return
     */
    public List<ReportStructure> downloadTasksInSameState(int state) {
        List<ReportStructure> reportList;
        List<Task> inStateTasks = tasksDataSource.getTasksInState(state);
        reportList = readyTaskList(inStateTasks);
        return reportList;
    }

    /**
     * return list of last completed Download tasks in "ReportStructure" style
     * you can use it as notifier
     *
     * @return
     */
    public List<ReportStructure> lastCompletedDownloads() {
        List<ReportStructure> reportList;
        List<Task> lastCompleted = tasksDataSource.getUnNotifiedCompleted();
        reportList = readyTaskList(lastCompleted);
        return reportList;
    }

    private List<ReportStructure> readyTaskList(List<Task> tasks) {
        List<ReportStructure> reportList = new ArrayList<ReportStructure>();
        for (Task task : tasks) {
            List<Chunk> taskChunks = chunksDataSource.chunksRelatedTask(task.id);
            ReportStructure singleReport = new ReportStructure();
            singleReport.setObjectValues(task, taskChunks);
            reportList.add(singleReport);
        }
        return reportList;
    }

    /**
     * <p>
     * check all notified tasks
     * so in another "lastCompletedDownloads" call ,completed task does not show again
     * <p>
     * persian:
     * "lastCompletedDownloads" list akharin task haii ke takmil shodeand ra namayesh midahad
     * ba seda zadan in method tamami task haii ke dar gozaresh e ghabli elam shode boodand ra
     * az liste "lastCompeletedDownloads" hazf mikonad
     * <p>
     * !!!SHIT!!!
     * </p>
     *
     * @return true or false
     */
    public boolean notifiedTaskChecked() {
        return tasksDataSource.checkUnNotifiedTasks();
    }

    public boolean delete(String token, boolean deleteTaskFile) {
        Task task = tasksDataSource.getTaskInfo(token);
        if (task.url != null) {
            List<Chunk> taskChunks =
                    chunksDataSource.chunksRelatedTask(task.id);
            for (Chunk chunk : taskChunks) {
                taskRepository.delete(task.id);
                chunksDataSource.delete(chunk.id);
            }
            return tasksDataSource.delete(task.id);
        }

        return false;
    }

    private List<Task> uncompleted() {
        return tasksDataSource.getUnCompletedTasks(QueueSort.oldestFirst);
    }

    private String insertNewTask(String repositoryName, String taskName, String url, int chunk, boolean priority) {
        Task task = new Task(0, taskName, url, TaskStates.INIT, chunk, repositoryName, priority);
        task.id = tasksDataSource.insertTask(task);
        return task.id;
    }

    private int setMaxChunk(int chunk) {
        if (chunk < MAX_CHUNKS) {
            return chunk;
        }
        return MAX_CHUNKS;
    }

    private String getUniqueName(String name) {
        String uniqueName = name;
        int count = 0;

        while (isDuplicatedName(uniqueName)) {
            uniqueName = name + "_" + count;
            count++;
        }

        return uniqueName;
    }

    private boolean isDuplicatedName(String name) {
        return tasksDataSource.containsTask(name);
    }

    /*
        valid values are
            INIT          = 0;
            READY         = 1;
            DOWNLOADING   = 2;
            PAUSED        = 3;
            DOWNLOAD_FINISHED      = 4;
            END           = 5;
        so if his token was wrong return -1
     */
    private void deleteSameDownloadNameTask(String saveName) {
        if (isDuplicatedName(saveName)) {
            Task task = tasksDataSource.getTaskInfoWithName(saveName);
            tasksDataSource.delete(task.id);
            taskRepository.delete(task.id);
        }
    }
}
