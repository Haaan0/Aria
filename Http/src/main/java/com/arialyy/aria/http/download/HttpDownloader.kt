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
import com.arialyy.annotations.TaskEnum
import com.arialyy.annotations.TaskEnum.DOWNLOAD
import com.arialyy.aria.core.inf.IDownloader

/**
 * @Author laoyuyu
 * @Description
 * @Date 14:11 AM 2023/1/20
 **/
class HttpDownloader(val target: Any) : IDownloader {

  override fun getTaskEnum(): TaskEnum {
    return DOWNLOAD
  }

  /**
   * start, create a task
   * @param url download url
   */
  fun load(url: String): HttpDStartController {
    return HttpDStartController(target, url)
  }

  /**
   * stop, cancel a task
   * @param taskId taskId
   */
  fun load(taskId: Int): HttpDStopController {
    return HttpDStopController(taskId)
  }

  /**
   * stop, cancel a task
   * @param filePath filePath
   */
  fun load(filePath: Uri): HttpDStopController2 {
    return HttpDStopController2(filePath)
  }

}