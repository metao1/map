package com.metao.async.download.database;

import com.metao.async.repository.Repository;
import com.metao.async.repository.RepositoryCache;
import com.metao.async.download.core.enums.QueueSort;
import com.metao.async.download.core.enums.TaskStates;
import com.metao.async.download.database.elements.Task;

import java.util.*;

import static java.util.Arrays.sort;

public class TasksDataSource {

    private Repository<Task> taskRepository;

    public TasksDataSource(Repository<Task> taskRepository) {
        this.taskRepository = taskRepository;
    }

    public String insertTask(Task task) {
        return taskRepository.put(task.id, task);
    }

    public boolean update(Task task) {
        if (taskRepository.contains(task.id)) {
            taskRepository.put(task.id, task);
            return true;
        } else {
            return false;
        }
    }

    public List<Task> getTasksInState(int state) {
        List<Task> tasks = new ArrayList<>();
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator taskEntryIterator = ramCacheRepository.snapshot().entrySet().iterator();
        while (taskEntryIterator.hasNext()) {
            Task task = (Task) taskEntryIterator.next();
            if (task.state == state) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> getUnNotifiedCompleted() {
        List<Task> completedTasks = new ArrayList<>();
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator taskEntryIterator = ramCacheRepository.snapshot().entrySet().iterator();
        while (taskEntryIterator.hasNext()) {
            Task task = (Task) taskEntryIterator.next();
            if (!task.notify) {
                completedTasks.add(task);
            }
        }
        return completedTasks;
    }

    public List<Task> getUnCompletedTasks(int sortType) {
        List<Task> unCompletedTasks = new ArrayList<>();
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Collection<Task> taskEntryIterator = ramCacheRepository.snapshot().values();
        for (Task task : taskEntryIterator) {
            if (task.state == TaskStates.INIT) {
                unCompletedTasks.add(task);
            }
        }
        Task[] tasksArray = unCompletedTasks.toArray(new Task[]{});
        TaskComparator taskComparator = null;
        switch (sortType) {
            case QueueSort.HighPriority:
                taskComparator = new TaskComparator(1);
                break;
            case QueueSort.LowPriority:
                taskComparator = new TaskComparator(0);
                break;
            case QueueSort.oldestFirst:
                taskComparator = new TaskComparator(1);
                break;
            case QueueSort.earlierFirst:
                taskComparator = new TaskComparator(0);
                break;
            case QueueSort.HighToLowPriority:
                taskComparator = new TaskComparator(0);
                break;
            case QueueSort.LowToHighPriority:
                taskComparator = new TaskComparator(1);
                break;

        }
        sort(tasksArray, taskComparator);
        return Arrays.asList(tasksArray);
    }

    public Task getTaskInfo(String id) {
        return (Task) taskRepository.getRamCacheRepository().get(id);
    }

    public Task getTaskInfoWithName(String name) {
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator<Task> taskIterator = ramCacheRepository.snapshot().values().iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.name.equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }

    public boolean delete(String taskID) {
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator taskEntryIterator = ramCacheRepository.snapshot().entrySet().iterator();
        while (taskEntryIterator.hasNext()) {
            Task task = (Task) taskEntryIterator.next();
            if (task.id.equalsIgnoreCase(taskID)) {
                taskRepository.delete(task.id);
                return true;
            }
        }
        return false;
    }


    public boolean containsTask(String name) {
        boolean result = false;
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator taskEntryIterator = ramCacheRepository.snapshot().entrySet().iterator();
        while (taskEntryIterator.hasNext()) {
            Task task = (Task) taskEntryIterator.next();
            if (task.name.equalsIgnoreCase(name)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean checkUnNotifiedTasks() {
        RepositoryCache ramCacheRepository = taskRepository.getRamCacheRepository();
        Iterator taskEntryIterator = ramCacheRepository.snapshot().entrySet().iterator();
        while (taskEntryIterator.hasNext()) {
            Task task = (Task) taskEntryIterator.next();
            if (!task.notify) {
                task.notify = true;
                update(task);
            }
        }
        return true;
    }

    private class TaskComparator implements Comparator<Task> {
        int mode = -1;

        TaskComparator(int mode) {
            this.mode = mode;
        }

        @Override
        public int compare(Task task1, Task task2) {
            switch (mode) {
                case 1:
                    return (task1.priority && task2.priority) ? 1 : 0;
                case 2:
                    return (!task1.priority && !task2.priority) ? 1 : 0;
                default:
                    return -1;
            }
        }
    }
}
