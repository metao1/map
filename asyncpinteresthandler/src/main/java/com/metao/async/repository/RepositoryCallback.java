package com.metao.async.repository;

/**
 * Created by metao on 2/1/2017.
 */
public class RepositoryCallback<T> implements RepositoryCallbackInterface<T> {

    @Override
    public void onDownloadFinished(String urlAddress, T t) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onDownloadProgress(String urlAddress, double progress) {

    }
}
