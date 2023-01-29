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

import com.arialyy.aria.core.common.RequestEnum
import java.net.CookieManager
import java.net.Proxy

/**
 * @Author laoyuyu
 * @Description
 * @Date 14:27 AM 2023/1/20
 **/
class HttpOption {
  private val params = hashMapOf<String, String>()
  private val headers = hashMapOf<String, String>()
  private var requestEnum = RequestEnum.GET
  private var proxy: Proxy? = null
  private var cookieManager: CookieManager? = null
  private var body: String? = null
  private var bodyBinary: ByteArray? = null

  fun setProxy(proxy: Proxy): HttpOption {
    this.proxy = proxy
    return this
  }

  fun getProxy() = proxy

  /**
   * set http request key
   */
  fun setParas(key: String, value: String): HttpOption {
    params[key] = value
    return this
  }

  fun setParams(params: Map<String, String>): HttpOption {
    this.params.putAll(params)
    return this
  }

  fun getParams() = params

  fun getCookieManager(): CookieManager? {
    return cookieManager
  }

  fun setBody(body: String): HttpOption {
    this.body = body
    return this
  }

  fun getBody() = body

  fun setBodyBinary(body: ByteArray): HttpOption {
    this.bodyBinary = body
    return this
  }

  fun getBodyBinary() = bodyBinary

  fun setCookieManager(cookieManager: CookieManager): HttpOption {
    this.cookieManager = cookieManager
    return this
  }

  fun setHeaders(headers: Map<String, String>): HttpOption {
    this.headers.putAll(headers)
    return this
  }

  fun getHeaders() = headers

  /**
   * Attempts to get file information before downloading a file
   * by default, the get method is used to get
   * if the server supports head requests, please set true
   */
  fun setRequestMethod(requestEnum: RequestEnum): HttpOption {
    this.requestEnum = requestEnum
    return this
  }

  fun getRequestMethod() = requestEnum
}
