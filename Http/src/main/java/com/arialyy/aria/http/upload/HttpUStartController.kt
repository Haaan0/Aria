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
package com.arialyy.aria.http.upload

import android.net.Uri
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.http.HttpBaseStartController
import com.arialyy.aria.http.HttpOption

/**
 * @Author laoyuyu
 * @Description
 * @Date 8:39 PM 2023/2/20
 **/
class HttpUStartController(target: Any, val filePath: Uri) : HttpBaseStartController(target) {

  init {
    httpTaskOption.savePathUri = filePath
  }

  override fun setTaskInterceptor(taskInterceptor: ITaskInterceptor): HttpUStartController {
    return super.setTaskInterceptor(taskInterceptor) as HttpUStartController
  }

  override fun setThreadNum(threadNum: Int): HttpUStartController {
    return super.setThreadNum(threadNum) as HttpUStartController
  }

  override fun setHttpOption(httpOption: HttpOption): HttpUStartController {
    return super.setHttpOption(httpOption) as HttpUStartController
  }

  /**
   * set uploader service url
   */
  fun setUploadUrl(url: String): HttpUStartController {
    httpTaskOption.sourUrl = url
    return this
  }
}