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

import android.os.Bundle
import android.os.Handler
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IBlockManager
import com.arialyy.aria.orm.entity.BlockRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * @Author laoyuyu
 * @Description
 * @Date 19:40 PM 2023/2/7
 **/
class ThreadTask2(
  private val adapter: IThreadTaskAdapter,
  private val handler: Handler,
  private val record: BlockRecord
) : IThreadTask, IThreadTaskObserver {
  private var isCanceled = false
  private var isStopped = false
  private var failCount = 3
  private var lastUpdateTime = System.currentTimeMillis()

  companion object {
    private const val MAX_RE_TRY_NUM = 3
    private const val RE_TRY_TIME = 1000 * 3L
  }

  override fun run() {
    adapter.run()
  }

  override fun cancel() {
    adapter.breakTask()
    isCanceled = true
    handler.obtainMessage(IBlockManager.STATE_CANCEL)
  }

  override fun stop() {
    adapter.breakTask()
    isStopped = true
    handler.obtainMessage(IBlockManager.STATE_STOP)
  }

  override fun setMaxSpeed(speed: Int) {
    adapter.setMaxSpeed(speed)
  }

  override fun isRunning(): Boolean {
    return !isCanceled && !isStopped
  }

  /**
   * thread task fail, we need count fail num
   * if [failCount] less than [MAX_RE_TRY_NUM], will retry the thread task
   */
  override fun onFail(e: Exception?) {
    Timber.e("execute thread fail, failNum: $failCount blockId: ${record.bId}, blockPath: ${record.blockPath}, sourceUrl: ${record.sourUrl}")
    if (failCount < MAX_RE_TRY_NUM) {
      Timber.e("retry thread, failCount: $failCount")
      DuaContext.duaScope.launch(Dispatchers.IO) {
        delay(RE_TRY_TIME)
      }
      run()
      return
    }
    val b = Bundle()
    b.putBoolean(IBlockManager.DATA_RETRY, false)
    b.putSerializable(IBlockManager.DATA_ERROR_INFO, e)
    handler.obtainMessage(IBlockManager.STATE_FAIL, b)
  }

  override fun onComplete() {
    val blockF = File(record.blockPath)
    if (blockF.length() != record.curProgress) {
      Timber.e("task fail, blockSize: ${blockF.length()} Not equal to curProgress: ${record.curProgress}")
      onFail(null)
      return
    }
    record.isComplete = true
    // update progress once a second, we need to check the progress difference.
    val diff = kotlin.math.abs(record.curProgress - blockF.length())
    if (diff != 0L) {
      handler.obtainMessage(IBlockManager.STATE_RUNNING, diff)
    }
    updateRecord()
    handler.obtainMessage(IBlockManager.STATE_COMPLETE)
  }

  private fun updateRecord() {
    val dao = DuaContext.getServiceManager().getDbService().getDuaDb().getRecordDao()
    DuaContext.duaScope.launch(Dispatchers.IO) {
      dao.updateBlockRecord(record)
    }
  }

  /**
   * update current thread progress, once a second
   */
  override fun onProgress(len: Long) {
    record.curProgress += len
    if (System.currentTimeMillis() - lastUpdateTime > 1000) {
      lastUpdateTime = System.currentTimeMillis()
      handler.obtainMessage(IBlockManager.STATE_RUNNING, len)
    }
  }

  override fun getThreadProgress(): Long {
    return record.curProgress
  }
}