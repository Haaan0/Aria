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
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.inf.ITaskManager
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.TaskCachePool.removeTask
import com.arialyy.aria.core.task.TaskCachePool.updateState
import com.arialyy.aria.core.task.TaskState
import com.arialyy.aria.exception.AriaException
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.http.download.HttpDOptionAdapter
import com.arialyy.aria.util.BlockUtil
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:04 2023/3/12
 **/
internal class HttpSubBlockManager(private val task: ITask, private val groupHandler: Handler) :
  IBlockManager {
  private lateinit var looper: Looper
  private lateinit var handler: Handler

  private var isStop = false
  private var isCancel = false
  private var progress: Long = 0L

  /**
   * Pass the message to the group task after the subtask is stopped
   */
  private val callback = Handler.Callback { msg ->
    when (msg.what) {
      ITaskManager.STATE_STOP -> {
        isStop = true
        saveData(IEntity.STATE_STOP)
        quitLooper()
      }
      ITaskManager.STATE_CANCEL -> {
        isCancel = true
        removeTask(task)
        BlockUtil.removeTaskBlock(task)
        quitLooper()
      }
      ITaskManager.STATE_FAIL -> {
      }
      ITaskManager.STATE_COMPLETE -> {
        saveData(IEntity.STATE_COMPLETE)
        val b = BlockUtil.mergeFile(task.taskState.taskRecord)

        if (!b) {
          Timber.e("merge block fail")
          onFail(false, AriaException("merge block fail"))
          return
        }
        task.taskState.speed = 0
        saveData(IEntity.STATE_COMPLETE)
      }
      ITaskManager.STATE_RUNNING -> {
      }
      ITaskManager.STATE_UPDATE_PROGRESS -> {
        val b = msg.data
        if (b != null) {
          val len = b.getLong(ITaskManager.DATA_ADD_LEN, 0)
          progress += len
          task.taskState.speed = len
          task.taskState.curProgress = progress
        }
      }
    }
    false
  }

  private fun saveData(state: Int) {
    val ts: TaskState = task.taskState
    ts.state = state
    ts.curProgress = progress
    if (state == IEntity.STATE_COMPLETE) {
      ts.curProgress = ts.fileSize
      removeTask(task)
    }
    updateState(task.taskId, state, progress)
  }

  override fun getHandler() = handler

  /**
   * 1.Shared thread pool for subtasks [HttpDGTaskManager.dispatcher]
   * 2.Subtasks support only single block downloads
   */
  override fun start() {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw IllegalThreadStateException("io operations cannot be in the main thread")
    }
    looper = Looper.myLooper()!!
    handler = Handler(looper, callback)
    // Synchronized sequential execution of all block
    task.getTaskOption(HttpTaskOption::class.java)
      .getOptionAdapter(HttpDOptionAdapter::class.java).threadList.forEach { tt ->
        if (isStop) {
          Timber.d("task stopped")
          return
        }
        if (isCancel) {
          Timber.d("task canceled")
          return
        }
        tt.run()
      }
  }

  private fun quitLooper() {
    looper.quit()
    handler.removeCallbacksAndMessages(null)
  }

  override fun setBlockNum(blockNum: Int) {
    Timber.i("Subtasks do not support chunked downloads")
  }
}