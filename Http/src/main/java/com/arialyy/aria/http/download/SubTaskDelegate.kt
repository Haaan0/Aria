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
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.core.task.ThreadTaskManager2
import com.arialyy.aria.exception.AriaException
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.http.SubState
import com.arialyy.aria.orm.entity.DEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Author laoyuyu
 * @Description
 * @Date 11:35 2023/3/12
 **/
internal class SubTaskDelegate(val adapter: HttpDTaskAdapter) : ITaskAdapterDelegate {
  private lateinit var blockManager: IBlockManager

  override fun isRunning(): Boolean {
    return ThreadTaskManager2.taskIsRunning(adapter.getTask().taskId)
  }

  override fun cancel() {
    sendMsg(ITaskManager.STATE_CANCEL)
  }

  override fun stop() {
    sendMsg(ITaskManager.STATE_STOP)
  }

  override fun start() {
    DuaContext.duaScope.launch(Dispatchers.IO) {
      adapter.addCoreInterceptor(HttpDCheckInterceptor())
      adapter.addCoreInterceptor(TimerInterceptor())
      adapter.addCoreInterceptor(HttpDHeaderInterceptor())
      adapter.addCoreInterceptor(HttpDBlockInterceptor())
      adapter.addCoreInterceptor(HttpBlockThreadInterceptor())
      val resp = adapter.interceptor()
      if (resp == null || resp.code != TaskResp.CODE_SUCCESS) {
        adapter.getTask().getTaskOption(HttpTaskOption::class.java).eventListener.onFail(
          false,
          AriaException("start task fail, task interrupt, code: ${resp?.code ?: TaskResp.CODE_INTERRUPT}")
        )
        sendMsg(ITaskManager.STATE_STOP)
        return@launch
      }
    }
  }

  /**
   * @param state [ITaskManager]
   */
  private fun sendMsg(state: Int) {
    blockManager.handler.obtainMessage(
      state,
      SubState(adapter.getTask().taskState.getEntity(DEntity::class.java).did)
    ).sendToTarget()
  }

  override fun setBlockManager(blockManager: IBlockManager) {
    this.blockManager = blockManager
  }

  override fun getBlockManager(): IBlockManager {
    return blockManager
  }
}