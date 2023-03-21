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
package com.arialyy.aria.util

import android.text.TextUtils
import timber.log.Timber
import java.security.MessageDigest

/**
 * @Author laoyuyu
 * @Description
 * @Date 21:38 2023/3/11
 **/
object DuaUtil {
  /**
   * 检测url是否合法
   *
   * @return `true` 合法，`false` 非法
   */
  fun checkUrl(url: String): Boolean {
    if (TextUtils.isEmpty(url)) {
      Timber.e("url不能为null")
      return false
    }
    if (!url.startsWith("http") && !url.startsWith("ftp") && !url.startsWith("sftp")) {
      Timber.e("url【$url】错误")
      return false
    }
    val index = url.indexOf("://")
    if (index == -1) {
      Timber.e("url【$url】不合法")
    }
    return true
  }

  fun getMD5Hash(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    val messageDigest = md.digest(str.toByteArray())
    val no = messageDigest.toBigInteger() // 使用 Kotlin 扩展函数将字节数组转换为 BigInteger
    var hashText = no.toString(16)
    while (hashText.length < 32) {
      hashText = "0$hashText"
    }
    return hashText
  }
}
