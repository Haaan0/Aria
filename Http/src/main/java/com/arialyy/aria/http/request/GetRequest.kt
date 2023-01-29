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

import com.arialyy.aria.core.common.RequestEnum.GET
import com.arialyy.aria.http.HttpOption
import com.arialyy.aria.util.CommonUtil
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * @Author laoyuyu
 * @Description
 * @Date 9:52 AM 2023/1/29
 **/
object GetRequest : IRequest {

  override fun getDConnection(url: String, option: HttpOption): HttpURLConnection {
    val params: Map<String, String> = option.getParams()

    val realUrl = if (params.isNotEmpty()) {
      val sb = StringBuilder()
      sb.append(url)
      if (!url.contains("?")) {
        sb.append("?")
      }

      val keys = params.keys
      for (key in keys) {
        sb.append(URLEncoder.encode(key, Charsets.UTF_8.toString()))
          .append("=")
          .append(URLEncoder.encode(params[key], Charsets.UTF_8.toString()))
          .append("&")
      }
      var temp = sb.toString()
      temp = temp.substring(0, temp.length - 1)
      URL(CommonUtil.convertUrl(temp))
    } else {
      URL(CommonUtil.convertUrl(url))
    }
    val conn = createConnection(realUrl, option)
    conn.requestMethod = GET.name
    return conn
  }
}