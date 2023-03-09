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

import android.os.Handler
import android.os.Looper
import com.arialyy.aria.core.inf.ITaskManager
import com.arialyy.aria.http.HttpTaskOption

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:43 PM 2023/3/7
 **/
internal class HttpDGTaskManager : ITaskManager {
  private lateinit var looper: Looper
  private lateinit var handler: Handler

  private val callback = Handler.Callback { msg ->
    when (msg.what) {
      ITaskManager.STATE_STOP -> {
      }
      ITaskManager.STATE_CANCEL -> {
      }
      ITaskManager.STATE_FAIL -> {
      }
      ITaskManager.STATE_COMPLETE -> {
      }
      ITaskManager.STATE_RUNNING -> {
      }
      ITaskManager.STATE_UPDATE_PROGRESS -> {
      }
    }
    false
  }

  fun start(taskOption: HttpTaskOption) {
    taskOption.getOptionAdapter(HttpDGOptionAdapter::class.java).subUrlList
  }

  override fun setLooper() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw IllegalThreadStateException("io operations cannot be in the main thread")
    }
    looper = Looper.myLooper()!!
    handler = Handler(looper, callback)
  }

  override fun stop() {
  }

  override fun isCompleted(): Boolean {
    TODO("Not yet implemented")
  }

  override fun getCurrentProgress(): Long {
    TODO("Not yet implemented")
  }

  override fun isStopped(): Boolean {
    TODO("Not yet implemented")
  }

  override fun isCanceled(): Boolean {
    TODO("Not yet implemented")
  }

  override fun isRunning(): Boolean {
    TODO("Not yet implemented")
  }

  override fun getHandler(): Handler {
    return handler
  }

  override fun hasFailedTask(): Boolean {
    TODO("Not yet implemented")
  }
}