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
import com.arialyy.annotations.TaskEnum
import com.arialyy.aria.core.inf.IDownloader

/**
 * @Author laoyuyu
 * @Description
 * @Date 7:53 AM 2023/3/6
 **/
class HttpDGLoader(val target: Any) : IDownloader {
  override fun getTaskEnum(): TaskEnum {
    return TaskEnum.DOWNLOAD_GROUP
  }

  /**
   * start, create a task
   * @param savePath download url
   */
  fun load(savePath: Uri): HttpDGStartController {
    return HttpDGStartController(target, savePath)
  }
}