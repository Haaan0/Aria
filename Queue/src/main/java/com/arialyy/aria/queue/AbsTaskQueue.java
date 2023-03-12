/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
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

import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.IPool;
import com.arialyy.aria.core.inf.ITaskQueue;
import com.arialyy.aria.core.inf.TaskSchedulerType;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.task.ThreadTaskManager2;
import timber.log.Timber;

/**
 * Created by lyy on 2017/2/23. 任务队列
 */
public abstract class AbsTaskQueue<TASK extends ITask> implements ITaskQueue<TASK> {

  private final IPool<TASK> DEF_CACHE_POOL = new BaseCachePool<>();
  private final IPool<TASK> DEF_EXE_POOL = new BaseExecutePool<>();
  private int maxSize;

  protected AbsTaskQueue() {
    maxSize = getMaxTaskSize();
  }

  @Override public boolean isFull() {
    return getExePool().size() < maxSize;
  }

  protected IPool<TASK> getCachePool() {
    return DEF_CACHE_POOL;
  }

  protected IPool<TASK> getExePool() {
    return DEF_EXE_POOL;
  }

  protected abstract int getMaxTaskSize();

  @Override public boolean addTask(TASK task) {
    if (task == null) {
      Timber.e("task is null");
      return false;
    }
    getCachePool().putTask(task);
    return false;
  }

  @Override public boolean taskExists(int taskId) {
    return getCachePool().taskExist(taskId) || getExePool().taskExist(taskId);
  }

  /**
   * @return if exePool has task, return true, otherwise false
   */
  @Override public boolean taskIsRunning(int taskId) {
    if (getExePool().getTask(taskId) != null) {
      return true;
    }
    boolean b = ThreadTaskManager2.INSTANCE.taskIsRunning(taskId);
    if (b) {
      ThreadTaskManager2.INSTANCE.stopThreadTask(taskId, false);
    }
    return false;
  }

  @Override public int startTask(TASK task) {
    if (task == null) {
      Timber.e("task is null");
      return -1;
    }
    if (getExePool().taskExist(task.getTaskId())) {
      Timber.w("task running, taskId: %s", task.getTaskId());
      return task.getTaskId();
    }
    Timber.i("start a task, taskId: %s", task.getTaskId());
    if (getExePool().size() >= getMaxTaskSize()) {
      Timber.i("exe queue is full, task into cache queue");
      task.setState(IEntity.STATE_WAIT);
      boolean b = getCachePool().putTask(task);
      return b ? task.getTaskId() : -1;
    }
    boolean b = getExePool().putTask(task);
    task.start(TaskSchedulerType.TYPE_DEFAULT);
    return b ? task.getTaskId() : -1;
  }

  @Override public void removeTask(TASK task) {
    if (task == null) {
      Timber.e("task is null");
      return;
    }
    if (getCachePool().taskExist(task.getTaskId())) {
      Timber.i("cache pool has task, which will be removed from the cache pool");
      getCachePool().removeTask(task.getTaskId());
    }
    if (getExePool().taskExist(task.getTaskId())) {
      stopTask(task);
      getExePool().removeTask(task.getTaskId());
    }
  }

  /**
   * 停止所有任务
   */
  @Override public void stopAllTask() {
    for (TASK task : getExePool().getAllTask()) {
      if (task != null) {
        int state = task.getTaskState().getState();
        if (task.isRunning()
            || (state != IEntity.STATE_COMPLETE && state != IEntity.STATE_CANCEL)) {
          task.stop(TaskSchedulerType.TYPE_STOP_NOT_NEXT);
        }
      }
    }

    for (TASK task : getCachePool().getAllTask()) {
      if (task != null) {
        task.stop(TaskSchedulerType.TYPE_STOP_NOT_NEXT);
      }
    }
    ThreadTaskManager2.INSTANCE.stopAllThreadTask();
    getCachePool().clear();
  }

  /**
   * 获取配置文件旧的最大任务数
   */
  public abstract int getOldMaxSize();

