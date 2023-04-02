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
import com.arialyy.aria.core.command.DeleteCmd
import com.arialyy.aria.core.command.StartCmd
import com.arialyy.aria.core.command.StopCmd
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.http.HttpBaseStartController
import com.arialyy.aria.http.HttpOption
import com.arialyy.aria.http.HttpUtil
import com.arialyy.aria.http.IHttpFileLenAdapter
import timber.log.Timber
import java.net.HttpURLConnection

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:38 PM 2023/1/22
 **/
class HttpDStartController(target: Any, val url: String) : HttpBaseStartController(target) {
  private val taskOptionAdapter = HttpDOptionDelegate()

  init {
    httpTaskOption.sourUrl = url
    httpTaskOption.taskOptionDelegate = taskOptionAdapter
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
    taskOptionAdapter.fileSizeAdapter = adapter
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
   * set file name
   */
  fun setFileName(fileName: String): HttpDStartController {
    taskOptionAdapter.fileName = fileName
    return this
  }

  /**
   * set file save path, if you don't set the [setFileName],
   * it will automatically try to get the file name, and if it fails,
   * the md5 code of the url will be used
   * eg: /mnt/sdcard/Downloads/
   *
   */
  fun setSavePath(savePath: Uri): HttpDStartController {
    httpTaskOption.savePathDir = savePath
    return this
  }

  fun add(): Int {
    if (!HttpUtil.checkHttpDParams(httpTaskOption)) {
      return -1
    }
    val task = HttpUtil.getSingDTask(httpTaskOption)
    val resp = AddCmd(task).executeCmd()
    return if (resp.isInterrupt()) -1 else task?.taskId ?: -1
  }

  fun start(): Int {
    if (!HttpUtil.checkHttpDParams(httpTaskOption)) {
      return -1
    }
    val task = HttpUtil.getSingDTask(httpTaskOption)
    val resp = StartCmd(task).executeCmd()
    return if (resp.isInterrupt()) -1 else task?.taskId ?: -1
  }

  fun resume(): Int {
    return start()
  }

  fun delete() {
    val task = HttpUtil.getSingDTask(httpTaskOption, true)
    if (task == null) {
      Timber.e("not found task, url: $url")
      return
    }
    DeleteCmd(task).executeCmd()
  }

  fun stop() {
    val task = HttpUtil.getSingDTask(httpTaskOption, false)
    if (task == null) {
      Timber.e("task not running, url: $url")
      return
    }
    StopCmd(task).executeCmd()
  }
}