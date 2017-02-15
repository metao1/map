package com.metao.async.repository;

import java.nio.charset.Charset;

/**
 * Repository cache used by the RAM cache layer when storing serialized object.
 */
public class StringRepositoryCache extends RepositoryCache<String, String> {

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public StringRepositoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, String value) {
        return value.getBytes(Charset.defaultCharset()).length;
    }
}
