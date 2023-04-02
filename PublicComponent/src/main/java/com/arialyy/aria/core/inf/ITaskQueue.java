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

package com.arialyy.aria.core.inf;

import com.arialyy.aria.core.service.IService;
import com.arialyy.aria.core.task.ITask;

/**
 * Created by lyy on 2016/8/16. 任务功能接口
 */
public interface ITaskQueue<TASK extends ITask> extends IService {

  /**
   * add task to cache queue
   */
  boolean addTask(TASK task);

  /**
   * @return {@code true} task exists
   */
  boolean taskExists(int taskId);

  /**
   * @return {@code true} task is running
   */
  boolean taskIsRunning(int taskId);

  /**
   * stop task
   */
  void stopTask(TASK task);

  /**
   * stop all task
   */
  void stopAllTask();

  /**
   * start a task
   *
   * @return TaskId is returned if the task is created successfully, and -1 is returned for failure.
   */
  int startTask(TASK task);

  /**
   * remove task
   */
  void deleteTask(TASK task);

  /**
   * Delete all active tasks
   */
  void deleteAllTask();

  /**
   * 重试
   */
  void reTry(TASK task);

  /**
   * get cache queue size
   */
  int getCacheSize();

  /**
   * get process queue size
   */
  int getQueueSize();

  /**
   * modify process queue size
   */
  void setQueueSize(int size);

  /**
   * queue is full
   *
   * @return true full
   */
  boolean isFull();

  /**
   * get task by id
   */
  TASK getTask(int taskId);

  /**
   * get next task
   */
  boolean startNextTask();
}