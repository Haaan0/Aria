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
import com.arialyy.aria.core.task.BlockUtil
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.orm.entity.TaskRecord
import timber.log.Timber

/**
 * block interceptor
 * 1. create block record
 * 2. check downloaded block
 */
internal class HttpDBlockInterceptor : ITaskInterceptor {
  private lateinit var task: ITask
  private lateinit var option: HttpDTaskOption

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    option = task.getTaskOption(HttpDTaskOption::class.java)
    if (task.taskState.fileSize < 0) {
      Timber.e("file size < 0")
      return TaskResp(TaskResp.CODE_GET_FILE_INFO_FAIL)
    }

    // if task not support resume, don't save record
    if (task.taskState.fileSize == 0L) {
      chain.blockManager.setBlockNum(1)
      checkBlock()
      return chain.proceed(chain.getTask())
    }
    val blockNum = checkRecord()
    chain.blockManager.setBlockNum(blockNum)
    checkBlock()
    return chain.proceed(chain.getTask())
  }

  /**
   * check task record, if record no exist, create taskRecord
   * @return blockNum
   */
  private suspend fun checkRecord(): Int {
    val recordDao = DuaContext.getServiceManager().getDbService().getDuaDb().getRecordDao()
    val recordWrapper = recordDao.getTaskRecordByKey(task.taskKey)

    if (recordWrapper == null) {
      Timber.i("record not found, create record")
      val blockNumInfo = BlockUtil.getBlockNum(task.taskState.fileSize)
      val taskRecord = TaskRecord(
        taskKey = task.taskKey,
        filePath = option.savePathUri!!,
        taskType = ITask.DOWNLOAD,
        fileLen = task.taskState.fileSize,
        blockNum = blockNumInfo.first,
        blockSize = task.taskState.blockSize
      )
      taskRecord.blockList.addAll(BlockUtil.createBlockRecord(task.taskState.fileSize))
      recordDao.insert(taskRecord)
      return taskRecord.blockNum
    }
    Timber.d("record existed")
    return recordWrapper.taskRecord.blockNum
  }

  /**
   * if block already exist, upload progress
   */
  private fun checkBlock() {

  }
}