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

import com.arialyy.aria.http.download.HttpDTaskOption
import com.arialyy.aria.util.CheckUtil
import timber.log.Timber

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:40 PM 2023/1/22
 **/
internal object HttpUtil {

  fun checkHttpDParams(option: HttpDTaskOption?): Boolean {
    if (option == null) {
      Timber.e("option is null")
      return false
    }
    if (option.sourUrl.isNullOrEmpty()) {
      Timber.e("url is null")
      return false
    }
    if (!CheckUtil.checkUrl(option.sourUrl!!)) {
      Timber.e("invalid url, ${option.sourUrl}")
      return false
    }
    if (option.savePathUri == null) {
      Timber.e("save path is null")
      return false
    }
    if (!CheckUtil.checkUri(option.savePathUri)) {
      Timber.e("invalid uri, ${option.savePathUri}")
      return false
    }
    return true
  }
}