package com.metao.async.download.database;

import com.metao.async.repository.Repository;
import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;

import java.util.List;

public class ChunksDataSource {

    private Repository<Chunk> chunkRepository;

    public ChunksDataSource(Repository<Chunk> chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public int insertChunks(Task task) {
        long lastChunkInserted = 0;
        if (task.size > 0) { // resumable
            long chunkSize = task.size / task.chunks;
            for (int i = 0; i < task.chunks; i++) {
                Chunk chunk = new Chunk(task.id);
                if (i == 0) {
                    chunk.begin = 0;
                } else {
                    chunk.begin = (chunkSize * i) + 1;
                }
                if (i == task.chunks - 1) {
                    chunk.end = task.size;
                } else {
                    chunk.end = chunkSize * (i + 1);
                }
                chunkRepository.put(chunk.taskId, chunk);
                lastChunkInserted = chunk.end;
            }
            return (int) lastChunkInserted - task.chunks + 1;
        } else {
            Chunk chunk = new Chunk(task.id);
            chunkRepository.put(chunk.taskId, chunk);
            return -1;
        }
    }

    public List<Chunk> chunksRelatedTask(String taskID) {
        return (List<Chunk>) chunkRepository.getRamCacheRepository().searchForValues(taskID);
    }

    public boolean delete(int chunkID) {
        return chunkRepository.delete(String.valueOf(chunkID));
    }
}
