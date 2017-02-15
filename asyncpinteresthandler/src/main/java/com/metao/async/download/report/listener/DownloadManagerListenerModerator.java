package com.metao.async.download.report.listener;

public class DownloadManagerListenerModerator {

    private DownloadManagerListener downloadManagerListener;

    public DownloadManagerListenerModerator(DownloadManagerListener listener) {
        downloadManagerListener = listener;
    }

    public void OnDownloadStarted(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.OnDownloadStarted(taskId);
        }
    }

    public void OnDownloadPaused(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.OnDownloadPaused(taskId);
        }
    }

    public void onDownloadProcess(String taskId, double percent, long downloadedLength) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadProgress(taskId, percent, downloadedLength);
        }
    }

    public void OnDownloadFinished(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.OnDownloadFinished(taskId);
        }
    }

    public void OnDownloadRebuildStart(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.OnDownloadRebuildStart(taskId);
        }
    }


    public void OnDownloadRebuildFinished(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.OnDownloadRebuildFinished(taskId);
        }
    }

    public void OnDownloadCompleted(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.OnDownloadCompleted(taskId);
        }
    }

    public void ConnectionLost(String taskId) {
        if (downloadManagerListener != null) {
            downloadManagerListener.connectionLost(taskId);
        }
    }
}
