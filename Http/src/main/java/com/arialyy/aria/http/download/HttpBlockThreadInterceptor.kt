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
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.IThreadTask
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.core.task.ThreadConfig
import com.arialyy.aria.core.task.ThreadTask2
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.orm.entity.BlockRecord

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:11 PM 2023/2/7
 **/
class HttpBlockThreadInterceptor : ITaskInterceptor {
  private lateinit var blockManager: IBlockManager
  private lateinit var taskOption: HttpDOptionDelegate

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    blockManager = chain.blockManager
    taskOption = chain.getTask().getTaskOption(HttpTaskOption::class.java)
      .getOptionDelegate(HttpDOptionDelegate::class.java)
    val unfinishedBlockList = taskOption.getUnfinishedBlockList()
    if (unfinishedBlockList.isEmpty()) {
      return TaskResp(TaskResp.CODE_INTERRUPT)
    }
    createThreadTask(unfinishedBlockList, chain)
    return TaskResp(TaskResp.CODE_SUCCESS)
  }

  private fun createThreadTask(blockRecordList: List<BlockRecord>, chain: TaskChain) {
    val threadTaskList = mutableListOf<IThreadTask>()

    val option = chain.getTask().getTaskOption(HttpTaskOption::class.java)
    blockRecordList.forEach {
      val threadConfig = ThreadConfig(it, option, DuaContext.getDConfig().maxSpeed)
      threadTaskList.add(
        ThreadTask2(
          adapter = if (option.getOptionDelegate(HttpDOptionDelegate::class.java).isChunkTask) HttpDCTTaskAdapter(
            threadConfig
          ) else HttpDBTaskAdapter(
            threadConfig
          ),
          handler = blockManager.handler,
          record = it
        )
      )
    }
    option.getOptionDelegate(HttpDOptionDelegate::class.java).threadList = threadTaskList
    blockManager.start()
  }
}