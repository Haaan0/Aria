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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.locks.ReentrantLock

object ThreadTaskManager2 {
  private val mThreadTasks: ConcurrentHashMap<Int, MutableSet<IThreadTask>> = ConcurrentHashMap()
  private val LOCK = ReentrantLock()

  /**
   * 任务是否在执行
   *
   * @return `true` 任务正在运行
   */
  fun taskIsRunning(taskId: Int): Boolean {
    return mThreadTasks[taskId] != null
  }

  /**
   * stop thread task
   * @param isRemoveTask if true, remove task and block
   */
  fun stopThreadTask(taskId: Int, isRemoveTask: Boolean = false) {
    try {
      LOCK.tryLock(2, SECONDS)
      val threadTaskList: MutableSet<IThreadTask>? = mThreadTasks[taskId]
      threadTaskList?.forEach {
        if (isRemoveTask) {
          it.cancel()
          return@forEach
        }
        it.stop()
      }
      mThreadTasks.remove(taskId)

    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      LOCK.unlock()
    }
  }

  /**
   * 删除所有线程任务
   */
  fun removeAllThreadTask() {
    if (mThreadTasks.isEmpty()) {
      return
    }
    try {
      LOCK.tryLock(2, SECONDS)
      for (threads in mThreadTasks.values) {
        for (tt in threads) {
          tt.stop()
        }
        threads.clear()
      }
      mThreadTasks.clear()
    } catch (e: InterruptedException) {
      e.printStackTrace()
    } finally {
      LOCK.unlock()
    }
  }

}