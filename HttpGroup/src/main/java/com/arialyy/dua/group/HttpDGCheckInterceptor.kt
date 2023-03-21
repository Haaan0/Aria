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
import com.arialyy.aria.util.DuaUtil
import com.arialyy.aria.util.FileUri
import com.arialyy.aria.util.FileUtils
import timber.log.Timber
import java.io.File

/**
 * 1. Check if the save path is valid
 * 2. Check all sub-task download addresses
 * @Author laoyuyu
 * @Description
 * @Date 8:56 PM 2023/3/6
 **/
internal class HttpDGCheckInterceptor : ITaskInterceptor {

  private lateinit var task: ITask
  private lateinit var optionAdapter: HttpDGOptionDelegate
  private lateinit var taskOption: HttpTaskOption
  private val dgDao by lazy {
    DuaContext.getServiceManager().getDbService().getDuaDb().getDGEntityDao()
  }

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    task = chain.getTask()
    taskOption = task.getTaskOption(HttpTaskOption::class.java)
    optionAdapter = taskOption.getOptionDelegate(HttpDGOptionDelegate::class.java)

    if (checkParams(taskOption.savePathDir)) {
      return TaskResp(TaskResp.CODE_INTERRUPT)
    }

    if (checkRecord()) {
      return TaskResp(TaskResp.CODE_INTERRUPT)
    }
    return chain.proceed(task)
  }

  /**
   * @return false interrupt task
   */
  private suspend fun checkRecord(): Boolean {
    val savePath = taskOption.savePathDir
    val dgE = dgDao.queryDGEntityByPath(savePath.toString())
    if (dgE == null) {
      createNewEntity()
      return true
    }

    val dir = File(FileUri.getPathByUri(savePath)!!)
    if (dir.exists()) {
      val subUrl = mutableListOf<String>()
      dgE.subList.forEach { subUrl.add(it.sourceUrl) }
      val diffUrl = subUrl.subtract(dgE.subList.toSet())
      if (diffUrl.isNotEmpty()) {
        Timber.e("invalid savePath, the path existed: $savePath, \ndiffUrl: $diffUrl")
        return false
      }
    }

    return true
  }

  /**
   * save new record
   */
  private suspend fun createNewEntity(): Boolean {
    Timber.d("create new task")
    // create sub task record
    val dgEntity = DGEntity(
      savePath = taskOption.savePathDir!!,
      urls = optionAdapter.subUrlList.toList(),
      subNameList = optionAdapter.subNameList
    )

    val subTask = mutableListOf<DEntity>()
    val dir = File(FileUri.getPathByUri(taskOption.savePathDir)!!)
    optionAdapter.subUrlList.forEachIndexed { index, it ->
      val subName = if (optionAdapter.subNameList.isNotEmpty()) {
        optionAdapter.subNameList[index]
      } else {
        FileUtils.getFileNameFromUrl(it) ?: "subTask_${index}"
      }
      val subFile = File(
        dir.path,
        subName
      )
      subTask.add(
        DEntity(
          sourceUrl = it,
          savePath = Uri.parse(subFile.toString()),
          fileName = subName
        )
      )
    }
    dgEntity.subList.addAll(subTask)
    task.taskState.setEntity(dgEntity)

    dgDao.insert(dgEntity)
    return true
  }

  /**
   * check option
   * @return false interrupt task
   */
  private fun checkParams(savePath: Uri?): Boolean {
    if (savePath == null) {
      Timber.d("save path is null")
      return false
    }
    if (optionAdapter.subUrlList.isEmpty()) {
      Timber.e("sub list is null")
      return false
    }

    optionAdapter.subUrlList.forEach {
      if (!DuaUtil.checkUrl(it)) {
        Timber.e("invalid url: $it")
        return false
      }
    }

    if (optionAdapter.subNameList.isNotEmpty() && optionAdapter.subNameList.size != optionAdapter.subUrlList.size) {
      Timber.e("subNameList.size must be consistent subUrlList.size")
      return false
    }

    return true
  }
}