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
import java.net.HttpURLConnection

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:38 PM 2023/1/22
 **/
class HttpDStartController(target: Any, val url: String) : HttpBaseStartController(target),
  IStartController {
  private val taskOptionSupport = HttpDOptionAdapter()

  init {
    httpTaskOption.sourUrl = url
    httpTaskOption.taskOptionAdapter = taskOptionSupport
  }

  override fun setTaskInterceptor(taskInterceptor: ITaskInterceptor): HttpDStartController {
    return super.setTaskInterceptor(taskInterceptor) as HttpDStartController
  }

  override fun setThreadNum(threadNum: Int): HttpDStartController {
    return super.setThreadNum(threadNum) as HttpDStartController
  }

  override fun setHttpOption(httpOption: HttpOption): HttpDStartController {
    return super.setHttpOption(httpOption) as HttpDStartController
  }

  /**
   * Maybe the server has special rules, you need set [IHttpFileLenAdapter] to get the file length from [HttpURLConnection.getHeaderFields]
   */
  fun setHttpFileLenAdapter(adapter: IHttpFileLenAdapter): HttpDStartController {
    taskOptionSupport.fileSizeAdapter = adapter
    return this
  }

  /**
   * set download listener
   */
  fun setListener(listener: HttpDownloadListener): HttpDStartController {
    DuaContext.getLifeManager().addCustomListener(target, listener)
    return this
  }

  /**
   * set file save path, eg: /mnt/sdcard/Downloads/test.zip
   */
  fun setSavePath(savePath: Uri): HttpDStartController {
    httpTaskOption.savePathUri = savePath
    return this
  }

  private fun createTask(): DownloadTask {
    if (HttpUtil.checkHttpDParams(httpTaskOption)) {
      throw IllegalArgumentException("invalid params")
    }
    val task = DownloadTask(httpTaskOption)
    task.adapter = HttpDTaskAdapter()
    TaskCachePool.putTask(task)
    return task
  }

  override fun add(): Int {
    if (!HttpUtil.checkHttpDParams(httpTaskOption)) {
      return -1
    }
    val task = createTask()
    val resp = AddCmd(task).executeCmd()
    return if (resp.isInterrupt()) -1 else task.taskId
  }

  override fun start(): Int {
    if (!HttpUtil.checkHttpDParams(httpTaskOption)) {
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