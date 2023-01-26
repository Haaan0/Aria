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

import android.database.Cursor
import android.net.Uri
import com.arialyy.aria.core.DuaContext
import timber.log.Timber
import java.io.InputStream

object CheckUtil {

  val HTTP_REGEX =
    Regex(
      "(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]",
      RegexOption.IGNORE_CASE
    )

  /**
   * check if url is correct
   */
  fun checkUrl(url: String) = HTTP_REGEX.matches(url)

  /**
   * 1. Check if Uri is correct
   * 2. Whether the file corresponding to Uri exists (may be deleted, or the system db has Uri related records, but the file is invalid or damaged)
   *
   * https://stackoverflow.com/questions/7645951/how-to-check-if-resource-pointed-by-uri-is-available
   */
  fun checkUri(uri: Uri?): Boolean {
    if (uri == null) return false
    val resolver = DuaContext.context.contentResolver

    //1. Check Uri
    var cursor: Cursor? = null
    val isUriExist: Boolean = try {
      cursor = resolver.query(uri, null, null, null, null)
      //cursor null: content Uri was invalid or some other error occurred
      //cursor.moveToFirst() false: Uri was ok but no entry found.
      (cursor != null && cursor.moveToFirst())
    } catch (t: Throwable) {
      Timber.e("1.Check Uri Error: ${t.message}")
      false
    } finally {
      try {
        cursor?.close()
      } catch (t: Throwable) {
      }
    }

    //2. Check File Exist
    //如果系统 db 存有 Uri 相关记录, 但是文件失效或者损坏 (If the system db has Uri related records, but the file is invalid or damaged)
    var ins: InputStream? = null
    val isFileExist: Boolean = try {
      ins = resolver.openInputStream(uri)
      // file exists
      true
    } catch (t: Throwable) {
      // File was not found eg: open failed: ENOENT (No such file or directory)
      Timber.e("2. Check File Exist Error: ${t.message}")
      false
    } finally {
      try {
        ins?.close()
      } catch (t: Throwable) {
      }
    }
    return isUriExist && isFileExist
  }
}