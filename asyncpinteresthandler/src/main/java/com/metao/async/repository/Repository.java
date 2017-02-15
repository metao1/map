package com.metao.async.repository;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.metao.async.download.appConstants.Helper;
import com.metao.async.download.database.elements.Chunk;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Created by metao on 2/1/2017.
 */
public class Repository<T> {

    private static final int RAM_SIZE = 10 * 1024;
    private final int hashCode;
    private final CacheSerializer<T> ramSerializer;
    private final SizeOf<Chunk> sizeOf;
    private DownloadHandler<T> serviceDownloadHandler;
    private DownloadHandler<Bitmap> bitmapDownloadHandler;
    private Type type;
    private RepositoryCache ramCacheRepository;
    private RepositoryCache<String, RepositoryCallbackInterface<Bitmap>> viewHolderCacheRepository;
    private RepositoryCache<String, ImageView> imageViewRepositoryCache;
    private RepositoryCache<String, Bitmap> bitmapCacheRepository;
    private RepositoryCache<String, T> serviceCacheRepository;
    private RepositoryType repositoryType = RepositoryType.STRING;
    private RepositoryCacheRamMode ramMode;
    private String repoName;

    public Repository(String repoName) {
        this.type = getSuperclassTypeParameter(getClass());
        this.hashCode = type.hashCode();
        this.repoName = repoName;
        ramCacheRepository = new RepositoryCache(RAM_SIZE);
        viewHolderCacheRepository = new RepositoryCache<>(RAM_SIZE);
        serviceCacheRepository = new RepositoryCache(RAM_SIZE);
        bitmapCacheRepository = new RepositoryCache(RAM_SIZE);
        imageViewRepositoryCache = new RepositoryCache<>(RAM_SIZE);
        ramMode = RepositoryCacheRamMode.ENABLE_WITH_REFERENCE;
        ramSerializer = new RamSerializer();
        sizeOf = new ChunkSize();

    }

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }

    public Repository<T> addService(final String url
            , RepositoryCallback<T> repositoryCallback) {
        if (serviceCacheRepository.snapshot().containsKey(url)) {//check cache for data availability
            T t = serviceCacheRepository.snapshot().get(url);
            repositoryCallback.onDownloadFinished(url, t);
            Log.d("tag", "using cache");
        } else {
            switch (ramMode) {
                case ENABLE_WITH_SPECIFIC_SERIALIZER:
                    this.ramCacheRepository = new StringRepositoryCache(RAM_SIZE);
                    break;
                case ENABLE_WITH_REFERENCE:
                    this.ramCacheRepository = new ReferenceRepositoryCache<>(RAM_SIZE, sizeOf);
                    break;
            }
            String taskId = UUID.randomUUID().toString();
            MessageArg messageArg = new MessageArg(taskId);
            messageArg.setJobRepositoryType(repositoryType());
            messageArg.setType(getType());
            messageArg.setUrl(url);
            serviceDownloadHandler = new DownloadHandler<>();
            serviceDownloadHandler.setRepoCallback(url, repositoryCallback);
            serviceDownloadHandler.setCacheRepository(serviceCacheRepository);
            serviceDownloadHandler.setTaskRepository();
            serviceDownloadHandler.setChunkRepository();
            serviceDownloadHandler.execute(messageArg);
        }
        return this;
    }

    public Repository<T> downloadBitmap(final String url, ImageView imageView) {
        imageViewRepositoryCache.put(url, imageView);
        if (bitmapCacheRepository.snapshot().containsKey(url)) {//check cache for data availability
            Bitmap bitmap = bitmapCacheRepository.snapshot().get(url);
            if (bitmap != null) {
                imageViewRepositoryCache.get(url).setImageBitmap(bitmap);
            }
            Log.d("tag", "using cache");
        } else {
            String taskId = UUID.randomUUID().toString();
            MessageArg messageArg = new MessageArg(taskId);
            messageArg.setJobRepositoryType(repositoryType());
            messageArg.setType(getType());
            messageArg.setUrl(url);
            bitmapDownloadHandler = new DownloadHandler<>();
            bitmapDownloadHandler.setRepoCallback(url, new RepositoryCallback<Bitmap>() {
                @Override
                public void onDownloadFinished(String urlAddress, Bitmap bitmap) {
                    if (imageViewRepositoryCache.contains(urlAddress)) {
                        imageViewRepositoryCache.get(urlAddress).setImageBitmap(bitmap);
                    }
                }
            });
            bitmapDownloadHandler.setCacheRepository(bitmapCacheRepository);
            bitmapDownloadHandler.setTaskRepository();
            bitmapDownloadHandler.setChunkRepository();
            bitmapDownloadHandler.execute(messageArg);
        }
        return this;
    }

    public Repository<T> downloadBitmapIntoViewHolder(final String url, RepositoryCallbackInterface<Bitmap> viewHolder) {
        viewHolderCacheRepository.put(url, viewHolder);
        if (bitmapCacheRepository.snapshot().containsKey(url)) {//check cache for data availability
            Bitmap bitmap = bitmapCacheRepository.snapshot().get(url);
            if (bitmap != null) {
                viewHolderCacheRepository.get(url).onDownloadFinished(url, bitmap);
            }
            Log.d("tag", "using cache");
        } else {
            String taskId = UUID.randomUUID().toString();
            MessageArg messageArg = new MessageArg(taskId);
            messageArg.setJobRepositoryType(repositoryType());
            messageArg.setType(getType());
            messageArg.setUrl(url);
            bitmapDownloadHandler = new DownloadHandler<>();
            bitmapDownloadHandler.setRepoCallback(url, new RepositoryCallback<Bitmap>() {
                @Override
                public void onDownloadFinished(String urlAddress, Bitmap bitmap) {
                    if (viewHolderCacheRepository.contains(urlAddress)) {
                        viewHolderCacheRepository.get(urlAddress).onDownloadFinished(urlAddress, bitmap);
                    }
                }

                @Override
                public void onDownloadProgress(String urlAddress, double progress) {
                    if (viewHolderCacheRepository.contains(urlAddress)) {
                        viewHolderCacheRepository.get(urlAddress).onDownloadProgress(urlAddress, progress);
                    }
                }
            });
            bitmapDownloadHandler.setCacheRepository(bitmapCacheRepository);
            bitmapDownloadHandler.setTaskRepository();
            bitmapDownloadHandler.setChunkRepository();
            bitmapDownloadHandler.execute(messageArg);
        }
        return this;
    }

    public int ramSize() {
        return RAM_SIZE;
    }

    public RepositoryType repositoryType() {
        return repositoryType;
    }

    public final Type getType() {
        return type;
    }

    public long getRamUsedInBytes() {
        return ramCacheRepository.snapshot().size();
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    public String put(String key, T object) {
        if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE)) {
            assert ramCacheRepository != null;
            Log.d("cache", key + " store into cache");
            ramCacheRepository.put(key, object);
        }
        String ramSerialized = null;
        if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            ramSerialized = ramSerializer.toString(object);
            ramCacheRepository.put(key, ramSerialized);
        }
        return Helper.createNewId();
    }

    public T get(String key) {
        Object ramResult = null;
        boolean isRamSerialized = ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER);
        boolean isRamReferenced = ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE);
        if (isRamSerialized || isRamReferenced) {
            ramResult = ramCacheRepository.get(key);
        }
        if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_REFERENCE)) {
            return (T) ramResult;
        } else if (ramMode.equals(RepositoryCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            return ramSerializer.fromString((String) ramResult);
        }
        // No data is available.
        return null;
    }

    public boolean delete(String key) {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE)) {
            ramCacheRepository.remove(key);
            return true;
        }
        return false;
    }

    public void invalidate() {
        invalidateRAM();
    }

    public void invalidateRAM() {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE)) {
            ramCacheRepository.evictAll();
        }
    }

    public boolean contains(String key) {
        if (!ramMode.equals(RepositoryCacheRamMode.DISABLE) && ramCacheRepository.snapshot().containsKey(key)) {
            return true;
        }
        return false;
    }

    public RepositoryCache getRamCacheRepository() {
        return ramCacheRepository;
    }

    public enum RepositoryType {
        JSON(1),
        XML(2),
        NORMAL(3),
        BITMAP(4),
        STRING(5);
        private int type = 0;

        RepositoryType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    class ChunkSize implements SizeOf<Chunk> {

        @Override
        public int sizeOf(Chunk object) {
            int size = 0;
            size += 4; // we suppose that char = 4 bytes
            size += object.taskId.length() * 4; // we suppose that char = 4 bytes
            size += 1; // we suppose that char = 4 bytes
            size += 4; // we suppose that char = 4 bytes
            size += object.taskId.length() * 4; // we suppose that char = 4 bytes
            size += 1; // we suppose that char = 4 bytes
            return size;
        }
    }

    private class RamSerializer implements CacheSerializer<T> {
        @Override
        public T fromString(String data) {
            return null;
        }

        @Override
        public String toString(T object) {
            return null;
        }
    }
}