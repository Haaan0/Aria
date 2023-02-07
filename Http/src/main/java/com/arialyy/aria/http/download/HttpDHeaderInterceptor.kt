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
package com.arialyy.aria.http.download

import android.net.TrafficStats
import android.net.Uri
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import com.arialyy.aria.core.processor.IHttpFileLenAdapter
import com.arialyy.aria.core.task.ITask
import com.arialyy.aria.core.task.ITaskInterceptor
import com.arialyy.aria.core.task.TaskChain
import com.arialyy.aria.core.task.TaskResp
import com.arialyy.aria.http.HttpUtil
import com.arialyy.aria.http.request.IRequest
import com.arialyy.aria.orm.entity.BlockRecord
import com.arialyy.aria.util.FileUtils
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.util.UUID

/**
 * @Author laoyuyu
 * @Description
 * @Date 2:27 PM 2023/1/28
 **/
internal class HttpDHeaderInterceptor : ITaskInterceptor {
  private lateinit var task: ITask
  private lateinit var taskOption: HttpDTaskOption

  companion object {
    private val CODE_30X = listOf(
      HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
      HttpURLConnection.HTTP_SEE_OTHER, HttpURLConnection.HTTP_CREATED, 307
    )
  }

  override suspend fun interceptor(chain: TaskChain): TaskResp {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw IllegalThreadStateException("io operations cannot be in the main thread")
    }
    Timber.i("step 1. get file info")
    task = chain.getTask()
    taskOption = task.getTaskOption(HttpDTaskOption::class.java)
    try {
      val fileSize = getFileSize()
      if (fileSize >= 0) {
        task.taskState.isSupportResume = fileSize != 0L
        task.taskState.isSupportBlock =
          task.taskState.isSupportResume && fileSize > BlockRecord.BLOCK_SIZE
        task.taskState.fileSize = fileSize
        return chain.proceed(task)
      }
    } catch (e: IOException) {
      Timber.e(
        "download fail, url: ${
          chain.getTask().getTaskOption(HttpDTaskOption::class.java).sourUrl
        }"
      )
      return TaskResp(TaskResp.CODE_GET_FILE_INFO_FAIL)
    }
    Timber.e("can't get fileSize")
    return TaskResp(TaskResp.CODE_INTERRUPT)
  }

  @Throws(IOException::class)
  private fun getFileSize(): Long {
    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode())
    val conn: HttpURLConnection = IRequest.getRequest(taskOption.httpOption!!)
      .getDConnection(taskOption.sourUrl!!, taskOption.httpOption!!)
    // https://httpwg.org/specs/rfc9110.html#byte.ranges
    conn.setRequestProperty("Range", "bytes=0-")
    // conn.setRequestProperty("Range", "bytes=0-1") // 尝试获取1个字节
    conn.connect()
    return handleConnect(conn)
  }

  @Throws(IOException::class)
  private fun handleConnect(conn: HttpURLConnection): Long {

    val code = conn.responseCode
    when {
      code == HttpURLConnection.HTTP_PARTIAL -> return getFileSizeFromHeader(
        conn.headerFields,
        taskOption
      )
      code == HttpURLConnection.HTTP_OK -> {
        val len = getFileSizeFromHeader(conn.headerFields, taskOption)
        if (len > 0) {
          return len
        }
        val contentType = conn.getHeaderField("Content-Type")
        if (contentType == "text/html") {
          val reader = BufferedReader(InputStreamReader(HttpUtil.convertInputStream(conn)))
          val sb = StringBuilder()
          var line: String?
          while (reader.readLine().also { line = it } != null) {
            sb.append(line)
          }
          reader.close()
          return handleUrlReTurn(conn, HttpUtil.getWindowReplaceUrl(sb.toString()))
        }
        val chunkSize = checkChunkFileSize(conn)
        if (chunkSize > -1) {
          Timber.d("the url is chunk task, ${conn.url}")
          return chunkSize
        }
        // code is 200, but file size cannot be obtained.
        return -1
      }
      code == 416 -> {
        return getFileSizeFromHeader(conn.headerFields, taskOption)
      }
      code in CODE_30X -> {
        Timber.d("handle 30x turn, code: $code")
        return handleUrlReTurn(conn, conn.getHeaderField("Location"))
      }
      code >= HttpURLConnection.HTTP_BAD_REQUEST -> {
        Timber.e("download fail, code: $code")
        return -1
      }
      else -> {
        return -1
      }
    }
  }

  /**
   * if headers has [rfc9112 Transfer-Encoding](https://httpwg.org/specs/rfc9112.html#chunked.trailer.section)
   * the url is chunk task
   */
  private fun checkChunkFileSize(conn: HttpURLConnection): Long {
    val chunkHead = conn.headerFields["Transfer-Encoding"]
    if (chunkHead.isNullOrEmpty()) {
      return -1
    }
    taskOption.isChunkTask = true
    return 0
  }

  @Throws(IOException::class) private fun handleUrlReTurn(
    oldConn: HttpURLConnection,
    newUrl: String?
  ): Long {
    Timber.i("handle 30x turn, new url: $newUrl")
    if (newUrl.isNullOrEmpty() || newUrl.equals("null", ignoreCase = true)) {
      return -1
    }
    var tempUrl = newUrl
    if (tempUrl.startsWith("/")) {
      val uri = Uri.parse(taskOption.sourUrl!!)
      tempUrl = uri.host + newUrl
    }
    if (!FileUtils.checkUrl(tempUrl)) {
      Timber.e("get redirect url fail, $tempUrl")
      return -1
    }

    taskOption.redirectUrl = newUrl
    val cookies = oldConn.getHeaderField("Set-Cookie")
    oldConn.disconnect()

    val newConn: HttpURLConnection = IRequest.getRequest(taskOption.httpOption!!)
      .getDConnection(taskOption.sourUrl!!, taskOption.httpOption!!)
    newConn.setRequestProperty("Cookie", cookies)
    newConn.setRequestProperty("Range", "bytes=" + 0 + "-")

    newConn.connect()
    return handleConnect(newConn)
  }

  /**
   * get file size from header, if user not set [IHttpFileLenAdapter], use [FileLenAdapter]
   */
  private fun getFileSizeFromHeader(
    header: Map<String, List<String>>,
    taskOption: HttpDTaskOption
  ): Long {
    var lenAdapter = taskOption.fileSizeAdapter
    if (lenAdapter == null) {
      lenAdapter = FileLenAdapter()
    }
    return lenAdapter.handleFileLen(header)
  }

  /**
   * https://httpwg.org/specs/rfc9110.html#field.content-range
   */
  private class FileLenAdapter : IHttpFileLenAdapter {
    override fun handleFileLen(headers: Map<String, List<String>>): Long {
      if (headers.isEmpty()) {
        Timber.e("header is empty, get file size fail")
        return -1
      }
      val sLength = headers["Content-Length"]
      if (sLength == null || sLength.isEmpty()) {
        return -1
      }
      val temp = sLength[0]
      var len = if (TextUtils.isEmpty(temp)) -1 else temp.toLong()
      // 某些服务，如果设置了conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      // 会返回 Content-Range: bytes 0-225427911/225427913
      if (len < 0) {
        val sRange = headers["Content-Range"]
        len = if (sRange == null || sRange.isEmpty()) {
          -1
        } else {
          val start = temp.indexOf("/")
          temp.substring(start + 1).toLong()
        }
      }
      return len
    }
  }
}
