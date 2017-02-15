package com.metao.async.download.core.chunkWorker;

import android.util.Log;
import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class AsyncWorker extends Thread {

    private final int BUFFER_SIZE = 1024;

    private final Task task;
    private final Chunk chunk;
    private final Moderator observer;
    public boolean stop = false;
    private ConnectionWatchDog watchDog;
    private boolean flag = true;

    public AsyncWorker(Task task, Chunk chunk, Moderator moderator) {
        this.task = task;
        this.chunk = chunk;
        this.observer = moderator;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(task.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Avoid timeout exception which usually occurs in low network
            connection.setConnectTimeout(0);
            connection.setReadTimeout(0);
            if (chunk.end != 0) { // support unresumable links
                connection.setRequestProperty("Range", "bytes=" + chunk.begin + "-" + chunk.end);
            }
            connection.connect();
            // Check response code first to avoid error stream
            int status = connection.getResponseCode();
            BufferedInputStream remoteFileIn;
            if (status == 416) {
                remoteFileIn = new BufferedInputStream(connection.getErrorStream());
            } else {
                remoteFileIn = new BufferedInputStream(connection.getInputStream());
            }
            int len = 0;
            // set watchDoger to stop thread after 1sec if no connection lost
            watchDog = new ConnectionWatchDog(5000, this);
            watchDog.start();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[BUFFER_SIZE];
            while (!this.isInterrupted() &&
                    (len = remoteFileIn.read(data)) > 0) {
                watchDog.reset();
                buffer.write(data, 0, len);
                process(len);
            }
            buffer.flush();
            if (data.length > 0) {
                chunk.data = buffer.toByteArray();
            }
            watchDog.interrupt();
            connection.disconnect();
            if (!this.isInterrupted()) {
                observer.rebuild(chunk);
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            observer.connectionLost(task.id);
            pauseRelatedTask();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(int read) {
        observer.process(chunk.taskId, read);
    }

    private void pauseRelatedTask() {
        observer.pause(task.id);
    }

    public void connectionTimeOut() {
        if (flag) {
            watchDog.interrupt();
            flag = false;
            observer.connectionLost(task.id);
            pauseRelatedTask();
        }

    }

}
