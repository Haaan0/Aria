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
import com.arialyy.aria.core.inf.ITaskManager
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.orm.entity.DEntity
import com.arialyy.aria.orm.entity.TaskRecord
import com.arialyy.aria.util.BlockUtil
import com.arialyy.aria.util.FileUtils
import com.arialyy.aria.util.uri
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
  private lateinit var option: HttpTaskOption
  private lateinit var blockManager: IBlockManager
  private lateinit var taskRecord: TaskRecord

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    blockManager = chain.blockManager as IBlockManager
    option = task.getTaskOption(HttpTaskOption::class.java)
    if (task.taskState.fileSize < 0) {
      Timber.e("file size < 0")
      return TaskResp(TaskResp.CODE_INTERRUPT)
    }

    // if task not support resume, don't save record
    if (task.taskState.fileSize == 0L) {
      blockManager.setBlockNum(1)
      // if block exist, delete the existed block
      removeBlock(0)
      return chain.proceed(chain.getTask())
    }
    val blockNum = checkRecord()
    task.taskState.taskRecord = taskRecord
    blockManager.setBlockNum(blockNum)
    val result = checkBlock()
    if (result != TaskResp.CODE_SUCCESS) {
      return TaskResp(result)
    }

    return chain.proceed(chain.getTask())
  }

  private fun removeBlock(blockId: Int) {
    val saveUri = task.taskState.getEntity(DEntity::class.java).getFilePath().uri()
    if (!FileUtils.uriEffective(saveUri)) {
      Timber.d("invalid uri: ${saveUri}")
      return
    }
    val blockF = File(BlockUtil.getBlockPathFormUri(saveUri, blockId))
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
    val recordWrapper = recordDao.queryTaskRecordByKey(task.filePath)

    if (recordWrapper == null) {
      Timber.i("record not found, create record")
      val blockNumInfo = BlockUtil.getBlockNum(task.taskState.fileSize)
      val taskRecord = TaskRecord(
        taskKey = task.filePath,
        filePath = task.filePath.uri(),
        taskType = ITask.SINGLE_DOWNLOAD,
        fileLen = task.taskState.fileSize,
        blockNum = blockNumInfo.first,
        blockSize = task.taskState.blockSize
      )

      taskRecord.blockList.addAll(
        BlockUtil.createBlockRecord(
          task.taskState.getEntity(DEntity::class.java).getFilePath(),
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
    val handler = blockManager.getHandler()
    val needUpdateBlockRecord = mutableSetOf<BlockRecord>()
    for (br in taskRecord.blockList) {
      val blockF = File(br.blockPath)
      if (blockF.exists()) {
        if (br.curProgress == blockF.length() && !br.isComplete) {
          br.isComplete = true
          needUpdateBlockRecord.add(br)
          handler.obtainMessage(ITaskManager.STATE_COMPLETE)
        }
        if (br.curProgress != blockF.length()) {
          br.curProgress = blockF.length()
          needUpdateBlockRecord.add(br)
          blockManager.putUnfinishedBlock(br)
        }
        // update task progress
        handler.obtainMessage(ITaskManager.STATE_UPDATE_PROGRESS, br.curProgress)
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