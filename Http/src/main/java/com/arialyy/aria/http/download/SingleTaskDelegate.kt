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

import android.os.Looper
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.task.DBlockManager
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.core.task.ThreadTaskManager2
import com.arialyy.aria.exception.AriaException
import com.arialyy.aria.http.HttpTaskOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 11:32 2023/3/12
 **/
internal class SingleTaskDelegate(val adapter: HttpDTaskAdapter) : ITaskAdapterDelegate {
  private val blockManager = DBlockManager(adapter.getTask())

  init {
    ThreadTaskManager2.putThreadManager(adapter.getTask().taskId, blockManager)
  }

  override fun isRunning(): Boolean {
    return blockManager.isRunning()
  }

  override fun cancel() {
    if (blockManager.isCanceled()) {
      Timber.w("task already canceled, taskId: ${adapter.getTask().taskId}")
      return
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      ThreadTaskManager2.stopThreadTask(adapter.getTask().taskId, true)
    }
  }

  override fun stop() {
    if (blockManager.isStopped()) {
      Timber.w("task already stopped, taskId: ${adapter.getTask().taskId}")
      return
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      ThreadTaskManager2.stopThreadTask(adapter.getTask().taskId)
    }
  }

  override fun start() {
    adapter.getTask().getTaskOption(HttpTaskOption::class.java).taskInterceptor.let {
      if (it.isNotEmpty()) {
        adapter.addInterceptors(it)
      }
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      Looper.prepare()
      blockManager.setLooper()
      adapter.addCoreInterceptor(HttpDCheckInterceptor())
      adapter.addCoreInterceptor(TimerInterceptor())
      adapter.addCoreInterceptor(HttpDHeaderInterceptor())
      adapter.addCoreInterceptor(HttpDBlockInterceptor())
      adapter.addCoreInterceptor(HttpBlockThreadInterceptor())
      val resp = adapter.interceptor()
      if (resp == null || resp.code != TaskResp.CODE_SUCCESS) {
        adapter.getTask().getTaskOption(HttpTaskOption::class.java).eventListener.onFail(
          false,
          AriaException("start task fail, task interrupt, code: ${resp?.code ?: TaskResp.CODE_INTERRUPT}")
        )
        blockManager.stop()
        return@launch
      }
      Looper.loop()
    }
  }

  override fun setBlockManager(blockManager: IBlockManager) {
    throw UnsupportedOperationException("Single task does not support setting up a block manager")
  }

  override fun getBlockManager(): IBlockManager {
    return blockManager
  }
}