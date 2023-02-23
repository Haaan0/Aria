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

import com.arialyy.annotations.TaskEnum
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.core.inf.IBaseLoader
import com.arialyy.aria.core.inf.IComponentLoader
import com.arialyy.aria.core.inf.IDownloader
import com.arialyy.aria.http.download.HttpDownloader
import com.arialyy.aria.http.upload.HttpULoader
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED

/**
 * @Author laoyuyu
 * @Description
 * @Date 14:07 AM 2023/1/20
 **/
class HttpComponentLoader : IComponentLoader {

  private val downloader by lazy(SYNCHRONIZED) {
    HttpDownloader(DuaContext.getLifeManager().getTargetByLoader(this)!!)
  }

  private val uploader by lazy(SYNCHRONIZED) {
    HttpULoader(DuaContext.getLifeManager().getTargetByLoader(this)!!)
  }

  private lateinit var loader: IBaseLoader

  override fun <T : IDownloader> download(): T {
    loader = downloader
    return downloader as T
  }

//  override fun <T : IUploader> upload(): T {
//    loader = uploader
//    return uploader as T
//  }

  override fun getTaskEnum(): TaskEnum {
    return loader.getTaskEnum()
  }
}