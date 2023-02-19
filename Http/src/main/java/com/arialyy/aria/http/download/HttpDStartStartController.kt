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

import android.net.Uri
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.command.AddCmd
import com.arialyy.aria.core.command.StartCmd
import com.arialyy.aria.core.inf.IStartController
import com.arialyy.aria.core.processor.IHttpFileLenAdapter
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskCachePool
import com.arialyy.aria.http.HttpBaseStartController
import com.arialyy.aria.http.HttpOption
import com.arialyy.aria.http.HttpUtil
import com.arialyy.aria.orm.entity.DEntity
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.HttpURLConnection

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:38 PM 2023/1/22
 **/
class HttpDStartStartController(target: Any, val url: String) : HttpBaseStartController(target),
  IStartController {

  private var httpDTaskOption = HttpDTaskOption()

  init {
    httpDTaskOption.sourUrl = url
  }

  /**
   * use multi-threaded download file, if file size <= 5m, this setting is not valid
   * @param threadNum  range [1 - 32]
   */
  fun setThreadNum(threadNum: Int): HttpDStartStartController {
    if (threadNum !in 1..32) {
      Timber.e("set thread num fail, only 0 < threadNum < 33, threadNum: $threadNum")
      return this
    }
    httpDTaskOption.threadNum = threadNum
    return this
  }

  /**
   * set http params, link Header
   */
  fun setHttpOption(httpOption: HttpOption): HttpDStartStartController {
    httpDTaskOption.httpOption = httpOption
    return this
  }

  /**
   * Maybe the server has special rules, you need set [IHttpFileLenAdapter] to get the file length from [HttpURLConnection.getHeaderFields]
   */
  fun setHttpFileLenAdapter(adapter: IHttpFileLenAdapter): HttpDStartStartController {
    httpDTaskOption.fileSizeAdapter = adapter
    return this
  }

  /**
   * if you want to do something before the task is executed, you can set up a task interceptor
   * eg: determine the network status before task execution
   */
  fun setTaskInterceptor(taskInterceptor: ITaskInterceptor): HttpDStartStartController {
    httpDTaskOption.taskInterceptor.add(taskInterceptor)
    return this
  }

  /**
   * set download listener
   */
  fun setListener(listener: HttpDownloadListener): HttpDStartStartController {
    DuaContext.getLifeManager().addCustomListener(target, listener)
    return this
  }

  /**
   * set file save path, eg: /mnt/sdcard/Downloads/test.zip
   */
  fun setSavePath(savePath: Uri): HttpDStartStartController {
    httpDTaskOption.savePathUri = savePath
    return this
  }

  private fun createTask(): DownloadTask {
    if (HttpUtil.checkHttpDParams(httpDTaskOption)) {
      throw IllegalArgumentException("invalid params")
    }
    val savePath = httpDTaskOption.savePathUri!!
    var util = TaskCachePool.getTaskUtil(savePath)
    if (util == null) {
      util = HttpDTaskUtil()
      TaskCachePool.putTaskUtil(savePath, util)
    }
    val task = DownloadTask(httpDTaskOption, util)
    DuaContext.duaScope.launch {
      val dEntity = findDEntityBySavePath(httpDTaskOption)
      TaskCachePool.putEntity(task.taskId, dEntity)
    }
    TaskCachePool.putTask(task)
    return task
  }

  /**
   * find DEntity, if that not exist, create and save it
   */
  private suspend fun findDEntityBySavePath(option: HttpDTaskOption): DEntity {
    val savePath = option.savePathUri
    val dao = DuaContext.getServiceManager().getDbService().getDuaDb().getDEntityDao()
    val de = dao.getDEntityBySavePath(savePath.toString())
    if (de != null) {
      return de
    }
    val newDe = DEntity(
      sourceUrl = option.sourUrl!!,
      savePath = savePath!!,
    )
    dao.insert(newDe)
    return newDe
  }

  override fun add(): Int {
    if (!HttpUtil.checkHttpDParams(httpDTaskOption)) {
      return -1
    }
    val task = createTask()
    val resp = AddCmd(task).executeCmd()
    return if (resp.isInterrupt()) -1 else task.taskId
  }

  override fun start(): Int {
    if (!HttpUtil.checkHttpDParams(httpDTaskOption)) {
      return -1
    }
    val task = createTask()
    val resp = StartCmd(task).executeCmd()
    return if (resp.isInterrupt()) -1 else task.taskId
  }

  override fun resume(): Int {
    return start()
  }
}