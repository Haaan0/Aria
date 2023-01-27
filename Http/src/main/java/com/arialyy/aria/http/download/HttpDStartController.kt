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
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.http.HttpBaseController
import com.arialyy.aria.http.HttpOption
import com.arialyy.aria.http.HttpUtil
import com.arialyy.aria.orm.EntityCachePool
import com.arialyy.aria.orm.entity.DEntity
import kotlinx.coroutines.launch

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:38 PM 2023/1/22
 **/
class HttpDStartController(target: Any, val url: String) : HttpBaseController(target),
  IStartController {

  private var httpDTaskOption = HttpDTaskOption()

  init {
    httpDTaskOption.sourUrl = url
  }

  /**
   * set http params, link Header
   */
  fun setHttpOption(httpOption: HttpOption): HttpBaseController {
    httpDTaskOption.httpOption = httpOption
    return this
  }

  fun setListener(listener: HttpDownloadListener): HttpBaseController {
    DuaContext.getLifeManager().addCustomListener(target, listener)
    return this
  }

  fun setSavePath(savePath: Uri): HttpDStartController {
    httpDTaskOption.savePathUri = savePath
    return this
  }

  private fun createTask(): DownloadTask {
    if (HttpUtil.checkHttpDParams(httpDTaskOption)) {
      throw IllegalArgumentException("invalid params")
    }
    val task = DownloadTask(httpDTaskOption)
    DuaContext.duaScope.launch {
      val dEntity = findDEntityBySavePath(httpDTaskOption)
      EntityCachePool.putEntity(task.taskId, dEntity)
    }
    return task
  }

  /**
   * find DEntity, if that not exist, create and save it
   */
  private suspend fun findDEntityBySavePath(option: HttpDTaskOption): DEntity {
    val savePath = option.savePathUri?.toString()
    if (savePath.isNullOrEmpty()) {
      throw IllegalArgumentException("savePath is null")
    }
    val dao = DuaContext.getServiceManager().getDbService().getDuaDb()?.getDEntityDao()
    val de = dao?.getDEntityBySavePath(savePath)
    if (de != null) {
      return de
    }
    val newDe = DEntity(
      sourceUrl = option.sourUrl!!,
      savePath = savePath,
    )
    dao?.insert(newDe)
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