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

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.arialyy.aria.core.DuaContext
import timber.log.Timber
import java.io.InputStream
import java.util.Locale

object FileUtils {

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
   * https://github.com/javakam/FileOperator/blob/master/library_core/src/main/java/ando/file/core/FileUtils.kt
   *
   * 1. Check if Uri is correct
   * 2. Whether the file corresponding to Uri exists (may be deleted, or the system db has Uri related records, but the file is invalid or damaged)
   *
   * https://stackoverflow.com/questions/7645951/how-to-check-if-resource-pointed-by-uri-is-available
   */
  fun uriEffective(uri: Uri?): Boolean {
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

    return isUriExist && checkFileExistByUri(uri, resolver)
  }

  /**
   * check file exist
   * @return true exist
   */
  fun checkFileExistByUri(uri: Uri, resolver: ContentResolver): Boolean {
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
    return isFileExist
  }

  /**
   * ### 路径分割
   *
   * ```
   * eg:
   * srcPath=/storage/emulated/0/Movies/myVideo.mp4  path=/storage/emulated/0/Movies name=myVideo suffix=mp4 nameSuffix=myVideo.mp4
   *
   * /xxx/xxx/note.txt ->  path: /xxx/xxx   name: note   suffix: txt
   * ///note.txt       ->  path: ///        name: note   suffix: txt
   * /note.txt         ->  path: ""         name: note   suffix: txt
   * note.txt          ->  path: ""         name: note   suffix: txt
   * ```
   */
  fun splitFilePath(
    srcPath: String?,
    nameSplit: Char = '/',
    suffixSplit: Char = '.',
    block: ((path: String, name: String, suffix: String, nameSuffix: String) -> Unit)? = null,
  ) {
    if (srcPath.isNullOrBlank()) return
    val cut = srcPath.lastIndexOf(nameSplit)
    // /xxx/xxx/note.txt +0: /xxx/xxx +1: /xxx/xxx/
    val path = if (cut == -1) "" else srcPath.substring(0, cut)
    val nameSuffix = if (cut == -1) srcPath else srcPath.substring(cut + 1)

    val dot = nameSuffix.lastIndexOf(suffixSplit)
    if (dot != -1) {
      val suffix = nameSuffix.substring((dot + 1)).lowercase(Locale.getDefault())
      val name = nameSuffix.substring(0, dot)
      Timber.d("splitFilePath srcPath=$srcPath path=$path  name=$name  suffix=$suffix nameSuffix=$nameSuffix")
      block?.invoke(path, name, suffix, nameSuffix)
    }
  }

  /**
   * abc.jpg -> jpg
   */
  fun getFileNameSuffix(path: String): String {
    var nameSuffix = path
    splitFilePath(srcPath = path) { _: String, _: String, suffix: String, _: String ->
      nameSuffix = suffix
    }
    return nameSuffix
  }

  /**
   * /storage/emulated/0/Movies/myVideo.mp4  ->  /storage/emulated/0/Movies
   */
  fun getFilePathFromFullPath(path: String?, split: Char = '/'): String? {
    if (path.isNullOrBlank()) return null
    val cut = path.lastIndexOf(split)
    // (cut+1): /storage/emulated/0/Movies/
    if (cut != -1) return path.substring(0, cut)
    return path
  }

  /**
   * /storage/emulated/0/Movies/myVideo.mp4  ->  myVideo.mp4
   */
  fun getFileNameFromPath(path: String?, split: Char = '/'): String? {
    if (path.isNullOrBlank()) return null
    val cut = path.lastIndexOf(split)
    if (cut != -1) return path.substring(cut + 1)
    return path
  }

  /**
   * /storage/emulated/0/Movies/myVideo.mp4  ->  myVideo.mp4
   */
  fun getFileNameFromUri(uri: Uri?): String? =
    uri?.use {
      var filename: String? = null
      val resolver = DuaContext.context.contentResolver
      val mimeType = resolver.getType(uri)

      if (mimeType == null) {
        filename = getFileNameFromPath(FileUri.getPathByUri(uri))
      } else {
        resolver.query(uri, null, null, null, null)?.use { c: Cursor ->
          val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (c.moveToFirst()) filename = c.getString(nameIndex)
        }
      }
      filename
    }
}