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

import android.net.Uri
import com.arialyy.aria.core.task.TaskCachePool
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 20:25 2023/2/19
 **/
class HttpDStopController(val taskId: Int) {

  fun cancel() {
    val task = TaskCachePool.getTask(taskId)
    if (task == null) {
      Timber.e("task not found, taskId: $taskId")
      return
    }
    HttpDStopController2(Uri.parse(task.filePath)).cancel()
  }

  fun stop() {
    val task = TaskCachePool.getTask(taskId)
    if (task == null) {
      Timber.e("task not found, taskId: $taskId")
      return
    }
    HttpDStopController2(Uri.parse(task.filePath)).stop()
  }

}