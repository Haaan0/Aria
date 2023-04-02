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
import com.arialyy.aria.core.inf.TaskSchedulerType
import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.http.download.HttpDTaskAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:43 PM 2023/3/7
 **/
internal class HttpDGTaskManager(private val task: HttpDGTask) : ITaskManager, IBlockManager {
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
  private var progress = 0L
  private var lastUpdateTime = System.currentTimeMillis()
  private val eventListener: IEventListener =
    task.getTaskOption(HttpTaskOption::class.java).eventListener
  private val canceledNum = AtomicInteger(0) // 已经取消的线程的数
  private val stoppedNum = AtomicInteger(0) // 已经停止的线程数
  private val failedNum = AtomicInteger(0) // 失败的线程数
  private val completedNum = AtomicInteger(0) // 完成的线程数

  private val callback = Handler.Callback { msg ->
    when (msg.what) {
      ITaskManager.SUB_STATE_STOP -> {
        stoppedNum.getAndDecrement()
        if (isStopped()) {
          Timber.d("isStopped")
          eventListener.onStop(progress)
          quitLooper()
        }
      }
      ITaskManager.SUB_STATE_CANCEL -> {
        canceledNum.getAndDecrement()
        if (isCanceled()) {
          Timber.d("isCanceled")
          eventListener.onCancel()
          quitLooper()
        }
      }
      ITaskManager.SUB_STATE_FAIL -> {
        failedNum.getAndDecrement()
      }
      ITaskManager.SUB_STATE_COMPLETE -> {
        completedNum.getAndDecrement()
        if (isCompleted()) {
          Timber.d("isCompleted")
          eventListener.onComplete()
        }
      }
      ITaskManager.SUB_STATE_RUNNING -> {
        val len = msg.obj as Long
        progress += len
        task.taskState.curProgress = progress
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
          eventListener.onProgress(progress)
        }
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
    scope.launch(Dispatchers.IO) {
      task.subTaskList.forEach {
        it.stop(TaskSchedulerType.TYPE_DEFAULT)
      }
    }
  }

  override fun delete() {
    scope.launch(Dispatchers.IO) {
      task.subTaskList.forEach {
        it.delete(TaskSchedulerType.TYPE_DEFAULT)
      }
      task.subTaskList.clear()
    }
  }

  override fun isCompleted(): Boolean {
    return completedNum.get() == task.subTaskList.size
  }

  override fun getCurrentProgress(): Long {
    return task.currentProgress
  }

  override fun isStopped(): Boolean {
    return stoppedNum.get() + canceledNum.get() + failedNum.get() + completedNum.get() == task.subTaskList.size
  }

  override fun isCanceled(): Boolean {
    return canceledNum.get() == task.subTaskList.size
  }

  override fun isRunning(): Boolean {
    return scope.isActive
  }

  override fun setBlockNum(blockNum: Int) {
    Timber.e("Group tasks do not support setting threads")
  }

  override fun getHandler(): Handler = handler

  override fun hasFailedTask(): Boolean {
    return failedNum.get() != 0
  }
}