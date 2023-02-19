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
import com.arialyy.aria.http.HttpUtil
import com.arialyy.aria.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 20:25 2023/2/19
 **/
class HttpDStopController2(val filePath: Uri) {

  private suspend fun checkUri(): Boolean {
    if (!FileUtils.uriEffective(filePath)) {
      Timber.e("invalid uri: $filePath")
      return false
    }
    return withContext(Dispatchers.IO) {
      val entity = DuaContext.getServiceManager().getDbService().getDuaDb().getDEntityDao()
        .getDEntityBySavePath(filePath.toString())
      if (entity == null) {
        Timber.e("No found task, uri: $filePath")
        return@withContext false
      }
      return@withContext true
    }
  }

  fun cancel() {
    DuaContext.duaScope.launch {
      if (checkUri()) {
        HttpUtil.getDTaskUtil(filePath).cancel()
      }
    }
  }

  fun stop() {
    DuaContext.duaScope.launch {
      if (checkUri()) {
        HttpUtil.getDTaskUtil(filePath).stop()
      }
    }
  }
}