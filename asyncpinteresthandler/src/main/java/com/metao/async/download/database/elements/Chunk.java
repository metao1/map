package com.metao.async.download.database.elements;

/**
 * "CREATE TABLE "+ TABLES.CHUNKS + " ("
 * + CHUNKS.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 * + CHUNKS.COLUMN_TASK_ID + " INTEGER, "
 * + CHUNKS.COLUMN_C_SIZE + " INTEGER, "
 * + CHUNKS.COLUMN_G_SIZE + " INTEGER "
 * + " ); ";
 */
public class Chunk {
    public int id = 0;
    public String taskId;
    public long begin;
    public long end;
    public boolean completed;
    public byte[] data;

    public Chunk(String taskId) {
        this.taskId = taskId;
    }

}
