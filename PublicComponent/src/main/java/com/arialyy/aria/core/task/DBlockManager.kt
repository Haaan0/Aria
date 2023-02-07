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

import android.os.Handler.Callback
import android.os.Looper
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.inf.ITaskOption
import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.exception.AriaException
import com.arialyy.aria.orm.entity.BlockRecord
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

class BlockManager(task: ITask) : IBlockManager {
  private val unfinishedBlockList = mutableListOf<BlockRecord>()
  private val canceledNum = AtomicInteger(0) // 已经取消的线程的数
  private val stoppedNum = AtomicInteger(0) // 已经停止的线程数
  private val failedNum = AtomicInteger(0) // 失败的线程数
  private val completedNum = AtomicInteger(0) // 完成的线程数
  private val channel = Channel<BlockRecord>()
  private val threadNum = task.getTaskOption(ITaskOption::class.java).threadNum
  private val scope = MainScope()
  private val dispatcher = ThreadPoolExecutor(
    threadNum, threadNum,
    0L, MILLISECONDS,
    LinkedBlockingQueue()
  ).asCoroutineDispatcher()

  private var progress: Long = 0 //当前总进度
  private lateinit var looper: Looper
  private var blockNum: Int = 1
  private var eventListener: IEventListener =
    task.getTaskOption(ITaskOption::class.java).taskListener

  private val callback = Callback { msg ->
    when (msg.what) {
      IBlockManager.STATE_STOP -> {
        stoppedNum.getAndIncrement()
        if (isStopped) {
          quitLooper()
        }
      }
      IBlockManager.STATE_CANCEL -> {
        canceledNum.getAndIncrement()
        if (isCanceled) {
          quitLooper()
        }
      }
      IBlockManager.STATE_FAIL -> {
        failedNum.getAndIncrement()
        if (hasFailedBlock()) {
          val b = msg.data
          eventListener.onFail(
            b.getBoolean(IBlockManager.DATA_RETRY, false),
            b.getSerializable(IBlockManager.DATA_ERROR_INFO) as AriaException?
          )
          quitLooper()
        }
      }
      IBlockManager.STATE_COMPLETE -> {
        completedNum.getAndIncrement()
        if (isCompleted) {
          Timber.d("isComplete, completeNum = %s", completedNum)
          eventListener.onComplete()
          quitLooper()
        }
      }
      IBlockManager.STATE_RUNNING -> {
        val b = msg.data
        if (b != null) {
          val len = b.getLong(IBlockManager.DATA_ADD_LEN, 0)
          progress += len
        }
      }
      IBlockManager.STATE_UPDATE_PROGRESS -> {
        progress = msg.obj as Long
      }
    }
    false
  }

  /**
   * 退出looper循环
   */
  private fun quitLooper() {
    looper.quit()
  }

  override fun putUnfinishedBlock(record: BlockRecord) {
    unfinishedBlockList.add(record)
  }

  override fun getChannel(): Channel<BlockRecord> {
    return channel
  }

  override fun setLopper(looper: Looper) {
    this.looper = looper
  }

  override fun setBlockNum(blockNum: Int) {
    this.blockNum = blockNum
  }

  override fun hasFailedBlock(): Boolean {
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

  override fun updateCurrentProgress(currentProgress: Long) {
    progress = currentProgress
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

  override fun getHandlerCallback(): Callback {
    return callback
  }
}