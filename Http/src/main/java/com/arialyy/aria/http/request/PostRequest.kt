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
package com.arialyy.aria.http.request

import com.arialyy.aria.core.common.RequestEnum
import com.arialyy.aria.http.HttpOption
import com.arialyy.aria.util.CommonUtil
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * @Author laoyuyu
 * @Description
 * @Date 19:52 AM 2023/1/29
 **/
internal object PostRequest : IRequest {
  override fun getDConnection(url: String, option: HttpOption): HttpURLConnection {
    val conn = createConnection(URL(CommonUtil.convertUrl(url)), option)
    conn.doInput = true
    conn.doOutput = true
    conn.useCaches = false
    conn.requestMethod = RequestEnum.POST.name

    if (option.getParams().isNotEmpty()
      || !option.getBody().isNullOrEmpty()
      || option.getBodyBinary() != null
    ) {
      conn.outputStream.use {
        val writer = BufferedWriter(OutputStreamWriter(it, "UTF-8"))
        if (option.getParams().isNotEmpty()) {
          writer.write(getQuery(option.getParams()))
        }
        if (!option.getBody().isNullOrEmpty()) {
          writer.write(option.getBody())
        }
        if (option.getBodyBinary() != null) {
          it.write(option.getBodyBinary())
        }
        writer.flush()
        writer.close()
        it.close()
      }
    }
    return conn
  }

  private fun getQuery(params: Map<String, String>): String {
    val result = StringBuilder()
    var first = true
    for (kv in params) {
      if (first) first = false else result.append("&")
      result.append(URLEncoder.encode(kv.key, Charsets.UTF_8.toString()))
      result.append("=")
      result.append(URLEncoder.encode(kv.value, Charsets.UTF_8.toString()))
    }
    return result.toString()
  }
}