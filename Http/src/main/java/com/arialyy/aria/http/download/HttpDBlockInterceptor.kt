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
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.core.task.BlockUtil
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.orm.entity.TaskRecord
import com.arialyy.aria.util.FileUri
import com.arialyy.aria.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * block interceptor
 * 1. create block record
 * 2. check downloaded block
 */
internal class HttpDBlockInterceptor : ITaskInterceptor {
  private lateinit var task: ITask
  private lateinit var option: HttpDTaskOption
  private lateinit var blockManager: IBlockManager
  private lateinit var taskRecord: TaskRecord

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    blockManager = chain.blockManager
    option = task.getTaskOption(HttpDTaskOption::class.java)
    if (task.taskState.fileSize < 0) {
      Timber.e("file size < 0")
      return TaskResp(TaskResp.CODE_GET_FILE_INFO_FAIL)
    }

    val savePath = FileUri.getPathByUri(task.getTaskOption(HttpDTaskOption::class.java).savePathUri)
    if (savePath.isNullOrEmpty()) {
      Timber.e("saveUri is null")
      return TaskResp(TaskResp.CODE_SAVE_URI_NULL)
    }

    // if task not support resume, don't save record
    if (task.taskState.fileSize == 0L) {
      chain.blockManager.setBlockNum(1)
      // if block exist, delete the existed block
      removeBlock()
      return chain.proceed(chain.getTask())
    }
    val blockNum = checkRecord()
    chain.blockManager.setBlockNum(blockNum)
    val result = checkBlock()
    if (result != TaskResp.CODE_SUCCESS) {
      return TaskResp(result)
    }

    return chain.proceed(chain.getTask())
  }

  private fun removeBlock() {
    val saveUri = task.getTaskOption(HttpDTaskOption::class.java).savePathUri
    if (!FileUtils.uriEffective(saveUri)) {
      return
    }
    val blockF = File(BlockUtil.getBlockPathFormUri(saveUri!!, 0))
    if (blockF.exists()) {
      blockF.delete()
    }
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

      taskRecord.blockList.addAll(
        BlockUtil.createBlockRecord(
          task.getTaskOption(HttpDTaskOption::class.java).savePathUri!!,
          task.taskState.fileSize
        )
      )
      recordDao.insert(taskRecord)
      this.taskRecord = taskRecord
      return taskRecord.blockNum
    }
    Timber.d("record existed")
    taskRecord = recordWrapper.taskRecord
    return recordWrapper.taskRecord.blockNum
  }

  /**
   * if block already exist, upload progress
   */
  private suspend fun checkBlock(): Int {
    val handler = blockManager.handler
    val needUpdateBlockRecord = mutableSetOf<BlockRecord>()
    for (br in taskRecord.blockList) {
      val blockF = File(br.blockPath)
      if (blockF.exists()) {
        if (br.curProgress == blockF.length() && !br.isComplete) {
          br.isComplete = true
          needUpdateBlockRecord.add(br)
          handler.obtainMessage(IBlockManager.STATE_COMPLETE)
        }
        if (br.curProgress != blockF.length()) {
          br.curProgress = blockF.length()
          needUpdateBlockRecord.add(br)
          blockManager.putUnfinishedBlock(br)
        }
        // update task progress
        handler.obtainMessage(IBlockManager.STATE_UPDATE_PROGRESS, br.curProgress)
      }
    }

    // update block record
    val dao = DuaContext.getServiceManager().getDbService().getDuaDb().getRecordDao()
    withContext(Dispatchers.IO) {
      dao.updateBlockList(needUpdateBlockRecord.toMutableList())
    }
    return TaskResp.CODE_SUCCESS
  }
}