  @Override public void stopTask(TASK task) {
    if (task == null) {
      Timber.e("stop fail, task is null");
      return;
    }
    int state = task.getTaskState().getState();
    boolean canStop = false;
    switch (state) {
      case IEntity.STATE_WAIT:
        getCachePool().removeTask(task.getTaskId());
        canStop = true;
        break;
      case IEntity.STATE_POST_PRE:
      case IEntity.STATE_PRE:
      case IEntity.STATE_RUNNING:
        getExePool().removeTask(task.getTaskId());
        canStop = true;
        break;
      case IEntity.STATE_STOP:
      case IEntity.STATE_OTHER:
      case IEntity.STATE_FAIL:
        Timber.w("stop task fail，it already topped, taskId: %d", task.getTaskId());
        if (taskIsRunning(task.getTaskId())) {
          getCachePool().removeTask(task.getTaskId());
          getExePool().removeTask(task.getTaskId());
          if (ThreadTaskManager2.INSTANCE.taskIsRunning(task.getTaskId())) {
            ThreadTaskManager2.INSTANCE.stopThreadTask(task.getTaskId(), false);
          }
        }
        break;
      case IEntity.STATE_CANCEL:
        Timber.w("stop task fail, it already removed, taskId: %d", task.getTaskId());
        break;
      case IEntity.STATE_COMPLETE:
        Timber.w("stop task fail, it already completed, taskId: %d", task.getTaskId());
        break;
    }

    if (canStop) {
      task.stop(TaskSchedulerType.TYPE_DEFAULT);
    }
  }

  @Override public void reTry(TASK task) {
    if (task == null) {
      Timber.e("task is null");
      return;
    }

    int state = task.getTaskState().getState();
    switch (state) {
      case IEntity.STATE_POST_PRE:
      case IEntity.STATE_PRE:
      case IEntity.STATE_RUNNING:
        Timber.w("task is running, will restart task, taskId: %d", task.getTaskId());
        task.stop(TaskSchedulerType.TYPE_STOP_NOT_NEXT);
        task.start(TaskSchedulerType.TYPE_DEFAULT);
        break;
      case IEntity.STATE_WAIT:
      case IEntity.STATE_STOP:
      case IEntity.STATE_OTHER:
      case IEntity.STATE_FAIL:
        task.start(TaskSchedulerType.TYPE_DEFAULT);
        break;
      case IEntity.STATE_CANCEL:
        Timber.e("retry task fail, it already removed, taskId: %d", task.getTaskId());
        break;
      case IEntity.STATE_COMPLETE:
        Timber.e("retry task fail, it already completed, taskId: %d", task.getTaskId());
        break;
    }
  }

  @Override public int getCacheSize() {
    return getCachePool().size();
  }

  @Override public int getQueueSize() {
    return getExePool().size();
  }

  @Override public void setQueueSize(int size) {
    int oldMaxSize = getOldMaxSize();
    int diff = maxSize - oldMaxSize;
    if (oldMaxSize == maxSize) {
      Timber.w("There is no change in size");
      return;
    }
    maxSize = size;
    //设置的任务数小于配置任务数
    if (diff <= -1 && getExePool().size() >= oldMaxSize) {
      for (int i = 0, len = Math.abs(diff); i < len; i++) {
        TASK eTask = getExePool().pollTask();
        if (eTask != null) {
          stopTask(eTask);
        }
      }
    }
    getExePool().setPoolSize(maxSize);
    if (diff >= 1) {
      for (int i = 0; i < diff; i++) {
        startNextTask();
      }
    }
  }

  @Override public TASK getTask(int taskId) {
    if (taskId < 1) {
      Timber.e("invalid taskId: %d", taskId);
      return null;
    }
    TASK ct = getCachePool().getTask(taskId);
    if (ct != null) {
      return ct;
    }
    TASK et = getExePool().getTask(taskId);
    if (et != null) {
      return et;
    }
    Timber.w("not found task, taskId: %d", taskId);
    return null;
  }

  @Override public boolean startNextTask() {
    TASK nextTask = getCachePool().pollTask();
    if (nextTask != null && nextTask.getTaskState().getState() == IEntity.STATE_WAIT) {
      return startTask(nextTask) != -1;
    }
    Timber.w("start next fail");
    return false;
  }
}
