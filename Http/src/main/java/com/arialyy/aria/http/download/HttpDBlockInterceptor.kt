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

import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.task.BlockManager
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import timber.log.Timber

/**
 * block interceptor
 * 1. create block record
 * 2. check downloaded block
 */
internal class HttpDBlockInterceptor : ITaskInterceptor {
  private lateinit var task: ITask

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    if (task.taskState.fileSize < 0) {
      Timber.e("file size < 0")
      return TaskResp(TaskResp.CODE_GET_FILE_INFO_FAIL)
    }

  }

  /**
   * check task record, if record no exist, create taskRecord
   */
  private suspend fun checkRecord() {
    val recordWrapper = DuaContext.getServiceManager().getDbService().getDuaDb()?.getRecordDao()
      ?.getTaskRecordByKey(task.taskKey)
    if (recordWrapper == null){

    }
  }

  private fun createBlockManager(): BlockManager {

  }

  /**
   * if block already exist, upload progress
   */
  private fun checkBlock() {

  }
}