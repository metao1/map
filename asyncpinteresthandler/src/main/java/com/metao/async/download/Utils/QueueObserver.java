package com.metao.async.download.Utils;

public interface QueueObserver {

    void wakeUp(String taskID);
}
