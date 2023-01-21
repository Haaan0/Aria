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
package com.arialyy.aria.core.common.receiver

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.arialyy.aria.core.inf.IComponentLoader
import com.arialyy.aria.core.inf.IDuaReceiver
import com.arialyy.aria.core.scheduler.TaskSchedulers
import java.lang.reflect.Proxy

/**
 * @Author laoyuyu
 * @Description
 * @Date 11:42 AM 2023/1/20
 **/
class LifLifecycleReceiver(val lifecycle: LifecycleOwner) : IDuaReceiver {

  /**
   * You need to associate the appropriate component dependencies
   * @param clazz eg: HttpLoader, FtpLoader
   */
  fun <T : IComponentLoader> setLoader(clazz: Class<T>): T {
    return Proxy.newProxyInstance(
      javaClass.classLoader, arrayOf(IComponentLoader::class.java)
    ) { proxy, method, args ->
      val result = method.invoke(proxy, args)
      if (method.name in IComponentLoader.proxyMethods) {
        lifecycle.lifecycle.addObserver(object : DefaultLifecycleObserver {
          override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            TaskSchedulers.getInstance()
              .register(lifecycle, (proxy as IComponentLoader).getTaskEnum())
          }

          override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            TaskSchedulers.getInstance().unRegister(lifecycle)
          }
        })
      }
      return@newProxyInstance result
    } as T
  }
}