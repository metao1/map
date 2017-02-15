package com.metao.async.download.report.exceptions;

public class QueueDownloadNotStartedException extends IllegalStateException {

    public QueueDownloadNotStartedException() {
        super("Queue Download not started yet");
    }
}
