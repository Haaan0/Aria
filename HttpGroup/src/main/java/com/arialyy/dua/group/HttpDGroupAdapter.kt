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
package com.arialyy.dua.group

import android.os.Looper
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.task.AbsTaskAdapter
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.core.task.ThreadTaskManager2
import com.arialyy.aria.exception.AriaException
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.http.download.TimerInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 21:58 2023/2/20
 **/
internal class HttpDGroupAdapter : AbsTaskAdapter() {
  private val taskManager by lazy {
    val manager = HttpDGTaskManager(getTask())
    ThreadTaskManager2.putTaskManager(getTask().taskId, manager)
    manager
  }

  init {
    getTask().getTaskOption(HttpTaskOption::class.java).eventListener =
      HttpDGEventListener(getTask() as HttpDGroupTask)
  }

  override fun getBlockManager(): IBlockManager {
    return taskManager
  }

  override fun isRunning(): Boolean {
    return ThreadTaskManager2.getTaskManager(getTask().taskId)?.isRunning() == true
  }

  override fun cancel() {
    ThreadTaskManager2.getTaskManager(getTask().taskId)?.let {
      if (it.isCanceled()) {
        Timber.w("task already canceled, taskId: ${getTask().taskId}")
        return
      }
      it.cancel()
    }
  }

  override fun stop() {
    ThreadTaskManager2.getTaskManager(getTask().taskId)?.let {
      if (it.isStopped()) {
        Timber.w("task already stopped, taskId: ${getTask().taskId}")
        return
      }
      it.stop()
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
      taskManager.setLooper()
      addCoreInterceptor(HttpDGCheckInterceptor())
      addCoreInterceptor(TimerInterceptor())
      addCoreInterceptor(HttpDGSubTaskInterceptor())

      val resp = interceptor()
      if (resp == null || resp.code != TaskResp.CODE_SUCCESS) {
        getTask().getTaskOption(HttpTaskOption::class.java).eventListener.onFail(
          false,
          AriaException("start task fail, task interrupt, code: ${resp?.code ?: TaskResp.CODE_INTERRUPT}")
        )
        taskManager.stop()
        return@launch
      }
      Looper.loop()
    }
  }
}