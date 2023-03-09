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

import android.net.Uri
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.http.HttpTaskOption
import com.arialyy.aria.orm.entity.DEntity
import com.arialyy.aria.orm.entity.DGEntity
import com.arialyy.aria.util.FileUri
import timber.log.Timber
import java.io.File

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:27 PM 2023/3/8
 **/
internal class HttpDGSubTaskInterceptor : ITaskInterceptor {

  private val dgDao by lazy {
    DuaContext.getServiceManager().getDbService().getDuaDb().getDGEntityDao()
  }

  private lateinit var task: ITask
  private lateinit var option: HttpTaskOption
  private lateinit var dgOption: HttpDGOptionAdapter

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    option = task.getTaskOption(HttpTaskOption::class.java)
    dgOption = option.getOptionAdapter(HttpDGOptionAdapter::class.java)
    if (!checkRecord()) {
      return TaskResp(TaskResp.CODE_INTERRUPT)
    }


  }

  /**
   * check dg task record, if dfEntiry exist, return false
   * otherwise, save new record
   */
  private suspend fun checkRecord(): Boolean {
    val entity = dgDao.getDGEntityByPath(option.savePathUri.toString())
    if (entity != null) {
      Timber.e("task already exist, filePath: ${option.savePathUri}")
      return false
    }
    // create sub task record
    val dgEntity = DGEntity(
      savePath = option.savePathUri!!,
      urls = dgOption.subUrlList.toList(),
      subNameList = dgOption.subNameList
    )

    val subTask = mutableListOf<DEntity>()
    val dir = File(FileUri.getPathByUri(option.savePathUri)!!)
    dgOption.subUrlList.forEachIndexed { index, it ->
      val subFile = File(
        dir.path,
        if (dgOption.subNameList.isNotEmpty()) dgOption.subNameList[index] else "dgTask${index}"
      )
      subTask.add(
        DEntity(
          sourceUrl = it,
          savePath = Uri.parse(subFile.toString()),
        )
      )
    }
    dgEntity.subList.addAll(subTask)

    dgDao.insert(dgEntity)
    return true
  }
}