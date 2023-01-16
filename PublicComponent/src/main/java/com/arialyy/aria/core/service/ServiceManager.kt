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

import android.content.Context
import com.arialyy.aria.core.DuaContext
import com.arialyy.aria.exception.AriaException
import timber.log.Timber

object ServiceManager {
  private val serviceCache = hashMapOf<String, IService>()

  private fun getServiceName(clazz: Class<*>) = clazz.name

  /**
   * register a service
   * @param serviceName [DuaContext.DB_SERVICE]
   */
  fun <T : IService> registerService(serviceName: String, context: Context, clazz: Class<T>) {
    if (!DuaContext.isService(serviceName)) {
      throw AriaException("$serviceName Not a service.")
    }
    val sn = getServiceName(clazz)
    val service = serviceCache[sn]
    if (service == null) {
      Timber.d("start register service: $sn")
      val s = clazz.newInstance()
      s.init(context)
      serviceCache[serviceName] = s
    }
  }

  /**
   * get datebase service, if already [registerService] custom service, return custom service
   */
  fun getDbService(serviceName: String): DbService {
    if (!DuaContext.isService(serviceName)) {
      throw AriaException("$serviceName Not a service.")
    }
    return (serviceCache[serviceName]
      ?: throw AriaException("service not found: $serviceName")) as DbService
  }
}