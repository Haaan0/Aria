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
package com.arialyy.aria.core.manager

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.arialyy.annotations.TaskEnum
import com.arialyy.aria.core.common.ProxyHelper
import com.arialyy.aria.core.download.DownloadGroupTaskListener
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.scheduler.M3U8PeerTaskListener
import com.arialyy.aria.core.scheduler.SubTaskListener
import com.arialyy.aria.core.scheduler.TaskInternalListenerInterface
import com.arialyy.aria.core.scheduler.TaskSchedulers
import com.arialyy.aria.util.ALog
import timber.log.Timber
import java.util.Timer

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:55 AM 2023/1/20
 **/
object LifecycleManager {

  private fun register(obj: Any) {
    if (obj is TaskInternalListenerInterface) {
      ProxyHelper.getInstance().checkProxyType(obj.javaClass)
      if (obj is DownloadTaskListener) {
        TaskSchedulers.getInstance().register(obj, TaskEnum.DOWNLOAD)
      }
      if (obj is DownloadGroupTaskListener) {
        TaskSchedulers.getInstance().register(obj, TaskEnum.DOWNLOAD_GROUP)
      }
      if (obj is M3U8PeerTaskListener) {
        TaskSchedulers.getInstance().register(obj, TaskEnum.M3U8_PEER)
      }
      if (obj is SubTaskListener<*, *>) {
        TaskSchedulers.getInstance().register(obj, TaskEnum.DOWNLOAD_GROUP_SUB)
      }
      return
    }
    val set: Set<Int> = ProxyHelper.getInstance().checkProxyType(obj.javaClass)
    if (set.isNotEmpty()) {
      for (type in set) {
        when (type) {
          ProxyHelper.PROXY_TYPE_DOWNLOAD -> {
            TaskSchedulers.getInstance().register(obj, TaskEnum.DOWNLOAD)
          }
          ProxyHelper.PROXY_TYPE_DOWNLOAD_GROUP -> {
            TaskSchedulers.getInstance().register(obj, TaskEnum.DOWNLOAD_GROUP)
          }
          ProxyHelper.PROXY_TYPE_M3U8_PEER -> {
            TaskSchedulers.getInstance().register(obj, TaskEnum.M3U8_PEER)
          }
          ProxyHelper.PROXY_TYPE_DOWNLOAD_GROUP_SUB -> {
            TaskSchedulers.getInstance().register(obj, TaskEnum.DOWNLOAD_GROUP_SUB)
          }
        }
      }
      return
    }
    Timber.e("没有Aria的注解方法，详情见：https://aria.laoyuyu.me/aria_doc/other/annotaion_invalid.html")
  }

  fun addObserver(lifecycle: Lifecycle) {
    lifecycle.addObserver(DuaObserver(lifecycle))
  }

  private class DuaObserver(val obj: Lifecycle) : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
      super.onCreate(owner)
      register(obj)
    }
  }
}

