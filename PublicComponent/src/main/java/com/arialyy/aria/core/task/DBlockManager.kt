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

import android.os.Handler
import android.os.Handler.Callback
import android.os.Looper
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.inf.ITaskManager
import com.arialyy.aria.core.inf.ITaskOption
import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.exception.AriaException
import com.arialyy.aria.orm.entity.BlockRecord
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

class BlockManager(task: ITask) : IBlockManager {
  private val unfinishedBlock = mutableListOf<BlockRecord>()
  private val canceledNum = AtomicInteger(0) // 已经取消的线程的数
  private val stoppedNum = AtomicInteger(0) // 已经停止的线程数
  private val failedNum = AtomicInteger(0) // 失败的线程数
  private val completedNum = AtomicInteger(0) // 完成的线程数
  private val threadNum = task.getTaskOption(ITaskOption::class.java).threadNum
  private val scope = MainScope()
  private val threadPool = ThreadPoolExecutor(
    threadNum, threadNum,
    0L, MILLISECONDS,
    LinkedBlockingQueue(),
  )
  private val dispatcher = threadPool.asCoroutineDispatcher()

  private var progress: Long = 0 //当前总进度
  private lateinit var looper: Looper
  private lateinit var handler: Handler
  private var blockNum: Int = 1
  private var eventListener: IEventListener =
    task.getTaskOption(ITaskOption::class.java).eventListener

  private val callback = Callback { msg ->
    when (msg.what) {
      ITaskManager.STATE_STOP -> {
        stoppedNum.getAndIncrement()
        if (isStopped()) {
          eventListener.onStop(getCurrentProgress())
          quitLooper()
        }
      }
      ITaskManager.STATE_CANCEL -> {
        canceledNum.getAndIncrement()
        if (isCanceled()) {
          eventListener.onCancel()
          quitLooper()
        }
      }
      ITaskManager.STATE_FAIL -> {
        failedNum.getAndIncrement()
        if (hasFailedTask()) {
          val b = msg.data
          eventListener.onFail(
            b.getBoolean(ITaskManager.DATA_RETRY, false),
            b.getSerializable(ITaskManager.DATA_ERROR_INFO) as AriaException?
          )
          quitLooper()
        }
      }
      ITaskManager.STATE_COMPLETE -> {
        completedNum.getAndIncrement()
        if (isCompleted()) {
          Timber.d("isComplete, completeNum = %s", completedNum)
          eventListener.onComplete()
          quitLooper()
        }
      }
      ITaskManager.STATE_RUNNING -> {
        val b = msg.data
        if (b != null) {
          val len = b.getLong(ITaskManager.DATA_ADD_LEN, 0)
          progress += len
        }
      }
      ITaskManager.STATE_UPDATE_PROGRESS -> {
        progress += msg.obj as Long
      }
    }
    false
  }

  /**
   * 退出looper循环
   */
  private fun quitLooper() {
    looper.quit()
    handler.removeCallbacksAndMessages(null)
    scope.cancel()
  }

  override fun putUnfinishedBlock(record: BlockRecord) {
    unfinishedBlock.add(record)
  }

  override fun getUnfinishedBlockList(): List<BlockRecord> {
    return unfinishedBlock
  }

  override fun setLooper() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw IllegalThreadStateException("io operations cannot be in the main thread")
    }
    looper = Looper.myLooper()!!
    handler = Handler(looper, callback)
  }

  override fun start(threadTaskList: List<IThreadTask>) {
    threadTaskList.forEach { tt ->
      scope.launch(dispatcher) {
        tt.run()
      }
    }
  }

  override fun stop() {
    quitLooper()
  }

  override fun setBlockNum(blockNum: Int) {
    this.blockNum = blockNum
  }

  override fun hasFailedTask(): Boolean {
    Timber.d("isFailed, blockBum = ${blockNum}, completedNum = ${completedNum.get()}, ")
    return failedNum.get() != 0
  }

  override fun isCompleted(): Boolean {
    // Timber.d("isCompleted, blockBum = ${blockNum}, completedNum = ${completedNum.get()}, ")
    return completedNum.get() == blockNum
  }

  override fun getCurrentProgress(): Long {
    return progress
  }

  /**
   * 所有子线程是否都已经停止
   */
  override fun isStopped(): Boolean {
    // Timber.d("isStopped, blockBum = ${blockNum}, stoppedNum = ${stoppedNum.get()}, completedNum = ${completedNum.get()}, failedNum = ${failedNum.get()}")
    return stoppedNum.get() + completedNum.get() + failedNum.get() == blockNum
  }

  override fun isCanceled(): Boolean {
    // Timber.d("isStopped, blockBum = ${blockNum}, canceledNum = $canceledNum")
    return canceledNum.get() == blockNum
  }

  override fun isRunning(): Boolean {
    return scope.isActive
  }

  override fun getHandler(): Handler {
    return handler
  }
}