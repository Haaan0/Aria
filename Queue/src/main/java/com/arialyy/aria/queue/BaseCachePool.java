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

import com.arialyy.aria.core.task.ITask;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import timber.log.Timber;

/**
 * Created by lyy on 2016/8/14. 任务缓存池，所有下载任务最先缓存在这个池中
 */
public class BaseCachePool<TASK extends ITask> implements IPool<TASK> {
  private static final int MAX_NUM = Integer.MAX_VALUE;  //最大下载任务数
  private static final Object LOCK = new Object();
  private Deque<TASK> mCacheQueue;
  private int mSize;

  BaseCachePool() {
    mSize = getPoolSize();
    mCacheQueue = new LinkedBlockingDeque<>(MAX_NUM);
  }

  /**
   * 获取被缓存的任务
   */
  @Override
  public List<TASK> getAllTask() {
    return new ArrayList<>(mCacheQueue);
  }

  /**
   * 清除所有缓存的任务
   */
  @Override
  public void clear() {
    mCacheQueue.clear();
  }

  /**
   * 将任务放在队首
   */
  public void putTaskToFirst(TASK task) {
    mCacheQueue.offerFirst(task);
  }

  @Override public void setPoolSize(int newSize) {
    synchronized (LOCK) {
      Deque<TASK> temp = new LinkedBlockingDeque<>(newSize);
      TASK task;
      while ((task = mCacheQueue.poll()) != null) {
        temp.offer(task);
      }
      mCacheQueue = temp;
      mSize = newSize;
    }
  }

  @Override public int getPoolSize() {
    return MAX_NUM;
  }

  @Override public boolean putTask(TASK task) {
    synchronized (LOCK) {
      if (task == null) {
        Timber.e("task is null");
        return false;
      }
      if (mCacheQueue.contains(task)) {
        Timber.e("put task fail, it is already in the queue, taskId: %d", task.getTaskId());
        return false;
      }
      boolean s = mCacheQueue.offer(task);
      Timber.e("put the task in the cache %s", (s ? "success" : "fail"));
      return s;
    }
  }

  @Override public TASK pollTask() {
    synchronized (LOCK) {
      return mCacheQueue.pollFirst();
    }
  }

  @Override public TASK getTask(int taskId) {
    if (taskId <= 0) {
      Timber.e("invalid taskId: %s", taskId);
      return null;
    }
    for (TASK task : mCacheQueue) {
      if (task.getTaskId() == taskId) {
        return task;
      }
    }
    Timber.w("not found task, taskId: %s", taskId);
    return null;
  }

  @Override public boolean taskExist(int taskId) {
    if (taskId <= 0) {
      Timber.e("invalid taskId: %s", taskId);
      return false;
    }

    return getTask(taskId) != null;
  }

  @Override public boolean removeTask(int taskId) {
    if (taskId <= 0) {
      Timber.e("invalid taskId: %s", taskId);
      return false;
    }
    return mCacheQueue.remove(getTask(taskId));
  }

  @Override public int size() {
    return mCacheQueue.size();
  }
}