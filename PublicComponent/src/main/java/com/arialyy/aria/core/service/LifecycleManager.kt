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

import com.arialyy.aria.core.inf.IBaseLoader
import com.arialyy.aria.core.inf.IComponentLoader
import com.arialyy.aria.core.listener.ITaskStatusListener
import java.util.concurrent.ConcurrentHashMap

/**
 * @Author laoyuyu
 * @Description
 * @Date 10:55 AM 2023/1/20
 **/
object LifecycleManager {

  private val listenerMap = ConcurrentHashMap<Any, MutableSet<ITaskStatusListener<*>>>()
  private val loaderMap = ConcurrentHashMap<IComponentLoader, Any>()

  fun getTargetByLoader(loader: IComponentLoader) = loaderMap[loader]

  /**
   * Associate the Loader with the Target
   */
  fun loaderAssociationTarget(target: Any, loader: IComponentLoader) {
    loaderMap[loader] = target
  }

  fun removeLoader(target: Any) {
    val tempKey = mutableListOf<IComponentLoader>()
    loaderMap.forEach {
      if (it.value == target) {
        tempKey.add(it.key)
      }
    }
    tempKey.forEach {
      loaderMap.remove(it)
    }
  }

  /**
   * Monitoring set by user, [IBaseLoader]
   */
  fun <T : ITaskStatusListener<*>> addCustomListener(target: Any, listener: T) {
    var listeners = listenerMap[target]
    if (listeners == null) {
      listeners = hashSetOf()
    }
    listeners.add(listener)
  }

  fun removeCustomListener(target: Any) {
    listenerMap.remove(target)
  }
}

