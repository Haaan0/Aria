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
import com.arialyy.aria.core.task.AbsTaskAdapter
import com.arialyy.aria.core.task.SingleDownloadTask
import com.arialyy.aria.http.HttpTaskOption

/**
 * @Author laoyuyu
 * @Description
 * @Date 1:47 PM 2023/1/28
 **/
class HttpDTaskAdapter(val isSubTask: Boolean = false) : AbsTaskAdapter() {

  private val delegate: ITaskAdapterDelegate

  init {
    getTask().getTaskOption(HttpTaskOption::class.java).eventListener =
      HttpDEventListener(getTask() as SingleDownloadTask)
    delegate = if (isSubTask) {
      SubTaskDelegate(this)
    } else {
      SingleTaskDelegate(this)
    }
  }

  fun setBlockManager(blockManager: IBlockManager) {
    delegate.setBlockManager(blockManager)
  }

  override fun getBlockManager(): IBlockManager {
    return delegate.getBlockManager()
  }

  override fun isRunning(): Boolean {
    return delegate.isRunning()
  }

  override fun cancel() {
    delegate.cancel()
  }

  override fun stop() {
    delegate.stop()
  }

  override fun start() {
    delegate.start()
  }
}