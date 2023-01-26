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

import com.arialyy.aria.core.task.ITask;
import java.util.List;

/**
 * Created by lyy on 2016/8/14. 任务池
 */
public interface IPool<T extends ITask> {

  List<T> getAllTask();

  void setPoolSize(int newSize);

  int getPoolSize();

  /**
   * 将下载任务添加到任务池中
   */
  boolean putTask(T task);

  /**
   * 按照队列原则取出任务
   *
   * @return 返回null或者下载任务
   */
  T pollTask();

  /**
   * 通过key获取任务，当任务不为空时，队列将删除该下载任务
   *
   * @return 返回null或者下载任务
   */
  T getTask(int taskId);

  /**
   * 任务是在存在
   *
   * @return {@code true} 任务存在
   */
  boolean taskExist(int taskId);

  /**
   * @return true:移除成功
   */
  boolean removeTask(int taskId);

  /**
   * 池子大小
   *
   * @return 返回缓存池或者执行池大小
   */
  int size();

  /**
   * remove all task
   */
  void clear();
}