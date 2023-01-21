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

import com.arialyy.annotations.TaskEnum
import com.arialyy.annotations.TaskEnum.DOWNLOAD
import com.arialyy.aria.core.inf.IDownloader
import com.arialyy.aria.http.HttpOption

/**
 * @Author laoyuyu
 * @Description
 * @Date 14:11 AM 2023/1/20
 **/
internal class HttpDownloader : IDownloader {
  private lateinit var uri: String
  private lateinit var savePath: String
  private var httpOption = HttpOption()

  override fun getTaskEnum(): TaskEnum {
    return DOWNLOAD
  }

  fun setSourceUri(uri: String): HttpDownloader {
    this.uri = uri
    return this
  }

  fun setSavePath(savePath: String): HttpDownloader {
    this.savePath = savePath
    return this
  }

  fun setHttpOption(httpOption: HttpOption): HttpDownloader {
    this.httpOption = httpOption
    return this
  }


}