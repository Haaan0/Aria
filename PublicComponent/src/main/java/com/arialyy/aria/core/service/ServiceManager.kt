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
package com.arialyy.aria.core.service

import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.queue.ITaskQueue
import com.arialyy.aria.core.service.QueueManager.registerQueue
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.core.task.UploadTask
import com.arialyy.aria.exception.AriaException
import timber.log.Timber

object ServiceManager {
  private val serviceCache = hashMapOf<String, IService>()

  /**
   * register a service
   * @param serviceName [DuaContext.DB_SERVICE]
   */
  fun <T : IService> registerService(serviceName: String, clazz: Class<T>) {
    if (!DuaContext.isService(serviceName)) {
      throw AriaException("$serviceName Not a service.")
    }
    Timber.d("start register service: $serviceName")
    val s = clazz.newInstance()
    s.init(DuaContext.context)
    serviceCache[serviceName] = s
  }

  /**
   * register a service
   * @param serviceName [DuaContext.DB_SERVICE]
   */
  fun registerService(serviceName: String, service: IService) {
    if (!DuaContext.isService(serviceName)) {
      throw AriaException("$serviceName Not a service.")
    }
    Timber.d("start register service: $serviceName")
    service.init(DuaContext.context)
    serviceCache[serviceName] = service
  }

  /**
   * get datebase service, if already [registerService] custom service, return custom service
   */
  fun getDbService(): DbService {
    if (!DuaContext.isService(DuaContext.DB_SERVICE)) {
      throw AriaException("${DuaContext.DB_SERVICE} Not a service.")
    }
    return (serviceCache[DuaContext.DB_SERVICE]
      ?: throw AriaException("service not found: ${DuaContext.DB_SERVICE}")) as DbService
  }

  /**
   * get queue service, if already [registerQueue] custom queue, return custom queue
   */
  fun getDownloadQueue(): com.arialyy.aria.queue.ITaskQueue<DownloadTask> {
    if (!DuaContext.isService(DuaContext.D_QUEUE)) {
      throw AriaException("${DuaContext.D_QUEUE} not a queue.")
    }
    return (serviceCache[DuaContext.D_QUEUE]
      ?: throw AriaException("queue not found: ${DuaContext.D_QUEUE}")) as com.arialyy.aria.queue.ITaskQueue<DownloadTask>
  }

  fun getUploadQueue(): com.arialyy.aria.queue.ITaskQueue<UploadTask> {
    if (!DuaContext.isService(DuaContext.U_QUEUE)) {
      throw AriaException("${DuaContext.U_QUEUE} not a queue.")
    }
    return (serviceCache[DuaContext.U_QUEUE]
      ?: throw AriaException("queue not found: ${DuaContext.U_QUEUE}")) as com.arialyy.aria.queue.ITaskQueue<UploadTask>
  }
}