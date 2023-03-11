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

import android.text.TextUtils
import com.arialyy.aria.util.FileUtils
import com.arialyy.aria.util.Regular
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

/**
 * @Author laoyuyu
 * @Description
 * @Date 12:40 PM 2023/1/22
 **/
object HttpUtil {

  /**
   * 拦截window.location.replace数据
   *
   * @return 重定向url
   */
  fun getWindowReplaceUrl(text: String?): String? {
    if (text.isNullOrEmpty()) {
      Timber.e("text is null")
      return null
    }
    val reg = Regular.REG_WINLOD_REPLACE
    val p = Pattern.compile(reg)
    val m = p.matcher(text)
    if (m.find()) {
      var s = m.group()
      s = s.substring(9, s.length - 2)
      return s
    }
    return null
  }

  /**
   * 转换HttpUrlConnect的inputStream流
   *
   * @return [GZIPInputStream]、[InflaterInputStream]
   * @throws IOException
   */
  @Throws(IOException::class) fun convertInputStream(connection: HttpURLConnection): InputStream? {
    val encoding = connection.getHeaderField("Content-Encoding")
    if (TextUtils.isEmpty(encoding)) {
      return connection.inputStream
    }
    if (encoding.contains("gzip")) {
      return GZIPInputStream(connection.inputStream)
    }
    if (encoding.contains("deflate")) {
      return InflaterInputStream(connection.inputStream)
    }
    return connection.inputStream
  }

  fun checkHttpDParams(option: HttpTaskOption?): Boolean {
    if (option == null) {
      Timber.e("option is null")
      return false
    }
    if (option.sourUrl.isNullOrEmpty()) {
      Timber.e("url is null")
      return false
    }
    if (!FileUtils.checkUrl(option.sourUrl!!)) {
      Timber.e("invalid url, ${option.sourUrl}")
      return false
    }
    if (option.savePathUri == null) {
      Timber.e("save path is null")
      return false
    }
    if (!FileUtils.uriEffective(option.savePathUri)) {
      Timber.e("invalid uri, ${option.savePathUri}")
      return false
    }
    if (!FileUtils.uriIsDir(option.savePathUri!!)){
      Timber.e("invalid uri, that path not a dir")
      return false
    }
    return true
  }
}