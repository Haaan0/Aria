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
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.inf.ITaskManager
import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.http.download.HttpDTaskAdapter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:43 PM 2023/3/7
 **/
internal class HttpDGTaskManager(val task: HttpDGroupTask) : ITaskManager, IBlockManager {
  private lateinit var looper: Looper
  private lateinit var handler: Handler
  private val subTaskNum = task.dgOptionAdapter.subTaskNum
  private val threadPool = ThreadPoolExecutor(
    subTaskNum, subTaskNum,
    0L, MILLISECONDS,
    LinkedBlockingQueue(),
  )
  private val dispatcher = threadPool.asCoroutineDispatcher()
  private val scope = MainScope()
  private val eventListener: IEventListener =
    task.getTaskOption(HttpTaskOption::class.java).eventListener

  private val callback = Handler.Callback { msg ->
    when (msg.what) {
      ITaskManager.SUB_STATE_STOP -> {
      }
      ITaskManager.SUB_STATE_CANCEL -> {
      }
      ITaskManager.SUB_STATE_FAIL -> {
      }
      ITaskManager.SUB_STATE_COMPLETE -> {
      }
      ITaskManager.SUB_STATE_RUNNING -> {
      }
    }
    false
  }

  override fun setLooper() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw IllegalThreadStateException("io operations cannot be in the main thread")
    }
    looper = Looper.myLooper()!!
    handler = Handler(looper, callback)
  }

  override fun start() {
    task.incompleteTaskList.forEach {
      scope.launch(dispatcher) {
        val adapter = HttpDTaskAdapter(true)
        adapter.init(it)
        adapter.start()
      }
    }
  }

  private fun quitLooper() {
    looper.quit()
    handler.removeCallbacksAndMessages(null)
    scope.cancel()
  }

  override fun stop() {
  }

  override fun cancel() {

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

  override fun setBlockNum(blockNum: Int) {
    TODO("Not yet implemented")
  }

  override fun getHandler(): Handler = handler

  override fun hasFailedTask(): Boolean {
    TODO("Not yet implemented")
  }
}