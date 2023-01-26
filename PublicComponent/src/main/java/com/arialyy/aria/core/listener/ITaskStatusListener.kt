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
package com.arialyy.aria.core.listener

import com.arialyy.aria.core.task.ITask

/**
 * @Author laoyuyu
 * @Description
 * @Date 11:43 AM 2023/1/22
 **/
interface ITaskStatusListener<TASK : ITask> {
  /**
   * 队列已经满了，继续创建任务，将会回调该方法
   */
  fun onWait(task: TASK) {}

  /**
   * 预处理，有时有些地址链接比较慢，这时可以先在这个地方出来一些界面上的UI，如按钮的状态。
   * 在这个回调中，任务是获取不到文件大小，下载速度等参数
   */
  fun onPre(task: TASK) {}

  /**
   * 任务预加载完成
   */
  fun onTaskPre(task: TASK) {}

  /**
   * 任务恢复下载
   */
  fun onTaskResume(task: TASK) {}

  /**
   * 任务开始
   */
  fun onTaskStart(task: TASK) {}

  /**
   * 任务停止
   */
  fun onTaskStop(task: TASK) {}

  /**
   * 任务取消
   */
  fun onTaskCancel(task: TASK) {}

  /**
   * 任务失败
   */
  fun onTaskFail(task: TASK, e: Exception?) {}

  /**
   * 任务完成
   */
  fun onTaskComplete(task: TASK) {}

  /**
   * 任务执行中
   */
  fun onTaskRunning(task: TASK) {}
}