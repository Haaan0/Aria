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

package com.arialyy.aria.core.inf

/**
 * @Author laoyuyu
 * @Description
 * @Date 9:36 PM 2023/3/6
 **/
interface ITaskManager {
  companion object {
    const val STATE_STOP = 0x01
    const val STATE_FAIL = 0x02
    const val STATE_CANCEL = 0x03
    const val STATE_COMPLETE = 0x04
    const val STATE_RUNNING = 0x05
    const val STATE_UPDATE_PROGRESS = 0x06
    const val STATE_PRE = 0x07
    const val STATE_START = 0x08
    const val DATA_RETRY = "DATA_RETRY"
    const val DATA_ERROR_INFO = "DATA_ERROR_INFO"
    const val DATA_THREAD_NAME = "DATA_THREAD_NAME"
    const val DATA_THREAD_LOCATION = "DATA_THREAD_LOCATION"
    const val DATA_ADD_LEN = "DATA_ADD_LEN" // 增加的长度
  }

  fun setLooper()

  fun stop()

  fun cancel()

  /**
   * 任务是否已经完成
   *
   * @return true 任务已完成
   */
  fun isCompleted(): Boolean

  /**
   * 获取当前任务进度
   *
   * @return 任务当前进度
   */
  fun getCurrentProgress(): Long

  fun isStopped(): Boolean

  fun isCanceled(): Boolean

  fun isRunning(): Boolean

  /**
   * 是否有失败的快
   *
   * @return true 有失败的快
   */
  fun hasFailedTask(): Boolean
}