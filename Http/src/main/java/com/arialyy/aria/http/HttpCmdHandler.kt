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
package com.arialyy.aria.http

import androidx.annotation.Keep
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.command.ICmdHandler
import com.arialyy.aria.core.command.StartCmd
import com.arialyy.aria.core.event.DeleteAllEvent
import com.arialyy.aria.core.event.Event
import com.arialyy.aria.core.event.EventMsgUtil
import com.arialyy.aria.core.event.ResumeAllEvent
import com.arialyy.aria.core.event.StopAllEvent
import com.arialyy.aria.http.download.HttpDTaskAdapter
import com.arialyy.aria.util.FileUtils
import com.arialyy.aria.util.isNotComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:40 PM 2023/4/2
 **/
@Keep
internal object HttpCmdHandler : ICmdHandler {
  override fun initHandler() {
    EventMsgUtil.getDefault().register(this)
  }

  @Event
  fun resumeAll(event: ResumeAllEvent) {
    DuaContext.duaScope.launch(Dispatchers.IO) {
      resumeDTask()
    }
  }

  @Event
  fun stopAll(event: StopAllEvent) {
    DuaContext.duaScope.launch(Dispatchers.IO) {
      DuaContext.getServiceManager().getDownloadQueue().stopAllTask()
    }
  }

  @Event
  fun removeAll(event: DeleteAllEvent) {
    DuaContext.duaScope.launch(Dispatchers.IO) {
      removeAllDTask(event)
    }
  }

  private suspend fun removeAllDTask(event: DeleteAllEvent) {
    val dao = DuaContext.getServiceManager().getDbService().getDuaDb().getDEntityDao()
    val entityList = dao.queryDEntityList()
    DuaContext.getServiceManager().getDownloadQueue().deleteAllTask()
    dao.deleteAll()
    if (event.onlyRemoveRecord) {
      Timber.d("Only remove record")
      return
    }
    // Delete the downloaded file
    entityList.forEach {
      if (it.fileIsComplete()) {
        val path = it.getFilePath()
        Timber.d("Delete file: $path")
        FileUtils.deleteFile(path)
      }
    }
  }

  /**
   * Recovery status is an unfinished task
   */
  private suspend fun resumeDTask() {
    val entityList = DuaContext.getServiceManager().getDbService().getDuaDb().getDEntityDao()
      .queryAllNotCompleteEntityList()
    if (entityList.isNullOrEmpty()) {
      Timber.w("No tasks to recover")
      return
    }
    entityList.forEach {
      if (!it.isNotComplete()) {
        Timber.d("Ignore the task, task status: ${it.state}")
        return@forEach
      }
      val taskOption = HttpTaskOption()
      taskOption.sourUrl = it.sourceUrl
      val tempTask = HttpUtil.getSingDTask(taskOption)
      if (tempTask == null) {
        Timber.e("Resume task fail, url: ${it.sourceUrl}")
        return@forEach
      }
      val taskAdapter = HttpDTaskAdapter()
      tempTask.adapter = taskAdapter
      StartCmd(tempTask).executeCmd()
    }
  }

}