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
package com.arialyy.aria.http.download

import com.arialyy.aria.core.command.DeleteCmd
import com.arialyy.aria.core.command.StopCmd
import com.arialyy.aria.core.task.TaskCachePool
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 20:25 2023/2/19
 **/
class HttpDStopController(val taskId: Int) {

  fun delete() {
    val task = TaskCachePool.getTaskById(taskId)
    if (task == null) {
      Timber.e("task not found, taskId: $taskId")
      return
    }
    DeleteCmd(task).executeCmd()
  }

  fun stop() {
    val task = TaskCachePool.getTaskById(taskId)
    if (task == null) {
      Timber.e("task not found, taskId: $taskId")
      return
    }
    StopCmd(task).executeCmd()
  }

  fun resume() {
    val task = TaskCachePool.getTaskById(taskId)
    if (task == null) {
      Timber.e("task not found, taskId: $taskId")
      return
    }
    val util = task.adapter
    if (util == null) {
      Timber.e("resume fail, please restart task, taskId: $taskId")
      return
    }
    util.start()
  }

}