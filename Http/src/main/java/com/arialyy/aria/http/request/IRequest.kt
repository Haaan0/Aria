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

import android.text.TextUtils
import com.arialyy.aria.core.AriaConfig
import com.arialyy.aria.core.ProtocolType
import com.arialyy.aria.core.common.RequestEnum.GET
import com.arialyy.aria.core.common.RequestEnum.HEAD
import com.arialyy.aria.core.common.RequestEnum.POST
import com.arialyy.aria.http.HttpOption
import com.arialyy.aria.util.SSLContextUtil
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

/**
 * @Author laoyuyu
 * @Description
 * @Date 19:48 AM 2023/1/29
 **/
interface IRequest {

  companion object {
    fun getRequest(option: HttpOption): IRequest {
      return when (option.getRequestMethod()) {
        GET -> GetRequest
        POST -> PostRequest
        HEAD -> HeadRequest
        else -> throw UnsupportedOperationException("unsupported method ${option.getRequestMethod()}")
      }
    }
  }

  fun getDConnection(url: String, option: HttpOption): HttpURLConnection

  @Throws(IOException::class) fun createConnection(
    url: URL,
    option: HttpOption
  ): HttpURLConnection {
    val conn: HttpURLConnection
    val urlConn: URLConnection = if (option.getProxy() != null) {
      url.openConnection(option.getProxy())
    } else {
      url.openConnection()
    }
    if (urlConn is HttpsURLConnection) {
      val config = AriaConfig.getInstance()
      conn = urlConn
      var sslContext = SSLContextUtil.getSSLContextFromAssets(
        config.dConfig.caName,
        config.dConfig.caPath, ProtocolType.Default
      )
      if (sslContext == null) {
        sslContext = SSLContextUtil.getDefaultSLLContext(ProtocolType.Default)
      }
      val ssf = sslContext!!.socketFactory
      conn.sslSocketFactory = ssf
      conn.hostnameVerifier = SSLContextUtil.HOSTNAME_VERIFIER
    } else {
      conn = urlConn as HttpURLConnection
    }
    setHeader(conn, option)
    setConnectAttr(conn)
    return conn
  }

  private fun setConnectAttr(conn: HttpURLConnection) {
    conn.connectTimeout = AriaConfig.getInstance().dConfig.connectTimeOut
  }

  /**
   * 设置头部参数
   */
  private fun setHeader(conn: HttpURLConnection, option: HttpOption) {
    option.getHeaders().forEach {
      conn.setRequestProperty(it.key, it.value)
    }

    if (conn.getRequestProperty("Accept-Language") == null) {
      conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7")
    }
    if (conn.getRequestProperty("Accept-Encoding") == null) {
      conn.setRequestProperty("Accept-Encoding", "identity")
    }
    if (conn.getRequestProperty("Accept-Charset") == null) {
      conn.setRequestProperty("Accept-Charset", "UTF-8")
    }
    if (conn.getRequestProperty("Connection") == null) {
      conn.setRequestProperty("Connection", "Keep-Alive")
    }
    if (conn.getRequestProperty("Charset") == null) {
      conn.setRequestProperty("Charset", "UTF-8")
    }
    if (conn.getRequestProperty("User-Agent") == null) {
      conn.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"
      )
    }
    if (conn.getRequestProperty("Accept") == null) {
      val accept = StringBuilder()
      accept.append("image/gif, ")
        .append("image/jpeg, ")
        .append("image/pjpeg, ")
        .append("image/webp, ")
        .append("image/apng, ")
        .append("application/xml, ")
        .append("application/xaml+xml, ")
        .append("application/xhtml+xml, ")
        .append("application/x-shockwave-flash, ")
        .append("application/x-ms-xbap, ")
        .append("application/x-ms-application, ")
        .append("application/msword, ")
        .append("application/vnd.ms-excel, ")
        .append("application/vnd.ms-xpsdocument, ")
        .append("application/vnd.ms-powerpoint, ")
        .append("application/signed-exchange, ")
        .append("text/plain, ")
        .append("text/html, ")
        .append("*/*")
      conn.setRequestProperty("Accept", accept.toString())
    }
    //302获取重定向地址
    conn.instanceFollowRedirects = false
    val manager = option.getCookieManager()
    if (manager != null) {
      val store = manager.cookieStore
      if (store != null && store.cookies.size > 0) {
        conn.setRequestProperty(
          "Cookie",
          TextUtils.join(";", store.cookies)
        )
      }
    }
  }
}