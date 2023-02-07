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

package com.arialyy.aria.core.manager;

import android.text.TextUtils;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.core.task.IThreadTask;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程任务管理器
 */
@Deprecated
public class ThreadTaskManager1 {
  private final String TAG = CommonUtil.getClassName(this);
  private static volatile ThreadTaskManager INSTANCE = null;
  private static final int CORE_POOL_NUM = 20;
  private static final ReentrantLock LOCK = new ReentrantLock();
  private final ThreadPoolExecutor mExePool;
  private final Map<Integer, Set<FutureContainer>> mThreadTasks = new ConcurrentHashMap<>();

  public static synchronized ThreadTaskManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ThreadTaskManager();
    }
    return INSTANCE;
  }

  private ThreadTaskManager() {
    mExePool = new ThreadPoolExecutor(CORE_POOL_NUM, Integer.MAX_VALUE,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>());
    mExePool.allowsCoreThreadTimeOut();
  }

  /**
   * 删除所有线程任务
   */
  public void removeAllThreadTask() {
    if (mThreadTasks.isEmpty()) {
      return;
    }
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      for (Set<FutureContainer> threads : mThreadTasks.values()) {
        for (FutureContainer container : threads) {
          if (container.future.isDone() || container.future.isCancelled()) {
            continue;
          }
          container.threadTask.destroy();
        }
        threads.clear();
      }
      mThreadTasks.clear();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 启动线程任务
   *
   * @param taskId {@link ITask#getTaskId()}
   * @param threadTask 线程任务{@link IThreadTask}
   */
  public void startThread(Integer taskId, IThreadTask threadTask) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return;
      }
      Set<FutureContainer> temp = mThreadTasks.get(taskId);
      if (temp == null) {
        temp = new HashSet<>();
        mThreadTasks.put(taskId, temp);
      }
      FutureContainer container = new FutureContainer();
      container.threadTask = threadTask;
      container.future = mExePool.submit(threadTask);
      temp.add(container);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 任务是否在执行
   *
   * @return {@code true} 任务正在运行
   */
  public boolean taskIsRunning(Integer taskId) {
    return mThreadTasks.get(taskId) != null;
  }

  /**
   * 停止任务的所有线程
   */
  public void removeTaskThread(Integer taskId) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return;
      }
      Set<FutureContainer> temp = mThreadTasks.get(taskId);
      if (temp != null && temp.size() > 0) {
        for (FutureContainer container : temp) {
          if (container.future.isDone() || container.future.isCancelled()) {
            continue;
          }
          container.threadTask.destroy();
        }
        temp.clear();
        mThreadTasks.remove(taskId);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * 根据线程名删除任务的中的线程
   *
   * @param threadName 线程名
   * @return true 删除线程成功；false 删除线程失败
   */
  public boolean removeSingleTaskThread(Integer taskId, String threadName) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return false;
      }
      if (TextUtils.isEmpty(threadName)) {
        ALog.e(TAG, "线程名为空");
        return false;
      }

      Set<FutureContainer> temp = mThreadTasks.get(taskId);
      if (temp != null && temp.size() > 0) {
        FutureContainer tempC = null;
        for (FutureContainer container : temp) {
          if (container.threadTask.getThreadName().equals(threadName)) {
            tempC = container;
            break;
          }
        }
        if (tempC != null) {
          tempC.threadTask.destroy();
          temp.remove(tempC);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
    return false;
  }

  /**
   * 删除单个线程任务
   *
   * @param task 线程任务
   */
  public boolean removeSingleTaskThread(Integer taskId, IThreadTask task) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return false;
      }
      if (task == null) {
        ALog.e(TAG, "线程任务为空");
        return false;
      }
      Set<FutureContainer> temp = mThreadTasks.get(taskId);
      if (temp != null && temp.size() > 0) {
        FutureContainer tempC = null;
        for (FutureContainer container : temp) {
          if (container.threadTask == task) {
            tempC = container;
            break;
          }
        }
        if (tempC != null) {
          task.destroy();
          temp.remove(tempC);
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
    return false;
  }

  /**
   * 重试线程任务
   *
   * @param task 线程任务
   */
  public void retryThread(IThreadTask task) {
    try {
      LOCK.tryLock(2, TimeUnit.SECONDS);
      if (mExePool.isShutdown()) {
        ALog.e(TAG, "线程池已经关闭");
        return;
      }
      try {
        if (task == null || task.isDestroy()) {
          ALog.e(TAG, "线程为空或线程已经中断");
          return;
        }
      } catch (Exception e) {
        ALog.e(TAG, "", e);
        return;
      }
      mExePool.submit(task);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  private static class FutureContainer {
    Future future;
    IThreadTask threadTask;
  }
}
