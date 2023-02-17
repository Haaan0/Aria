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
package com.arialyy.aria.core

import android.annotation.SuppressLint
import android.content.Context
import com.arialyy.aria.core.service.LifecycleManager
import com.arialyy.aria.core.service.ServiceManager
import kotlinx.coroutines.MainScope

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:40 AM 2023/1/16
 **/
@SuppressLint("StaticFieldLeak")
object DuaContext {
  const val DB_SERVICE = "DB_SERVICE"
  const val D_QUEUE = "D_QUEUE"
  const val DG_QUEUE = "DG_QUEUE"
  const val U_QUEUE = "U_QUEUE"
  const val SCHEDULER = "SCHEDULER"

  private val serviceArray = arrayOf(DB_SERVICE, D_QUEUE, DG_QUEUE, U_QUEUE, SCHEDULER)
  val duaScope = MainScope()

  lateinit var context: Context

  fun isService(serviceName: String) = serviceName in serviceArray

  fun getServiceManager() = ServiceManager

  fun getLifeManager() = LifecycleManager

  fun getCommonConfig() = AriaConfig.getInstance().cConfig

  fun getDConfig() = AriaConfig.getInstance().dConfig

  fun getUConfig() = AriaConfig.getInstance().uConfig
}