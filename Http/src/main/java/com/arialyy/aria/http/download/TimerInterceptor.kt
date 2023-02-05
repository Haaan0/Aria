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

import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.inf.ITaskOption
import com.arialyy.aria.core.manager.ThreadTaskManager
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import timber.log.Timber
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @Author laoyuyu
 * @Description
 * @Date 2:27 PM 2023/2/05
 **/
open class TimerInterceptor : ITaskInterceptor {
  private var mTimer: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)

  @Synchronized private fun closeTimer() {
    if (!mTimer.isShutdown) {
      mTimer.shutdown()
    }
  }

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    val b = startTimer(chain)
    if (!b) {
      return TaskResp(TaskResp.CODE_INTERRUPT)
    }
    return chain.proceed(chain.getTask())
  }

  @Synchronized private fun startTimer(chain: TaskChain): Boolean {
    closeTimer()
    try {
      mTimer = ScheduledThreadPoolExecutor(1)
      val blockManager = chain.blockManager
      mTimer.scheduleWithFixedDelay(object : Runnable {
        override fun run() {
          // 线程池中是不抛异常的，没有日志，很难定位问题，需要手动try-catch
          try {
            if (blockManager.isCompleted
              || blockManager.hasFailedBlock()
              || !isRunning(chain.getTask())
            ) {
              ThreadTaskManager.getInstance().removeTaskThread(chain.getTask().taskId)
              closeTimer()
              return
            }
            if (chain.getTask().taskState.curProgress >= 0) {
              chain.getTask().getTaskOption(ITaskOption::class.java).taskListener.onProgress(
                blockManager.currentProgress
              )
              return
            }
            Timber.d("未知状态")
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }

      }, 0, 1000, MILLISECONDS)
    } catch (e: Exception) {
      Timber.e(e)
      return false
    }
    return true
  }

  @Synchronized fun isRunning(task: ITask): Boolean {
    return ThreadTaskManager.getInstance().taskIsRunning(task.taskId)
  }
}