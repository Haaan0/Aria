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
import com.arialyy.aria.core.inf.IThreadStateManager
import com.arialyy.aria.core.listener.IEventListener
import com.arialyy.aria.exception.AriaException
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class BlockManager(
  val mListener: IEventListener,
  val looper: Looper,
  private val blockNum: Int
) : IThreadStateManager {
  private val blockList = mutableListOf<BlockState>()
  private val canceledNum = AtomicInteger(0) // 已经取消的线程的数
  private val stoppedNum = AtomicInteger(0) // 已经停止的线程数

  private val failedNum = AtomicInteger(0) // 失败的线程数

  private val completedNum = AtomicInteger(0) // 完成的线程数

  private var progress: Long = 0 //当前总进度

  private val callback = Callback { msg ->
    when (msg.what) {
      IThreadStateManager.STATE_STOP -> {
        stoppedNum.getAndIncrement()
        if (isStopped) {
          quitLooper()
        }
      }
      IThreadStateManager.STATE_CANCEL -> {
        canceledNum.getAndIncrement()
        if (isCanceled) {
          quitLooper()
        }
      }
      IThreadStateManager.STATE_FAIL -> {
        failedNum.getAndIncrement()
        if (hasFailedBlock()) {
          val b = msg.data
          mListener.onFail(
            b.getBoolean(IThreadStateManager.DATA_RETRY, false),
            b.getSerializable(IThreadStateManager.DATA_ERROR_INFO) as AriaException?
          )
          quitLooper()
        }
      }
      IThreadStateManager.STATE_COMPLETE -> {
        completedNum.getAndIncrement()
        if (isCompleted) {
          Timber.d("isComplete, completeNum = %s", completedNum)
          mListener.onComplete()
          quitLooper()
        }
      }
      IThreadStateManager.STATE_RUNNING -> {
        val b = msg.data
        if (b != null) {
          val len = b.getLong(IThreadStateManager.DATA_ADD_LEN, 0)
          progress += len
        }
      }
      IThreadStateManager.STATE_UPDATE_PROGRESS -> {
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

  fun addBlockState(state: BlockState) {
    blockList.add(state)
  }

  fun getBlockState(): BlockState {
    return blockList.removeFirst()
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

  override fun getHandlerCallback(): Callback {
    return callback
  }

}