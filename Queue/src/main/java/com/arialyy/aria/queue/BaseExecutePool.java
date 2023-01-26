/*
 * Copyright (C) 2016  AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arialyy.aria.queue;

import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.task.ITask;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import timber.log.Timber;

/**
 * Created by lyy on 2016/8/15. 任务执行池，所有当前下载任务都该任务池中，默认下载大小为2
 */
public class BaseExecutePool<TASK extends ITask> implements IPool<TASK> {
  private static final int MAX_NUM = 2;  //max task size
  private static final Object LOCK = new Object();
  private Deque<TASK> mExecuteQueue;
  private int mSize;

  BaseExecutePool() {
    mSize = getPoolSize();
    mExecuteQueue = new LinkedBlockingDeque<>(mSize);
  }

  /**
   * return max queue size
   */
  @Override
  public int getPoolSize() {
    return MAX_NUM;
  }

  /**
   * 获取所有正在执行的任务
   */
  @Override
  public List<TASK> getAllTask() {
    return new ArrayList<>(mExecuteQueue);
  }

  @Override public boolean putTask(TASK task) {
    synchronized (LOCK) {
      if (task == null) {
        Timber.e("task is null");
        return false;
      }
      if (mExecuteQueue.contains(task)) {
        Timber.e("put task fail: %s is already in the queue", task.getTaskId());
        return false;
      }
      if (mExecuteQueue.size() >= mSize && pollFirstTask()) {
        return putNewTask(task);
      }
      return putNewTask(task);
    }
  }

  /**
   * update pool size
   */
  @Override
  public void setPoolSize(int newSize) {
    if (newSize < 1) {
      Timber.e("update pool size fail, size less than 1");
      return;
    }
    synchronized (LOCK) {
      Deque<TASK> temp = new LinkedBlockingDeque<>(newSize);
      TASK task;
      while ((task = mExecuteQueue.poll()) != null) {
        temp.offer(task);
      }
      mExecuteQueue = temp;
      mSize = newSize;
    }
  }

  /**
   * 添加新任务
   *
   * @param newTask 新任务
   */
  boolean putNewTask(TASK newTask) {
    synchronized (LOCK) {
      boolean s = mExecuteQueue.offer(newTask);
      Timber.d("offer %s into queue %s ", newTask.getTaskId(), (s ? "success" : "fail"));
      return s;
    }
  }

  /**
   * check pool, if pool size equal to max size, remove first task
   */
  boolean pollFirstTask() {
    synchronized (LOCK) {
      TASK oldTask = mExecuteQueue.pollFirst();
      if (oldTask == null) {
        Timber.w("poll task fail, task is null");
        return false;
      }
      oldTask.stop(TaskSchedulerType.TYPE_DEFAULT);
      return true;
    }
  }

  @Override public TASK pollTask() {
    synchronized (LOCK) {
      return mExecuteQueue.poll();
    }
  }

  @Override public TASK getTask(int taskId) {
    if (taskId < 0) {
      Timber.e("invalid taskId");
      return null;
    }
    synchronized (LOCK) {
      for (TASK task : mExecuteQueue) {
        if (task.getTaskId() == taskId) {
          return task;
        }
      }
    }
    Timber.w("not found task, taskId: %s", taskId);
    return null;
  }

  @Override public boolean taskExist(int taskId) {
    return getTask(taskId) != null;
  }

  @Override public boolean removeTask(int taskId) {
    if (taskId < 0) {
      Timber.e("invalid taskId");
      return false;
    }
    TASK task = getTask(taskId);
    if (task == null) {
      Timber.e("task not exist");
      return false;
    }
    synchronized (LOCK) {
      return mExecuteQueue.remove(getTask(taskId));
    }
  }

  @Override public int size() {
    return mExecuteQueue.size();
  }

  @Override public void clear() {
    Timber.i("exe queue does not support clear operation");
  }
}