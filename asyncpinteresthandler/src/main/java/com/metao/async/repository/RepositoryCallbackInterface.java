package com.metao.async.repository;

/**
 * Created by metao on 2/1/2017.
 */
public interface RepositoryCallbackInterface<T> {
    void onDownloadFinished(String urlAddress, T t);

    void onError(Throwable throwable);

    void onDownloadProgress(String urlAddress, double progress);
}
