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
package com.arialyy.aria.core.task

import com.arialyy.aria.core.inf.ITaskManager
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

object ThreadTaskManager2 {
  private val taskManagerMap: ConcurrentHashMap<Int, ITaskManager> = ConcurrentHashMap()

  fun getTaskManager(taskId: Int) = taskManagerMap[taskId]

  fun putTaskManager(taskId: Int, taskManager: ITaskManager) {
    taskManagerMap[taskId] = taskManager
  }

  /**
   * 任务是否在执行
   *
   * @return `true` 任务正在运行
   */
  fun taskIsRunning(taskId: Int): Boolean {
    return taskManagerMap[taskId]?.isRunning() == true
  }

  /**
   * stop thread task
   * @param isRemoveTask if true, remove task and block
   */
  fun stopThreadTask(taskId: Int, isRemoveTask: Boolean = false) {
    val taskManager = taskManagerMap[taskId]
    if (taskManager == null) {
      Timber.d("task already stop, taskId: $taskId")
      return
    }
    if (isRemoveTask) {
      taskManager.delete()
    } else {
      taskManager.stop()
    }
    taskManagerMap.remove(taskId)
  }

  /**
   * 删除所有线程任务
   */
  fun stopAllThreadTask() {
    if (taskManagerMap.isEmpty()) {
      return
    }
    taskManagerMap.forEach {
      it.value.delete()
    }
    taskManagerMap.clear()
  }

}