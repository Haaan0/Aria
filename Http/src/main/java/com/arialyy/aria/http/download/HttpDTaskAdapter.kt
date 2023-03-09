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
import com.arialyy.aria.core.task.AbsTaskAdapter
import com.arialyy.aria.core.task.BlockManager
import com.arialyy.aria.core.task.DownloadTask
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
 * @Date 1:47 PM 2023/1/28
 **/
internal class HttpDTaskAdapter : AbsTaskAdapter() {

  private var blockManager: BlockManager? = null

  init {
    getTask().getTaskOption(HttpTaskOption::class.java).eventListener =
      HttpDEventListener(getTask() as DownloadTask)
  }

  override fun getTaskManager(): BlockManager {
    if (blockManager == null) {
      blockManager = BlockManager(getTask())
    }
    return blockManager!!
  }

  override fun isRunning(): Boolean {
    return getTaskManager().isRunning()
  }

  override fun cancel() {
    if (getTaskManager().isCanceled()) {
      Timber.w("task already canceled, taskId: ${getTask().taskId}")
      return
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      ThreadTaskManager2.stopThreadTask(getTask().taskId, true)
    }
  }

  override fun stop() {
    if (getTaskManager().isStopped()) {
      Timber.w("task already stopped, taskId: ${getTask().taskId}")
      return
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      ThreadTaskManager2.stopThreadTask(getTask().taskId)
    }
  }

  override fun start() {
    getTask().getTaskOption(HttpTaskOption::class.java).taskInterceptor.let {
      if (it.isNotEmpty()) {
        addInterceptors(it)
      }
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      Looper.prepare()
      blockManager?.setLooper()
      addCoreInterceptor(HttpDCheckInterceptor())
      addCoreInterceptor(TimerInterceptor())
      addCoreInterceptor(HttpDHeaderInterceptor())
      addCoreInterceptor(HttpDBlockInterceptor())
      addCoreInterceptor(HttpBlockThreadInterceptor())
      val resp = interceptor()
      if (resp == null || resp.code != TaskResp.CODE_SUCCESS) {
        getTask().getTaskOption(HttpTaskOption::class.java).eventListener.onFail(
          false,
          AriaException("start task fail, task interrupt, code: ${resp?.code ?: TaskResp.CODE_INTERRUPT}")
        )
        blockManager?.stop()
        return@launch
      }
      Looper.loop()
    }
  }

  override fun resume() {
    getTask().getTaskOption(HttpTaskOption::class.java).taskInterceptor.let {
      if (it.isNotEmpty()) {
        addInterceptors(it)
      }
    }
    DuaContext.duaScope.launch(Dispatchers.IO) {
      Looper.prepare()
      blockManager?.setLooper()
      addCoreInterceptor(TimerInterceptor())
      addCoreInterceptor(HttpDBlockInterceptor())
      addCoreInterceptor(HttpBlockThreadInterceptor())
      val resp = interceptor()
      if (resp == null || resp.code != TaskResp.CODE_SUCCESS) {
        getTask().getTaskOption(HttpTaskOption::class.java).eventListener.onFail(
          false,
          AriaException("start task fail, task interrupt, code: ${resp?.code ?: TaskResp.CODE_INTERRUPT}")
        )
        blockManager?.stop()
        return@launch
      }
      Looper.loop()
    }
  }
}