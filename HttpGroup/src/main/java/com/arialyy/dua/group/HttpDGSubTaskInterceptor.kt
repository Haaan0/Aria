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

import com.arialyy.aria.core.common.TaskOption
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.inf.ITaskManager
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.SingleDownloadTask
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.http.SubState
import com.arialyy.aria.http.download.HttpDTaskAdapter
import com.arialyy.aria.orm.entity.DEntity
import com.arialyy.aria.orm.entity.DGEntity
import timber.log.Timber

/**
 * Subtasks do not support chunking
 * @Author laoyuyu
 * @Description
 * @Date 08:51 2023/3/12
 **/
internal class HttpDGSubTaskInterceptor : ITaskInterceptor {
  private lateinit var task: ITask
  private lateinit var taskOption: HttpTaskOption
  private lateinit var blockManager: IBlockManager

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    blockManager = chain.blockManager
    taskOption = task.getTaskOption(HttpTaskOption::class.java)
    createSubTask(chain)
    blockManager.start()
    return TaskResp(TaskResp.CODE_SUCCESS)
  }

  /**
   * 1. file exist
   * 2. correct file length
   * 3. send complete msg
   */
  private fun checkTaskIsComplete(entity: DEntity): Boolean {
    if (entity.fileIsComplete()) {
      blockManager.handler.obtainMessage(ITaskManager.STATE_COMPLETE, SubState(entity.did))
      return true
    }
    return false
  }

  private fun createSubTask(chain: TaskChain) {
    task.taskState.getEntity(DGEntity::class.java).subList.forEach {
      val tp = TaskOption()
      tp.sourUrl = it.sourceUrl
      tp.savePathDir = taskOption.savePathDir
      tp.threadNum = 1
      tp.eventListener = HttpSubListener()
      val subTask = SingleDownloadTask(tp)
      val subAdapter = HttpDTaskAdapter(true)
      subAdapter.setBlockManager(HttpSubBlockManager(subTask, chain.blockManager.handler))
      subTask.adapter = subAdapter
      if (it.isComplete && checkTaskIsComplete(it)) {
        (chain.getTask() as HttpDGroupTask).addIncompleteTaskList(subTask)
        Timber.d("task already complete, sourUrl: ${it.sourceUrl}")
      }
      (chain.getTask() as HttpDGroupTask).addSubTask(subTask)
    }
  }

}